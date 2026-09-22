package com.raishxn.gtna.common.machine.multiMachineBase;

import com.gregtechceu.gtceu.api.capability.recipe.FluidRecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.gui.UITemplate;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IDisplayUIMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableMultiblockMachine;
import com.gregtechceu.gtceu.api.machine.steam.SteamEnergyRecipeHandler;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableFluidTank;
import com.gregtechceu.gtceu.api.machine.trait.RecipeHandlerList;
import com.gregtechceu.gtceu.api.pattern.MultiblockState;
import com.gregtechceu.gtceu.api.pattern.TraceabilityPredicate;
import com.gregtechceu.gtceu.api.pattern.util.PatternMatchContext;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.common.data.GTBlocks;
import com.gregtechceu.gtceu.common.data.GTMaterials;

import com.lowdragmc.lowdraglib.gui.modular.ModularUI;
import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib.gui.util.ClickData;
import com.lowdragmc.lowdraglib.gui.widget.ComponentPanelWidget;
import com.lowdragmc.lowdraglib.gui.widget.DraggableScrollableWidgetGroup;
import com.lowdragmc.lowdraglib.gui.widget.LabelWidget;
import com.lowdragmc.lowdraglib.syncdata.annotation.DescSynced;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;
import com.lowdragmc.lowdraglib.utils.BlockInfo;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import com.raishxn.gtna.api.machine.feature.IPatternBufferModeHost;
import com.raishxn.gtna.client.renderer.GTNATextures;
import com.raishxn.gtna.common.data.GTNABlocks;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.stream.Stream;

