package com.raishxn.gtna.planner.neoforge.crafting;

import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Shares equivalent in-flight calculations without sharing cancellation between
 * their callers. The owner can still cancel every worker when its source data
 * becomes obsolete.
 */
final class InFlightRequestCoordinator<K, V> {

    private final Executor executor;
    private final ConcurrentHashMap<K, SharedTask> tasks = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Object, java.util.Set<RequestView>> owners = new ConcurrentHashMap<>();
    private final AtomicLong submitted = new AtomicLong();
    private final AtomicLong deduplicated = new AtomicLong();
    private final AtomicLong cancelled = new AtomicLong();
    private final AtomicInteger inFlight = new AtomicInteger();

    InFlightRequestCoordinator(Executor executor) {
        this.executor = Objects.requireNonNull(executor, "executor");
    }

    Future<V> submit(K key, Callable<V> calculation) {
        return submit(key, new Object(), calculation, Integer.MAX_VALUE);
    }

    Future<V> submit(K key, Object owner, Callable<V> calculation) {
        return submit(key, owner, calculation, Integer.MAX_VALUE);
    }

    Future<V> submit(K key, Object owner, Callable<V> calculation, int maxInFlight) {
        Objects.requireNonNull(key, "key");
        Objects.requireNonNull(owner, "owner");
        Objects.requireNonNull(calculation, "calculation");
        if (maxInFlight < 1) throw new IllegalArgumentException("maxInFlight must be positive");
        while (true) {
            SharedTask existing = tasks.get(key);
            if (existing != null) {
                if (!existing.isDone()) {
                    deduplicated.incrementAndGet();
                    return existing.newView(owner);
                }
                tasks.remove(key, existing);
                continue;
            }

            reserve(maxInFlight);
            SharedTask created = new SharedTask(key, calculation);
            if (tasks.putIfAbsent(key, created) != null) {
                inFlight.decrementAndGet();
                continue;
            }
            try {
                executor.execute(created.worker);
                submitted.incrementAndGet();
                return created.newView(owner);
            } catch (RuntimeException rejected) {
                if (tasks.remove(key, created)) inFlight.decrementAndGet();
                created.fail(rejected);
                throw rejected;
            }
        }
    }

    private void reserve(int limit) {
        while (true) {
            int observed = inFlight.get();
            if (observed >= limit) throw new PerGridLimitExceededException();
            if (inFlight.compareAndSet(observed, observed + 1)) return;
        }
    }

    void cancelAll() {
        for (SharedTask task : tasks.values()) {
            if (task.worker.cancel(true)) cancelled.incrementAndGet();
        }
    }

    int cancelOwner(Object owner) {
        Objects.requireNonNull(owner, "owner");
        var views = owners.get(owner);
        if (views == null) return 0;
        int stopped = 0;
        for (RequestView view : java.util.List.copyOf(views)) {
            if (view.cancel(true)) stopped++;
        }
        return stopped;
    }

    Stats stats() {
        return new Stats(inFlight.get(), submitted.get(), deduplicated.get(), cancelled.get());
    }

    record Stats(int inFlight, long submitted, long deduplicated, long cancelled) {}

    private final class SharedTask {

        private final K key;
        private final CompletableFuture<V> result = new CompletableFuture<>();
        private final AtomicInteger subscribers = new AtomicInteger();
        private final FutureTask<V> worker;

        private SharedTask(K key, Callable<V> calculation) {
            this.key = key;
            this.worker = new FutureTask<>(calculation) {

                @Override
                protected void done() {
                    publish();
                }
            };
        }

        private boolean isDone() {
            return worker.isDone();
        }

        /** A caller may cancel this view without cancelling another caller's plan. */
        private Future<V> newView(Object owner) {
            return new RequestView(this, owner);
        }

        private void fail(RuntimeException failure) {
            result.completeExceptionally(failure);
        }

        private void publish() {
            // Retire the shared task before publishing the result. Completing the result releases
            // every caller's view, so a caller that observes completion could otherwise read a
            // request still counted as in flight - which is what a loaded runner kept catching.
            try {
                V value = worker.get();
                retire();
                result.complete(value);
            } catch (CancellationException stopped) {
                retire();
                result.cancel(false);
            } catch (ExecutionException failure) {
                retire();
                result.completeExceptionally(failure.getCause());
            } catch (InterruptedException interrupted) {
                Thread.currentThread().interrupt();
                retire();
                result.completeExceptionally(interrupted);
            }
        }

        private void retire() {
            if (tasks.remove(key, this)) inFlight.decrementAndGet();
        }
    }

    private final class RequestView implements Future<V> {

        private final SharedTask task;
        private final Object owner;
        private final CompletableFuture<V> view;
        private final AtomicBoolean released = new AtomicBoolean();
        private final AtomicBoolean interruptOnRelease = new AtomicBoolean();

        private RequestView(SharedTask task, Object owner) {
            this.task = task;
            this.owner = owner;
            task.subscribers.incrementAndGet();
            owners.computeIfAbsent(owner, ignored -> ConcurrentHashMap.newKeySet()).add(this);
            view = task.result.thenApply(value -> value);
            view.whenComplete((value, failure) -> release(interruptOnRelease.get()));
        }

        private void release(boolean mayInterruptIfRunning) {
            if (!released.compareAndSet(false, true)) return;
            var views = owners.get(owner);
            if (views != null) {
                views.remove(this);
                if (views.isEmpty()) owners.remove(owner, views);
            }
            if (task.subscribers.decrementAndGet() == 0 && !task.worker.isDone() &&
                    task.worker.cancel(mayInterruptIfRunning)) {
                cancelled.incrementAndGet();
            }
        }

        @Override
        public boolean cancel(boolean mayInterruptIfRunning) {
            if (mayInterruptIfRunning) interruptOnRelease.set(true);
            return view.cancel(false);
        }

        @Override
        public boolean isCancelled() {
            return view.isCancelled();
        }

        @Override
        public boolean isDone() {
            return view.isDone();
        }

        @Override
        public V get() throws InterruptedException, ExecutionException {
            return view.get();
        }

        @Override
        public V get(long timeout, TimeUnit unit)
                                                  throws InterruptedException, ExecutionException, TimeoutException {
            return view.get(timeout, unit);
        }
    }

    static final class PerGridLimitExceededException extends java.util.concurrent.RejectedExecutionException {

        @java.io.Serial
        private static final long serialVersionUID = 1L;

        private PerGridLimitExceededException() {
            super("per-grid planner limit reached");
        }
    }
}
