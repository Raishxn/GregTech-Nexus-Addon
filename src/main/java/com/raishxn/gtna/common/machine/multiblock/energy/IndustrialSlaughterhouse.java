package com.raishxn.gtna.common.machine.multiblock.energy;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.capability.recipe.ItemRecipeCapability;
import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.gui.fancy.FancyMachineUIWidget;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.feature.IFancyUIMachine;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IDisplayUIMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.MultiblockDisplayText;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.data.recipe.builder.GTRecipeBuilder;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.api.recipe.OverclockingLogic;
import com.gregtechceu.gtceu.api.recipe.RecipeHelper;
import com.gregtechceu.gtceu.api.recipe.modifier.ModifierFunction;
import com.gregtechceu.gtceu.common.item.IntCircuitBehaviour;
import com.gregtechceu.gtceu.utils.GTUtil;
import com.lowdragmc.lowdraglib.gui.modular.ModularUI;
import com.lowdragmc.lowdraglib.gui.widget.*;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;
import com.raishxn.gtna.common.data.GTNARecipeType;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraftforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@ParametersAreNonnullByDefault
public class IndustrialSlaughterhouse extends WorkableElectricMultiblockMachine implements IDisplayUIMachine, IFancyUIMachine {

    protected static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(
            IndustrialSlaughterhouse.class, WorkableElectricMultiblockMachine.MANAGED_FIELD_HOLDER);

    @Persisted
    private final List<ItemStack> lastDrops = new ArrayList<>();

    private static final String[] MOB_LIST_PASSIVE = new String[] { "chicken", "cow", "pig", "sheep", "rabbit", "horse", "goat" };
    private static final String[] MOB_LIST_HOSTILE = new String[] { "zombie", "skeleton", "creeper", "spider", "enderman", "witch", "blaze" };
    private static final String[] BOSS_MOBS = new String[] { "warden", "wither", "elder_guardian" };

    public IndustrialSlaughterhouse(IMachineBlockEntity holder, Object... args) {
        super(holder, args);
    }

    @Override
    public @NotNull ManagedFieldHolder getFieldHolder() { return MANAGED_FIELD_HOLDER; }

    @Override
    public @NotNull GTRecipeType getRecipeType() { return GTNARecipeType.SLAUGHTERHOUSE_RECIPES; }

    @Override
    public void afterWorking() {
        super.afterWorking();
        if (getLevel() == null || getLevel().isClientSide) return;

        ServerLevel serverLevel = (ServerLevel) getLevel();
        int circuit = getCircuitFromInputBus();
        long voltage = getEnergyContainer().getInputVoltage();
        int tier = GTUtil.getTierByVoltage(voltage);

        String[] currentMobList = null;
        int baseTier;
        int multiplierBase;

        switch (circuit) {
            case 2 -> { currentMobList = MOB_LIST_HOSTILE; baseTier = GTValues.MV; multiplierBase = 2; }
            case 3 -> { currentMobList = BOSS_MOBS; baseTier = GTValues.ZPM; multiplierBase = 3; }
            case 4 -> { baseTier = GTValues.UHV; multiplierBase = 5; }
            default -> { currentMobList = MOB_LIST_PASSIVE; baseTier = GTValues.LV; multiplierBase = 2; }
        }

        if (tier < baseTier) return;

        int multiplier = (int) Math.pow(multiplierBase, (tier - baseTier));
        int loops = Math.max(1, (tier - 2) * 8);

        List<ItemStack> allGeneratedDrops = new ArrayList<>();

        for (int i = 0; i < loops; i++) {
            List<ItemStack> drops = new ArrayList<>();
            if (circuit == 4) {
                drops.add(new ItemStack(Items.DRAGON_EGG));
                drops.add(new ItemStack(Items.DRAGON_BREATH, 4));
                drops.add(new ItemStack(Items.DRAGON_HEAD));
            } else if (currentMobList != null) {
                int index = GTValues.RNG.nextInt(currentMobList.length);
                LootTable table = serverLevel.getServer().getLootData().getLootTable(new ResourceLocation("minecraft", "entities/" + currentMobList[index]));
                drops.addAll(table.getRandomItems(new LootParams.Builder(serverLevel).create(LootContextParamSets.EMPTY)));
            }

            for (ItemStack stack : drops) {
                if (!stack.isEmpty()) {
                    ItemStack outputStack = stack.copy();
                    outputStack.setCount((int) Math.min(outputStack.getMaxStackSize(), (long) stack.getCount() * multiplier));
                    allGeneratedDrops.add(outputStack);
                }
            }
        }

        if (!allGeneratedDrops.isEmpty()) {
            for (ItemStack stack : allGeneratedDrops) {
                if (lastDrops.size() >= 5) lastDrops.remove(0);
                lastDrops.add(stack.copy());
            }
            var builder = GTRecipeBuilder.ofRaw();
            builder.recipeType(GTNARecipeType.SLAUGHTERHOUSE_RECIPES);
            for (ItemStack stack : allGeneratedDrops) {
                builder.outputItems(stack);
            }
            GTRecipe tempRecipe = builder.buildRawRecipe();
            RecipeHelper.handleRecipeIO(this, tempRecipe, IO.OUT, this.recipeLogic.getChanceCaches());
        }
    }

