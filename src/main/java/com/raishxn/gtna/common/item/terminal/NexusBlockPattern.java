package com.raishxn.gtna.common.item.terminal;

import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiController;
import com.gregtechceu.gtceu.api.pattern.BlockPattern;
import com.gregtechceu.gtceu.api.pattern.MultiblockState;
import com.gregtechceu.gtceu.api.pattern.TraceabilityPredicate;
import com.gregtechceu.gtceu.api.pattern.predicates.SimplePredicate;
import com.gregtechceu.gtceu.api.pattern.util.RelativeDirection;
import com.gregtechceu.gtceu.common.block.CoilBlock;

import com.lowdragmc.lowdraglib.utils.BlockInfo;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;

import com.mojang.datafixers.util.Pair;
import com.raishxn.gtna.common.item.terminal.ui.NexusTerminalUIFactory;
import it.unimi.dsi.fastutil.ints.IntObjectPair;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import org.apache.commons.lang3.ArrayUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;
import java.util.function.BiPredicate;
import java.util.function.Consumer;

/**
 * Extended BlockPattern that supports advanced auto-building features
 * for the Nexus Structure Terminal. Based on GTMThings AdvancedBlockPattern.
 */
public class NexusBlockPattern extends BlockPattern {

    static Direction[] FACINGS = { Direction.SOUTH, Direction.NORTH, Direction.WEST, Direction.EAST, Direction.UP,
            Direction.DOWN };
    static Direction[] FACINGS_H = { Direction.SOUTH, Direction.NORTH, Direction.WEST, Direction.EAST };

    public final int[][] aisleRepetitions;
    public final RelativeDirection[] structureDir;
    protected final TraceabilityPredicate[][][] blockMatches;
    protected final int fingerLength;
    protected final int thumbLength;
    protected final int palmLength;
    protected final int[] centerOffset;

    public NexusBlockPattern(TraceabilityPredicate[][][] predicatesIn, RelativeDirection[] structureDir,
                             int[][] aisleRepetitions, int[] centerOffset) {
        super(predicatesIn, structureDir, aisleRepetitions, centerOffset);
        this.blockMatches = predicatesIn;
        this.fingerLength = predicatesIn.length;
        this.structureDir = structureDir;
        this.aisleRepetitions = aisleRepetitions;

        if (this.fingerLength > 0) {
            this.thumbLength = predicatesIn[0].length;
            if (this.thumbLength > 0) {
                this.palmLength = predicatesIn[0][0].length;
            } else {
                this.palmLength = 0;
            }
        } else {
            this.thumbLength = 0;
            this.palmLength = 0;
        }

        this.centerOffset = centerOffset;
    }

    @Nullable
    public static NexusBlockPattern fromBlockPattern(BlockPattern blockPattern) {
        try {
            Class<?> clazz = BlockPattern.class;
            Field blockMatchesField = clazz.getDeclaredField("blockMatches");
            blockMatchesField.setAccessible(true);
            TraceabilityPredicate[][][] blockMatches = (TraceabilityPredicate[][][]) blockMatchesField
                    .get(blockPattern);

            Field structureDirField = clazz.getDeclaredField("structureDir");
            structureDirField.setAccessible(true);
            RelativeDirection[] structureDir = (RelativeDirection[]) structureDirField.get(blockPattern);

            Field aisleRepetitionsField = clazz.getDeclaredField("aisleRepetitions");
            aisleRepetitionsField.setAccessible(true);
            int[][] aisleRepetitions = (int[][]) aisleRepetitionsField.get(blockPattern);

            Field centerOffsetField = clazz.getDeclaredField("centerOffset");
            centerOffsetField.setAccessible(true);
            int[] centerOffset = (int[]) centerOffsetField.get(blockPattern);

            return new NexusBlockPattern(blockMatches, structureDir, aisleRepetitions, centerOffset);
        } catch (Exception ignored) {}
        return null;
    }

