package com.raishxn.gtna.common.machine.trait;

import com.gregtechceu.gtceu.api.capability.IParallelHatch;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.capability.recipe.IRecipeCapabilityHolder;
import com.gregtechceu.gtceu.api.capability.recipe.ItemRecipeCapability;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.machine.feature.IRecipeLogicMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;
import com.gregtechceu.gtceu.api.recipe.ActionResult;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.RecipeHelper;
import com.gregtechceu.gtceu.api.recipe.content.Content;
import com.gregtechceu.gtceu.api.recipe.ingredient.SizedIngredient;
import com.raishxn.gtna.api.machine.IThreadModifierMachine;
import com.raishxn.gtna.api.machine.multiblock.ParallelMachine;
import com.raishxn.gtna.utils.GTNARecipeUtils;
import com.raishxn.gtna.utils.ThreadMultiplierStrategy;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

public class GTNAMultipleRecipesLogic extends RecipeLogic {

    private final List<GTNARecipeUtils.ActiveRecipe> activeRecipes = new ArrayList<>();

    public GTNAMultipleRecipesLogic(MetaMachine machine) {
        super((IRecipeLogicMachine) machine);
    }

    public int getActiveRecipeCount() {
        return activeRecipes.size();
    }

    public int getMaxThreads() {
        int threads = 1;
        // Verifica se a máquina implementa a interface de Thread Modifier (Thread Hatch)
        if (machine instanceof IThreadModifierMachine modifierMachine) {
            threads += modifierMachine.getAdditionalThread();
        }
        // Verifica modificadores definidos na definição da Multiblock (Hardcoded)
        if (machine instanceof MetaMachine metaMachine &&
                metaMachine.getDefinition() instanceof MultiblockMachineDefinition mbDefinition) {
            threads *= ThreadMultiplierStrategy.getAdditionalMultiplier(mbDefinition);
        }
        return Math.max(1, threads);
    }

    public int getMaxParallel() {
        if (machine instanceof ParallelMachine parallelMachine) {
            return parallelMachine.getMaxParallel();
        }
        if (machine instanceof WorkableElectricMultiblockMachine workable) {
            return workable.getParallelHatch()
                    .map(IParallelHatch::getCurrentParallel)
                    .orElse(1);
        }
        return 1;
    }

    @Override
    public void serverTick() {
        MetaMachine metaMachine = (MetaMachine) this.machine;
        if (metaMachine.getLevel() == null || metaMachine.getLevel().isClientSide) return;

        boolean visualChanged = false;

        // 1. Atualiza e remove receitas concluídas
        Iterator<GTNARecipeUtils.ActiveRecipe> iterator = activeRecipes.iterator();
        while (iterator.hasNext()) {
            GTNARecipeUtils.ActiveRecipe active = iterator.next();
            // active.update() retorna true se a receita terminou
            if (active.update()) {
                completeRecipe(active);
                iterator.remove();
                visualChanged = true;
            }
        }

        // 2. Verifica se a máquina está ligada (Soft Mallet / Controller)
        boolean isMachineEnabled = true;
        if (machine instanceof WorkableElectricMultiblockMachine workable) {
            isMachineEnabled = workable.isWorkingEnabled();
        }

        if (isMachineEnabled) {
            int maxThreads = getMaxThreads();

            // Só procura novas receitas se houver espaço nas threads
            if (activeRecipes.size() < maxThreads) {
                List<GTRecipe> possibleRecipes = new ArrayList<>();

                // Busca receitas compatíveis com os inputs atuais
                var recipeIterator = machine.getRecipeType().searchRecipe((IRecipeCapabilityHolder) machine, recipe -> true);

                // Limite de segurança para não travar o server iterando milhares de receitas
                int searchLimit = 30;
                while (recipeIterator.hasNext() && possibleRecipes.size() < searchLimit) {
                    GTRecipe r = recipeIterator.next();
                    if (r != null) possibleRecipes.add(r);
                }

                // Tenta iniciar receitas encontradas
                for (GTRecipe validRecipe : possibleRecipes) {
                    // Se já atingiu o limite de threads, para de procurar
                    if (activeRecipes.size() >= maxThreads) break;

                    // LÓGICA DE UNICIDADE:
                    // Impede que a mesma receita rode em duas threads diferentes.
                    // O Parallel Hatch deve cuidar da quantidade (x4, x16, etc).
                    // As Threads devem cuidar da variedade (Receita A + Receita B).
                    if (isRecipeAlreadyActive(validRecipe)) continue;

                    if (tryStartRecipe(validRecipe)) {
                        visualChanged = true;
                        // Nota: Não damos 'break' aqui pois queremos ver se cabem mais receitas de OUTROS tipos
                        // nas threads restantes.
                    }
                }
            }
        }
    }

    private boolean tryStartRecipe(GTRecipe recipe) {
        // Cast seguro pois RecipeLogic sempre está atrelado a uma IRecipeLogicMachine
        var logicMachine = (IRecipeLogicMachine) machine;

        // 1. Modifica a receita (Paralelo + Overclock)
        // Isso chama o getRecipeModifier da WorkableElectricMultipleRecipesMachine.
        // Graças à correção anterior, o 'modifiedRecipe' terá o paralelo ajustado à quantidade de itens reais.
        GTRecipe modifiedRecipe = logicMachine.fullModifyRecipe(recipe.copy());

        // Se falhou em modificar (ex: voltagem insuficiente), cancela.
        if (modifiedRecipe == null) return false;

        // 2. Valida se existem itens para a receita MODIFICADA
        // Ex: Se o modificador aplicou x4, o matchContents verificará se existem 4x inputs.
        if (!RecipeHelper.matchContents((IRecipeCapabilityHolder) machine, modifiedRecipe).isSuccess()) {
            return false;
        }

        // 3. Consome inputs e inicia
        ActionResult result = RecipeHelper.handleRecipeIO((IRecipeCapabilityHolder) machine, modifiedRecipe, IO.IN, this.getChanceCaches());

        if (result.isSuccess()) {
            // Cria a instância da receita ativa
            GTNARecipeUtils.ActiveRecipe active = new GTNARecipeUtils.ActiveRecipe(
                    modifiedRecipe,
                    modifiedRecipe.duration,
                    this.getChanceCaches()
            );
            this.activeRecipes.add(active);
            return true;
        }

        return false;
    }

