package com.raishxn.gtna.common.machine.multiblock.part;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.gui.widget.SlotWidget;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.feature.IInteractedMachine;
import com.gregtechceu.gtceu.api.machine.feature.IMachineLife;
import com.gregtechceu.gtceu.api.machine.feature.IRecipeLogicMachine;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiController;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IWorkableMultiController;
import com.gregtechceu.gtceu.api.machine.multiblock.part.TieredIOPartMachine;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableItemStackHandler;
import com.gregtechceu.gtceu.api.transfer.item.CustomItemStackHandler;
import com.gregtechceu.gtceu.common.data.GTDamageTypes;

import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import com.lowdragmc.lowdraglib.syncdata.annotation.DescSynced;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

import com.google.common.collect.ImmutableMap;
import com.raishxn.gtna.common.data.GTNAItems;

import java.util.Map;

import javax.annotation.ParametersAreNonnullByDefault;

/**
 * GTOCore {@code GRIND_BALL_HATCH} port: a single-slot IV part that holds one grinding ball for the
 * ISA Mill. The part declares no recipe capability of its own ({@link IO#NONE}); the controller
 * reads the stored ball directly in its recipe gate and applies the durability damage.
 *
 * <p>
 * The front face uses the original GTO {@code ball_hatch_idle}/{@code ball_hatch_spinning} rotor
 * textures while formed; the filtered slot still stores the actual grinding ball item.
 */
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class BallHatchPartMachine extends TieredIOPartMachine
                                  implements IInteractedMachine, IMachineLife {

    protected static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(
            BallHatchPartMachine.class, TieredIOPartMachine.MANAGED_FIELD_HOLDER);

    /** GTOCore grind ball item → grinding tier. Soapstone is tier 1, Aluminium is tier 2. */
    public static final Map<Item, Integer> GRINDBALL = ImmutableMap.of(
            GTNAItems.GRINDBALL_SOAPSTONE.get(), 1,
            GTNAItems.GRINDBALL_ALUMINIUM.get(), 2);

    @Persisted
    @DescSynced
    private boolean isWorking;

    private final NotifiableItemStackHandler inventory;

    public BallHatchPartMachine(IMachineBlockEntity holder, Object... args) {
        super(holder, GTValues.IV, IO.NONE);
        this.inventory = new NotifiableItemStackHandler(this, 1, IO.NONE, IO.NONE,
                size -> new CustomItemStackHandler(size) {

                    @Override
                    public int getSlotLimit(int slot) {
                        return 1;
                    }
                }).setFilter(stack -> GRINDBALL.containsKey(stack.getItem()));
        this.inventory.addChangedListener(this::onBallChanged);
    }

    @Override
    public ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }

    public NotifiableItemStackHandler getInventory() {
        return inventory;
    }

    public ItemStack getBallStack() {
        return inventory.getStackInSlot(0);
    }

    public void setBallStack(ItemStack stack) {
        inventory.setStackInSlot(0, stack);
    }

    /** GTOCore {@code onMachineChanged}: wake the controllers when the ball is inserted or removed. */
    private void onBallChanged() {
        if (isRemote()) {
            return;
        }
        for (IMultiController controller : getControllers()) {
            if (controller instanceof IRecipeLogicMachine recipeLogicMachine) {
                recipeLogicMachine.getRecipeLogic().updateTickSubscription();
            }
        }
    }

    /** True while the controller is running a recipe with this hatch; synced for the client renderer. */
    public boolean isWorking() {
        return isWorking;
    }

    @Override
    public boolean beforeWorking(IWorkableMultiController controller) {
        isWorking = true;
        return true;
    }

    @Override
    public boolean afterWorking(IWorkableMultiController controller) {
        isWorking = false;
        return true;
    }

    /** GTOCore behaviour: touching a running ball hatch hurts, like putting a hand in the mill. */
    @Override
    public InteractionResult onUse(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand,
                                   BlockHitResult hit) {
        if (!isRemote() && isWorking && !player.isCreative()) {
            player.hurt(GTDamageTypes.TURBINE.source(level), 40);
            return InteractionResult.FAIL;
        }
        return InteractionResult.PASS;
    }

    @Override
    public void onMachineRemoved() {
        // GTOCore keeps the ball inside a running hatch: the machine is milled into the product.
        if (!isWorking) {
            clearInventory(inventory.storage);
        }
    }

    @Override
    public Widget createUIWidget() {
        return new WidgetGroup(0, 0, 26, 26)
                .addWidget(new SlotWidget(inventory.storage, 0, 4, 4, true, true));
    }
}
