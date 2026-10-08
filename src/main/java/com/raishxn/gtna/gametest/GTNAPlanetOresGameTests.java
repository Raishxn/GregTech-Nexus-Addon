package com.raishxn.gtna.gametest;

import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.PropertyKey;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.common.data.GTMaterialBlocks;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import com.raishxn.gtna.GTNACORE;
import com.raishxn.gtna.common.data.worldgen.GTNAPlanetOres;
import com.raishxn.gtna.common.data.worldgen.GTNAPlanetOres.Planet;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@PrefixGameTestTemplate(false)
@GameTestHolder("gtna")
public final class GTNAPlanetOresGameTests {

    private GTNAPlanetOresGameTests() {}

    /**
     * Every planet has veins, every vein material has an ore block in that planet's stone, the stones are in the
     * replaceable tag, and a placed planet stone resolves to its own ore prefix.
     */
    @GameTest(template = "empty_12", timeoutTicks = 20)
    public static void planetVeinsHaveOreBlocksOnTheirStones(GameTestHelper helper) {
        if (!GTNAPlanetOres.enabled()) {
            helper.fail("Ad Astra must be loaded in the game test run");
            return;
        }
        Map<Planet, Integer> veins = new EnumMap<>(Planet.class);
        List<String> problems = new ArrayList<>();
        for (var entry : GTRegistries.ORE_VEINS.entries()) {
            var id = entry.getKey();
            if (!GTNACORE.MOD_ID.equals(id.getNamespace()) || !id.getPath().startsWith("planet/")) continue;
            var vein = entry.getValue();
            for (Planet planet : Planet.values()) {
                if (!vein.dimensionFilter().contains(planet.dimension)) continue;
                veins.merge(planet, 1, Integer::sum);
                for (var material : vein.veinGenerator().getAllEntries()) {
                    Material mat = material.vein().right().orElse(null);
                    if (mat == null || !mat.hasProperty(PropertyKey.ORE)) continue;
                    if (GTMaterialBlocks.MATERIAL_BLOCKS.get(planet.orePrefix(), mat) == null)
                        problems.add(id.getPath() + ": no " + planet.id + " ore for " + mat.getName());
                }
            }
        }
        for (Planet planet : Planet.values()) {
            if (veins.getOrDefault(planet, 0) == 0) problems.add(planet.id + " has no vein");
            var stone = BuiltInRegistries.BLOCK.get(planet.stone()).defaultBlockState();
            if (!stone.is(GTNAPlanetOres.PLANET_STONES)) problems.add(planet.stone() + " is not in the stone tag");
            var prefix = ChemicalHelper.getOrePrefix(stone).orElse(null);
            if (prefix != planet.orePrefix()) problems.add(planet.stone() + " resolves to prefix " + prefix);
        }
        helper.assertTrue(problems.isEmpty(), String.join("; ", problems));
        GTNACORE.LOGGER.info("Planet veins: {}", veins);
        helper.succeed();
    }
}
