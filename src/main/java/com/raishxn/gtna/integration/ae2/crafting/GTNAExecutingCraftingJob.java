package com.raishxn.gtna.integration.ae2.crafting;

import com.raishxn.gtna.integration.ae2.pattern.IParallelPatternDetails;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;

import appeng.api.config.Actionable;
import appeng.api.crafting.IPatternDetails;
import appeng.api.crafting.PatternDetailsHelper;
import appeng.api.networking.IGrid;
import appeng.api.networking.crafting.ICraftingPlan;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.GenericStack;
import appeng.crafting.CraftingLink;
import appeng.crafting.execution.ElapsedTimeTracker;
import appeng.crafting.inv.ListCraftingInventory;
import appeng.me.service.CraftingService;

import java.util.HashMap;
import java.util.Map;

import org.jetbrains.annotations.Nullable;

final class GTNAExecutingCraftingJob {

    private static final String NBT_LINK = "link";
    private static final String NBT_PLAYER_ID = "playerId";
    private static final String NBT_FINAL_OUTPUT = "finalOutput";
    private static final String NBT_WAITING_FOR = "waitingFor";
    private static final String NBT_TIME_TRACKER = "timeTracker";
    private static final String NBT_REMAINING_AMOUNT = "remainingAmount";
    private static final String NBT_TASKS = "tasks";
    private static final String NBT_CRAFTING_PROGRESS = "#craftingProgress";

    final CraftingLink link;
    final ListCraftingInventory waitingFor;
    final Map<IPatternDetails, TaskProgress> tasks = new HashMap<>();
    final ElapsedTimeTracker timeTracker;
    final IGTNAElapsedTimeTracker tracker;
    GenericStack finalOutput;
    long remainingAmount;
    @Nullable
    Integer playerId;

    @FunctionalInterface
    interface CraftingDifferenceListener {
        void onCraftingDifference(AEKey what);
    }

    GTNAExecutingCraftingJob(ICraftingPlan plan, CraftingDifferenceListener listener, CraftingLink link, @Nullable Integer playerId) {
        this.finalOutput = plan.finalOutput();
        this.remainingAmount = this.finalOutput.amount();
        this.waitingFor = new ListCraftingInventory(listener::onCraftingDifference);
        this.timeTracker = new ElapsedTimeTracker();
        this.tracker = (IGTNAElapsedTimeTracker) this.timeTracker;
        for (var entry : plan.emittedItems()) {
            waitingFor.insert(entry.getKey(), entry.getLongValue(), Actionable.MODULATE);
            tracker.gtna$addMaxItems(entry.getLongValue(), entry.getKey().getType());
        }
        for (var entry : plan.patternTimes().entrySet()) {
            tasks.computeIfAbsent(entry.getKey(), p -> new TaskProgress()).value += entry.getValue();
            for (var output : entry.getKey().getOutputs()) {
                long amount = output.amount() * entry.getValue() * output.what().getAmountPerUnit();
                tracker.gtna$addMaxItems(amount, output.what().getType());
            }
        }
        this.link = link;
        this.playerId = playerId;
    }

    GTNAExecutingCraftingJob(CompoundTag data, CraftingDifferenceListener listener, GTNAOptimizedCraftingCpuLogic cpu) {
        this.link = new CraftingLink(data.getCompound(NBT_LINK), cpu.cluster);
        IGrid grid = cpu.cluster.getGrid();
        if (grid != null) {
            ((CraftingService) grid.getCraftingService()).addLink(link);
        }

        this.finalOutput = GenericStack.readTag(data.getCompound(NBT_FINAL_OUTPUT));
        this.remainingAmount = data.getLong(NBT_REMAINING_AMOUNT);
        this.waitingFor = new ListCraftingInventory(listener::onCraftingDifference);
        this.waitingFor.readFromNBT(data.getList(NBT_WAITING_FOR, Tag.TAG_COMPOUND));
        this.timeTracker = new ElapsedTimeTracker(data.getCompound(NBT_TIME_TRACKER));
        this.tracker = (IGTNAElapsedTimeTracker) this.timeTracker;
        this.playerId = data.contains(NBT_PLAYER_ID, Tag.TAG_INT) ? data.getInt(NBT_PLAYER_ID) : null;

        ListTag tasksTag = data.getList(NBT_TASKS, Tag.TAG_COMPOUND);
        for (int i = 0; i < tasksTag.size(); ++i) {
            CompoundTag item = tasksTag.getCompound(i);
            var pattern = AEItemKey.fromTag(item);
            var details = PatternDetailsHelper.decodePattern(pattern, cpu.cluster.getLevel());
            if (details != null) {
                long parallel = item.getLong("parallel");
                details = IParallelPatternDetails.of(details, cpu.cluster.getLevel(), Math.max(1L, parallel));
                TaskProgress tp = new TaskProgress();
                tp.value = item.getLong(NBT_CRAFTING_PROGRESS);
                this.tasks.put(details, tp);
            }
        }
    }

    CompoundTag writeToNBT() {
        CompoundTag data = new CompoundTag();

        CompoundTag linkData = new CompoundTag();
        link.writeToNBT(linkData);
        data.put(NBT_LINK, linkData);
        data.put(NBT_FINAL_OUTPUT, GenericStack.writeTag(finalOutput));
        data.put(NBT_WAITING_FOR, waitingFor.writeToNBT());
        data.put(NBT_TIME_TRACKER, timeTracker.writeToNBT());

        ListTag list = new ListTag();
        for (var entry : tasks.entrySet()) {
            var item = entry.getKey().getDefinition().toTag();
            item.putLong(NBT_CRAFTING_PROGRESS, entry.getValue().value);
            if (entry.getKey() instanceof IParallelPatternDetails parallelPatternDetails) {
                item.putLong("parallel", parallelPatternDetails.getParallel());
            }
            list.add(item);
        }
        data.put(NBT_TASKS, list);
        data.putLong(NBT_REMAINING_AMOUNT, remainingAmount);
        if (this.playerId != null) {
            data.putInt(NBT_PLAYER_ID, this.playerId);
        }
        return data;
    }

    static final class TaskProgress {
        long value = 0L;
    }
}
