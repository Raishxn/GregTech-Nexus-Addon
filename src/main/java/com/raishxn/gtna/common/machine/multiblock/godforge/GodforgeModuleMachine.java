package com.raishxn.gtna.common.machine.multiblock.godforge;

import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableMultiblockMachine;
import com.gregtechceu.gtceu.api.pattern.BlockPattern;
import com.gregtechceu.gtceu.api.pattern.FactoryBlockPattern;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.RecipeHelper;
import com.gregtechceu.gtceu.api.recipe.content.ContentModifier;
import com.gregtechceu.gtceu.api.recipe.modifier.ModifierFunction;
import com.gregtechceu.gtceu.api.recipe.modifier.ParallelLogic;

import com.lowdragmc.lowdraglib.syncdata.annotation.DescSynced;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;

import net.minecraft.core.GlobalPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;

import com.raishxn.gtna.api.capability.WirelessEnergyManager;
import com.raishxn.gtna.api.machine.feature.godforge.GodforgeModuleStats;
import com.raishxn.gtna.api.machine.feature.godforge.GodforgeModuleStats.Type;
import com.raishxn.gtna.api.machine.feature.godforge.GodforgeOverclock;
import com.raishxn.gtna.common.data.GTNAGodforgeContent;
import com.raishxn.gtna.utils.datastructure.Int128;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.math.BigInteger;
import java.util.UUID;
import java.util.function.Supplier;

import static com.gregtechceu.gtceu.api.pattern.Predicates.*;

/**
 * Forge of Gods module, ported from GTNH {@code MTEBaseModule} and its Smelting/Molten/Plasma subclasses. It has no
 * energy hatches: the whole recipe energy is taken from the owner's wireless network when the recipe starts, with
 * heat, parallels, speed and voltage pushed by the forge every five seconds.
 */