    /**
     * Verifica se uma receita do mesmo tipo já está rodando em alguma thread.
     */
    private boolean isRecipeAlreadyActive(GTRecipe recipe) {
        if (recipe.id == null) return false; // Segurança para receitas dinâmicas sem ID

        for (GTNARecipeUtils.ActiveRecipe active : activeRecipes) {
            if (active.recipe.id != null && active.recipe.id.equals(recipe.id)) {
                return true;
            }
        }
        return false;
    }

    private void completeRecipe(GTNARecipeUtils.ActiveRecipe active) {
        if (active != null && active.recipe != null) {
            // Entrega os outputs
            RecipeHelper.handleRecipeIO((IRecipeCapabilityHolder) machine, active.recipe, IO.OUT, active.chanceCaches);
        }
    }

    // --- Métodos de Display (Visualização no WAILA/TOP/Holo) ---

    public List<Component> getRecipeDisplayInfo() {
        List<Component> infoList = new ArrayList<>();

        for (int i = 0; i < activeRecipes.size(); i++) {
            GTNARecipeUtils.ActiveRecipe active = activeRecipes.get(i);

            int prog = active.progress;
            int max = active.maxProgress;
            float currentSec = prog / 20.0f;
            float maxSec = max / 20.0f;
            int percentage = max > 0 ? (int)((prog / (float)max) * 100) : 0;

            ChatFormatting percentColor = percentage < 33 ? ChatFormatting.RED : (percentage < 66 ? ChatFormatting.YELLOW : ChatFormatting.GREEN);

            MutableComponent line1 = Component.literal("Thread " + (i + 1) + ": ")
                    .withStyle(ChatFormatting.GOLD)
                    .append(Component.literal(String.format(Locale.US, "%.1fs / %.1fs ", currentSec, maxSec))
                            .withStyle(ChatFormatting.WHITE))
                    .append(Component.literal(String.format("(%d%%)", percentage))
                            .withStyle(percentColor));

            infoList.add(line1);

            // Lógica para pegar o nome do Output principal para exibição
            String outputName = "Unknown";
            int totalCount = 1;

            if (active.recipe.outputs.containsKey(ItemRecipeCapability.CAP)) {
                List<Content> itemOutputs = active.recipe.outputs.get(ItemRecipeCapability.CAP);
                if (itemOutputs != null && !itemOutputs.isEmpty()) {
                    Content content = itemOutputs.get(0);
                    Object inner = content.getContent();

                    if (inner instanceof ItemStack stack) {
                        outputName = stack.getHoverName().getString();
                        totalCount = stack.getCount();
                    } else if (inner instanceof SizedIngredient sized) {
                        ItemStack[] stacks = sized.getItems();
                        if (stacks.length > 0) outputName = stacks[0].getHoverName().getString();
                        totalCount = sized.getAmount();
                    } else if (inner instanceof Ingredient ing) {
                        ItemStack[] stacks = ing.getItems();
                        if (stacks.length > 0) outputName = stacks[0].getHoverName().getString();
                    }
                }
            }

            // Calcula o tempo por item (para mostrar eficiência do paralelo)
            double timePerItem = (maxSec > 0 && totalCount > 0) ? (maxSec / totalCount) : maxSec;

            String displayName = outputName;
            int maxLength = 20; // Aumentei um pouco para caber nomes maiores
            if (displayName.length() > maxLength) {
                displayName = displayName.substring(0, maxLength) + "...";
            }

            MutableComponent line2 = Component.literal(" -> ")
                    .withStyle(ChatFormatting.DARK_GRAY)
                    .append(Component.literal(displayName)
                            .withStyle(Style.EMPTY.withColor(ChatFormatting.LIGHT_PURPLE).withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.literal(outputName)))))
                    .append(Component.literal(" x" + totalCount)
                            .withStyle(ChatFormatting.AQUA))
                    .append(Component.literal(String.format(Locale.US, " (%.2fs/item)", timePerItem))
                            .withStyle(ChatFormatting.GRAY));

            infoList.add(line2);
        }
        return infoList;
    }

    @Override
    public void saveCustomPersistedData(@NotNull CompoundTag tag, boolean forDrop) {
        super.saveCustomPersistedData(tag, forDrop);
        // Salva progresso visual apenas.
        // Nota: Restaurar receitas completas em GTCEu requer serialização complexa.
        // Em reload, as receitas ativas serão perdidas (mas inputs consumidos).
        tag.putInt("ActiveRecipeCount", activeRecipes.size());
        for (int i = 0; i < activeRecipes.size(); i++) {
            GTNARecipeUtils.ActiveRecipe recipe = activeRecipes.get(i);
            tag.putInt("RProg" + i, recipe.progress);
            tag.putInt("RMax" + i, recipe.maxProgress);
        }
    }

    @Override
    public void loadCustomPersistedData(@NotNull CompoundTag tag) {
        super.loadCustomPersistedData(tag);
        // Limpa receitas ao carregar para evitar inconsistência.
        // A máquina precisará pegar os inputs e começar de novo.
        activeRecipes.clear();
    }
}