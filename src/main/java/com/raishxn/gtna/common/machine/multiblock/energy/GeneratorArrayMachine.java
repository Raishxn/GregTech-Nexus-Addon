package com.raishxn.gtna.common.machine.multiblock.energy;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.recipe.FluidRecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.capability.recipe.ItemRecipeCapability;
import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.gui.fancy.FancyMachineUIWidget;
import com.gregtechceu.gtceu.api.gui.widget.SlotWidget;
import com.gregtechceu.gtceu.api.item.MetaMachineItem;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableItemStackHandler;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.api.recipe.content.ContentModifier;
import com.gregtechceu.gtceu.api.recipe.modifier.ModifierFunction;
import com.gregtechceu.gtceu.api.recipe.modifier.ParallelLogic;
import com.gregtechceu.gtceu.api.transfer.item.CustomItemStackHandler;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;

import com.lowdragmc.lowdraglib.gui.modular.ModularUI;
import com.lowdragmc.lowdraglib.gui.util.ClickData;
import com.lowdragmc.lowdraglib.gui.widget.ComponentPanelWidget;
import com.lowdragmc.lowdraglib.gui.widget.DraggableScrollableWidgetGroup;
import com.lowdragmc.lowdraglib.gui.widget.LabelWidget;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;

import net.minecraft.core.GlobalPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;

import com.raishxn.gtna.api.capability.WirelessEnergyManager;
import com.raishxn.gtna.common.data.NexusEnergyNetwork;
import com.raishxn.gtna.utils.datastructure.Int128;

import java.util.List;
import java.util.UUID;

/**
 * GTOCore Generator Array adapted to GTCEu 7.5.3. Generator items in the controller slot (or the
 * input bus for existing worlds) select the fuel
 * map and provide up to four parallel generators. Fuel is consumed by the normal GT recipe runner;
 * produced EU first enters the output hatch. Wireless mode moves that EU into the owner's Nexus
 * Flux Matrix network without ever creating energy when the network cannot accept it.
 */
public class GeneratorArrayMachine extends WorkableElectricMultiblockMachine {

    protected static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(
            GeneratorArrayMachine.class, WorkableElectricMultiblockMachine.MANAGED_FIELD_HOLDER);

    private static final int MAX_GENERATORS = 4;
    private static final double OUTPUT_MULTIPLIER = 1.3;
    private static final int WIRELESS_LOSS_PERCENT = 5;

    @Persisted
    private boolean wirelessMode;
    @Persisted
    private final NotifiableItemStackHandler generatorStorage;

    public GeneratorArrayMachine(IMachineBlockEntity holder, Object... args) {
        super(holder, args);
        generatorStorage = new NotifiableItemStackHandler(this, 1, IO.IN, IO.IN,
                slots -> new CustomItemStackHandler(slots) {

                    @Override
                    public int getSlotLimit(int slot) {
                        return MAX_GENERATORS;
                    }
                });
        generatorStorage.setFilter(stack -> generatorType(stack) != null);
    }

