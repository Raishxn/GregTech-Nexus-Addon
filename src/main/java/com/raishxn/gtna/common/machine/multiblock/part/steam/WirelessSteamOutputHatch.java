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

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.fluids.capability.IFluidHandler;

import com.raishxn.gtna.api.capability.SteamWirelessNetworkManager;
import com.raishxn.gtna.config.ConfigHolder;

import java.util.UUID;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class WirelessSteamOutputHatch extends SteamHatchPartMachine {

    private final long transferRate;
    private final boolean isSteel;

    public WirelessSteamOutputHatch(IMachineBlockEntity holder, boolean isSteel, Object... args) {
        super(holder, args);
        this.isSteel = isSteel;
        this.transferRate = isSteel ? ConfigHolder.INSTANCE.wirelessSteam.steelTransferRate :
                ConfigHolder.INSTANCE.wirelessSteam.bronzeTransferRate;
        this.setWorkingEnabled(false);
        if (this.isSteel) {
            if (this.tank.getStorages().length > 0) {
                this.tank.getStorages()[0].setCapacity(ConfigHolder.INSTANCE.wirelessSteam.steelBuffer);
            }
        } else if (this.tank.getStorages().length > 0) {
            this.tank.getStorages()[0].setCapacity(ConfigHolder.INSTANCE.wirelessSteam.bronzeBuffer);
        }
    }

    @Override
    protected NotifiableFluidTank createTank(int initialCapacity, int slots, Object... args) {
        int configuredCapacity = isSteel ? ConfigHolder.INSTANCE.wirelessSteam.steelBuffer :
                ConfigHolder.INSTANCE.wirelessSteam.bronzeBuffer;
        return new NotifiableFluidTank(this, 1, configuredCapacity, IO.OUT)
                .setFilter(fluidStack -> fluidStack.getFluid().is(GTMaterials.Steam.getFluidTag()));
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (getLevel() != null && !getLevel().isClientSide) {
            this.subscribeServerTick(this::updateWireless);
        }
    }

    private void updateWireless() {
        if (!ConfigHolder.INSTANCE.wirelessSteam.enabled) {
            return;
        }
        if (getLevel() instanceof ServerLevel serverLevel) {
            UUID ownerId = getOwnerUUID();
            if (ownerId == null) return;

            long currentSteam = tank.getFluidInTank(0).getAmount();

            if (currentSteam > 0) {
                int toPush = (int) Math.min(currentSteam, transferRate);

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
                .widget(new LabelWidget(11, 30, () -> tank.getFluidInTank(0).getAmount() + "").setTextColor(-1)
                        .setDropShadow(true))
                .widget(new LabelWidget(6, 6, getBlockState().getBlock().getDescriptionId()))
                .widget(new TankWidget(tank.getStorages()[0], 90, 35, true, true)
                        .setBackground(GuiTextures.FLUID_SLOT))
                .widget(UITemplate.bindPlayerInventory(entityPlayer.getInventory(),
                        GuiTextures.SLOT_STEAM.get(isSteel), 7, 84, true));
    }
}