    private int getOutputBusCount() {
        var ioMap = getCapabilitiesFlat().get(IO.OUT);
        if (ioMap == null) return 0;
        var handlers = ioMap.get(ItemRecipeCapability.CAP);
        if (handlers == null) return 0;

        Set<IItemHandler> uniqueHandlers = new HashSet<>();
        for (Object h : handlers) {
            if (h instanceof IItemHandler handler) uniqueHandlers.add(handler);
        }
        return uniqueHandlers.size();
    }

    private int getCircuitFromInputBus() {
        var capabilities = getCapabilitiesFlat().get(IO.IN);
        if (capabilities != null) {
            var handlers = capabilities.get(ItemRecipeCapability.CAP);
            if (handlers != null) {
                for (var handler : handlers) {
                    if (handler instanceof IItemHandler itemHandler) {
                        for (int i = 0; i < itemHandler.getSlots(); i++) {
                            ItemStack stack = itemHandler.getStackInSlot(i);
                            if (IntCircuitBehaviour.isIntegratedCircuit(stack)) return IntCircuitBehaviour.getCircuitConfiguration(stack);
                        }
                    }
                }
            }
        }
        return 0;
    }

    @Nullable
    public static ModifierFunction recipeModifier(MetaMachine machine, @NotNull GTRecipe recipe) {
        if (!(machine instanceof IndustrialSlaughterhouse slaughterhouse)) return ModifierFunction.NULL;
        return OverclockingLogic.NON_PERFECT_OVERCLOCK.getModifier(machine, recipe, slaughterhouse.getEnergyContainer().getInputVoltage());
    }

    @Override
    public ModularUI createUI(Player entityPlayer) {
        return new ModularUI(198, 208, this, entityPlayer).widget(new FancyMachineUIWidget(this, 198, 208));
    }

    @Override
    public Widget createUIWidget() {
        var group = new WidgetGroup(0, 0, 182 + 8, 117 + 8);
        var screen = new DraggableScrollableWidgetGroup(4, 4, 182, 117).setBackground(GuiTextures.DISPLAY);
        screen.addWidget(new LabelWidget(4, 5, self().getBlockState().getBlock().getDescriptionId()));
        screen.addWidget(new ComponentPanelWidget(4, 20, this::addDisplayText).setMaxWidthLimit(170));
        group.addWidget(screen);
        group.setBackground(GuiTextures.BACKGROUND_INVERSE);
        return group;
    }

    @Override
    public void addDisplayText(@NotNull List<Component> textList) {
        int circuit = getCircuitFromInputBus();
        int tier = GTUtil.getTierByVoltage(getEnergyContainer().getInputVoltage());

        String mode;
        int base;
        int mBase;
        switch (circuit) {
            case 2 -> { mode = "Hostile"; base = GTValues.MV; mBase = 2; }
            case 3 -> { mode = "Bosses"; base = GTValues.ZPM; mBase = 3; }
            case 4 -> { mode = "Dragon"; base = GTValues.UHV; mBase = 5; }
            default -> { mode = "Passive"; base = GTValues.LV; mBase = 2; }
        }

        MultiblockDisplayText.builder(textList, isFormed())
                .setWorkingStatus(recipeLogic.isWorkingEnabled(), recipeLogic.isActive())
                .addWorkingStatusLine()
                .addEnergyUsageLine(getEnergyContainer())
                .addCustom(text -> {
                    text.add(Component.literal("Mode: ").append(Component.literal(mode).withStyle(ChatFormatting.AQUA)));

                    if (tier >= base) {
                        int currentMult = (int) Math.pow(mBase, (tier - base));
                        text.add(Component.literal("Drop Mult: ").append(Component.literal(currentMult + "x").withStyle(ChatFormatting.GOLD)));
                    } else {
                        text.add(Component.literal("Required: " + GTValues.VNF[base]).withStyle(ChatFormatting.RED));
                    }

                    text.add(Component.literal("Output Buses: " + getOutputBusCount())
                            .withStyle(getOutputBusCount() > 0 ? ChatFormatting.GREEN : ChatFormatting.RED));

                    if (!lastDrops.isEmpty()) {
                        text.add(Component.literal("Latest Drops:").withStyle(ChatFormatting.YELLOW));
                        for (ItemStack s : lastDrops) {
                            text.add(Component.literal("- " + s.getCount() + "x " + s.getHoverName().getString())
                                    .withStyle(ChatFormatting.GRAY));
                        }
                    }
                });
    }
}