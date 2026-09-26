package com.raishxn.gtna.common.data;

import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

/** Original GTOCore story and functional lines for the multiblocks ported in GTNAMachines3. */
final class GTNAGTOTooltips {

    private GTNAGTOTooltips() {}

    static boolean handles(String path) {
        return switch (path) {
            case "brick_kiln", "thermal_power_pump", "liquefaction_furnace", "generator_array", "fishing_ground", "evaporation_plant", "greenhouse", "component_assembler", "component_assembly_line", "large_greenhouse", "cold_ice_freezer", "chemical_plant", "mega_alloy_blast_smelter", "isa_mill", "rocket_large_turbine", "supercritical_steam_turbine", "industrial_flotation_cell", "vacuum_drying_furnace" -> true;
            default -> false;
        };
    }

    static boolean append(MultiblockMachineDefinition definition, List<Component> lines) {
        switch (definition.getId().getPath()) {
            case "brick_kiln" -> {
                section(lines, "Main Function");
                item(lines, "Fires bricks and ceramics from compressed clay and coal");
                item(lines, "Runs without an energy hatch");
            }
            case "thermal_power_pump" -> {
                section(lines, "Main Function");
                item(lines, "Condenses steam into water according to the surrounding biome");
                item(lines, "Oceans and rivers produce the most; the Nether produces nothing");
                item(lines, "Rain increases output by 50%");
            }
            case "liquefaction_furnace" -> {
                section(lines, "Running Requirements");
                item(lines, "Melts items into fluids when the coils meet the recipe temperature");
                item(lines, "The stainless-steel extension tower accepts Parallel and Accelerate Hatches");
            }
            case "generator_array" -> {
                section(lines, "Main Function");
                item(lines, "Runs up to four generators of the same type and tier");
                item(lines, "Generation scales by 1.3x per installed generator; fuel use scales with output");
                item(lines, "Supported generators: Steam Turbine, Gas Turbine, Combustion Generator");
                item(lines, "Wireless output sends energy to the owner's Nexus Flux Matrix with 5% loss");
            }
            case "fishing_ground" -> {
                story(lines,
                        "Like eating fish?",
                        "AFFL-200 intelligent large fishing farm§r is a regular on GregTech cuisine series",
                        "Powerful §eintelligent breeding system§r brings powerful productivity",
                        "Can meet the entire branch office employees' §aaquatic food consumption needs");
                section(lines, "Set the circuit to enable automatic fishing");
                item(lines, "Circuit 1: random fishing");
                item(lines, "Circuit 2: catch fish");
                item(lines, "Circuit 3: catch junk");
                item(lines, "Circuit 4: catch treasure");
            }
            case "evaporation_plant" -> {
                section(lines, "Main Function");
                item(lines, "Evaporates brine and other fluids in up to five tower stages");
                section(lines, "Running Requirements");
                item(lines, "The base requires one non-steam fluid input; each stage has a fluid output");
                item(lines,
                        "The auxiliary tower accepts Parallel and Accelerate Hatches in its stainless evaporation casings or exposed titanium shell");
                item(lines, "An IV Parallel Hatch provides 4 parallel recipes");
            }
            case "greenhouse" -> {
                section(lines, "Running Requirements");
                item(lines, "Requires sunlight to operate");
                item(lines, "Speed slows down when sunlight is insufficient");
            }
            case "component_assembler" -> {
                story(lines,
                        "GTO Group's thoughtful gift for rookie employees, making assembly as easy as building blocks",
                        "Supports up to IV tier recipes, effectively reducing early-stage component processing costs",
                        "Friendly reminder from the chairman: Treat it well, your first salary might not even cover one of its parts");
                section(lines, "Running Requirements");
                item(lines, "Can only run IV-tier and below recipes");
                item(lines,
                        "After upgrading the structure, it supports tier UV, and allows installing Accelerate Hatches");
                item(lines, "After upgrading the structure again, it supports Parallel Control Hatch and Laser Hatch");
            }
            case "component_assembly_line" -> {
                lines.add(Component.literal(
                        "Culmination of GTO Cosmic GregTech's years of effort, milestone in industrial automation")
                        .withStyle(ChatFormatting.GOLD));
                lines.add(Component.literal(
                        "Integrating complex component assembly into perfectly synchronized production line artistry")
                        .withStyle(ChatFormatting.GRAY));
                lines.add(Component
                        .literal("Beneath iridium shells, thousands of precision servo motors dance in perfect harmony")
                        .withStyle(ChatFormatting.AQUA));
                lines.add(Component.literal(
                        "Though construction costs are staggering, the efficiency gains make it worth every penny")
                        .withStyle(ChatFormatting.YELLOW));
                lines.add(
                        Component.literal("From screws to chips, all basic components can be efficiently produced here")
                                .withStyle(ChatFormatting.GREEN));
                lines.add(Component
                        .literal("The chairman once said: This is industry's closest approximation to divine creation")
                        .withStyle(ChatFormatting.GOLD));
                lines.add(Component.literal("Maintenance costs are equally high, but a necessary burden for the future")
                        .withStyle(ChatFormatting.RED));
                lines.add(Component.literal("Ushering in a new era of basic component manufacturing")
                        .withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD));
                section(lines, "Running Requirements");
                item(lines, "All component assembly casings must have the same tier");
                item(lines, "The casing tier limits which recipes can start");
                item(lines, "A Parallel Hatch sets the batch size");
            }
            case "large_greenhouse" -> {
                section(lines, "Running Requirements");
                item(lines, "Can cultivate trees and general crops");
                item(lines, "Can operate without sunlight");
                item(lines, "Select the tree or crop recipe mode at the controller");
            }
            case "cold_ice_freezer" -> {
                story(lines,
                        "A liquid ice leak accident unexpectedly led to the birth of this cryogenic device",
                        "Liquid ice seems to have reacted strangely with the aluminum frame of the cryo control room, apparently creating a stable superconducting environment",
                        "Tungsten steel pipes' high thermal conductivity creates vortex circulation, freezing 64 samples in one rotation cycle",
                        "The employee responsible for the accident has disappeared, but the chairman treasures this machine like a precious jewel");
                section(lines, "Running Requirements");
                item(lines, "Requires to provide §b 2^(Voltage tier - 2 ) * 10mb/s§r of §6Liquid Ice§r");
                info(lines, "Consumes Liquid Ice once immediately when the recipe starts");
                multiplier(lines, "Time Cost Multiply", "0.5");
                parallel(lines, 64);
            }
            case "chemical_plant" -> {
                story(lines,
                        "In the world of chemistry, every molecule tells its own story",
                        "The chairman holds an electron microscope, attempting to unravel the secrets of elements",
                        "But his subordinates know he's just posing for the camera");
                section(lines, "Coil Efficiency Bonus");
                item(lines, "Each coil tier above Cupronickel, Reduces energy consumption and duration by 5%");
                section(lines, "Running Requirements");
                item(lines, "Parallel Control Hatch sets the batch size");
                item(lines, "Perfect overclock: duration is divided by 4 per voltage tier");
            }
            case "mega_alloy_blast_smelter" -> {
                section(lines, "Running Requirements");
                item(lines, "Runs Alloy Blast Smelter recipes with heating coils and a Parallel Hatch");
                multiplier(lines, "Energy Cost Multiply", "0.8");
                multiplier(lines, "Time Cost Multiply", "0.6");
            }
            case "isa_mill" -> {
                story(lines,
                        "This Isa 1672N grinder, despite its green appearance, is actually not eco-friendly at all",
                        "The exhaust fan always emits strange odors, and few employees want to touch the messy output",
                        "It violently crushes all ores through wet ball milling, but its efficiency is surprisingly high",
                        "Lab scientists have to admit while holding their noses: sometimes brute force does solve problems");
                section(lines, "Running Requirements");
                item(lines, "Requires a Grinding Ball Hatch with the ball tier specified by the recipe");
                item(lines, "Soapstone processes circuit 1 recipes; Aluminium processes circuit 10 recipes");
                info(lines, "Each started recipe consumes grinding ball durability");
                parallel(lines, 2);
                item(lines, "Perfect overclock: duration is divided by 4 per voltage tier");
            }
            case "rocket_large_turbine" -> {
                section(lines, "Main Function");
                item(lines, "Base Production EUt: 5120");
                item(lines, "Each Rotor Holder tier above EV adds 10% efficiency and multiplies EU/t by 2");
                item(lines, "High-Speed Mode triples output and increases rotor wear tenfold");
                item(lines, "The rocket engine module doubles output and adds 20% efficiency");
            }
            case "supercritical_steam_turbine" -> {
                section(lines, "Power Generation Efficiency");
                item(lines, "Base Production EUt: 16384");
                item(lines, "Each Rotor Holder tier above IV adds 10% efficiency and multiplies EU/t by 2");
                section(lines, "After Module Installation");
                item(lines, "Gains 2x speed");
                item(lines, "Gains additional 20% turbine efficiency");
                item(lines, "Rotor wear rate becomes 2x");
                section(lines, "High-Speed Mode");
                item(lines, "High-speed mode can further increase operating speed, multiplied with modules");
            }
            case "industrial_flotation_cell" -> {
                story(lines,
                        "As a general purification process this Isa U-276 ore separator has become quite mature",
                        "Using pine oil flotation the workshop always smells like essential balm",
                        "Regardless working here does mean fewer mosquito bites");
                lines.add(Component.literal("Tips: Safe production, alarm ringing!").withStyle(ChatFormatting.RED));
                lines.add(Component.literal("Industrial Flotation Mining Pool")
                        .withStyle(ChatFormatting.GOLD));
                section(lines, "Running Requirements");
                item(lines, "Consumes Milled ore, ethylxanthate and turpentine to produce matching ore foam");
                item(lines, "Parallel Control Hatch sets the batch size");
                item(lines, "Perfect overclock: duration is divided by 4 per voltage tier");
            }
            case "vacuum_drying_furnace" -> {
                section(lines, "Main Function");
                item(lines, "Vacuum Drying returns flotation ore foams to dusts, Red Mud and Water");
                section(lines, "Running Requirements");
                item(lines, "Heating coil temperature must meet the Vacuum Drying recipe requirement");
                item(lines, "Select Dehydrator mode to run the separate Dehydrator recipe family");
                item(lines, "Dehydrator parallel doubles every 900K of coil temperature; Vacuum Drying stays serial");
            }
            default -> {
                return false;
            }
        }
        recipeTypes(definition, lines);
        lines.add(GTNASources.line(GTNASources.GTO));
        return true;
    }

    private static void story(List<Component> lines, String... text) {
        for (String value : text) {
            lines.add(Component.literal(value).withStyle(ChatFormatting.GRAY));
        }
    }

    private static void section(List<Component> lines, String text) {
        lines.add(Component.literal("- " + text).withStyle(ChatFormatting.GOLD));
    }

    private static void item(List<Component> lines, String text) {
        lines.add(Component.literal("∘ " + text).withStyle(ChatFormatting.YELLOW));
    }

    private static void info(List<Component> lines, String text) {
        lines.add(Component.literal("# " + text).withStyle(ChatFormatting.GRAY));
    }

    private static void multiplier(List<Component> lines, String label, String value) {
        lines.add(Component.literal("- " + label + " : " + value).withStyle(ChatFormatting.BLUE));
    }

    private static void parallel(List<Component> lines, int amount) {
        lines.add(Component.literal("- Parallel Number : " + amount).withStyle(ChatFormatting.GREEN));
    }

    private static void recipeTypes(MultiblockMachineDefinition definition, List<Component> lines) {
        var types = definition.getRecipeTypes();
        if (types.length == 0) return;
        MutableComponent line = Component.literal("- Recipes Type : ").withStyle(ChatFormatting.YELLOW);
        for (int i = 0; i < types.length; i++) {
            if (i > 0) line.append(Component.literal(", ").withStyle(ChatFormatting.WHITE));
            ResourceLocation id = types[i].registryName;
            line.append(Component.translatable(id.getNamespace() + "." + id.getPath())
                    .withStyle(ChatFormatting.WHITE));
        }
        lines.add(line);
    }
}
