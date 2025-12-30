package com.raishxn.gtna.common.machine.multiblock.part.steam;

import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.gui.UITemplate;
import com.gregtechceu.gtceu.api.gui.widget.TankWidget;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableFluidTank;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gregtechceu.gtceu.common.machine.multiblock.part.SteamHatchPartMachine;
import com.lowdragmc.lowdraglib.gui.modular.ModularUI;
import com.lowdragmc.lowdraglib.gui.widget.ImageWidget;
import com.lowdragmc.lowdraglib.gui.widget.LabelWidget;
import com.raishxn.gtna.api.capability.SteamWirelessNetworkManager;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.fluids.capability.IFluidHandler;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.UUID;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class WirelessSteamOutputHatch extends SteamHatchPartMachine {

    private final long transferRate;
    private final boolean isSteel;

    public WirelessSteamOutputHatch(IMachineBlockEntity holder, boolean isSteel, Object... args) {
        super(holder, args);
        this.isSteel = isSteel;
        this.transferRate = isSteel ? 1_000_000L : 1_000L;
        // Desativa o Auto-IO (puxar/empurrar vizinhos), pois usamos lógica Wireless
        this.setWorkingEnabled(false);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (!getLevel().isClientSide) {
            this.subscribeServerTick(this::updateWireless);
        }
    }

    @Override
    protected NotifiableFluidTank createTank(int initialCapacity, int slots, Object... args) {
        // SOBRESCRITA CRÍTICA:
        // SteamHatchPartMachine cria tanque IO.IN por padrão.
        // Forçamos IO.OUT para que a Boiler "enxergue" que pode colocar vapor aqui.
        int capacity = this.isSteel ? Integer.MAX_VALUE : 20000;

        return new NotifiableFluidTank(this, 1, capacity, IO.OUT)
                .setFilter(fluidStack -> fluidStack.getFluid().is(GTMaterials.Steam.getFluidTag()));
    }

    private void updateWireless() {
        if (getLevel() instanceof ServerLevel serverLevel) {
            UUID ownerId = getOwnerUUID();
            if (ownerId == null) return;

            long currentSteam = tank.getFluidInTank(0).getAmount();

            if (currentSteam > 0) {
                int toPush = (int) Math.min(currentSteam, transferRate);

                // Envia para a rede global
                boolean success = SteamWirelessNetworkManager.addSteamToGlobalSteamMap(serverLevel, ownerId, toPush);

                if (success) {
                    tank.drain(toPush, IFluidHandler.FluidAction.EXECUTE);
                }
            }
        }
    }

    @Override
    public ModularUI createUI(Player entityPlayer) {
        return new ModularUI(176, 166, this, entityPlayer)
                .background(GuiTextures.BACKGROUND_STEAM.get(isSteel))
                .widget(new ImageWidget(7, 16, 81, 55, GuiTextures.DISPLAY_STEAM.get(isSteel)))
                .widget(new LabelWidget(11, 20, "gtceu.gui.fluid_amount"))
                .widget(new LabelWidget(11, 30, () -> tank.getFluidInTank(0).getAmount() + "").setTextColor(-1).setDropShadow(true))
                .widget(new LabelWidget(6, 6, getBlockState().getBlock().getDescriptionId()))
                // IO.OUT permite que o Widget mostre que é saída (setas visuais, se houver)
                .widget(new TankWidget(tank.getStorages()[0], 90, 35, true, true)
                        .setBackground(GuiTextures.FLUID_SLOT))
                .widget(UITemplate.bindPlayerInventory(entityPlayer.getInventory(),
                        GuiTextures.SLOT_STEAM.get(isSteel), 7, 84, true));
    }
}