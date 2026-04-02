package com.raishxn.gtna.common.item.terminal;

import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiController;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import com.raishxn.gtna.common.item.terminal.ui.NexusTerminalUIFactory;

/**
 * Entry-point for executing auto-build with Nexus Terminal settings.
 * Delegates to {@link NexusBlockPattern} which handles the actual
 * block placement, AE2 integration, and replace/demolition logic.
 */
public class NexusAutoBuilder {

    /**
     * Execute auto-build using Nexus Terminal settings stored in the terminal ItemStack.
     *
     * @param player        The player triggering the build
     * @param controller    The multiblock controller to build around
     * @param terminalStack The Nexus Structure Terminal ItemStack containing NBT settings
     */
    public static void autoBuild(Player player, IMultiController controller, ItemStack terminalStack) {
        NexusTerminalUIFactory.AutoBuildSetting setting = NexusTerminalUIFactory.AutoBuildSetting
                .getSetting(terminalStack);

        NexusBlockPattern pattern = NexusBlockPattern.fromBlockPattern(controller.getPattern());
        if (pattern != null) {
            pattern.autoBuild(player, controller.getMultiblockState(), setting, terminalStack);
        }
    }
}