    public void autoBuild(Player player, MultiblockState worldState, NexusTerminalUIFactory.AutoBuildSetting setting,
                          ItemStack terminalStack) {
        Level world = player.level();
        int minZ = -centerOffset[4];
        clearWorldState(worldState);
        IMultiController controller = worldState.getController();
        BlockPos centerPos = controller.self().getPos();
        Direction facing = controller.self().getFrontFacing();
        Direction upwardsFacing = controller.self().getUpwardsFacing();
        boolean isFlipped = controller.self().isFlipped();
        Object2IntOpenHashMap<SimplePredicate> cacheGlobal = worldState.getGlobalCount();
        Object2IntOpenHashMap<SimplePredicate> cacheLayer = worldState.getLayerCount();
        Map<BlockPos, Object> blocks = new HashMap<>();
        Set<BlockPos> placeBlockPos = new HashSet<>();
        blocks.put(centerPos, controller);

        int[] repeat = new int[this.fingerLength];
        for (int h = 0; h < this.fingerLength; h++) {
            var minH = aisleRepetitions[h][0];
            var maxH = aisleRepetitions[h][1];
            if (minH != maxH) {
                repeat[h] = Math.max(minH, Math.min(maxH, setting.getRepetitions()));
            } else {
                repeat[h] = minH;
            }
        }

        boolean noHatch = setting.isNoHatchMode();
        boolean replaceMode = setting.isReplaceMode();
        int useAE = setting.isUseAE() ? 1 : 0;

        for (int c = 0, z = minZ++, r; c < this.fingerLength; c++) {
            for (r = 0; r < repeat[c]; r++) {
                cacheLayer.clear();
                for (int b = 0, y = -centerOffset[1]; b < this.thumbLength; b++, y++) {
                    for (int a = 0, x = -centerOffset[0]; a < this.palmLength; a++, x++) {
                        TraceabilityPredicate predicate = this.blockMatches[c][b][a];
                        BlockPos pos = setActualRelativeOffset(x, y, z, facing, upwardsFacing, isFlipped)
                                .offset(centerPos.getX(), centerPos.getY(), centerPos.getZ());
                        updateWorldState(worldState, pos, predicate);

                        ItemStack replaceItemStack = null;
                        if (!world.isEmptyBlock(pos)) {
                            Block blockStateBlock = world.getBlockState(pos).getBlock();
                            if (replaceMode && !(blockStateBlock instanceof net.minecraft.world.level.block.AirBlock)) {
                                replaceItemStack = blockStateBlock.asItem().getDefaultInstance();
                            } else {
                                blocks.put(pos, world.getBlockState(pos));
                                for (SimplePredicate limit : predicate.limited) {
                                    limit.testLimited(worldState);
                                }
                                continue;
                            }
                        }

                        boolean find = false;
                        BlockInfo[] infos = new BlockInfo[0];
                        for (SimplePredicate limit : predicate.limited) {
                            if (limit.minLayerCount > 0 && isPlaceHatch(limit.candidates.get(), noHatch)) {
                                int curr = cacheLayer.getInt(limit);
                                if (curr < limit.minLayerCount &&
                                        (limit.maxLayerCount == -1 || curr < limit.maxLayerCount)) {
                                    cacheLayer.addTo(limit, 1);
                                } else {
                                    continue;
                                }
                            } else {
                                continue;
                            }
                            infos = limit.candidates == null ? null : limit.candidates.get();
                            find = true;
                            break;
                        }
                        if (!find) {
                            for (SimplePredicate limit : predicate.limited) {
                                if (limit.minCount > 0 && isPlaceHatch(limit.candidates.get(), noHatch)) {
                                    int curr = cacheGlobal.getInt(limit);
                                    if (curr < limit.minCount && (limit.maxCount == -1 || curr < limit.maxCount)) {
                                        cacheGlobal.addTo(limit, 1);
                                    } else {
                                        continue;
                                    }
                                } else {
                                    continue;
                                }
                                infos = limit.candidates == null ? null : limit.candidates.get();
                                find = true;
                                break;
                            }
                        }
                        if (!find) {
                            for (SimplePredicate limit : predicate.limited) {
                                if (!isPlaceHatch(limit.candidates.get(), noHatch)) {
                                    continue;
                                }
                                if (limit.maxLayerCount != -1 &&
                                        cacheLayer.getOrDefault(limit, Integer.MAX_VALUE) == limit.maxLayerCount) {
                                    continue;
                                }
                                if (limit.maxCount != -1 &&
                                        cacheGlobal.getOrDefault(limit, Integer.MAX_VALUE) == limit.maxCount) {
                                    continue;
                                }
                                cacheLayer.addTo(limit, 1);
                                cacheGlobal.addTo(limit, 1);
                                infos = ArrayUtils.addAll(infos,
                                        limit.candidates == null ? null : limit.candidates.get());
                            }
                            for (SimplePredicate common : predicate.common) {
                                if (common.candidates != null && predicate.common.size() > 1 &&
                                        !isPlaceHatch(common.candidates.get(), noHatch)) {
                                    continue;
                                }
                                infos = ArrayUtils.addAll(infos,
                                        common.candidates == null ? null : common.candidates.get());
                            }
                        }

                        List<ItemStack> candidates = applySetting(infos, setting, terminalStack);

                        if (replaceMode && replaceItemStack != null &&
                                !candidates.isEmpty() && ItemStack.isSameItem(candidates.get(0), replaceItemStack)) {
                            continue;
                        }

                        // Check AE2 network first if enabled
                        ItemStack found = null;
                        IItemHandler handler = null;
                        int foundSlot = -1;

                        if (useAE == 1 && com.raishxn.gtna.integration.ae2.NexusAE2Link.isAE2Available()) {
                            ItemStack extracted = com.raishxn.gtna.integration.ae2.NexusAE2Link
                                    .extractItem(terminalStack, world, player, candidates);
                            if (extracted != null) {
                                found = extracted;
                                handler = new ItemStackHandler(NonNullList.withSize(1, extracted));
                                foundSlot = 0;
                            }
                        }

                        // Fallback to check inventory for matching items
                        if (found == null) {
                            if (!player.isCreative()) {
                                var foundHandler = getMatchStackWithHandler(candidates,
                                        player.getCapability(ForgeCapabilities.ITEM_HANDLER), player);
                                if (foundHandler != null) {
                                    foundSlot = foundHandler.firstInt();
                                    handler = foundHandler.second();
                                    found = handler.getStackInSlot(foundSlot).copy();
                                }
                            } else {
                                for (ItemStack candidate : candidates) {
                                    found = candidate.copy();
                                    if (!found.isEmpty() && found.getItem() instanceof BlockItem) {
                                        break;
                                    }
                                    found = null;
                                }
                            }
                        }

                        if (found == null) continue;

                        // Handle replace mode: remove old block and return to inventory
                        IItemHandler holderHandler = null;
                        int holderSlot = -1;
                        if (replaceMode && replaceItemStack != null) {
                            Pair<IItemHandler, Integer> holderResult = foundHolderSlot(player, replaceItemStack);
                            holderHandler = holderResult.getFirst();
                            holderSlot = holderResult.getSecond();
                            if (holderHandler != null && holderSlot < 0) {
                                continue;
                            }
                        }

                        if (replaceMode && replaceItemStack != null) {
                            world.removeBlock(pos, true);
                            if (holderHandler != null) holderHandler.insertItem(holderSlot, replaceItemStack, false);
                        }

                        BlockItem itemBlock = (BlockItem) found.getItem();
                        BlockPlaceContext context = new BlockPlaceContext(world, player, InteractionHand.MAIN_HAND,
                                found, BlockHitResult.miss(player.getEyePosition(0), Direction.UP, pos));
                        InteractionResult interactionResult = itemBlock.place(context);
                        if (interactionResult != InteractionResult.FAIL) {
                            placeBlockPos.add(pos);
                            if (handler != null) {
                                handler.extractItem(foundSlot, 1, false);
                            }
                        }
                        if (world.getBlockEntity(pos) instanceof IMachineBlockEntity machineBlockEntity) {
                            blocks.put(pos, machineBlockEntity.getMetaMachine());
                        } else {
                            blocks.put(pos, world.getBlockState(pos));
                        }
                    }
                }
                z++;
            }
        }
        Direction frontFacing = controller.self().getFrontFacing();
        blocks.forEach((pos, block) -> {
            if (!(block instanceof IMultiController)) {
                if (block instanceof BlockState && placeBlockPos.contains(pos)) {
                    resetFacing(pos, (BlockState) block, frontFacing, (p, f) -> {
                        Object object = blocks.get(p.relative(f));
                        return object == null ||
                                (object instanceof BlockState && ((BlockState) object).getBlock() == Blocks.AIR);
                    }, state -> world.setBlock(pos, state, 3));
                } else if (block instanceof MetaMachine machine) {
                    resetFacing(pos, machine.getBlockState(), frontFacing, (p, f) -> {
                        Object object = blocks.get(p.relative(f));
                        if (object == null || (object instanceof BlockState blockState && blockState.isAir())) {
                            return machine.isFacingValid(f);
                        }
                        return false;
                    }, state -> world.setBlock(pos, state, 3));
                }
            }
        });
    }

