package com.raishxn.gtna.common.data;

import com.gregtechceu.gtceu.api.item.ComponentItem;
import com.raishxn.gtna.common.item.GTNAVajraItem;
import com.raishxn.gtna.common.item.StructureDetectBehavior;
import com.raishxn.gtna.common.item.StructureWriteBehavior;
import com.tterrag.registrate.util.entry.ItemEntry;
import com.tterrag.registrate.util.nullness.NonNullBiConsumer;
import net.minecraft.resources.ResourceLocation;

import static com.gregtechceu.gtceu.common.data.GTItems.attach;
import static com.raishxn.gtna.api.registry.GTNARegistry.REGISTRATE;

public class GTNAItems {

    static {
        REGISTRATE.creativeModeTab(() -> GTNACreativeModeTabs.ITEMS);
    }

    // Ferramentas e Itens de Debug Existentes
    public static ItemEntry<GTNAVajraItem> VAJRA;
    public static ItemEntry<ComponentItem> DEBUG_STRUCTURE_WRITER;
    public static ItemEntry<ComponentItem> STRUCTURE_DETECT;

    // --- NOVOS COMPONENTES HIDRÁULICOS ---
    public static ItemEntry<ComponentItem> HYDRAULIC_MOTOR;
    public static ItemEntry<ComponentItem> HYDRAULIC_PISTON;
    public static ItemEntry<ComponentItem> HYDRAULIC_PUMP;
    public static ItemEntry<ComponentItem> HYDRAULIC_ARM;
    public static ItemEntry<ComponentItem> HYDRAULIC_CONVEYOR;
    public static ItemEntry<ComponentItem> HYDRAULIC_REGULATOR;
    public static ItemEntry<ComponentItem> HYDRAULIC_VAPOR_GENERATOR;
    public static ItemEntry<ComponentItem> HYDRAULIC_STEAM_JET_SPEWER;
    public static ItemEntry<ComponentItem> HYDRAULIC_STEAM_RECEIVER;

    public static void init() {

        // --- Items Originais ---

        /*DEBUG_STRUCTURE_WRITER = REGISTRATE
                .item("debug_structure_writer", ComponentItem::create)
                .lang("Debug Structure Writer")
                .onRegister(attach(StructureWriteBehavior.INSTANCE))
                .model(NonNullBiConsumer.noop())
                .register();*/

        STRUCTURE_DETECT = REGISTRATE
                .item("structure_detect", ComponentItem::create)
                .lang("Structure Detector")
                .properties(stack -> stack.stacksTo(1))
                .onRegister(attach(StructureDetectBehavior.INSTANCE))
                .model((ctx, provider) -> {
                    provider.generated(ctx, new ResourceLocation("gtceu", "item/portable_scanner"));
                })
                .register();

        // --- Registros dos Novos Componentes ---

        HYDRAULIC_MOTOR = REGISTRATE.item("hydraulic_motor", ComponentItem::create)
                .lang("Hydraulic Motor")
                .register();

        HYDRAULIC_PISTON = REGISTRATE.item("hydraulic_piston", ComponentItem::create)
                .lang("Hydraulic Piston")
                .register();

        HYDRAULIC_PUMP = REGISTRATE.item("hydraulic_pump", ComponentItem::create)
                .lang("Hydraulic Pump") // (podendo trasferir 1000000L)
                .register();

        HYDRAULIC_ARM = REGISTRATE.item("hydraulic_arm", ComponentItem::create)
                .lang("Hydraulic Arm")
                .register();

        HYDRAULIC_CONVEYOR = REGISTRATE.item("hydraulic_conveyor", ComponentItem::create)
                .lang("Hydraulic Conveyor") // (podendo transferir 16 stacks)
                .register();

        HYDRAULIC_REGULATOR = REGISTRATE.item("hydraulic_regulator", ComponentItem::create)
                .lang("Hydraulic Regulator") // (transferindo 20000000L)
                .register();

        HYDRAULIC_VAPOR_GENERATOR = REGISTRATE.item("hydraulic_vapor_generator", ComponentItem::create)
                .lang("Hydraulic Vapor Generator")
                .register();

        HYDRAULIC_STEAM_JET_SPEWER = REGISTRATE.item("hydraulic_steam_jet_spewer", ComponentItem::create)
                .lang("Hydraulic Steam Jet Spewer")
                .register();

        HYDRAULIC_STEAM_RECEIVER = REGISTRATE.item("hydraulic_steam_receiver", ComponentItem::create)
                .lang("Hydraulic Steam Receiver")
                .register();
    }
}