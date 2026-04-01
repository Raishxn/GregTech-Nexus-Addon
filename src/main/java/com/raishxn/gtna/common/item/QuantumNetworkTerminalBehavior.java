package com.raishxn.gtna.common.item;

import com.gregtechceu.gtceu.api.item.component.IItemUIFactory;

import com.lowdragmc.lowdraglib.gui.factory.HeldItemUIFactory;
import com.lowdragmc.lowdraglib.gui.modular.ModularUI;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class QuantumNetworkTerminalBehavior implements IItemUIFactory {

    public static final QuantumNetworkTerminalBehavior INSTANCE = new QuantumNetworkTerminalBehavior();

    protected QuantumNetworkTerminalBehavior() {}

    @Override
    public ModularUI createUI(HeldItemUIFactory.HeldItemHolder playerInventoryHolder, Player entityPlayer) {
        return new QuantumTerminalUI(playerInventoryHolder, entityPlayer).createModularUI();
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Item item, Level level, Player player, InteractionHand usedHand) {
        ItemStack stack = player.getItemInHand(usedHand);
        if (!level.isClientSide() && player instanceof ServerPlayer serverPlayer) {
            HeldItemUIFactory.INSTANCE.openUI(serverPlayer, usedHand);
        }
        return new InteractionResultHolder<>(InteractionResult.SUCCESS, stack);
    }
}
