package com.raishxn.gtna.planner.api.crafting.planner;

import com.raishxn.gtna.planner.api.amount.UfoAmount;

import java.util.Objects;

/** Outcome and bounded diagnostics of one planning attempt. */
public record PlanningResult<K>(Status status, CraftingPlan<K> plan, Diagnostics diagnostics) {

    public PlanningResult {
        Objects.requireNonNull(status, "status");
        Objects.requireNonNull(plan, "plan");
        Objects.requireNonNull(diagnostics, "diagnostics");
    }

    public enum Status {
        COMPLETE,
        MISSING_INGREDIENTS,
        CANCELLED,
        TIMED_OUT,
        OPERATION_LIMIT,
        DEPTH_LIMIT
    }

    /**
     * How the last plan's shortage divides between what is consumed, what is handed back and what wears
     * out. Totals and kinds rather than the keys themselves: diagnostics carry numbers, and naming the
     * items would put what a player is short of into every log line.
     */
    public record ShortageSummary(UfoAmount consumable, UfoAmount seed, UfoAmount carrier,
                                  int consumableKinds, int seedKinds, int carrierKinds) {

        public ShortageSummary {
            Objects.requireNonNull(consumable, "consumable");
            Objects.requireNonNull(seed, "seed");
            Objects.requireNonNull(carrier, "carrier");
        }

        /** Shared by every plan that is short of nothing, which is most of them. */
        private static final ShortageSummary NONE = new ShortageSummary(UfoAmount.ZERO, UfoAmount.ZERO, UfoAmount.ZERO,
                0, 0, 0);

        public static <K> ShortageSummary of(CraftingPlan.Shortage<K> shortage) {
            Objects.requireNonNull(shortage, "shortage");
            if (shortage.isEmpty()) {
                return NONE;
            }
            return new ShortageSummary(total(shortage.consumable()), total(shortage.seed()),
                    total(shortage.carrier()), shortage.consumable().size(), shortage.seed().size(),
                    shortage.carrier().size());
        }

        private static <K> UfoAmount total(java.util.Map<K, UfoAmount> amounts) {
            // A loop, not a stream. Three streams per plan cost more than the summary is worth, which
            // the allocation gate said plainly: a plan with no shortage at all was charged eighteen
            // hundred bytes an operation for computing three zeroes.
            UfoAmount sum = UfoAmount.ZERO;
            for (UfoAmount amount : amounts.values()) {
                sum = sum.add(amount);
            }
            return sum;
        }
    }

    public record Diagnostics(long graphRevision, long operations, int maximumDepth, long elapsedNanos,
                              ShortageSummary shortage, long cycleCuts, long[] choiceLinks) {

        public Diagnostics {
            if (graphRevision < 0L || operations < 0L || maximumDepth < 0 || elapsedNanos < 0L || cycleCuts < 0L) {
                throw new IllegalArgumentException("diagnostics must be non-negative");
            }
            Objects.requireNonNull(shortage, "shortage");
            if (cycleCuts < 0L) {
                throw new IllegalArgumentException("cycle cuts must be non-negative");
            }
            if (choiceLinks != null) {
                if (choiceLinks.length != IterativeCraftingPlanner.CHOICE_LINK_COUNT) {
                    throw new IllegalArgumentException(
                            "a route-choice histogram needs " + IterativeCraftingPlanner.CHOICE_LINK_COUNT + " links");
                }
                for (long count : choiceLinks) {
                    if (count < 0L) throw new IllegalArgumentException("route-choice counts must be non-negative");
                }
                choiceLinks = choiceLinks.clone();
            }
        }

        /**
         * One count per comparison link, or {@code null} when the last plan had no route to choose.
         * The order is documented on {@link IterativeCraftingPlanner#CHOICE_LINK_COUNT}; a defensive
         * copy is returned so an operator reading diagnostics cannot mutate the recorded plan.
         */
        @Override
        public long[] choiceLinks() {
            return choiceLinks == null ? null : choiceLinks.clone();
        }
    }
}
