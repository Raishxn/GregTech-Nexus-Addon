package com.raishxn.gtna.common.machine.multiblock.godforge;

import com.gregtechceu.gtceu.api.pattern.BlockPattern;
import com.gregtechceu.gtceu.api.pattern.FactoryBlockPattern;
import com.gregtechceu.gtceu.api.pattern.MultiblockState;
import com.gregtechceu.gtceu.api.pattern.TraceabilityPredicate;

import com.lowdragmc.lowdraglib.utils.BlockInfo;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import com.google.common.base.Suppliers;
import com.raishxn.gtna.common.data.GTNAGodforgeContent;
import com.raishxn.gtna.common.data.multiblock.GTNAMultiBlockFileReader;
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;

import java.util.Map;
import java.util.function.Supplier;

import static com.gregtechceu.gtceu.api.pattern.Predicates.any;
import static com.gregtechceu.gtceu.api.pattern.Predicates.controller;

/**
 * Ring pieces of the Forge of Gods. GTNH builds and removes the rings with {@code buildPiece} while the star renderer
 * is
 * active ({@code buildFirstRing}/{@code destroyFirstRing} and friends); here the same .mbs files are walked once with
 * recording predicates to get the world position of every ring block for the controller's orientation.
 */
public final class GodforgeRings {

    public static final String RING_BLOCKS_KEY = "gtna_godforge_ring_blocks";
    private static final String RING_CHARS = "BCDEGHIK";

    private static final Supplier<BlockPattern>[] COLLECTORS = createCollectors();

    private GodforgeRings() {}

    @SuppressWarnings("unchecked")
    private static Supplier<BlockPattern>[] createCollectors() {
        Supplier<BlockPattern>[] collectors = new Supplier[3];
        String[] names = { "god_forge_ring_1", "god_forge_ring_2", "god_forge_ring_3" };
        for (int i = 0; i < 3; i++) {
            String name = names[i];
            collectors[i] = Suppliers.memoize(() -> {
                FactoryBlockPattern pattern = GTNAMultiBlockFileReader.start(null, name).where('~', controller(any()))
                        .where(' ', any());
                for (char c : RING_CHARS.toCharArray()) pattern.where(c, recorder(c));
                return pattern.build();
            });
        }
        return collectors;
    }

    private static TraceabilityPredicate recorder(char symbol) {
        return new TraceabilityPredicate(state -> {
            Map<BlockPos, Character> blocks = state.getMatchContext().getOrCreate(RING_BLOCKS_KEY,
                    Object2ObjectLinkedOpenHashMap::new);
            blocks.put(state.getPos().immutable(), symbol);
            return true;
        }, () -> new BlockInfo[] { BlockInfo.fromBlockState(blockFor(symbol).defaultBlockState()) });
    }

    public static Block blockFor(char symbol) {
        return switch (symbol) {
            case 'B' -> GTNAGodforgeContent.SINGULARITY_SHIELDING_CASING.get();
            case 'C' -> GTNAGodforgeContent.GUIDANCE_CASING.get();
            case 'D' -> GTNAGodforgeContent.BOUNDLESS_STRUCTURE_CASING.get();
            case 'E' -> GTNAGodforgeContent.MAGNETIC_CONFINEMENT_CASING.get();
            case 'G' -> GTNAGodforgeContent.REMOTE_GRAVITON_FLOW_MODULATOR.get();
            case 'H' -> GTNAGodforgeContent.GRAVITATIONAL_LENS.get();
            case 'I' -> GTNAGodforgeContent.CENTRAL_GRAVITON_FLOW_MODULATOR.get();
            case 'K' -> GTNAGodforgeContent.MEDIAL_GRAVITON_FLOW_MODULATOR.get();
            default -> Blocks.AIR;
        };
    }

    /** World position and pattern symbol of every block of ring {@code ring} (1-3). */
    public static Map<BlockPos, Character> positions(Level level, BlockPos controller, Direction front, Direction up,
                                                     boolean flipped, int ring) {
        MultiblockState state = new MultiblockState(level, controller);
        COLLECTORS[ring - 1].get().checkPatternAt(state, controller, front, up, flipped, false);
        Map<BlockPos, Character> blocks = state.getMatchContext().get(RING_BLOCKS_KEY);
        return blocks == null ? Map.of() : blocks;
    }

    /** {@code buildPiece(..._RING, ...)}: places the ring blocks without neighbour updates. */
    public static void build(Level level, Map<BlockPos, Character> blocks) {
        for (var entry : blocks.entrySet()) {
            if (!level.getBlockState(entry.getKey()).isAir()) continue;
            BlockState state = blockFor(entry.getValue()).defaultBlockState();
            level.setBlock(entry.getKey(), state, Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE);
        }
    }

    /** {@code buildPiece(..._RING_AIR, ...)}: removes the ring blocks into the controller. */
    public static void destroy(Level level, Map<BlockPos, Character> blocks) {
        BlockState air = Blocks.AIR.defaultBlockState();
        for (var entry : blocks.entrySet()) {
            BlockPos pos = entry.getKey();
            if (!level.getBlockState(pos).is(blockFor(entry.getValue()))) continue;
            level.setBlock(pos, air, Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE);
        }
    }
}
