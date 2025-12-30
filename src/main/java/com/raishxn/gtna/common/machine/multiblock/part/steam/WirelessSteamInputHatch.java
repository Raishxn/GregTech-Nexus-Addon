package com.raishxn.gtna.common.machine.multiblock.part.steam;

import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.gui.UITemplate;
import com.gregtechceu.gtceu.api.gui.widget.TankWidget;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableFluidTank;
// Import necessário para o CustomFluidTank, caso o compilador exija
import com.gregtechceu.gtceu.api.transfer.fluid.CustomFluidTank;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gregtechceu.gtceu.common.machine.multiblock.part.SteamHatchPartMachine;
import com.lowdragmc.lowdraglib.gui.modular.ModularUI;
import com.lowdragmc.lowdraglib.gui.widget.ImageWidget;
import com.lowdragmc.lowdraglib.gui.widget.LabelWidget;
import com.raishxn.gtna.api.capability.SteamWirelessNetworkManager;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.UUID;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class WirelessSteamInputHatch extends SteamHatchPartMachine {

    private final long transferRate;
    private final boolean isSteel;

    public WirelessSteamInputHatch(IMachineBlockEntity holder, boolean isSteel, Object... args) {
        super(holder, args);
        this.isSteel = isSteel;
        this.transferRate = isSteel ? 1_000_000L : 1_000L;
        this.setWorkingEnabled(false);

        // CORREÇÃO: Acessa o tanque interno (CustomFluidTank) para definir a capacidade
        if (this.isSteel) {
            // NotifiableFluidTank armazena CustomFluidTanks em um array 'storages'
            // Precisamos pegar o primeiro (e único) tanque e setar a capacidade nele
            if (this.tank.getStorages().length > 0) {
                this.tank.getStorages()[0].setCapacity(Integer.MAX_VALUE);
            }
        }
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
        // Cria com capacidade inicial padrão (será corrigido no construtor via getStorages)
        return new NotifiableFluidTank(this, 1, initialCapacity, IO.IN)
                .setFilter(fluidStack -> fluidStack.getFluid().is(GTMaterials.Steam.getFluidTag()));
    }

    private void updateWireless() {
        if (getLevel() instanceof ServerLevel serverLevel) {
            UUID ownerId = getOwnerUUID();
            if (ownerId == null) return;

            long currentSteam = tank.getFluidInTank(0).getAmount();
            // Pega a capacidade do tanque específico
            long capacity = tank.getTankCapacity(0);
            long spaceNeeded = capacity - currentSteam;

            if (spaceNeeded > 0) {
                int toPull = (int) Math.min(spaceNeeded, transferRate);

                if (SteamWirelessNetworkManager.consumeSteamFromGlobalMap(serverLevel, ownerId, toPull)) {
                    FluidStack steamStack = GTMaterials.Steam.getFluid(toPull);
                    tank.fill(steamStack, IFluidHandler.FluidAction.EXECUTE);
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
                .widget(new TankWidget(tank.getStorages()[0], 90, 35, true, true)
                        .setBackground(GuiTextures.FLUID_SLOT))
                .widget(UITemplate.bindPlayerInventory(entityPlayer.getInventory(),
                        GuiTextures.SLOT_STEAM.get(isSteel), 7, 84, true));
    }
}