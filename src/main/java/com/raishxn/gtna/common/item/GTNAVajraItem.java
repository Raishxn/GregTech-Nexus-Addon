package com.raishxn.gtna.common.item;

import com.gregtechceu.gtceu.api.item.tool.GTToolItem;
import com.gregtechceu.gtceu.api.item.tool.IGTToolDefinition;
import com.gregtechceu.gtceu.api.item.tool.MaterialToolTier;
import com.gregtechceu.gtceu.api.item.tool.behavior.IToolBehavior;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gregtechceu.gtceu.common.data.item.GTToolActions; // Importe isso!
import com.raishxn.gtna.api.item.tool.GTNAToolType;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntMaps;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.ToolAction;
import net.minecraftforge.common.ToolActions;
import org.jetbrains.annotations.NotNull;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.List;

@ParametersAreNonnullByDefault
public class GTNAVajraItem extends GTToolItem {

    public GTNAVajraItem(Properties properties) {
        super(
                GTNAToolType.VAJRA,
                new MaterialToolTier(GTMaterials.Neutronium),
                GTMaterials.Neutronium,
                new IGTToolDefinition() {
                    @Override
                    public @NotNull List<IToolBehavior> getBehaviors() {
                        List<IToolBehavior> behaviors = new ArrayList<>();

                        behaviors.add(new IToolBehavior() {
                            // Behavior BRUTE: Desativa escudos
                            @Override
                            public boolean canDisableShield(ItemStack stack, ItemStack shield, LivingEntity entity, LivingEntity attacker) {
                                return true;
                            }

                            // AQUI ESTÁ A CORREÇÃO BASEADA NO SEU ARQUIVO GTToolActions
                            @Override
                            public boolean canPerformAction(ItemStack stack, ToolAction action) {
                                // 1. Ações Vanilla (Picareta, Machado, Pá, Enxada, Espada, Tesoura)
                                if (ToolActions.DEFAULT_PICKAXE_ACTIONS.contains(action)) return true;
                                if (ToolActions.DEFAULT_AXE_ACTIONS.contains(action)) return true;
                                if (ToolActions.DEFAULT_SHOVEL_ACTIONS.contains(action)) return true;
                                if (ToolActions.DEFAULT_HOE_ACTIONS.contains(action)) return true;
                                if (ToolActions.DEFAULT_SWORD_ACTIONS.contains(action)) return true;
                                if (ToolActions.DEFAULT_SHEARS_ACTIONS.contains(action)) return true;

                                // 2. Ações Específicas do GregTech (Wrench, Crowbar, etc.)
                                // Usamos os SETS definidos no arquivo GTToolActions
                                if (GTToolActions.DEFAULT_WRENCH_ACTIONS.contains(action)) return true;
                                if (GTToolActions.DEFAULT_WIRE_CUTTER_ACTIONS.contains(action)) return true;
                                if (GTToolActions.DEFAULT_CROWBAR_ACTIONS.contains(action)) return true;
                                if (GTToolActions.DEFAULT_SCREWDRIVER_ACTIONS.contains(action)) return true;
                                if (GTToolActions.DEFAULT_HAMMER_ACTIONS.contains(action)) return true;     // Hard Hammer
                                if (GTToolActions.DEFAULT_MALLET_ACTIONS.contains(action)) return true;     // Soft Mallet
                                if (GTToolActions.DEFAULT_SAW_ACTIONS.contains(action)) return true;
                                if (GTToolActions.DEFAULT_KNIFE_ACTIONS.contains(action)) return true;

                                // Nota: "File" e "Mortar" não possuem ToolActions específicas nesse arquivo.
                                // Eles funcionam principalmente via Tags de Item e isSuitableForCrafting.

                                return false;
                            }
                        });
                        return behaviors;
                    }

                    @Override
                    public boolean isToolEffective(BlockState state) {
                        return true;
                    }

                    @Override
                    public int getDamagePerAction(ItemStack stack) {
                        return 1;
                    }

                    @Override
                    public int getDamagePerCraftingAction(ItemStack stack) {
                        return 1;
                    }

                    @Override
                    public boolean isSuitableForBlockBreak(ItemStack stack) {
                        return true;
                    }

                    @Override
                    public boolean isSuitableForAttacking(ItemStack stack) {
                        return true;
                    }

                    // IMPORTANTE: Isso permite que a Vajra seja usada como Martelo/File/Mortar no Crafting Grid
                    @Override
                    public boolean isSuitableForCrafting(ItemStack stack) {
                        return true;
                    }

                    @Override
                    public boolean canApplyEnchantment(ItemStack stack, Enchantment enchantment) {
                        return true;
                    }

                    @Override
                    public @NotNull Object2IntMap<Enchantment> getDefaultEnchantments(ItemStack stack) {
                        return Object2IntMaps.emptyMap();
                    }

                    @Override
                    public boolean doesSneakBypassUse() {
                        return true;
                    }
                },
                properties
        );
    }

    @Override
    public boolean isCorrectToolForDrops(ItemStack stack, BlockState state) {
        return true;
    }
}