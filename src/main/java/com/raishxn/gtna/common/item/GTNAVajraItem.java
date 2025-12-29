package com.raishxn.gtna.common.item;

import com.gregtechceu.gtceu.api.item.tool.GTToolItem;
import com.gregtechceu.gtceu.api.item.tool.IGTToolDefinition; // Importamos a interface que você enviou
import com.gregtechceu.gtceu.api.item.tool.MaterialToolTier;
import com.gregtechceu.gtceu.api.item.tool.behavior.IToolBehavior;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.raishxn.gtna.api.item.tool.GTNAToolType;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntMaps;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

import javax.annotation.ParametersAreNonnullByDefault;
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
                        return List.of(); // Adicione comportamentos aqui se necessário
                    }

                    @Override
                    public boolean isToolEffective(BlockState state) {
                        return true; // Vajra é efetiva contra tudo
                    }

                    @Override
                    public int getDamagePerAction(ItemStack stack) {
                        return 1; // Custo de durabilidade por bloco
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

                    @Override
                    public boolean isSuitableForCrafting(ItemStack stack) {
                        return false;
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

                    // Você pode sobrescrever getBaseDamage, getAttackSpeed aqui se quiser valores fixos
                },
                properties
        );
    }

    @Override
    public boolean isCorrectToolForDrops(ItemStack stack, BlockState state) {
        return true;
    }
}