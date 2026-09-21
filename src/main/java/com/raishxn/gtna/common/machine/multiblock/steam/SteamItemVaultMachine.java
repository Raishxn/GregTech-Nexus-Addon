package com.raishxn.gtna.common.machine.multiblock.steam;

import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableItemStackHandler;

import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import com.raishxn.gtna.common.machine.multiMachineBase.SteamMultiMachineBase;

import java.util.List;

/**
 * GTNA-native port of GTNL's Steam Item Vault (GPL-3.0 structure, reimplemented): a very large item
 * storage for the steam era. Each slot holds a large stack, and the contents are reachable through
 * the structure's item buses.
 */
public class SteamItemVaultMachine extends SteamMultiMachineBase {

    protected static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(
            SteamItemVaultMachine.class, SteamMultiMachineBase.MANAGED_FIELD_HOLDER);

    /** 256 slots x 64,000 per slot = 16,384,000 items. */
    private static final int SLOTS = 256;
    private static final int SLOT_LIMIT = 64_000;

    @Persisted
    private final NotifiableItemStackHandler vaultStorage;

    public SteamItemVaultMachine(IMachineBlockEntity holder, Object... args) {
        super(holder, false, args);
        this.vaultStorage = new NotifiableItemStackHandler(this, SLOTS, IO.BOTH, IO.BOTH,
                size -> new VaultItemStackHandler(size, SLOT_LIMIT));
    }

    @Override
    public ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }

    /** The large item storage, exposed for the structure's item buses and for tests. */
    public NotifiableItemStackHandler getVaultStorage() {
        return vaultStorage;
    }

    @Override
    public void addDisplayText(List<Component> textList) {
        super.addDisplayText(textList);
        if (!isFormed()) {
            return;
        }
        int distinct = 0;
        long total = 0;
        for (int i = 0; i < vaultStorage.getSlots(); i++) {
            ItemStack stack = vaultStorage.getStackInSlot(i);
            if (!stack.isEmpty()) {
                distinct++;
                total += stack.getCount();
            }
        }
        textList.add(Component.translatable("gtna.multiblock.vault.types", distinct, SLOTS)
                .withStyle(ChatFormatting.AQUA));
        textList.add(Component.translatable("gtna.multiblock.vault.items", total)
                .withStyle(ChatFormatting.GOLD));
    }
}
