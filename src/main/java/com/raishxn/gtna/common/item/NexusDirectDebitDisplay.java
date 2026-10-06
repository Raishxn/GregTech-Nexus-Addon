package com.raishxn.gtna.common.item;

import com.gregtechceu.gtceu.utils.FormattingUtil;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;

import com.raishxn.gtna.common.data.NexusEnergyNetwork;

import java.util.List;
import java.util.UUID;

/** Shared terminal/matrix view of direct transactions, separate from instantaneous hatch EU/t. */
public final class NexusDirectDebitDisplay {

    private NexusDirectDebitDisplay() {}

    public static void append(List<Component> lines, NexusEnergyNetwork network, UUID owner) {
        var debit = network.getLastDirectDebit(owner);
        if (debit == null) return;
        lines.add(Component.translatable("gtna.nexus.direct_debit", Component.translatable(debit.machineType()),
                debit.amount().toHumanReadableString()).withStyle(
                        style -> style
                                .withColor(net.minecraft.ChatFormatting.RED)
                                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                                        Component.translatable("gtna.nexus.direct_debit_details",
                                                FormattingUtil.formatNumbers(debit.amount().toBigInteger()),
                                                debit.source().pos().toShortString(),
                                                debit.source().dimension().location(),
                                                debit.tick())))));
    }
}
