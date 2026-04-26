package com.raishxn.gtna.common.machine.multiblock.part.ae;

import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.integration.ae2.machine.MEBusPartMachine;

import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;

import appeng.api.config.Actionable;
import appeng.api.networking.security.IActionSource;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.KeyCounter;
import appeng.api.storage.IStorageMounts;
import appeng.api.storage.IStorageProvider;
import appeng.api.storage.MEStorage;
import it.unimi.dsi.fastutil.objects.Object2LongMap;
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;

public class GTNAMEStorageAccessPartMachine extends MEBusPartMachine implements MEStorage, IStorageProvider {

    protected static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(
            GTNAMEStorageAccessPartMachine.class, MEBusPartMachine.MANAGED_FIELD_HOLDER);

    private static final String STORAGE_TAG = "MEStorageContents";
    private static final String KEY_TAG = "key";
    private static final String AMOUNT_TAG = "amount";

    public enum Mode {
        STORAGE("gtna.machine.me_storage_access_hatch.mode"),
        BIG_STORAGE("gtna.machine.me_big_storage_access_hatch.mode"),
        IO_PORT("gtna.machine.me_io_port_hatch.mode");

        private final String translationKey;

        Mode(String translationKey) {
            this.translationKey = translationKey;
        }

        public Component label() {
            return Component.translatable(translationKey);
        }
    }

    private final Mode mode;
    private final Object2LongOpenHashMap<AEKey> storedStacks = new Object2LongOpenHashMap<>();
    private long capacityBytes;
    private boolean infinite;
    private boolean storageMounted;

    public GTNAMEStorageAccessPartMachine(IMachineBlockEntity holder, Mode mode, Object... args) {
        super(holder, IO.IN, args);
        this.mode = mode;
        getMainNode().addService(IStorageProvider.class, this);
    }

    @Override
    public ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }

    public Mode getMode() {
        return mode;
    }

    public void configureStorage(long capacityBytes, boolean infinite) {
        this.capacityBytes = Math.max(0L, capacityBytes);
        this.infinite = infinite;
        this.storageMounted = this.capacityBytes > 0L || infinite;
        requestStorageRefresh();
    }

    public void clearStorageMount() {
        this.capacityBytes = 0L;
        this.infinite = false;
        this.storageMounted = false;
        requestStorageRefresh();
    }

    public long getCapacityBytes() {
        return capacityBytes;
    }

    public long getUsedBytes() {
        long used = 0L;
        for (Object2LongMap.Entry<AEKey> entry : storedStacks.object2LongEntrySet()) {
            used = saturatedAdd(used, bytesFor(entry.getKey(), entry.getLongValue()));
        }
        return used;
    }

    public int getStoredTypes() {
        return storedStacks.size();
    }

    public boolean isInfinite() {
        return infinite;
    }

    public boolean isStorageMounted() {
        return storageMounted;
    }

    @Override
    public void mountInventories(IStorageMounts storageMounts) {
        if (storageMounted) {
            storageMounts.mount(this, mode == Mode.BIG_STORAGE ? 10 : 0);
        }
    }

    @Override
    public boolean isPreferredStorageFor(AEKey what, IActionSource source) {
        return storageMounted && (infinite || storedStacks.containsKey(what) || getUsedBytes() < capacityBytes);
    }

    @Override
    public long insert(AEKey what, long amount, Actionable mode, IActionSource source) {
        MEStorage.checkPreconditions(what, amount, mode, source);
        if (!storageMounted || amount == 0L) {
            return 0L;
        }
        long accepted = infinite ? amount : Math.min(amount, remainingAmountFor(what));
        if (accepted <= 0L) {
            return 0L;
        }
        if (mode == Actionable.MODULATE) {
            storedStacks.addTo(what, accepted);
            onStoredContentsChanged();
        }
        return accepted;
    }

    @Override
    public long extract(AEKey what, long amount, Actionable mode, IActionSource source) {
        MEStorage.checkPreconditions(what, amount, mode, source);
        if (!storageMounted || amount == 0L) {
            return 0L;
        }
        long stored = storedStacks.getLong(what);
        long extracted = Math.min(stored, amount);
        if (extracted <= 0L) {
            return 0L;
        }
        if (mode == Actionable.MODULATE) {
            long remaining = stored - extracted;
            if (remaining <= 0L) {
                storedStacks.removeLong(what);
            } else {
                storedStacks.put(what, remaining);
            }
            onStoredContentsChanged();
        }
        return extracted;
    }

    @Override
    public void getAvailableStacks(KeyCounter out) {
        for (Object2LongMap.Entry<AEKey> entry : storedStacks.object2LongEntrySet()) {
            if (entry.getLongValue() > 0L) {
                out.add(entry.getKey(), entry.getLongValue());
            }
        }
    }

    @Override
    public Component getDescription() {
        return Component.translatable(getBlockState().getBlock().getDescriptionId());
    }

    @Override
    public void saveCustomPersistedData(CompoundTag tag, boolean forDrop) {
        super.saveCustomPersistedData(tag, forDrop);
        ListTag list = new ListTag();
        for (Object2LongMap.Entry<AEKey> entry : storedStacks.object2LongEntrySet()) {
            if (entry.getLongValue() <= 0L) {
                continue;
            }
            CompoundTag stackTag = new CompoundTag();
            stackTag.put(KEY_TAG, entry.getKey().toTagGeneric());
            stackTag.putLong(AMOUNT_TAG, entry.getLongValue());
            list.add(stackTag);
        }
        tag.put(STORAGE_TAG, list);
    }

    @Override
    public void loadCustomPersistedData(CompoundTag tag) {
        super.loadCustomPersistedData(tag);
        storedStacks.clear();
        ListTag list = tag.getList(STORAGE_TAG, Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            CompoundTag stackTag = list.getCompound(i);
            AEKey key = AEKey.fromTagGeneric(stackTag.getCompound(KEY_TAG));
            long amount = stackTag.getLong(AMOUNT_TAG);
            if (key != null && amount > 0L) {
                storedStacks.put(key, amount);
            }
        }
    }

    private long remainingAmountFor(AEKey key) {
        long remainingBytes = capacityBytes - getUsedBytes();
        if (remainingBytes <= 0L) {
            return 0L;
        }
        int amountPerByte = Math.max(1, key.getAmountPerByte());
        if (remainingBytes > Long.MAX_VALUE / amountPerByte) {
            return Long.MAX_VALUE;
        }
        return remainingBytes * amountPerByte;
    }

    private static long bytesFor(AEKey key, long amount) {
        int amountPerByte = Math.max(1, key.getAmountPerByte());
        long bytes = amount / amountPerByte;
        if (amount % amountPerByte != 0L) {
            bytes++;
        }
        return bytes;
    }

    private void onStoredContentsChanged() {
        markDirty();
        requestStorageRefresh();
    }

    private void requestStorageRefresh() {
        if (!isRemote()) {
            IStorageProvider.requestUpdate(getMainNode());
        }
    }

    private static long saturatedAdd(long a, long b) {
        long result = a + b;
        return result < 0L || result < a ? Long.MAX_VALUE : result;
    }
}
