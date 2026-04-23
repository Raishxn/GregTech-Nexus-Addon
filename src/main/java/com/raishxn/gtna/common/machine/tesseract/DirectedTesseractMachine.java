package com.raishxn.gtna.common.machine.tesseract;

import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.TickableSubscription;
import com.gregtechceu.gtceu.api.machine.feature.IFancyUIMachine;
import com.gregtechceu.gtceu.api.machine.feature.IMachineLife;
import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;

import com.lowdragmc.lowdraglib.gui.texture.GuiTextureGroup;
import com.lowdragmc.lowdraglib.gui.texture.TextTexture;
import com.lowdragmc.lowdraglib.gui.widget.ButtonWidget;
import com.lowdragmc.lowdraglib.gui.widget.ComponentPanelWidget;
import com.lowdragmc.lowdraglib.gui.widget.DraggableScrollableWidgetGroup;
import com.lowdragmc.lowdraglib.gui.widget.LabelWidget;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import com.lowdragmc.lowdraglib.syncdata.annotation.DescSynced;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraft.world.level.Level;

import appeng.api.config.Actionable;
import appeng.api.crafting.IPatternDetails;
import appeng.api.stacks.AEFluidKey;
import appeng.api.networking.security.IActionSource;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.AEKeyType;
import appeng.api.stacks.GenericStack;
import appeng.api.stacks.KeyCounter;
import appeng.api.storage.MEStorage;
import appeng.crafting.pattern.AEProcessingPattern;
import appeng.helpers.patternprovider.PatternProviderTarget;
import appeng.me.storage.CompositeStorage;
import appeng.me.storage.ExternalStorageFacade;
import com.google.common.collect.HashMultiset;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Multiset;
import com.raishxn.gtna.GTNACORE;
import com.raishxn.gtna.integration.ae2.pattern.IParallelPatternDetails;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class DirectedTesseractMachine extends MetaMachine
                                      implements IFancyUIMachine, IMachineLife, ITesseractMarkerInteractable {

    protected static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(
            DirectedTesseractMachine.class, MetaMachine.MANAGED_FIELD_HOLDER);

    public static final Multiset<ImmutableList<TesseractDirectedTarget>> HIGHLIGHTS = HashMultiset.create();

    @Persisted
    private final List<String> serializedTargets = new ArrayList<>();

    @DescSynced
    private int targetCount;

    @DescSynced
    private String lastRouteIssue = "";

    private final List<PendingInsert> pendingInserts = new ArrayList<>();
    @Nullable
    private TickableSubscription pendingInsertSubscription;

    public DirectedTesseractMachine(IMachineBlockEntity holder) {
        super(holder);
    }

    @Override
    public ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }

    @Override
    public void onLoad() {
        super.onLoad();
        targetCount = serializedTargets.size();
        updatePendingSubscription();
    }

    @Override
    public void onUnload() {
        super.onUnload();
        if (pendingInsertSubscription != null) {
            pendingInsertSubscription.unsubscribe();
            pendingInsertSubscription = null;
        }
    }

    public boolean pushPatternFromProvider(IPatternDetails patternDetails, KeyCounter[] inputHolder,
                                           Set<AEKey> patternInputs, IActionSource actionSource) {
        lastRouteIssue = "";
        List<TesseractDirectedTarget> targets = getTargets();
        if (targets.isEmpty() || inputHolder.length == 0 || !checkInput(inputHolder)) {
            return false;
        }

        List<OrderedPush> orderedInputs = getOrderedInputs(patternDetails, inputHolder);
        if (orderedInputs.isEmpty() || orderedInputs.size() > targets.size()) {
            GTNACORE.LOGGER.debug("[GTNA] Directed Tesseract {} rejected pattern {} because {} ordered inputs do not fit {} targets",
                    getPos(), patternDetails.getDefinition(), orderedInputs.size(), targets.size());
            return false;
        }
        long fluidInputs = orderedInputs.stream().filter(push -> push.key() instanceof AEFluidKey).count();
        GTNACORE.LOGGER.debug("[GTNA] Directed Tesseract {} received {} ordered inputs ({} fluid) for pattern {}",
                getPos(), orderedInputs.size(), fluidInputs, patternDetails.getDefinition());
        for (int i = 0; i < orderedInputs.size(); i++) {
            OrderedPush push = orderedInputs.get(i);
            GTNACORE.LOGGER.debug("[GTNA] Directed Tesseract {} input[{}] = {} x{}",
                    getPos(), i, push.key(), push.amount());
        }

        List<PendingInsert> readyNow = new ArrayList<>();
        List<PendingInsert> pendingLater = new ArrayList<>();
        for (int i = 0; i < orderedInputs.size(); i++) {
            OrderedPush orderedPush = orderedInputs.get(i);
            TesseractDirectedTarget directedTarget = targets.get(i);
            PatternProviderTarget target = resolveTarget(directedTarget, actionSource);
            MEStorage storage = resolveStorage(directedTarget);
            TargetCapabilitySummary capabilitySummary = inspectTargetCapabilities(directedTarget);
            if (target == null) {
                GTNACORE.LOGGER.debug("[GTNA] Directed Tesseract {} failed to resolve target {}",
                        getPos(), describeTarget(directedTarget));
                return false;
            }
            if (storage == null) {
                GTNACORE.LOGGER.debug("[GTNA] Directed Tesseract {} failed to resolve storage {}",
                        getPos(), describeTarget(directedTarget));
                return false;
            }
            if (target.containsPatternInput(patternInputs)) {
                GTNACORE.LOGGER.debug("[GTNA] Directed Tesseract {} ignoring blocked-target check for explicit route {}",
                        getPos(), describeTarget(directedTarget));
            }
            if (!isTargetCompatible(orderedPush.key(), capabilitySummary)) {
                String inputKind = inputKind(orderedPush.key());
                lastRouteIssue = "Input #" + (i + 1) + " is " + inputKind + ", but target #" + (i + 1) +
                        " is " + capabilitySummary.displayName();
                GTNACORE.LOGGER.warn(
                        "[GTNA] Directed Tesseract {} incompatible route: {} ({} x{} -> {})",
                        getPos(),
                        lastRouteIssue,
                        orderedPush.key(),
                        orderedPush.amount(),
                        describeTarget(directedTarget));
                markDirty();
                return false;
            }

            long amount = orderedPush.amount();
            if (amount <= 0L) {
                continue;
            }

            long inserted = insertIntoTarget(directedTarget, storage, orderedPush.key(), amount, Actionable.SIMULATE,
                    actionSource);
            if (orderedPush.key() instanceof AEFluidKey) {
                GTNACORE.LOGGER.debug("[GTNA] Directed Tesseract {} simulated fluid insert {} / {} into {}",
                        getPos(), inserted, amount, describeTarget(directedTarget));
            }
            if (inserted == amount) {
                readyNow.add(new PendingInsert(directedTarget, orderedPush.key(), amount));
                continue;
            }

            if (inserted > 0L) {
                readyNow.add(new PendingInsert(directedTarget, orderedPush.key(), inserted));
            }
            pendingLater.add(new PendingInsert(directedTarget, orderedPush.key(), amount - inserted));
        }

        for (PendingInsert pendingInsert : readyNow) {
            if (!insertNow(pendingInsert, actionSource)) {
                return false;
            }
        }

        if (!pendingLater.isEmpty()) {
            long fluidPendings = pendingLater.stream().filter(insert -> insert.key() instanceof AEFluidKey).count();
            if (fluidPendings > 0) {
                GTNACORE.LOGGER.debug("[GTNA] Directed Tesseract {} queued {} fluid pending insert(s)",
                        getPos(), fluidPendings);
            }
            pendingInserts.addAll(pendingLater);
            updatePendingSubscription();
            markDirty();
        }
        return true;
    }

    @Override
    public boolean onMarkerInteract(Player player, List<TesseractDirectedTarget> targets) {
        if (targets.isEmpty()) {
            return false;
        }
        setTargets(targets);
        if (!player.level().isClientSide) {
            player.displayClientMessage(Component.translatable("gtna.machine.directed_tesseract.bind_success"), true);
        }
        return true;
    }

    @Override
    public Widget createUIWidget() {
        WidgetGroup group = new WidgetGroup(0, 0, 290, 150);
        group.addWidget(new LabelWidget(8, 6, () -> "Directed Tesseract"));
        group.addWidget(new LabelWidget(8, 18, () -> Component.translatable(
                "gtna.machine.directed_tesseract.target_count", targetCount).getString()));
        group.addWidget(new LabelWidget(8, 30, () -> Component.translatable(
                "gtna.machine.directed_tesseract.pending", pendingInserts.size()).getString()));
        if (!lastRouteIssue.isBlank()) {
            group.addWidget(new LabelWidget(8, 42, () -> "Issue: " + lastRouteIssue));
        }
        group.addWidget(new ButtonWidget(
                186,
                6,
                88,
                14,
                new GuiTextureGroup(GuiTextures.BUTTON, new TextTexture("Highlight")),
                clickData -> {
                    if (clickData.isRemote && !serializedTargets.isEmpty()) {
                        highlightTargets();
                    }
                }));

        int listY = lastRouteIssue.isBlank() ? 48 : 60;
        int listHeight = lastRouteIssue.isBlank() ? 92 : 80;
        DraggableScrollableWidgetGroup targetList = new DraggableScrollableWidgetGroup(8, listY, 274, listHeight)
                .setBackground(GuiTextures.DISPLAY);
        targetList.addWidget(new ComponentPanelWidget(4, 4, components -> components.addAll(getTargetDisplayText()))
                .setMaxWidthLimit(264));
        group.addWidget(targetList);
        return group;
    }

    public List<TesseractDirectedTarget> getTargets() {
        List<TesseractDirectedTarget> targets = new ArrayList<>(serializedTargets.size());
        for (String serializedTarget : serializedTargets) {
            try {
                targets.add(TesseractDirectedTarget.deserialize(serializedTarget));
            } catch (RuntimeException exception) {
                GTNACORE.LOGGER.warn("[GTNA] Invalid serialized tesseract target '{}'", serializedTarget, exception);
            }
        }
        targets.sort(TesseractDirectedTarget.SORTER);
        return targets;
    }

    public void setTargets(List<TesseractDirectedTarget> targets) {
        serializedTargets.clear();
        targets.stream()
                .sorted(TesseractDirectedTarget.SORTER)
                .map(TesseractDirectedTarget::serialize)
                .forEach(serializedTargets::add);
        targetCount = serializedTargets.size();
        lastRouteIssue = "";
        markDirty();
    }

    private void updatePendingSubscription() {
        if (!pendingInserts.isEmpty()) {
            pendingInsertSubscription = subscribeServerTick(pendingInsertSubscription, this::flushPendingInserts);
        } else if (pendingInsertSubscription != null) {
            pendingInsertSubscription.unsubscribe();
            pendingInsertSubscription = null;
        }
    }

    private void flushPendingInserts() {
        if (!(getLevel() instanceof ServerLevel serverLevel)) {
            return;
        }
        for (int i = 0; i < pendingInserts.size(); i++) {
            PendingInsert pendingInsert = pendingInserts.get(i);
            if (insertNow(pendingInsert, IActionSource.empty())) {
                pendingInserts.remove(i--);
            }
        }
        updatePendingSubscription();
        if (pendingInserts.isEmpty()) {
            markDirty();
        }
    }

    private boolean insertNow(PendingInsert pendingInsert, IActionSource actionSource) {
        MEStorage storage = resolveStorage(pendingInsert.target());
        if (storage == null) {
            GTNACORE.LOGGER.debug("[GTNA] Directed Tesseract {} could not resolve storage for pending insert {}",
                    getPos(), describeTarget(pendingInsert.target()));
            return false;
        }
        long inserted = insertIntoTarget(pendingInsert.target(), storage, pendingInsert.key(), pendingInsert.amount(),
                Actionable.MODULATE, actionSource);
        if (pendingInsert.key() instanceof AEFluidKey) {
            GTNACORE.LOGGER.debug("[GTNA] Directed Tesseract {} modulated fluid insert {} / {} into {}",
                    getPos(), inserted, pendingInsert.amount(), describeTarget(pendingInsert.target()));
        }
        return inserted == pendingInsert.amount();
    }

    private List<OrderedPush> getOrderedInputs(IPatternDetails patternDetails, KeyCounter[] inputHolder) {
        List<OrderedPush> ordered = new ArrayList<>();
        if (patternDetails instanceof AEProcessingPattern processingPattern) {
            appendSparseInputs(ordered, processingPattern.getSparseInputs());
            return ordered;
        }
        if (patternDetails instanceof IParallelPatternDetails parallelDetails) {
            IPatternDetails delegate = parallelDetails.getDelegate();
            if (delegate instanceof AEProcessingPattern processingPattern) {
                appendSparseInputs(ordered, scaleSparseInputs(
                        processingPattern.getSparseInputs(), parallelDetails.getParallel()));
                return ordered;
            }
        }
        if (patternDetails.supportsPushInputsToExternalInventory()) {
            patternDetails.pushInputsToExternalInventory(inputHolder, (key, amount) -> {
                if (amount > 0L) {
                    ordered.add(new OrderedPush(key, amount));
                }
            });
            return ordered;
        }
        for (KeyCounter keyCounter : inputHolder) {
            for (var entry : keyCounter) {
                if (entry.getLongValue() > 0L) {
                    ordered.add(new OrderedPush(entry.getKey(), entry.getLongValue()));
                }
            }
        }
        return ordered;
    }

    private boolean checkInput(KeyCounter[] inputHolder) {
        for (KeyCounter input : inputHolder) {
            boolean illegal = input.keySet().stream()
                    .map(AEKey::getType)
                    .map(AEKeyType::getId)
                    .anyMatch(id -> !id.equals(AEKeyType.items().getId()) && !id.equals(AEKeyType.fluids().getId()));
            if (illegal) {
                return false;
            }
        }
        return true;
    }

    private @Nullable PatternProviderTarget resolveTarget(TesseractDirectedTarget target, IActionSource actionSource) {
        if (!(getLevel() instanceof ServerLevel serverLevel)) {
            return null;
        }
        ServerLevel targetLevel = serverLevel.getServer().getLevel(target.pos().dimension());
        if (targetLevel == null) {
            return null;
        }
        var blockEntity = targetLevel.getBlockEntity(target.pos().pos());
        if (blockEntity == null || blockEntity.isRemoved()) {
            return null;
        }
        PatternProviderTarget directTarget = PatternProviderTarget.get(
                targetLevel, target.pos().pos(), blockEntity, target.face(), actionSource);
        if (directTarget != null) {
            return directTarget;
        }
        return PatternProviderTarget.get(
                targetLevel, target.pos().pos(), blockEntity, target.face().getOpposite(), actionSource);
    }

    private long insertIntoTarget(TesseractDirectedTarget target, MEStorage storage, AEKey key, long amount,
                                  Actionable actionable, IActionSource actionSource) {
        if (amount <= 0L) {
            return 0L;
        }
        long inserted = storage.insert(key, amount, actionable, actionSource);
        if (inserted > 0L || !(key instanceof AEFluidKey fluidKey)) {
            return inserted;
        }
        return insertFluidAcrossAllSides(target, fluidKey, amount, actionable, actionSource);
    }

    private @Nullable MEStorage resolveStorage(TesseractDirectedTarget target) {
        if (!(getLevel() instanceof ServerLevel serverLevel)) {
            return null;
        }
        ServerLevel targetLevel = serverLevel.getServer().getLevel(target.pos().dimension());
        if (targetLevel == null) {
            return null;
        }
        var blockEntity = targetLevel.getBlockEntity(target.pos().pos());
        if (blockEntity == null || blockEntity.isRemoved()) {
            return null;
        }

        MEStorage directStorage = createStorage(blockEntity, target.face());
        if (directStorage != null) {
            return directStorage;
        }
        return createStorage(blockEntity, target.face().getOpposite());
    }

    private static @Nullable MEStorage createStorage(net.minecraft.world.level.block.entity.BlockEntity blockEntity,
                                                     net.minecraft.core.Direction side) {
        MEStorage itemStorage = blockEntity.getCapability(ForgeCapabilities.ITEM_HANDLER, side)
                .map(ExternalStorageFacade::of)
                .orElse(null);
        MEStorage fluidStorage = blockEntity.getCapability(ForgeCapabilities.FLUID_HANDLER, side)
                .map(ExternalStorageFacade::of)
                .orElse(null);
        if (itemStorage != null && fluidStorage != null) {
            return new CompositeStorage(Map.of(AEKeyType.items(), itemStorage, AEKeyType.fluids(), fluidStorage));
        }
        if (itemStorage != null) {
            return itemStorage;
        }
        return fluidStorage;
    }

    private long insertFluidAcrossAllSides(TesseractDirectedTarget target, AEFluidKey fluidKey, long amount,
                                           Actionable actionable,
                                           IActionSource actionSource) {
        if (!(getLevel() instanceof ServerLevel serverLevel)) {
            return 0L;
        }
        ServerLevel targetLevel = serverLevel.getServer().getLevel(target.pos().dimension());
        if (targetLevel == null) {
            return 0L;
        }
        var blockEntity = targetLevel.getBlockEntity(target.pos().pos());
        if (blockEntity == null || blockEntity.isRemoved()) {
            return 0L;
        }

        logFluidTargetDiagnostics(target, blockEntity, fluidKey, amount);

        List<Direction> orderedSides = new ArrayList<>(8);
        orderedSides.add(target.face());
        orderedSides.add(target.face().getOpposite());
        for (Direction direction : Direction.values()) {
            if (!orderedSides.contains(direction)) {
                orderedSides.add(direction);
            }
        }

        for (Direction direction : orderedSides) {
            MEStorage storage = createStorage(blockEntity, direction);
            if (storage == null) {
                continue;
            }
            long inserted = storage.insert(fluidKey, amount, actionable, actionSource);
            if (inserted > 0L) {
                GTNACORE.LOGGER.debug("[GTNA] Directed Tesseract {} inserted fluid {} / {} into {} via fallback side {}",
                        getPos(), inserted, amount, describeTarget(target), direction);
                return inserted;
            }
        }
        MEStorage nullSideStorage = createStorage(blockEntity, null);
        if (nullSideStorage != null) {
            long inserted = nullSideStorage.insert(fluidKey, amount, actionable, actionSource);
            if (inserted > 0L) {
                GTNACORE.LOGGER.debug("[GTNA] Directed Tesseract {} inserted fluid {} / {} into {} via fallback null side",
                        getPos(), inserted, amount, describeTarget(target));
                return inserted;
            }
        }
        return 0L;
    }

    private void logFluidTargetDiagnostics(TesseractDirectedTarget target,
                                           net.minecraft.world.level.block.entity.BlockEntity blockEntity,
                                           AEFluidKey fluidKey, long amount) {
        String machineInfo = "none";
        if (blockEntity instanceof MetaMachineBlockEntity machineBlockEntity && machineBlockEntity.getMetaMachine() != null) {
            MetaMachine metaMachine = machineBlockEntity.getMetaMachine();
            machineInfo = metaMachine.getClass().getSimpleName() + " front=" + metaMachine.getFrontFacing();
        }
        GTNACORE.LOGGER.debug("[GTNA] Fluid diagnostics for {} blockEntity={} block={} machine={} amount={} fluid={}",
                describeTarget(target),
                blockEntity.getClass().getName(),
                blockEntity.getBlockState().getBlock(),
                machineInfo,
                amount,
                fluidKey);

        logFluidSideDiagnostics(blockEntity, target, fluidKey, amount, null, "null");
        for (Direction direction : Direction.values()) {
            logFluidSideDiagnostics(blockEntity, target, fluidKey, amount, direction, direction.getName());
        }
    }

    private void logFluidSideDiagnostics(net.minecraft.world.level.block.entity.BlockEntity blockEntity,
                                         TesseractDirectedTarget target, AEFluidKey fluidKey, long amount,
                                         @Nullable Direction direction, String label) {
        var capability = blockEntity.getCapability(ForgeCapabilities.FLUID_HANDLER, direction);
        IFluidHandler handler = capability.orElse(null);
        if (handler == null) {
            GTNACORE.LOGGER.debug("[GTNA] Fluid diagnostics {} side={} handler=false", describeTarget(target), label);
            return;
        }

        int accepted = handler.fill(fluidKey.toStack((int) Math.min(Integer.MAX_VALUE, amount)),
                IFluidHandler.FluidAction.SIMULATE);
        GTNACORE.LOGGER.debug("[GTNA] Fluid diagnostics {} side={} handler=true tanks={} accepted={} / {}",
                describeTarget(target),
                label,
                handler.getTanks(),
                accepted,
                amount);
    }

    private List<Component> getTargetDisplayText() {
        List<TesseractDirectedTarget> targets = getTargets();
        List<Component> lines = new ArrayList<>();
        lines.add(Component.translatable("gtna.machine.directed_tesseract.ui.provider"));
        lines.add(Component.translatable("gtna.machine.directed_tesseract.ui.order"));
        if (targets.isEmpty()) {
            lines.add(Component.translatable("gtna.machine.directed_tesseract.ui.empty"));
            return lines;
        }
        lines.add(Component.literal(""));
        lines.add(Component.literal("Bound Targets"));
        for (TesseractDirectedTarget target : targets) {
            lines.add(Component.literal(formatTargetLine(target) + " [" + inspectTargetCapabilities(target).displayName() + "]"));
        }
        return lines;
    }

    private void highlightTargets() {
        HIGHLIGHTS.add(ImmutableList.copyOf(getTargets()), 200);
    }

    private static void appendSparseInputs(List<OrderedPush> ordered, GenericStack[] sparseInputs) {
        for (GenericStack sparseInput : sparseInputs) {
            if (sparseInput == null || sparseInput.amount() <= 0L) {
                continue;
            }
            ordered.add(new OrderedPush(sparseInput.what(), sparseInput.amount()));
        }
    }

    private static GenericStack[] scaleSparseInputs(GenericStack[] sparseInputs, long multiplier) {
        GenericStack[] scaled = new GenericStack[sparseInputs.length];
        for (int i = 0; i < sparseInputs.length; i++) {
            GenericStack sparseInput = sparseInputs[i];
            if (sparseInput == null) {
                continue;
            }
            scaled[i] = new GenericStack(sparseInput.what(), safeMultiply(sparseInput.amount(), multiplier));
        }
        return scaled;
    }

    private static long safeMultiply(long left, long right) {
        if (left <= 0L || right <= 0L) {
            return 0L;
        }
        if (left > Long.MAX_VALUE / right) {
            return Long.MAX_VALUE;
        }
        return left * right;
    }

    private static String describeTarget(TesseractDirectedTarget target) {
        return formatTargetLine(target);
    }

    private static String formatTargetLine(TesseractDirectedTarget target) {
        ResourceKey<Level> dim = target.pos().dimension();
        var pos = target.pos().pos();
        return "#" + target.order() + " " + target.face().getName() + " @ " + pos.getX() + ", " + pos.getY() + ", " +
                pos.getZ() + " [" + dim.location() + "]";
    }

    private TargetCapabilitySummary inspectTargetCapabilities(TesseractDirectedTarget target) {
        if (!(getLevel() instanceof ServerLevel serverLevel)) {
            return TargetCapabilitySummary.UNKNOWN;
        }
        ServerLevel targetLevel = serverLevel.getServer().getLevel(target.pos().dimension());
        if (targetLevel == null) {
            return TargetCapabilitySummary.UNKNOWN;
        }
        var blockEntity = targetLevel.getBlockEntity(target.pos().pos());
        if (blockEntity == null || blockEntity.isRemoved()) {
            return TargetCapabilitySummary.UNKNOWN;
        }
        if (blockEntity instanceof MetaMachineBlockEntity machineBlockEntity && machineBlockEntity.getMetaMachine() != null) {
            String machineName = machineBlockEntity.getMetaMachine().getClass().getSimpleName();
            if (machineName.contains("FluidHatchPartMachine")) {
                return TargetCapabilitySummary.FLUID_HATCH;
            }
            if (machineName.contains("ItemBusPartMachine")) {
                return TargetCapabilitySummary.ITEM_BUS;
            }
        }

        boolean item = false;
        boolean fluid = false;
        for (Direction direction : Direction.values()) {
            item |= blockEntity.getCapability(ForgeCapabilities.ITEM_HANDLER, direction).isPresent();
            fluid |= blockEntity.getCapability(ForgeCapabilities.FLUID_HANDLER, direction).isPresent();
        }
        item |= blockEntity.getCapability(ForgeCapabilities.ITEM_HANDLER, null).isPresent();
        fluid |= blockEntity.getCapability(ForgeCapabilities.FLUID_HANDLER, null).isPresent();

        if (item && fluid) {
            return TargetCapabilitySummary.BOTH;
        }
        if (fluid) {
            return TargetCapabilitySummary.FLUID;
        }
        if (item) {
            return TargetCapabilitySummary.ITEM;
        }
        return TargetCapabilitySummary.NONE;
    }

    private static boolean isTargetCompatible(AEKey key, TargetCapabilitySummary capabilitySummary) {
        if (key instanceof AEFluidKey) {
            return capabilitySummary.acceptsFluid();
        }
        return capabilitySummary.acceptsItem();
    }

    private static String inputKind(AEKey key) {
        return key instanceof AEFluidKey ? "Fluid" : "Item";
    }

    private record OrderedPush(AEKey key, long amount) {}

    private record PendingInsert(TesseractDirectedTarget target, AEKey key, long amount) {}

    private enum TargetCapabilitySummary {
        ITEM("Item"),
        FLUID("Fluid"),
        ITEM_BUS("Item Bus"),
        FLUID_HATCH("Fluid Hatch"),
        BOTH("Item+Fluid"),
        NONE("No Cap"),
        UNKNOWN("Unknown");

        private final String displayName;

        TargetCapabilitySummary(String displayName) {
            this.displayName = displayName;
        }

        public boolean acceptsItem() {
            return this == ITEM || this == ITEM_BUS || this == BOTH || this == UNKNOWN;
        }

        public boolean acceptsFluid() {
            return this == FLUID || this == FLUID_HATCH || this == BOTH || this == UNKNOWN;
        }

        public String displayName() {
            return displayName;
        }
    }
}
