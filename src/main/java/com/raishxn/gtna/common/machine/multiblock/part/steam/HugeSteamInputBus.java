package com.raishxn.gtna.common.machine.multiblock.part.steam;

import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.gui.UITemplate;
import com.gregtechceu.gtceu.api.gui.widget.SlotWidget;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.multiblock.part.MultiblockPartMachine;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableItemStackHandler;
import com.gregtechceu.gtceu.config.ConfigHolder; // Necessário para pegar a config de steel/bronze
import com.lowdragmc.lowdraglib.gui.modular.ModularUI;
import com.lowdragmc.lowdraglib.gui.widget.DraggableScrollableWidgetGroup;
import com.lowdragmc.lowdraglib.gui.widget.LabelWidget;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.world.entity.player.Player;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class HugeSteamInputBus extends MultiblockPartMachine {

    protected static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(
            HugeSteamInputBus.class, MultiblockPartMachine.MANAGED_FIELD_HOLDER);

    @Persisted
    public final NotifiableItemStackHandler inventory;

    public HugeSteamInputBus(IMachineBlockEntity holder, Object... args) {
        super(holder);
        this.inventory = new NotifiableItemStackHandler(this, 64, IO.IN, IO.IN);
    }

    @Override
    public ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }

    @Override
    public ModularUI createUI(Player entityPlayer) {
        boolean isSteel = ConfigHolder.INSTANCE.machines.steelSteamMultiblocks;

        DraggableScrollableWidgetGroup list = new DraggableScrollableWidgetGroup(7, 20, 162, 108);
        list.setBackground(GuiTextures.DISPLAY_STEAM.get(isSteel));

        for (int i = 0; i < 64; i++) {
            int col = i % 9;
            int row = i / 9;
            list.addWidget(new SlotWidget(inventory, i, 1 + col * 18, 1 + row * 18)
                    .setBackgroundTexture(GuiTextures.SLOT_STEAM.get(isSteel)));
        }

        return new ModularUI(176, 220, this, entityPlayer)
                .background(GuiTextures.BACKGROUND_STEAM.get(isSteel))
                .widget(new LabelWidget(7, 7, getBlockState().getBlock().getDescriptionId()))
                .widget(list)
                .widget(UITemplate.bindPlayerInventory(entityPlayer.getInventory(),
                        GuiTextures.SLOT_STEAM.get(isSteel), 7, 138, true));
    }
}