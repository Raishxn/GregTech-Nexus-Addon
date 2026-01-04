package com.raishxn.gtna.common.data;

import com.gregtechceu.gtceu.api.item.ComponentItem;
import com.raishxn.gtna.common.item.StructureDetectBehavior;
import com.tterrag.registrate.util.entry.ItemEntry;
import net.minecraft.resources.ResourceLocation;

import static com.gregtechceu.gtceu.common.data.GTItems.attach;
import static com.raishxn.gtna.api.registry.GTNARegistry.REGISTRATE;

public class GTNAItems {

    static {
        REGISTRATE.creativeModeTab(() -> GTNACreativeModeTabs.ITEMS);
    }

    // Ferramentas e Itens de Debug Existentes
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
    public static ItemEntry<ComponentItem> PRECISION_STEAM_COMPONENT;

    public static void init() {

        STRUCTURE_DETECT = REGISTRATE
                .item("structure_detect", ComponentItem::create)
                .lang("Structure Detector")
                .properties(stack -> stack.stacksTo(1))
                .onRegister(attach(StructureDetectBehavior.INSTANCE))
                .model((ctx, provider) -> {
                    provider.generated(ctx, new ResourceLocation("gtceu", "item/portable_scanner"));
                })
                .register();

        HYDRAULIC_MOTOR = REGISTRATE.item("hydraulic_motor", ComponentItem::create)
                .properties(stack -> stack.stacksTo(64))
                .lang("Hydraulic Motor")
                .register();
        HYDRAULIC_PISTON = REGISTRATE.item("hydraulic_piston", ComponentItem::create)
                .lang("Hydraulic Piston")
                .properties(stack -> stack.stacksTo(64))
                .register();
        HYDRAULIC_PUMP = REGISTRATE.item("hydraulic_pump", ComponentItem::create)
                .lang("Hydraulic Pump")
                .properties(stack -> stack.stacksTo(64))
                .register();
        HYDRAULIC_ARM = REGISTRATE.item("hydraulic_arm", ComponentItem::create)
                .lang("Hydraulic Arm")
                .properties(stack -> stack.stacksTo(64))
                .register();
        HYDRAULIC_CONVEYOR = REGISTRATE.item("hydraulic_conveyor", ComponentItem::create)
                .lang("Hydraulic Conveyor")
                .properties(stack -> stack.stacksTo(64))
                .register();
        HYDRAULIC_REGULATOR = REGISTRATE.item("hydraulic_regulator", ComponentItem::create)
                .lang("Hydraulic Regulator")
                .properties(stack -> stack.stacksTo(64))
                .register();
        HYDRAULIC_VAPOR_GENERATOR = REGISTRATE.item("hydraulic_vapor_generator", ComponentItem::create)
                .lang("Hydraulic Vapor Generator")
                .properties(stack -> stack.stacksTo(64))
                .register();
        HYDRAULIC_STEAM_JET_SPEWER = REGISTRATE.item("hydraulic_steam_jet_spewer", ComponentItem::create)
                .lang("Hydraulic Steam Jet Spewer")
                .properties(stack -> stack.stacksTo(64))
                .register();
        HYDRAULIC_STEAM_RECEIVER = REGISTRATE.item("hydraulic_steam_receiver", ComponentItem::create)
                .lang("Hydraulic Steam Receiver")
                .properties(stack -> stack.stacksTo(64))
                .register();
        PRECISION_STEAM_COMPONENT = REGISTRATE.item("precision_steam_component", ComponentItem::create)
                .lang("Precision Steam Component")
                .properties(stack -> stack.stacksTo(64))
                .register();

    }
}