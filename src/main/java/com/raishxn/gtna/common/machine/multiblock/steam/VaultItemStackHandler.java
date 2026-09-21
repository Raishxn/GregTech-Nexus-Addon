package com.raishxn.gtna.common.machine.multiblock.steam;

import com.gregtechceu.gtceu.api.transfer.item.CustomItemStackHandler;

import net.minecraft.world.item.ItemStack;

/**
 * Item handler that allows large per-slot stacks, so the Steam Item Vault can hold millions of items
 * in a bounded number of slots (GTNL parity, reimplemented).
 */
public class VaultItemStackHandler extends CustomItemStackHandler {

    private final int slotLimit;

    public VaultItemStackHandler(int size, int slotLimit) {
        super(size);
        this.slotLimit = slotLimit;
    }

    @Override
    public int getSlotLimit(int slot) {
        return slotLimit;
    }

    @Override
    protected int getStackLimit(int slot, ItemStack stack) {
        return slotLimit;
    }
}
