package com.raishxn.gtna.api.machine.multiblock;

import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiController;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

import java.util.List;

/**
 * Appends the "Formed modules: n / total" line to a multiblock's UI. Used by GTNA's multiblock
 * display mixin and can be called from any machine's {@code addDisplayText}.
 */
public final class GTNAModuleDisplay {

    private GTNAModuleDisplay() {}

    public static void append(List<Component> textList, IMultiController controller) {
        if (!(controller instanceof IGTNAModuleHost host)) {
            return;
        }
        MultiblockMachineDefinition definition = controller.self().getDefinition();
        int total = GTNASubPatterns.get(definition).size();
        int formed = host.gtna$formedModuleCount();
        if (total <= 0 && formed <= 0) {
            return;
        }
        textList.add(Component.translatable("gtna.machine.modules_amount", formed, total)
                .withStyle(ChatFormatting.AQUA));
    }
}
