package com.raishxn.gtna.common.data;

import com.gregtechceu.gtceu.api.data.RotationState;
import com.gregtechceu.gtceu.api.item.ComponentItem;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;
import com.gregtechceu.gtceu.common.data.GTBlocks;

import net.minecraft.client.color.item.ItemColor;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;

import com.raishxn.gtna.GTNACORE;
import com.raishxn.gtna.api.machine.multiblock.GTNASubPatterns;
import com.raishxn.gtna.common.data.material.GodforgeChainMaterials;
import com.raishxn.gtna.common.data.multiblock.GTNAMultiBlockFileReader;
import com.raishxn.gtna.common.machine.multiblock.electric.NanoForgeMachine;
import com.tterrag.registrate.util.entry.ItemEntry;

import static com.gregtechceu.gtceu.api.pattern.Predicates.abilities;
import static com.gregtechceu.gtceu.api.pattern.Predicates.any;
import static com.gregtechceu.gtceu.api.pattern.Predicates.blocks;
import static com.gregtechceu.gtceu.api.pattern.Predicates.controller;
import static com.gregtechceu.gtceu.api.pattern.Predicates.frames;
import static com.raishxn.gtna.api.registry.GTNARegistry.REGISTRATE;

/**
 * GTNH Nano Forge (G-0196): pieces converted from {@code MTENanoForge} by {@code tools/convert_nano_forge_structure.py}
 * (main, tier 2 and tier 3; tier 4 is not ported) and GTNH nanites. Nanite textures are GT5U / Modernity-GTNH
 * originals: the generic one is tinted with the material colour, the custom ones are drawn as is.
 */
public final class GTNANanoForge {

    public static final ItemEntry<ComponentItem> CARBON_NANITES = nanite("carbon", "Carbon", 0x141414);
    public static final ItemEntry<ComponentItem> NEUTRONIUM_NANITES = nanite("neutronium", "Neutronium", 0xfafafa);
    public static final ItemEntry<ComponentItem> TRANSCENDENT_METAL_NANITES = nanite("transcendent_metal",
            "Transcendent Metal", 0x323232);
    public static final ItemEntry<ComponentItem> SILVER_NANITES = nanite("silver", "Silver", 0xdcdcff);
    public static final ItemEntry<ComponentItem> GOLD_NANITES = nanite("gold", "Gold", 0xffe650);
    public static final ItemEntry<ComponentItem> GLOWSTONE_NANITES = custom("glowstone", "Glowstone", true);
    public static final ItemEntry<ComponentItem> SIX_PHASED_COPPER_NANITES = nanite("six_phased_copper",
            "Six-Phased Copper", 0xff7814);
    public static final ItemEntry<ComponentItem> WHITE_DWARF_MATTER_NANITES = custom("white_dwarf_matter",
            "White Dwarf Matter", true);
    public static final ItemEntry<ComponentItem> BLACK_DWARF_MATTER_NANITES = nanite("black_dwarf_matter",
            "Black Dwarf Matter", 0x0c0c0c);
    public static final ItemEntry<ComponentItem> UNIVERSIUM_NANITES = custom("universium", "Universium", false);
    public static final ItemEntry<ComponentItem> ETERNITY_NANITES = custom("eternity", "Eternity", false);