public abstract class SteamMultiMachineBase extends WorkableMultiblockMachine
                                            implements IDisplayUIMachine, IPatternBufferModeHost {

    public static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(
            SteamMultiMachineBase.class, WorkableMultiblockMachine.MANAGED_FIELD_HOLDER);

    /** Match-context key written by {@link #casing()} with the lowest steam casing tier found. */
    public static final String CASING_TIER_KEY = "gtnaSteamCasingTier";
    public static final int BRONZE_TIER = 1;
    public static final int STEEL_TIER = 2;

    /** GTNL parity: high pressure doubles the processing speed... */
    private static final double HIGH_PRESSURE_DURATION_MULTIPLIER = 0.5;
    /** ...and doubles the steam consumption (mB steam per EU). */
    private static final double HIGH_PRESSURE_CONVERSION_MULTIPLIER = 2.0;

    private final boolean isSteel;

    /**
     * True when the formed structure used steel casings instead of bronze (GTNL
     * {@code SteamMultiMachineBase#isHighPressure}, where {@code tierMachine == 2}). Recomputed on
     * every structure formation and synced so the UI can render the steel skin.
     */
    @Persisted
    @DescSynced
    private boolean highPressure;

    @Nullable
    protected SteamEnergyRecipeHandler steamEnergy = null;

    public SteamMultiMachineBase(IMachineBlockEntity holder, boolean isSteel, Object... args) {
        super(holder, args);
        this.isSteel = isSteel;
    }

    @Override
    public ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }

    /** Whether the formed structure is in high-pressure (steel casing) mode. */
    public boolean isHighPressure() {
        return isSteel || highPressure;
    }

    /** Nominal mB of steam per EU, before the high-pressure bonus. Overridable per machine. */
    protected double getConversionRate() {
        return 1.0;
    }

    /** Steam per EU actually used by the handler: the nominal rate, doubled under high pressure. */
    public double getEffectiveConversionRate() {
        return getConversionRate() * (isHighPressure() ? HIGH_PRESSURE_CONVERSION_MULTIPLIER : 1.0);
    }

    /**
     * The GTNL industrial steam casing shell ({@code metaCasing02} 1/2): bronze tier 1, steel tier 2.
     * This is the block that defines the appearance of the large steam multiblocks.
     */
    public static TraceabilityPredicate industrialCasing() {
        return tieredCasing(new Block[] { GTNABlocks.INDUSTRIAL_STEAM_CASING.get() },
                new Block[] { GTNABlocks.ADVANCED_INDUSTRIAL_STEAM_CASING.get() });
    }

    /** The GTNL machine casing shell ({@code sBlockCasings1:10} / {@code sBlockCasings2:0}). */
    public static TraceabilityPredicate machineCasing() {
        return tieredCasing(new Block[] { GTBlocks.CASING_BRONZE_BRICKS.get() },
                new Block[] { GTBlocks.CASING_STEEL_SOLID.get() });
    }

    /**
     * A casing shell that accepts any of the machine/industrial casings and records the <b>lowest</b>
     * tier found in the match context, exactly like GTNL's {@code ofBlocksTiered} casing lists.
     * Machines whose pattern does not use this predicate simply never see a steel tier.
     */
    public static TraceabilityPredicate casing() {
        return tieredCasing(
                new Block[] { GTBlocks.CASING_BRONZE_BRICKS.get(), GTNABlocks.INDUSTRIAL_STEAM_CASING.get() },
                new Block[] { GTBlocks.CASING_STEEL_SOLID.get(),
                        GTNABlocks.ADVANCED_INDUSTRIAL_STEAM_CASING.get() });
    }

    /** Tiered bronze/steel gearbox casing. */
    public static TraceabilityPredicate gearboxCasing() {
        return tieredCasing(new Block[] { GTBlocks.CASING_BRONZE_GEARBOX.get() },
                new Block[] { GTBlocks.CASING_STEEL_GEARBOX.get() });
    }

    /** Tiered bronze/steel pipe casing. */
    public static TraceabilityPredicate pipeCasing() {
        return tieredCasing(new Block[] { GTBlocks.CASING_BRONZE_PIPE.get() },
                new Block[] { GTBlocks.CASING_STEEL_PIPE.get() });
    }

    /** Tiered bronze/steel firebox. */
    public static TraceabilityPredicate fireboxCasing() {
        return tieredCasing(new Block[] { GTBlocks.FIREBOX_BRONZE.get() },
                new Block[] { GTBlocks.FIREBOX_STEEL.get() });
    }

    /** Tiered bronze/steel GT frame. */
    public static TraceabilityPredicate frameCasing() {
        return tieredCasing(new Block[] { ChemicalHelper.getBlock(TagPrefix.frameGt, GTMaterials.Bronze) },
                new Block[] { ChemicalHelper.getBlock(TagPrefix.frameGt, GTMaterials.Steel) });
    }

    private static TraceabilityPredicate tieredCasing(Block[] bronze, Block[] steel) {
        List<Block> bronzeList = List.of(bronze);
        List<Block> steelList = List.of(steel);
        return new TraceabilityPredicate(state -> {
            BlockState blockState = state.getBlockState();
            int tier = -1;
            for (Block block : bronzeList) {
                if (blockState.is(block)) {
                    tier = BRONZE_TIER;
                    break;
                }
            }
            if (tier < 0) {
                for (Block block : steelList) {
                    if (blockState.is(block)) {
                        tier = STEEL_TIER;
                        break;
                    }
                }
            }
            if (tier < 0) {
                return false;
            }
            recordCasingTier(state, tier);
            return true;
        }, () -> Stream.concat(bronzeList.stream(), steelList.stream())
                .map(block -> new BlockInfo(block.defaultBlockState(), null))
                .toArray(BlockInfo[]::new));
    }

    private static void recordCasingTier(MultiblockState state, int tier) {
        PatternMatchContext context = state.getMatchContext();
        int current = context.getOrDefault(CASING_TIER_KEY, Integer.MAX_VALUE);
        if (tier < current) {
            context.set(CASING_TIER_KEY, tier);
        }
    }

    private static boolean matchesSteamCasing(MultiblockState state) {
        int tier = casingTier(state.getBlockState());
        if (tier < 0) {
            return false;
        }
        recordCasingTier(state, tier);
        return true;
    }

    /** {@code -1} when the block is not a steam casing, otherwise the casing tier. */
    public static int casingTier(BlockState state) {
        if (state.is(GTBlocks.CASING_BRONZE_BRICKS.get()) ||
                state.is(GTNABlocks.INDUSTRIAL_STEAM_CASING.get())) {
            return BRONZE_TIER;
        }
        if (state.is(GTBlocks.CASING_STEEL_SOLID.get()) ||
                state.is(GTNABlocks.ADVANCED_INDUSTRIAL_STEAM_CASING.get())) {
            return STEEL_TIER;
        }
        return -1;
    }

    @Override
    public void onStructureFormed() {
        super.onStructureFormed();
        this.highPressure = getMultiblockState().getMatchContext()
                .getOrDefault(CASING_TIER_KEY, BRONZE_TIER) >= STEEL_TIER;
        for (var part : getParts()) {
            if (!PartAbility.STEAM.isApplicable(part.self().getDefinition().getBlock())) continue;
            var handlers = part.getRecipeHandlers();
            for (var hl : handlers) {
                if (!hl.isValid(IO.IN)) continue;
                for (var fluidHandler : hl.getCapability(FluidRecipeCapability.CAP)) {
                    if (!(fluidHandler instanceof NotifiableFluidTank nft)) continue;
                    if (nft.isFluidValid(0, GTMaterials.Steam.getFluid(1))) {
                        steamEnergy = new SteamEnergyRecipeHandler(nft, getEffectiveConversionRate());
                        addHandlerList(RecipeHandlerList.of(IO.IN, steamEnergy));
                        return;
                    }
                }
            }
        }
        if (steamEnergy == null) {
            onStructureInvalid();
        }
    }

    @Override
    public void onStructureInvalid() {
        super.onStructureInvalid();
        this.steamEnergy = null;
        this.highPressure = false;
    }

    /**
     * Applies the high-pressure duration bonus on top of whatever the machine's recipe modifier
     * produced. Subclasses that override {@link #getRealRecipe} must route their result through
     * {@link #applyHighPressure(GTRecipe)} (or through {@code createThreadedRecipe}, which does).
     */
    @Nullable
    @Override
    protected GTRecipe getRealRecipe(GTRecipe recipe) {
        return applyHighPressure(super.getRealRecipe(recipe));
    }

    /** GTNL parity: a steel-cased (high pressure) structure processes at double speed. */
    @Nullable
    protected GTRecipe applyHighPressure(@Nullable GTRecipe recipe) {
        if (recipe == null || !isHighPressure()) {
            return recipe;
        }
        GTRecipe copy = recipe.copy();
        copy.duration = Math.max(1, (int) (copy.duration * HIGH_PRESSURE_DURATION_MULTIPLIER));
        return copy;
    }

    public IGuiTexture getScreenTexture() {
        return GuiTextures.DISPLAY_STEAM.get(isHighPressure());
    }

    @Override
    public ModularUI createUI(Player entityPlayer) {
        var screen = new DraggableScrollableWidgetGroup(7, 4, 162, 121)
                .setBackground(getScreenTexture());

        screen.addWidget(new LabelWidget(4, 5, self().getBlockState().getBlock().getDescriptionId()));
        screen.addWidget(new ComponentPanelWidget(4, 17, this::addDisplayText)
                .setMaxWidthLimit(150)
                .clickHandler(this::handleDisplayClick));

        return new ModularUI(176, 216, this, entityPlayer)
                .background(GuiTextures.BACKGROUND_STEAM.get(isHighPressure()))
                .widget(screen)
                // The addon logo in the bottom-right corner (GTNL convention). Added to the ModularUI,
                // not the scrollable screen group, whose scissor would clip its right/bottom edge.
                .widget(GTNATextures.logo(151, 107))
                .widget(UITemplate.bindPlayerInventory(entityPlayer.getInventory(),
                        GuiTextures.SLOT_STEAM.get(isHighPressure()), 7, 134, true));
    }

    public void handleDisplayClick(String componentData, ClickData clickData) {}

    @Override
    public @Nullable String gtna$resolvePatternBufferMode(com.gregtechceu.gtceu.api.recipe.GTRecipe recipe) {
        if (getRecipeTypes().length <= 1) {
            return null;
        }
        return recipe.getType().registryName.toString();
    }

    @Override
    public boolean gtna$applyPatternBufferMode(String modeId, com.gregtechceu.gtceu.api.recipe.GTRecipe recipe) {
        if (modeId == null || modeId.isBlank()) {
            return false;
        }
        // Exact GTM formula (MachineModeFancyConfigurator.setActiveRecipeTypeAndUpdateTickSubs).
        for (int i = 0; i < getRecipeTypes().length; i++) {
            if (gtna$matchesModeId(modeId, getRecipeTypes()[i])) {
                boolean needUpdateTickSubs = !keepSubscribing() && getActiveRecipeType() != i;
                setActiveRecipeType(i); // @Persisted: NBT + network sync are automatic
                if (needUpdateTickSubs) {
                    getRecipeLogic().updateTickSubscription();
                }
                return true;
            }
        }
        return false;
    }

    @Override
    public void addDisplayText(List<Component> textList) {
        IDisplayUIMachine.super.addDisplayText(textList);
        if (isFormed()) {
            if (steamEnergy != null && steamEnergy.getCapacity() > 0) {
                long steamStored = steamEnergy.getStored();
                textList.add(Component.translatable("gtceu.multiblock.steam.steam_stored", steamStored,
                        steamEnergy.getCapacity()));
            }

            if (isHighPressure()) {
                textList.add(Component.translatable("gtna.multiblock.steam.high_pressure")
                        .withStyle(ChatFormatting.AQUA));
            }

            if (!isWorkingEnabled()) {
                textList.add(Component.translatable("gtceu.multiblock.work_paused"));

            } else if (isActive()) {
                textList.add(Component.translatable("gtceu.multiblock.running"));

                int currentProgress = (int) (recipeLogic.getProgressPercent() * 100);
                double maxInSec = (float) recipeLogic.getDuration() / 20.0f;
                double currentInSec = (float) recipeLogic.getProgress() / 20.0f;

                textList.add(Component.translatable("gtceu.multiblock.progress",
                        String.format("%.2f", (float) currentInSec),
                        String.format("%.2f", (float) maxInSec), currentProgress));

            } else {
                textList.add(Component.translatable("gtceu.multiblock.idling"));
            }

            if (recipeLogic.isWaiting()) {
                textList.add(Component.translatable("gtceu.multiblock.steam.low_steam")
                        .setStyle(Style.EMPTY.withColor(ChatFormatting.RED)));
            }
        }
    }
}
