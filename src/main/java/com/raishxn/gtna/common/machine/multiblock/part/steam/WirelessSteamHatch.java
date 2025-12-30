package com.raishxn.gtna.common.machine.multiblock.part.steam;

import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableFluidTank;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gregtechceu.gtceu.common.machine.multiblock.part.FluidHatchPartMachine;
import com.lowdragmc.lowdraglib.side.fluid.FluidHelper;
import com.raishxn.gtna.api.data.SteamNetworkData;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.fluids.FluidStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.UUID;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class WirelessSteamHatch extends FluidHatchPartMachine {

    @Nullable
    private UUID ownerUUID;
    private final int throughputLimit;

    public WirelessSteamHatch(IMachineBlockEntity holder, int tier, IO io) {
        // Inicializa como um Hatch de Fluido padrão.
        // Capacidade visual é definida aqui, mas a lógica real vem do createTank.
        super(holder, tier, io, tier == 0 ? 20000 : Integer.MAX_VALUE, 1);
        this.throughputLimit = tier == 0 ? 20000 : Integer.MAX_VALUE;
    }
    @Override
    protected NotifiableFluidTank createTank(int initialCapacity, int slots, Object... args) {
        // CORREÇÃO: O quarto argumento deve ser 'this.io' (Enum), e não um boolean.
        return new NotifiableFluidTank(this, slots, initialCapacity, this.io) {

            @Override
            public int fill(FluidStack resource, FluidAction action) {
                // Se não tiver dono ou não for vapor, rejeita
                if (ownerUUID == null || !resource.getFluid().isSame(GTMaterials.Steam.getFluid())) {
                    return 0;
                }

                // Pega os dados da rede
                SteamNetworkData data = SteamNetworkData.get(getLevel());
                if (data == null) return 0;

                // Calcula quanto pode aceitar baseado no limite de Tier
                // Usa throughputLimit que definimos na classe
                int toFill = Math.min(resource.getAmount(), throughputLimit);

                if (action.execute()) {
                    data.addSteam(ownerUUID, toFill);
                }
                return toFill;
            }

            @NotNull
            @Override
            public FluidStack drain(int maxDrain, FluidAction action) {
                if (ownerUUID == null) return FluidStack.EMPTY;

                SteamNetworkData data = SteamNetworkData.get(getLevel());
                if (data == null) return FluidStack.EMPTY;

                long currentSteam = data.getSteam(ownerUUID);

                // Limita pelo Tier e pelo que tem na rede
                long limit = Math.min(maxDrain, throughputLimit);
                int toDrain = (int) Math.min(limit, currentSteam);

                if (toDrain <= 0) return FluidStack.EMPTY;

                if (action.execute()) {
                    data.consumeSteam(ownerUUID, toDrain);
                }

                return GTMaterials.Steam.getFluid(toDrain);
            }

            // Necessário para satisfazer a interface, mas a lógica real está acima
            @NotNull
            @Override
            public FluidStack drain(FluidStack resource, FluidAction action) {
                if (resource.isEmpty() || !resource.getFluid().isSame(GTMaterials.Steam.getFluid())) {
                    return FluidStack.EMPTY;
                }
                return drain(resource.getAmount(), action);
            }

            @Override
            public boolean isFluidValid(int tank, @NotNull FluidStack stack) {
                return stack.getFluid().isSame(GTMaterials.Steam.getFluid());
            }
        };
    }

    @Override
    protected InteractionResult onScrewdriverClick(Player playerIn, InteractionHand hand, Direction gridSide, BlockHitResult hitResult) {
        if (!isRemote()) {
            if (playerIn.isShiftKeyDown()) {
                if (this.ownerUUID == null) {
                    this.ownerUUID = playerIn.getUUID();
                    // Não precisamos chamar updateHandler() pois o createTank já verifica o ownerUUID dinamicamente

                    String limitText = (this.throughputLimit == Integer.MAX_VALUE) ? "Infinito" : this.throughputLimit + " mB/t";
                    playerIn.sendSystemMessage(Component.literal("§aRede de Vapor vinculada a: " + playerIn.getName().getString()));
                    playerIn.sendSystemMessage(Component.literal("§7Capacidade: " + limitText));

                    // Atualiza o estado do bloco para salvar
                    this.getHolder().self().setChanged();
                    return InteractionResult.SUCCESS;
                } else {
                    playerIn.sendSystemMessage(Component.literal("§eHatch já vinculado! Quebre para resetar."));
                    return InteractionResult.CONSUME;
                }
            }
        }
        return super.onScrewdriverClick(playerIn, hand, gridSide, hitResult);
    }

    @Override
    public void saveCustomPersistedData(CompoundTag tag, boolean forDrop) {
        super.saveCustomPersistedData(tag, forDrop);
        if (this.ownerUUID != null) {
            tag.putUUID("OwnerUUID", this.ownerUUID);
        }
    }

    @Override
    public void loadCustomPersistedData(CompoundTag tag) {
        super.loadCustomPersistedData(tag);
        if (tag.hasUUID("OwnerUUID")) {
            this.ownerUUID = tag.getUUID("OwnerUUID");
        }
    }
}