    public static final MultiblockMachineDefinition NANO_FORGE = REGISTRATE
            .multiblock("nano_forge", NanoForgeMachine::new)
            .langValue("Nano Forge")
            .rotationState(RotationState.NON_Y_AXIS)
            .recipeType(GTNARecipeType.NANO_FORGE_RECIPES)
            .recipeModifiers(NanoForgeMachine::nanoTier)
            .appearanceBlock(GTNABlocks.RADIANT_NAQUADAH_ALLOY_CASING)
            .pattern(definition -> GTNAMultiBlockFileReader.start(definition, "nano_forge_main")
                    .where('C', blocks(GTNABlocks.RADIANT_NAQUADAH_ALLOY_CASING.get()))
                    .where('F', frames(GodforgeChainMaterials.StellarAlloy))
                    .where('B', blocks(GTNABlocks.RADIANT_NAQUADAH_ALLOY_CASING.get())
                            .or(abilities(PartAbility.IMPORT_ITEMS).setPreviewCount(1))
                            .or(abilities(PartAbility.IMPORT_FLUIDS).setPreviewCount(1))
                            .or(abilities(PartAbility.EXPORT_ITEMS).setPreviewCount(1))
                            .or(abilities(PartAbility.INPUT_ENERGY).setMaxGlobalLimited(2).setPreviewCount(1))
                            .or(abilities(PartAbility.INPUT_LASER).setMaxGlobalLimited(1).setPreviewCount(0))
                            .or(abilities(PartAbility.MAINTENANCE).setExactLimit(1)))
                    .where('~', controller(blocks(definition.getBlock())))
                    .where(' ', any())
                    .build())
            .workableCasingModel(GTNACORE.id("block/casings/radiant_naquadah_alloy_casing"),
                    com.gregtechceu.gtceu.GTCEu.id("block/multiblock/assembly_line"))
            .tooltips(Component.translatable("gtna.machine.nano_forge.tooltip.0"),
                    Component.translatable("gtna.machine.nano_forge.tooltip.1"),
                    Component.translatable("gtna.machine.nano_forge.tooltip.2"),
                    Component.translatable("gtna.machine.nano_forge.tooltip.3"),
                    Component.translatable("gtna.machine.nano_forge.tooltip.4"),
                    Component.translatable("gtna.machine.nano_forge.tooltip.5"),
                    Component.translatable("gtna.machine.nano_forge.tooltip.6"))
            .register();

    private GTNANanoForge() {}

    /** Nanites that unlock tiers 1, 2 and 3. */
    public static Item[] tierNanites() {
        return new Item[] { CARBON_NANITES.get(), NEUTRONIUM_NANITES.get(), TRANSCENDENT_METAL_NANITES.get() };
    }

    private static ItemEntry<ComponentItem> nanite(String id, String lang, int color) {
        return REGISTRATE.item(id + "_nanites", ComponentItem::create)
                .lang(lang + " Nanites")
                .color(() -> () -> (ItemColor) (stack, layer) -> layer == 0 ? color : -1)
                .model((ctx, provider) -> provider.generated(ctx, GTNACORE.id("item/nanites/generic"),
                        GTNACORE.id("item/nanites/generic_overlay")))
                .register();
    }

    private static ItemEntry<ComponentItem> custom(String id, String lang, boolean overlay) {
        ResourceLocation base = GTNACORE.id("item/nanites/" + id);
        return REGISTRATE.item(id + "_nanites", ComponentItem::create)
                .lang(lang + " Nanites")
                .model((ctx, provider) -> {
                    if (overlay) provider.generated(ctx, base, GTNACORE.id("item/nanites/" + id + "_overlay"));
                    else provider.generated(ctx, base);
                })
                .register();
    }

    public static void init() {
        var id = NANO_FORGE.getId();
        // Order matters: NanoForgeMachine counts consecutive formed extensions from index 0.
        GTNASubPatterns.register(id, def -> GTNAMultiBlockFileReader.start(def, "nano_forge_tier2")
                .where('C', blocks(GTNABlocks.RADIANT_NAQUADAH_ALLOY_CASING.get()))
                .where('A', blocks(GTBlocks.CASING_ASSEMBLY_LINE.get()))
                .where('~', controller(blocks(def.getBlock())))
                .where(' ', any())
                .build(), Component.translatable("gtna.machine.nano_forge.module", 2));
        GTNASubPatterns.register(id, def -> GTNAMultiBlockFileReader.start(def, "nano_forge_tier3")
                .where('C', blocks(GTNABlocks.RADIANT_NAQUADAH_ALLOY_CASING.get()))
                .where('A', blocks(GTBlocks.CASING_ASSEMBLY_LINE.get()))
                .where('F', frames(GodforgeChainMaterials.StellarAlloy))
                .where('~', controller(blocks(def.getBlock())))
                .where(' ', any())
                .build(), Component.translatable("gtna.machine.nano_forge.module", 3));
    }
}