    /**
     * Determine if a block position should place a hatch or not
     * based on the noHatch setting.
     */
    private boolean isPlaceHatch(BlockInfo[] blockInfos, boolean noHatch) {
        if (!noHatch) return true;
        if (blockInfos != null && blockInfos.length > 0) {
            var blockInfo = blockInfos[0];
            return !(blockInfo.getBlockState()
                    .getBlock() instanceof com.gregtechceu.gtceu.api.block.MetaMachineBlock machineBlock) ||
                    !isHatchBlock(machineBlock);
        }
        return true;
    }

    /**
     * Check if a MetaMachineBlock is a hatch (multiblock part machine).
     */
    private boolean isHatchBlock(com.gregtechceu.gtceu.api.block.MetaMachineBlock machineBlock) {
        try {
            var def = machineBlock.getDefinition();
            var machine = def.createMetaMachine(
                    (IMachineBlockEntity) def.getBlockEntityType().create(
                            BlockPos.ZERO, machineBlock.defaultBlockState()));
            return machine instanceof com.gregtechceu.gtceu.api.machine.multiblock.part.MultiblockPartMachine;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Apply settings (coil tier selection, etc.) to candidate BlockInfos.
     */
    private List<ItemStack> applySetting(BlockInfo[] blockInfos, NexusTerminalUIFactory.AutoBuildSetting setting,
                                         ItemStack terminalStack) {
        List<ItemStack> candidates = new ArrayList<>();
        if (blockInfos != null && blockInfos.length > 0) {
            boolean processed = false;

            // Check if this block info array matches a configurable BlockCategory
            com.raishxn.gtna.common.item.terminal.ui.BlockSelectionConfigWidget.BlockCategory category = null;

            if (Arrays.stream(blockInfos).anyMatch(info -> info.getBlockState().getBlock() instanceof CoilBlock)) {
                category = com.raishxn.gtna.common.item.terminal.ui.BlockSelectionConfigWidget.BlockCategory.COILS;
            } else if (Arrays.stream(blockInfos)
                    .anyMatch(info -> info.getItemStackForm().getDescriptionId().contains("machine_casing"))) {
                        category = com.raishxn.gtna.common.item.terminal.ui.BlockSelectionConfigWidget.BlockCategory.MACHINE_CASING;
                    } else
                if (Arrays.stream(blockInfos).anyMatch(info -> info.getBlockState()
                        .getBlock() instanceof com.raishxn.gtna.common.block.NexusCapacitorBlock)) {
                            category = com.raishxn.gtna.common.item.terminal.ui.BlockSelectionConfigWidget.BlockCategory.WIRELESS_CAPACITOR;
                        } else
                    if (Arrays.stream(blockInfos).anyMatch(info -> isHatchBlock(info))) {
                        // Determine if it's a muffler or rotor holder hatch based on the blocks (simplified)
                        if (Arrays.stream(blockInfos)
                                .anyMatch(info -> info.getItemStackForm().getDescriptionId().contains("muffler"))) {
                            category = com.raishxn.gtna.common.item.terminal.ui.BlockSelectionConfigWidget.BlockCategory.MUFFLER;
                        } else if (Arrays.stream(blockInfos)
                                .anyMatch(info -> info.getItemStackForm().getDescriptionId().contains("rotor"))) {
                                    category = com.raishxn.gtna.common.item.terminal.ui.BlockSelectionConfigWidget.BlockCategory.ROTOR_HOLDER;
                                }
                    }

            if (category != null) {
                ItemStack selectedBlock = com.raishxn.gtna.common.item.terminal.ui.BlockSelectionConfigWidget
                        .getSelectedBlock(terminalStack, category);
                if (selectedBlock != null && !selectedBlock.isEmpty()) {
                    candidates.add(selectedBlock.copy());
                    processed = true;
                }
            }

            // Fallback: Just return available candidates if nothing was overriden by UI selection
            if (!processed) {
                for (BlockInfo info : blockInfos) {
                    if (info.getBlockState().getBlock() != net.minecraft.world.level.block.Blocks.AIR) {
                        candidates.add(info.getItemStackForm());
                    }
                }
            }
        }
        return candidates;
    }

    private boolean isHatchBlock(BlockInfo info) {
        return info.getBlockState().getBlock() instanceof com.gregtechceu.gtceu.api.block.MetaMachineBlock;
    }

    private Pair<IItemHandler, Integer> foundHolderSlot(Player player, ItemStack targetStack) {
        IItemHandler handler = null;
        int foundSlot = -1;
        if (!player.isCreative()) {
            handler = player.getCapability(ForgeCapabilities.ITEM_HANDLER).orElse(null);
            if (handler != null) {
                for (int i = 0; i < handler.getSlots(); i++) {
                    @NotNull
                    ItemStack stack = handler.getStackInSlot(i);
                    if (stack.isEmpty()) {
                        if (foundSlot < 0) {
                            foundSlot = i;
                        }
                    } else if (ItemStack.isSameItemSameTags(targetStack, stack) &&
                            (stack.getCount() + 1) <= stack.getMaxStackSize()) {
                                foundSlot = i;
                            }
                }
            }
        }
        return new Pair<>(handler, foundSlot);
    }

    private void clearWorldState(MultiblockState worldState) {
        try {
            Class<?> clazz = Class.forName("com.gregtechceu.gtceu.api.pattern.MultiblockState");
            Method method = clazz.getDeclaredMethod("clean");
            method.setAccessible(true);
            method.invoke(worldState);
        } catch (Exception ignored) {}
    }

    private void updateWorldState(MultiblockState worldState, BlockPos posIn, TraceabilityPredicate predicate) {
        try {
            Class<?> clazz = Class.forName("com.gregtechceu.gtceu.api.pattern.MultiblockState");
            Method method = clazz.getDeclaredMethod("update", BlockPos.class, TraceabilityPredicate.class);
            method.setAccessible(true);
            method.invoke(worldState, posIn, predicate);
        } catch (Exception ignored) {}
    }

    private BlockPos setActualRelativeOffset(int x, int y, int z, Direction facing, Direction upwardsFacing,
                                             boolean isFlipped) {
        int[] c0 = new int[] { x, y, z }, c1 = new int[3];
        if (facing == Direction.UP || facing == Direction.DOWN) {
            Direction of = facing == Direction.DOWN ? upwardsFacing : upwardsFacing.getOpposite();
            for (int i = 0; i < 3; i++) {
                switch (structureDir[i].getActualDirection(of)) {
                    case UP -> c1[1] = c0[i];
                    case DOWN -> c1[1] = -c0[i];
                    case WEST -> c1[0] = -c0[i];
                    case EAST -> c1[0] = c0[i];
                    case NORTH -> c1[2] = -c0[i];
                    case SOUTH -> c1[2] = c0[i];
                }
            }
            int xOffset = upwardsFacing.getStepX();
            int zOffset = upwardsFacing.getStepZ();
            int tmp;
            if (xOffset == 0) {
                tmp = c1[2];
                c1[2] = zOffset > 0 ? c1[1] : -c1[1];
                c1[1] = zOffset > 0 ? -tmp : tmp;
            } else {
                tmp = c1[0];
                c1[0] = xOffset > 0 ? c1[1] : -c1[1];
                c1[1] = xOffset > 0 ? -tmp : tmp;
            }
            if (isFlipped) {
                if (upwardsFacing == Direction.NORTH || upwardsFacing == Direction.SOUTH) {
                    c1[0] = -c1[0];
                } else {
                    c1[2] = -c1[2];
                }
            }
        } else {
            for (int i = 0; i < 3; i++) {
                switch (structureDir[i].getActualDirection(facing)) {
                    case UP -> c1[1] = c0[i];
                    case DOWN -> c1[1] = -c0[i];
                    case WEST -> c1[0] = -c0[i];
                    case EAST -> c1[0] = c0[i];
                    case NORTH -> c1[2] = -c0[i];
                    case SOUTH -> c1[2] = c0[i];
                }
            }
            if (upwardsFacing == Direction.WEST || upwardsFacing == Direction.EAST) {
                int xOffset = upwardsFacing == Direction.EAST ? facing.getClockWise().getStepX() :
                        facing.getClockWise().getOpposite().getStepX();
                int zOffset = upwardsFacing == Direction.EAST ? facing.getClockWise().getStepZ() :
                        facing.getClockWise().getOpposite().getStepZ();
                int tmp;
                if (xOffset == 0) {
                    tmp = c1[2];
                    c1[2] = zOffset > 0 ? -c1[1] : c1[1];
                    c1[1] = zOffset > 0 ? tmp : -tmp;
                } else {
                    tmp = c1[0];
                    c1[0] = xOffset > 0 ? -c1[1] : c1[1];
                    c1[1] = xOffset > 0 ? tmp : -tmp;
                }
            } else if (upwardsFacing == Direction.SOUTH) {
                c1[1] = -c1[1];
                if (facing.getStepX() == 0) {
                    c1[0] = -c1[0];
                } else {
                    c1[2] = -c1[2];
                }
            }
            if (isFlipped) {
                if (upwardsFacing == Direction.NORTH || upwardsFacing == Direction.SOUTH) {
                    if (facing == Direction.NORTH || facing == Direction.SOUTH) {
                        c1[0] = -c1[0];
                    } else {
                        c1[2] = -c1[2];
                    }
                } else {
                    c1[1] = -c1[1];
                }
            }
        }
        return new BlockPos(c1[0], c1[1], c1[2]);
    }

    @Nullable
    private static IntObjectPair<IItemHandler> getMatchStackWithHandler(
                                                                        List<ItemStack> candidates,
                                                                        LazyOptional<IItemHandler> cap,
                                                                        Player player) {
        IItemHandler handler = cap.resolve().orElse(null);
        if (handler == null) {
            return null;
        }
        for (int i = 0; i < handler.getSlots(); i++) {
            @NotNull
            ItemStack stack = handler.getStackInSlot(i);
            if (stack.isEmpty()) continue;

            @NotNull
            LazyOptional<IItemHandler> stackCap = stack.getCapability(ForgeCapabilities.ITEM_HANDLER);
            if (stackCap.isPresent()) {
                var rt = getMatchStackWithHandler(candidates, stackCap, player);
                if (rt != null) {
                    return rt;
                }
            } else if (candidates.stream().anyMatch(candidate -> ItemStack.isSameItemSameTags(candidate, stack)) &&
                    !stack.isEmpty() && stack.getItem() instanceof BlockItem) {
                        return IntObjectPair.of(i, handler);
                    }
        }
        return null;
    }

    private void resetFacing(BlockPos pos, BlockState blockState, Direction facing,
                             BiPredicate<BlockPos, Direction> checker, Consumer<BlockState> consumer) {
        if (blockState.hasProperty(BlockStateProperties.FACING)) {
            tryFacings(blockState, pos, checker, consumer, BlockStateProperties.FACING,
                    facing == null ? FACINGS : ArrayUtils.addAll(new Direction[] { facing }, FACINGS));
        } else if (blockState.hasProperty(BlockStateProperties.HORIZONTAL_FACING)) {
            tryFacings(blockState, pos, checker, consumer, BlockStateProperties.HORIZONTAL_FACING,
                    facing == null || facing.getAxis() == Direction.Axis.Y ? FACINGS_H :
                            ArrayUtils.addAll(new Direction[] { facing }, FACINGS_H));
        }
    }

    private void tryFacings(BlockState blockState, BlockPos pos, BiPredicate<BlockPos, Direction> checker,
                            Consumer<BlockState> consumer, Property<Direction> property, Direction[] facings) {
        Direction found = null;
        for (Direction f : facings) {
            if (checker.test(pos, f)) {
                found = f;
                break;
            }
        }
        if (found == null) {
            found = Direction.NORTH;
        }
        consumer.accept(blockState.setValue(property, found));
    }
}