    @Override
    public ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }

    public NotifiableItemStackHandler getGeneratorStorage() {
        return generatorStorage;
    }

    @Override
    public ModularUI createUI(Player player) {
        return new ModularUI(198, 225, this, player)
                .widget(new FancyMachineUIWidget(this, 198, 225));
    }

    @Override
    public Widget createUIWidget() {
        WidgetGroup group = new WidgetGroup(0, 0, 190, 142);
        var screen = new DraggableScrollableWidgetGroup(4, 4, 182, 96)
                .setBackground(GuiTextures.DISPLAY);
        screen.addWidget(new LabelWidget(4, 5, self().getBlockState().getBlock().getDescriptionId()));
        screen.addWidget(new ComponentPanelWidget(4, 17, this::addDisplayText)
                .textSupplier(this.getLevel().isClientSide ? null : this::addDisplayText)
                .setMaxWidthLimit(170)
                .clickHandler(this::handleDisplayClick));
        group.addWidget(screen);

        WidgetGroup slotPanel = new WidgetGroup(4, 104, 182, 34);
        slotPanel.setBackground(GuiTextures.BACKGROUND_INVERSE);
        slotPanel.addWidget(new SlotWidget(generatorStorage.storage, 0, 8, 8, true, true)
                .setBackground(GuiTextures.SLOT));
        slotPanel.addWidget(new LabelWidget(32, 13, "gtna.machine.generator_array.generator_slot"));
        group.addWidget(slotPanel);
        group.setBackground(GuiTextures.BACKGROUND_INVERSE);
        return group;
    }

    public static ModifierFunction recipeModifier(MetaMachine machine, GTRecipe recipe) {
        if (!(machine instanceof GeneratorArrayMachine array) || !array.isFormed()) {
            return ModifierFunction.NULL;
        }
        GeneratorSelection selection = array.findGenerator();
        if (selection == null || selection.recipeType != recipe.recipeType || recipe.getOutputEUt().isEmpty()) {
            return ModifierFunction.NULL;
        }
        long recipeEUt = recipe.getOutputEUt().getTotalEU();
        long generatorBudget = (long) (GTValues.V[selection.tier] * selection.count * OUTPUT_MULTIPLIER);
        long hatchBudget = array.getEnergyContainer().getOutputVoltage() *
                array.getEnergyContainer().getOutputAmperage();
        long maximumEUt = Math.min(generatorBudget, hatchBudget);
        if (recipeEUt <= 0 || maximumEUt < recipeEUt) {
            return ModifierFunction.NULL;
        }
        int desiredParallel = (int) Math.min(Integer.MAX_VALUE, maximumEUt / recipeEUt);
        int parallel = ParallelLogic.getParallelAmountFast(array, recipe, desiredParallel);
        if (parallel <= 0) {
            return ModifierFunction.NULL;
        }
        return ModifierFunction.builder()
                .inputModifier(ContentModifier.multiplier(parallel))
                .outputModifier(ContentModifier.multiplier(parallel))
                .eutMultiplier(parallel)
                .parallels(parallel)
                .build();
    }

    private GeneratorSelection findGenerator() {
        ItemStack installed = generatorStorage.getStackInSlot(0);
        GTRecipeType installedType = generatorType(installed);
        if (installedType != null) {
            MachineDefinition definition = ((MetaMachineItem) installed.getItem()).getDefinition();
            return new GeneratorSelection(installedType, definition.getTier(),
                    Math.min(MAX_GENERATORS, installed.getCount()));
        }
        var inputs = getCapabilitiesFlat().get(IO.IN);
        if (inputs == null) return null;
        var handlers = inputs.get(ItemRecipeCapability.CAP);
        if (handlers == null) return null;
        GeneratorSelection chosen = null;
        for (Object candidate : handlers) {
            if (!(candidate instanceof IItemHandler handler)) continue;
            for (int slot = 0; slot < handler.getSlots(); slot++) {
                ItemStack stack = handler.getStackInSlot(slot);
                if (stack.isEmpty() || !(stack.getItem() instanceof MetaMachineItem machineItem)) continue;
                MachineDefinition definition = machineItem.getDefinition();
                GTRecipeType recipeType = generatorType(stack);
                if (recipeType == null) continue;
                if (chosen == null) {
                    chosen = new GeneratorSelection(recipeType, definition.getTier(), 0);
                } else if (chosen.recipeType != recipeType || chosen.tier != definition.getTier()) {
                    continue;
                }
                chosen = new GeneratorSelection(recipeType, chosen.tier,
                        Math.min(MAX_GENERATORS, chosen.count + stack.getCount()));
            }
        }
        return chosen;
    }

    private static GTRecipeType generatorType(ItemStack stack) {
        if (stack.isEmpty() || !(stack.getItem() instanceof MetaMachineItem machineItem)) return null;
        MachineDefinition definition = machineItem.getDefinition();
        if (definition == null || definition.getRecipeTypes() == null) return null;
        for (GTRecipeType type : definition.getRecipeTypes()) {
            if (type == GTRecipeTypes.STEAM_TURBINE_FUELS || type == GTRecipeTypes.GAS_TURBINE_FUELS ||
                    type == GTRecipeTypes.COMBUSTION_GENERATOR_FUELS)
                return type;
        }
        return null;
    }

    public int getInstalledGeneratorCount() {
        GeneratorSelection selection = isFormed() ? findGenerator() : null;
        return selection == null ? 0 : selection.count;
    }

    @Override
    public GTRecipe fullModifyRecipe(GTRecipe recipe) {
        // GTOCore discards fuel byproducts before parallelizing. GTCEu's default output trimming
        // would also remove EU when the dynamo has no recipe output slot, cancelling all fuel.
        GTRecipe fuel = recipe.copy();
        fuel.outputs.remove(ItemRecipeCapability.CAP);
        fuel.outputs.remove(FluidRecipeCapability.CAP);
        return doModifyRecipe(fuel);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (getLevel() instanceof ServerLevel) {
            subscribeServerTick(() -> {
                if (getOffsetTimer() % 5 == 0) refreshGeneratorMode();
                transferWirelessEnergy();
            });
        }
    }

    public void refreshGeneratorMode() {
        if (!isFormed() || getRecipeLogic().isActive()) return;
        GeneratorSelection selection = findGenerator();
        if (selection == null) return;
        GTRecipeType[] types = getRecipeTypes();
        for (int index = 0; index < types.length; index++) {
            if (types[index] == selection.recipeType && getActiveRecipeType() != index) {
                setActiveRecipeType(index);
                getRecipeLogic().updateTickSubscription();
                return;
            }
        }
    }

    private void transferWirelessEnergy() {
        if (!wirelessMode || !isFormed() || !(getLevel() instanceof ServerLevel level)) return;
        UUID owner = getOwnerUUID();
        if (owner == null) return;
        NexusEnergyNetwork network = NexusEnergyNetwork.get(level);
        if (!network.isMatrixFormed(owner)) return;
        var output = getEnergyContainer();
        long stored = output.getEnergyStored();
        if (stored <= 0) return;
        long maxTransfer = Math.max(0, output.getOutputVoltage() * output.getOutputAmperage());
        long offered = Math.min(stored, maxTransfer);
        Int128 matrixLimit = network.getTransferLimit(owner);
        if (matrixLimit.isZero() || matrixLimit.isNegative()) return;
        if (matrixLimit.compareTo(new Int128(offered)) < 0) {
            offered = matrixLimit.toLong();
        }
        if (offered <= 0) return;
        long removed = -output.changeEnergy(-offered);
        if (removed <= 0) return;
        long afterLoss = removed - removed * WIRELESS_LOSS_PERCENT / 100;
        if (afterLoss <= 0) {
            output.changeEnergy(removed);
            return;
        }
        Int128 accepted = WirelessEnergyManager.addEnergy(level, owner, new Int128(afterLoss));
        if (accepted.isZero()) {
            output.changeEnergy(removed);
            return;
        }
        long acceptedEU = accepted.toLong();
        long charged = Math.min(removed, (acceptedEU * 100 + 99 - WIRELESS_LOSS_PERCENT) /
                (100 - WIRELESS_LOSS_PERCENT));
        output.changeEnergy(removed - charged);
        WirelessEnergyManager.reportConnection(level, owner, GlobalPos.of(level.dimension(), getPos()), true,
                getTier(), 1, "Generator Array", accepted);
    }

    @Override
    public void addDisplayText(List<Component> textList) {
        super.addDisplayText(textList);
        textList.add(Component.translatable("gtna.machine.generator_array.generators",
                getInstalledGeneratorCount()));
        textList.add(Component.translatable("gtna.machine.generator_array.wireless")
                .append(ComponentPanelWidget.withButton(
                        Component.translatable(wirelessMode ? "gtna.machine.generator_array.on" :
                                "gtna.machine.generator_array.off"),
                        "wireless_switch")));
    }

    @Override
    public void handleDisplayClick(String componentData, ClickData clickData) {
        if (!clickData.isRemote && "wireless_switch".equals(componentData)) {
            wirelessMode = !wirelessMode;
            markDirty();
        }
    }

    private record GeneratorSelection(GTRecipeType recipeType, int tier, int count) {}
}