public class GodforgeModuleMachine extends WorkableMultiblockMachine
                                   implements IGodforgeModule,
                                   com.gregtechceu.gtceu.api.machine.feature.IFancyUIMachine,
                                   com.gregtechceu.gtceu.api.machine.feature.multiblock.IDisplayUIMachine,
                                   com.raishxn.gtna.api.machine.IThreadModifierMachine,
                                   com.raishxn.gtna.api.machine.multiblock.ParallelMachine {

    protected static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(
            GodforgeModuleMachine.class, WorkableMultiblockMachine.MANAGED_FIELD_HOLDER);

    private final GodforgeModuleStats stats;

    @Persisted
    @DescSynced
    private boolean connected;
    @Persisted
    @Nullable
    private UUID networkOwner;
    /** GTNH voltage config panel: parallel cap and "always max parallel". */
    @Persisted
    private int setMaxParallel = 1;
    @Persisted
    private boolean alwaysMaxParallel = true;

    /** Energy computed by the last modifier call, debited when that recipe starts. */
    private BigInteger pendingEU = BigInteger.ZERO;
    private int pendingParallel;
    private long pendingHeat;

    @Nullable
    private com.raishxn.gtna.common.machine.multiblock.part.ThreadPartMachine threadPart;

    public GodforgeModuleMachine(IMachineBlockEntity holder, Type type, Object... args) {
        super(holder, args);
        this.stats = new GodforgeModuleStats(type);
    }

    // ---- GTNA Thread Hatch: distinct recipes in parallel, gated by forge upgrades ----

    @Override
    protected com.gregtechceu.gtceu.api.machine.trait.RecipeLogic createRecipeLogic(Object... args) {
        // The Exotic module rolls its own random recipe, so it stays single-threaded.
        if (this instanceof GodforgeExoticModuleMachine) return super.createRecipeLogic(args);
        return new com.raishxn.gtna.common.machine.trait.GTNAMultipleRecipesLogic(this);
    }

    @Override
    public @Nullable com.raishxn.gtna.common.machine.multiblock.part.ThreadPartMachine getThreadPartMachine() {
        return threadPart;
    }

    @Override
    public void setThreadPartMachine(@Nullable com.raishxn.gtna.common.machine.multiblock.part.ThreadPartMachine part) {
        threadPart = part;
    }

    /** A hatch above the unlocked tier still lets the structure form, it just adds no threads. */
    @Override
    public int getAdditionalThread() {
        if (threadPart == null || !threadHatchUnlocked()) return 0;
        return threadPart.getThreadCount();
    }

    public boolean threadHatchUnlocked() {
        return threadPart != null && threadPart.getTier() <= stats.maxThreadHatchTier;
    }

    @Override
    public int getMaxParallel() {
        return actualParallel();
    }

    @Override
    public com.gregtechceu.gtceu.api.recipe.modifier.RecipeModifier getRecipeModifier() {
        return GodforgeModuleMachine::recipeModifier;
    }

    @Override
    public void onStructureInvalid() {
        super.onStructureInvalid();
        threadPart = null;
    }

    @Override
    public ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }

    @Override
    public GodforgeModuleStats godforgeStats() {
        return stats;
    }

    public Type type() {
        return stats.type;
    }

    @Nullable
    protected UUID networkOwner() {
        return networkOwner;
    }

    @Override
    public void setNetworkOwner(UUID owner) {
        networkOwner = owner;
    }

    @Override
    public void connect() {
        stats.connected = true;
        connected = true;
    }

    @Override
    public void disconnect() {
        stats.connected = false;
        connected = false;
    }

    public boolean isConnected() {
        return connected;
    }

    /** {@code getActualParallel}: the forge's maximum, or the configured cap below it. */
    public int actualParallel() {
        int max = Math.max(1, stats.calculatedMaxParallel);
        return alwaysMaxParallel ? max : Math.max(1, Math.min(max, setMaxParallel));
    }

    public void setMaxParallel(int parallel) {
        setMaxParallel = Math.max(1, parallel);
        markDirty();
    }

    public void toggleAlwaysMaxParallel() {
        alwaysMaxParallel = !alwaysMaxParallel;
        markDirty();
    }

    /** With The Boundless Flow the processing voltage is chosen per module. */
    public void setProcessingVoltage(long voltage) {
        if (!stats.voltageConfig) return;
        stats.processingVoltage = Math.max(2_000_000_000L, voltage);
        markDirty();
    }

    // ---- GUI (GTNH MTEBaseModuleGui) ----

    @Override
    public com.lowdragmc.lowdraglib.gui.modular.ModularUI createUI(net.minecraft.world.entity.player.Player player) {
        return com.gregtechceu.gtceu.api.machine.feature.IFancyUIMachine.super.createUI(player);
    }

    @Override
    public com.lowdragmc.lowdraglib.gui.widget.Widget createUIWidget() {
        var group = new com.lowdragmc.lowdraglib.gui.widget.WidgetGroup(0, 0, 182, 117);
        var screen = new com.lowdragmc.lowdraglib.gui.widget.DraggableScrollableWidgetGroup(4, 4, 174, 109)
                .setBackground(getScreenTexture());
        screen.addWidget(new com.lowdragmc.lowdraglib.gui.widget.LabelWidget(4, 5,
                self().getBlockState().getBlock().getDescriptionId()));
        screen.addWidget(new com.lowdragmc.lowdraglib.gui.widget.ComponentPanelWidget(4, 17, this::addDisplayText)
                .textSupplier(isRemote() ? null : this::addDisplayText).setMaxWidthLimit(164)
                .clickHandler(this::handleDisplayClick));
        group.addWidget(screen);
        group.setBackground(com.gregtechceu.gtceu.api.gui.GuiTextures.BACKGROUND_INVERSE);
        return group;
    }

    @Override
    public void attachSideTabs(com.gregtechceu.gtceu.api.gui.fancy.TabsWidget sideTabs) {
        com.gregtechceu.gtceu.api.machine.feature.IFancyUIMachine.super.attachSideTabs(sideTabs);
        sideTabs.attachSubTab(new com.raishxn.gtna.common.machine.multiblock.godforge.gui.GodforgePage(
                "gtna.godforge.gui.voltage_config", com.gregtechceu.gtceu.api.gui.GuiTextures.BUTTON_POWER
                        .getSubTexture(0, 0, 1, 0.5),
                this::voltagePage));
    }

    private com.lowdragmc.lowdraglib.gui.widget.Widget voltagePage() {
        var page = new com.lowdragmc.lowdraglib.gui.widget.WidgetGroup(0, 0, 200, 120);
        page.setBackground(com.raishxn.gtna.common.machine.multiblock.godforge.gui.GodforgeGui.BACKGROUND_STAR);
        page.addWidget(new com.lowdragmc.lowdraglib.gui.widget.ImageWidget(5, 5, 190, 10,
                com.raishxn.gtna.common.machine.multiblock.godforge.gui.GodforgeGui.text("gtna.godforge.gui.parallel",
                        0xFFAA00)));
        page.addWidget(
                new com.raishxn.gtna.common.machine.multiblock.godforge.gui.GodforgeGui.ServerField(10, 18, 120, 14,
                        () -> String.valueOf(setMaxParallel), value -> {
                            try {
                                setMaxParallel(Integer.parseInt(value.trim()));
                            } catch (NumberFormatException ignored) {}
                        }).setNumbersOnly(1, Integer.MAX_VALUE));
        page.addWidget(new com.lowdragmc.lowdraglib.gui.widget.SwitchWidget(136, 17, 54, 16,
                (click, pressed) -> {
                    if (!click.isRemote) toggleAlwaysMaxParallel();
                }).setSupplier(() -> alwaysMaxParallel)
                .setTexture(new com.lowdragmc.lowdraglib.gui.texture.GuiTextureGroup(
                        com.gregtechceu.gtceu.api.gui.GuiTextures.BUTTON,
                        new com.lowdragmc.lowdraglib.gui.texture.TextTexture("gtna.godforge.gui.max_parallel")),
                        new com.lowdragmc.lowdraglib.gui.texture.GuiTextureGroup(
                                com.raishxn.gtna.common.machine.multiblock.godforge.gui.GodforgeGui.BUTTON_SPACE_PRESSED,
                                new com.lowdragmc.lowdraglib.gui.texture.TextTexture(
                                        "gtna.godforge.gui.max_parallel")))
                .setHoverTooltips("gtna.godforge.gui.max_parallel.tooltip"));
        page.addWidget(new com.lowdragmc.lowdraglib.gui.widget.ImageWidget(5, 40, 190, 10,
                com.raishxn.gtna.common.machine.multiblock.godforge.gui.GodforgeGui.text(
                        "gtna.godforge.gui.voltageinfo",
                        0xFFAA00)));
        page.addWidget(
                new com.raishxn.gtna.common.machine.multiblock.godforge.gui.GodforgeGui.ServerField(10, 53, 180, 14,
                        () -> String.valueOf(stats.processingVoltage), value -> {
                            try {
                                setProcessingVoltage(Long.parseLong(value.trim()));
                            } catch (NumberFormatException ignored) {}
                        }).setNumbersOnly(2_000_000_000L, Long.MAX_VALUE)
                        .setHoverTooltips(net.minecraft.network.chat.Component.translatable(
                                "gtna.godforge.text.tooltip.voltageadjustment"),
                                net.minecraft.network.chat.Component.translatable(
                                        "gtna.godforge.text.tooltip.voltageadjustment.1")));
        page.addWidget(new com.lowdragmc.lowdraglib.gui.widget.ComponentPanelWidget(10, 74, lines -> {
            if (!stats.voltageConfig) {
                lines.add(net.minecraft.network.chat.Component.translatable(
                        "gtna.godforge.button.voltageconfig.tooltip.02").withStyle(net.minecraft.ChatFormatting.RED));
            }
        }).setMaxWidthLimit(180));
        return page;
    }

    @Override
    public void addDisplayText(java.util.List<net.minecraft.network.chat.Component> text) {
        com.gregtechceu.gtceu.api.machine.multiblock.MultiblockDisplayText.builder(text, isFormed())
                .setWorkingStatus(recipeLogic.isWorkingEnabled(), recipeLogic.isActive())
                .addWorkingStatusLine()
                .addProgressLine(recipeLogic.getProgress(), recipeLogic.getMaxProgress(),
                        recipeLogic.getProgressPercent());
        if (!isFormed()) return;
        text.add(net.minecraft.network.chat.Component.translatable("gtna.godforge.gui.modulestatus").append(" ")
                .append(net.minecraft.network.chat.Component.translatable("gtna.godforge.gui.modulestatus." +
                        connected).withStyle(connected ? net.minecraft.ChatFormatting.GREEN :
                                net.minecraft.ChatFormatting.RED)));
        if (stats.type != Type.EXOTIC) {
            stat(text, "heat", String.valueOf(stats.heat));
            stat(text, "effectiveheat", String.valueOf(stats.heatForOC));
        }
        stat(text, "parallel", String.valueOf(actualParallel()));
        if (threadPart != null) {
            if (threadHatchUnlocked()) {
                text.add(net.minecraft.network.chat.Component.translatable("gtna.godforge.gui.threads",
                        net.minecraft.network.chat.Component.literal("1 + " + threadPart.getThreadCount())
                                .withStyle(net.minecraft.ChatFormatting.AQUA)));
            } else {
                int t = threadPart.getTier();
                String need = t > com.gregtechceu.gtceu.api.GTValues.OpV ? "END" :
                        t > com.gregtechceu.gtceu.api.GTValues.UIV ? "CD" : "GPCI";
                text.add(net.minecraft.network.chat.Component.translatable("gtna.godforge.gui.threads.locked", need,
                        com.gregtechceu.gtceu.api.GTValues.VN[stats.maxThreadHatchTier])
                        .withStyle(net.minecraft.ChatFormatting.RED));
            }
        }
        stat(text, "speedbonus", String.format(java.util.Locale.ROOT, "%.4f", stats.speedBonus));
        stat(text, "energydiscount", String.format(java.util.Locale.ROOT, "%.4f", stats.energyDiscount));
        stat(text, "ocdivisor", String.format(java.util.Locale.ROOT, "%.2f", stats.overclockTimeFactor));
        stat(text, "processingvoltage", com.gregtechceu.gtceu.utils.FormattingUtil.formatNumbers(
                stats.processingVoltage) + " EU/t");
        if (stats.type == Type.PLASMA) {
            text.add(net.minecraft.network.chat.Component.translatable("gtna.godforge.gui.plasmarecipetier",
                    stats.plasmaTier));
            text.add(net.minecraft.network.chat.Component.translatable("gtna.godforge.gui.plasmamultistep",
                    stats.multiStepPlasma));
        }
    }

    private static void stat(java.util.List<net.minecraft.network.chat.Component> text, String key, String value) {
        text.add(net.minecraft.network.chat.Component.translatable("gtna.godforge.gui." + key).append(": ")
                .append(net.minecraft.network.chat.Component.literal(value)
                        .withStyle(net.minecraft.ChatFormatting.GOLD)));
    }

    // ---- Structure: 7x7x13, GTNH MTEBaseModule ----

    private static final String[][] SHAPE = {
            { "       ", "  BBB  ", " BBBBB ", " BB~BB ", " BBBBB ", "  BBB  ", "       " },
            { "  CCC  ", " CFFFC ", "CFFFFFC", "CFFFFFC", "CFFFFFC", " CFFFC ", "  CCC  " },
            { "       ", "       ", "   E   ", "  EAE  ", "   E   ", "       ", "       " },
            { "       ", "       ", "   E   ", "  EAE  ", "   E   ", "       ", "       " },
            { "       ", "       ", "   E   ", "  EAE  ", "   E   ", "       ", "       " },
            { "       ", "       ", "   E   ", "  EAE  ", "   E   ", "       ", "       " },
            { "       ", "       ", "   E   ", "  EAE  ", "   E   ", "       ", "       " },
            { "       ", "       ", "       ", "   D   ", "       ", "       ", "       " },
            { "       ", "       ", "       ", "   D   ", "       ", "       ", "       " },
            { "       ", "       ", "       ", "   D   ", "       ", "       ", "       " },
            { "       ", "       ", "       ", "   D   ", "       ", "       ", "       " },
            { "       ", "       ", "       ", "   D   ", "       ", "       ", "       " },
            { "       ", "       ", "       ", "   G   ", "       ", "       ", "       " } };

    public static BlockPattern createPattern(MultiblockMachineDefinition definition, Type type) {
        // StructureLib slices go back from the controller and rows top-down; GTCEu wants front-to-back aisles
        // read bottom-up, so both are reversed.
        var pattern = FactoryBlockPattern.start();
        for (int slice = SHAPE.length - 1; slice >= 0; slice--) {
            String[] rows = new String[SHAPE[slice].length];
            for (int row = 0; row < rows.length; row++) rows[row] = SHAPE[slice][rows.length - 1 - row];
            pattern.aisle(rows);
        }
        Supplier<Block> core = type == Type.SMELTING ? GTNAGodforgeContent.HYPOGEN_COIL::get :
                GTNAGodforgeContent.HARMONIC_PHONON_TRANSMISSION_CONDUIT::get;
        var hatches = blocks(GTNAGodforgeContent.SINGULARITY_SHIELDING_CASING.get())
                .or(abilities(PartAbility.IMPORT_ITEMS))
                .or(abilities(PartAbility.IMPORT_FLUIDS))
                .or(abilities(PartAbility.EXPORT_ITEMS))
                .or(abilities(PartAbility.EXPORT_FLUIDS));
        if (type != Type.EXOTIC) {
            hatches = hatches.or(abilities(com.raishxn.gtna.api.machine.multiblock.GTNAPartAbility.THREAD_HATCH)
                    .setMaxGlobalLimited(1));
        }
        return pattern.where('~', controller(blocks(definition.get())))
                .where('A', blocks(core.get()))
                .where('B', hatches)
                .where('C', blocks(GTNAGodforgeContent.SINGULARITY_SHIELDING_CASING.get()))
                .where('D', blocks(GTNAGodforgeContent.GUIDANCE_CASING.get()))
                .where('E', blocks(GTNAGodforgeContent.BOUNDLESS_STRUCTURE_CASING.get()))
                .where('F', blocks(GTNAGodforgeContent.MAGNETIC_CONFINEMENT_CASING.get()))
                .where('G', blocks(GTNAGodforgeContent.STELLAR_ENERGY_SIPHON_CASING.get()))
                .where(' ', any())
                .build();
    }

    // ---- Recipes ----

    /** Largest parallel amount (up to {@code limit}) whose scaled inputs the parts can supply. */
    private static int parallelByMatching(GodforgeModuleMachine module, GTRecipe recipe, int limit) {
        if (!matches(module, recipe, 1)) return 0;
        int low = 1, high = limit;
        while (low < high) {
            int mid = (int) (((long) low + high + 1) / 2);
            if (matches(module, recipe, mid)) low = mid;
            else high = mid - 1;
        }
        return low;
    }

    private static boolean matches(GodforgeModuleMachine module, GTRecipe recipe, int parallel) {
        GTRecipe scaled = recipe.copy(ContentModifier.multiplier(parallel), false);
        scaled.tickInputs.remove(com.gregtechceu.gtceu.api.capability.recipe.EURecipeCapability.CAP);
        return RecipeHelper.matchRecipe(module, scaled).isSuccess();
    }

    /** Recipe heat, as GTCEu blast recipes store it. */
    private static int recipeHeat(GTRecipe recipe) {
        return recipe.data.contains("ebf_temp") ? recipe.data.getInt("ebf_temp") : 0;
    }

    /**
     * GT5 processing with the module stats: heat and voltage checks, parallels with the sub-tick multiplier,
     * GT5 overclocks (heat overclocks for Smelting/Molten). The resulting energy is paid in {@link #beforeWorking}.
     */
    public static ModifierFunction recipeModifier(MetaMachine machine, @NotNull GTRecipe recipe) {
        if (!(machine instanceof GodforgeModuleMachine module) || !module.connected) return ModifierFunction.NULL;
        GodforgeModuleStats stats = module.stats;
        var eu = RecipeHelper.getRealEUtWithIO(recipe);
        long recipeEUt = eu.isInput() ? eu.stack().getTotalEU() : 0;
        int heat = recipeHeat(recipe);
        boolean heated = stats.type == Type.SMELTING || stats.type == Type.MOLTEN;
        if (heated && heat > stats.heat) return ModifierFunction.NULL;
        if (stats.type == Type.SMELTING && recipeEUt > stats.processingVoltage) return ModifierFunction.NULL;
        if (stats.type == Type.PLASMA && (recipe.data.getInt(
                com.raishxn.gtna.data.recipe.GTNAGodforgeRecipes.PLASMA_TIER) > stats.plasmaTier ||
                recipe.data.getBoolean(com.raishxn.gtna.data.recipe.GTNAGodforgeRecipes.PLASMA_MULTISTEP) &&
                        !stats.multiStepPlasma)) {
            return ModifierFunction.NULL;
        }

        int maxParallel = module.actualParallel();
        long voltage = Math.min(stats.processingVoltage, Long.MAX_VALUE / maxParallel);
        GodforgeOverclock probe = calculator(stats, recipeEUt, recipe.duration, heat, voltage, heated);
        probe.parallel = maxParallel;
        int subTickParallel = (int) Math.min(Integer.MAX_VALUE, (long) (maxParallel * probe.multiplierUnderOneTick()));
        int parallel = ParallelLogic.getParallelAmountWithoutEU(machine, recipe, Math.max(1, subTickParallel));
        // Creative/infinite parts (GTMThings) only answer through the recipe handler path, which ParallelLogic's
        // inventory count does not see; probe the parallel amount through that path instead.
        if (parallel <= 0) parallel = parallelByMatching(module, recipe, Math.max(1, subTickParallel));
        if (parallel <= 0) return ModifierFunction.NULL;

        GodforgeOverclock calc = calculator(stats, recipeEUt, recipe.duration, heat, voltage, heated);
        calc.parallel = Math.min(maxParallel, parallel);
        calc.calculate();
        if (calc.consumption() == Long.MAX_VALUE || calc.calculatedDuration() == Integer.MAX_VALUE) {
            return ModifierFunction.NULL;
        }
        module.pendingEU = BigInteger.valueOf(calc.consumption()).multiply(BigInteger.valueOf(
                calc.calculatedDuration()));
        module.pendingParallel = parallel;
        module.pendingHeat = heat;
        if (module.networkOwner != null && module.getLevel() instanceof ServerLevel level &&
                WirelessEnergyManager.getEnergy(level, module.networkOwner).toBigInteger()
                        .compareTo(module.pendingEU) < 0) {
            return ModifierFunction.NULL;
        }
        ModifierFunction scaled = ModifierFunction.builder()
                .inputModifier(ContentModifier.multiplier(parallel))
                .outputModifier(ContentModifier.multiplier(parallel))
                .durationMultiplier((double) calc.calculatedDuration() / Math.max(1, recipe.duration))
                .parallels(parallel)
                .build();
        return original -> {
            GTRecipe modified = scaled.apply(original);
            if (modified == null) return null;
            // The energy was priced above and is paid from the wireless network, not from hatches.
            var tickInputs = new java.util.HashMap<>(modified.tickInputs);
            tickInputs.remove(com.gregtechceu.gtceu.api.capability.recipe.EURecipeCapability.CAP);
            modified.tickInputs.clear();
            modified.tickInputs.putAll(tickInputs);
            return modified;
        };
    }

    /**
     * Molten module: blast furnace outputs that have a molten (or other) fluid leave as that fluid, as in GTNH
     * {@code Godforge.initMoltenModuleRecipes} — 144 mB per ingot of material, 1000 mB for non-molten fluids.
     */
    static void convertOutputsToMolten(GTRecipe recipe) {
        convertOutputsToMolten(recipe.outputs);
    }

    /** Same conversion on a recipe's (or recipe builder's) output map. */
    public static void convertOutputsToMolten(
                                              java.util.Map<com.gregtechceu.gtceu.api.capability.recipe.RecipeCapability<?>, java.util.List<com.gregtechceu.gtceu.api.recipe.content.Content>> outputs) {
        var itemCap = com.gregtechceu.gtceu.api.capability.recipe.ItemRecipeCapability.CAP;
        var fluidCap = com.gregtechceu.gtceu.api.capability.recipe.FluidRecipeCapability.CAP;
        var items = outputs.get(itemCap);
        if (items == null || items.isEmpty()) return;
        var keptItems = new java.util.ArrayList<com.gregtechceu.gtceu.api.recipe.content.Content>();
        var fluids = new java.util.ArrayList<>(outputs.getOrDefault(fluidCap, java.util.List.of()));
        for (var content : items) {
            var ingredient = itemCap.of(content.content);
            var stacks = ingredient.getItems();
            net.minecraft.world.level.material.Fluid fluid = null;
            long amount = 0;
            if (stacks.length > 0) {
                var materialStack = com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper.getMaterialStack(stacks[0]);
                var material = materialStack.material();
                if (!materialStack.isEmpty() && material.hasFluid()) {
                    var molten = material.getFluid(
                            com.gregtechceu.gtceu.api.fluids.store.FluidStorageKeys.MOLTEN);
                    var liquid = material.getFluid(
                            com.gregtechceu.gtceu.api.fluids.store.FluidStorageKeys.LIQUID);
                    if (molten != null || liquid != null) {
                        fluid = molten != null ? molten : liquid;
                        amount = 144L * materialStack.amount() / com.gregtechceu.gtceu.api.GTValues.M *
                                stacks[0].getCount();
                    } else {
                        fluid = material.getFluid();
                        amount = 1000L * stacks[0].getCount();
                    }
                }
            }
            if (fluid == null || amount <= 0) {
                keptItems.add(content);
                continue;
            }
            fluids.add(new com.gregtechceu.gtceu.api.recipe.content.Content(
                    com.gregtechceu.gtceu.api.recipe.ingredient.FluidIngredient.of(fluid,
                            (int) Math.min(Integer.MAX_VALUE, amount)),
                    content.chance, content.maxChance, content.tierChanceBoost));
        }
        outputs.put(itemCap, keptItems);
        outputs.put(fluidCap, fluids);
    }

    private static GodforgeOverclock calculator(GodforgeModuleStats stats, long recipeEUt, int duration, int heat,
                                                long voltage, boolean heated) {
        GodforgeOverclock calc = new GodforgeOverclock();
        calc.recipeEUt = recipeEUt;
        calc.duration = duration;
        calc.machineVoltage = voltage;
        calc.machineAmperage = Integer.MAX_VALUE;
        calc.durationModifier = stats.speedBonus;
        calc.eutModifier = stats.energyDiscount;
        calc.durationDecreasePerOC = stats.overclockTimeFactor;
        if (heated) {
            calc.recipeHeat = heat;
            calc.heatOC = true;
            calc.heatDiscount = true;
            calc.machineHeat = Math.max(heat, stats.heatForOC);
            calc.heatDiscountExponent = stats.heatEnergyDiscount();
        }
        return calc;
    }

    @Override
    public boolean beforeWorking(@Nullable GTRecipe recipe) {
        if (!connected || networkOwner == null || !(getLevel() instanceof ServerLevel level)) return false;
        if (pendingEU.signum() > 0 && !WirelessEnergyManager.consumeDirectEnergy(level, networkOwner,
                Int128.fromBigInteger(pendingEU), GlobalPos.of(level.dimension(), getPos()), "godforge_module")) {
            return false;
        }
        stats.powerTally = stats.powerTally.add(pendingEU);
        stats.recipeTally += pendingParallel;
        stats.currentRecipeHeat = pendingHeat;
        pendingEU = BigInteger.ZERO;
        return super.beforeWorking(recipe);
    }

    @Override
    public void saveCustomPersistedData(CompoundTag tag, boolean forDrop) {
        super.saveCustomPersistedData(tag, forDrop);
        tag.putString("powerTally", stats.powerTally.toString());
        tag.putLong("recipeTally", stats.recipeTally);
        tag.putLong("currentRecipeHeat", stats.currentRecipeHeat);
        tag.putLong("processingVoltage", stats.processingVoltage);
        tag.putBoolean("isVoltageConfigUnlocked", stats.voltageConfig);
    }

    @Override
    public void loadCustomPersistedData(CompoundTag tag) {
        super.loadCustomPersistedData(tag);
        String power = tag.getString("powerTally");
        stats.powerTally = power.isEmpty() ? BigInteger.ZERO : new BigInteger(power);
        stats.recipeTally = tag.getLong("recipeTally");
        stats.currentRecipeHeat = tag.getLong("currentRecipeHeat");
        stats.connected = connected;
        if (tag.contains("processingVoltage")) stats.processingVoltage = tag.getLong("processingVoltage");
        stats.voltageConfig = tag.getBoolean("isVoltageConfigUnlocked");
    }
}
