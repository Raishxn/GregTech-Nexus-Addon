package com.raishxn.gtna.common.item;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;

import com.raishxn.gtna.common.data.NexusEnergyNetwork;
import com.raishxn.gtna.utils.datastructure.Int128;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.UUID;

/** Shared exact network balance and explicit unlimited capacity presentation. */
public final class NexusNetworkDisplay {

    private NexusNetworkDisplay() {}

    public static String formatEnergy(BigInteger energy) {
        if (energy.compareTo(Int128.MAX_VALUE.toBigInteger()) <= 0)
            return Int128.fromBigInteger(energy).toHumanReadableString();
        return new BigDecimal(energy).round(new MathContext(4)).toEngineeringString();
    }

    public static Component machineName(String name) {
        return name.startsWith("block.") ? Component.translatable(name) : Component.literal(name);
    }

    public static Component capacity(boolean unlimited, Int128 capacity) {
        return Component.translatable("gtna.nexus.capacity", unlimited ? "∞" : capacity.toHumanReadableString());
    }

    public static Component transfer(boolean unlimited, Int128 transfer) {
        return Component.translatable("gtna.nexus.transfer", unlimited ? "∞" : transfer.toHumanReadableString());
    }

    public static Component energy(NexusEnergyNetwork network, UUID owner) {
        var amount = network.getExactEnergy(owner);
        return Component.translatable("gtna.nexus.energy", formatEnergy(amount),
                network.isUnlimited(owner) ? "∞" : network.getMaxCapacity(owner).toHumanReadableString())
                .withStyle(style -> style.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                        Component.literal(NumberFormat.getIntegerInstance(Locale.US).format(amount) + " EU"))));
    }
}
