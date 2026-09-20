package com.raishxn.gtna.common.machine.multiblock.part.ae;

import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.feature.IInteractedMachine;
import com.gregtechceu.gtceu.api.machine.feature.IMachineLife;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiController;
import com.gregtechceu.gtceu.api.machine.multiblock.part.MultiblockPartMachine;
import com.gregtechceu.gtceu.api.machine.trait.RecipeHandlerList;
import com.gregtechceu.gtceu.common.data.GTItems;
import com.gregtechceu.gtceu.utils.ResearchManager;

import com.lowdragmc.lowdraglib.gui.modular.ModularUI;
import com.lowdragmc.lowdraglib.syncdata.annotation.DescSynced;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.TickTask;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

import lombok.Getter;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Proxy for a distant {@link GTNAMEPatternBufferPartMachine} (GTLCore
 * {@code MEPatternBufferProxyPartMachine} parity).
 *
 * <p>
 * Place this part inside a multiblock, then bind it to a buffer with a data stick
 * (shift-right-click the buffer, then right-click the proxy). The proxy borrows the buffer's
 * slot handlers, so the controller around the proxy can run the patterns stored in the buffer.
 *
 * <p>
 * Unlike the reference, which routes through dedicated ME handler traits, this implementation
 * delegates {@link #getRecipeHandlers()} to the buffer: the official GTM controller collects
 * handlers through {@code IMultiPart.getRecipeHandlers()}, so the behaviour is equivalent on the
 * GTM base the GTNA targets.
 */
public class GTNAMEPatternBufferProxyPartMachine extends MultiblockPartMachine
                                                 implements IMachineLife, IInteractedMachine {

    protected static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(
            GTNAMEPatternBufferProxyPartMachine.class, MultiblockPartMachine.MANAGED_FIELD_HOLDER);

    @Persisted
    @Getter
    @DescSynced
    private BlockPos bufferPos;
    private @Nullable GTNAMEPatternBufferPartMachine buffer;
    private boolean bufferResolved;

    public GTNAMEPatternBufferProxyPartMachine(IMachineBlockEntity holder) {
        super(holder);
    }

    @Override
    public ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }

    // ------------------------------------------------------------------
    // Buffer binding
    // ------------------------------------------------------------------

    public void setBuffer(@Nullable BlockPos pos) {
        bufferResolved = true;
        Level level = getLevel();
        releaseBuffer();
        if (level != null && pos != null &&
                MetaMachine.getMachine(level, pos) instanceof GTNAMEPatternBufferPartMachine machine) {
            bufferPos = pos;
            buffer = machine;
            machine.addProxy(this);
            notifyControllerHandlersChanged();
        }
    }

    public @Nullable GTNAMEPatternBufferPartMachine getBuffer() {
        if (!bufferResolved) {
            setBuffer(bufferPos);
        }
        return buffer;
    }

    protected void releaseBuffer() {
        GTNAMEPatternBufferPartMachine old = buffer;
        if (old != null) {
            old.removeProxy(this);
        }
        buffer = null;
        bufferPos = null;
        notifyControllerHandlersChanged();
    }

    /** Called by the buffer when one of its slots is invalidated, so the controllers re-scan. */
    public void onBufferSlotInvalidated(int slot) {
        notifyControllerHandlersChanged();
    }

    /**
     * Asks the controllers that own this part to refresh so the freshly bound buffer's handlers
     * become visible without reforming the structure.
     */
    private void notifyControllerHandlersChanged() {
        if (isRemote()) return;
        for (IMultiController controller : getControllers()) {
            controller.self().notifyBlockUpdate();
        }
        markDirty();
    }

    // ------------------------------------------------------------------
    // IMultiPart
    // ------------------------------------------------------------------

    @Override
    public List<RecipeHandlerList> getRecipeHandlers() {
        GTNAMEPatternBufferPartMachine machine = getBuffer();
        return machine == null ? List.of() : machine.getRecipeHandlers();
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (getLevel() instanceof ServerLevel serverLevel) {
            serverLevel.getServer().tell(new TickTask(0, () -> setBuffer(bufferPos)));
        }
    }

    @Override
    public void onMachineRemoved() {
        releaseBuffer();
    }

    // ------------------------------------------------------------------
    // Interaction / GUI
    // ------------------------------------------------------------------

    @Override
    public InteractionResult onUse(BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand,
                                   BlockHitResult hit) {
        ItemStack stack = player.getItemInHand(hand);
        if (stack.isEmpty() || !stack.is(GTItems.TOOL_DATA_STICK.asItem())) {
            return InteractionResult.PASS;
        }
        if (world.isClientSide) {
            return InteractionResult.SUCCESS;
        }
        // Do not steal a data stick that carries research data.
        if (ResearchManager.readResearchId(stack) != null) {
            return InteractionResult.PASS;
        }
        var tag = stack.getTag();
        if (tag == null || !tag.contains("pos")) {
            return InteractionResult.PASS;
        }
        int[] posArray = tag.getIntArray("pos");
        if (posArray.length == 3) {
            setBuffer(new BlockPos(posArray[0], posArray[1], posArray[2]));
            player.sendSystemMessage(Component.translatable("gtna.machine.pattern_buffer.proxy_bound"));
        }
        return InteractionResult.CONSUME;
    }

    @Override
    public boolean shouldOpenUI(Player player, InteractionHand hand, BlockHitResult hit) {
        return getBuffer() != null;
    }

    @Override
    public ModularUI createUI(Player entityPlayer) {
        GTNAMEPatternBufferPartMachine machine = getBuffer();
        // Fall back to the part's own UI when unbound so the player is not locked out.
        return machine == null ? super.createUI(entityPlayer) : machine.createUI(entityPlayer);
    }
}
