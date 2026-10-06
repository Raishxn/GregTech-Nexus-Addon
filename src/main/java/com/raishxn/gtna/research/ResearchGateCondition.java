package com.raishxn.gtna.research;

import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.RecipeCondition;
import com.gregtechceu.gtceu.api.recipe.condition.RecipeConditionType;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.raishxn.gtna.data.recipe.GTNARecipeConditions;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * Shows, in the recipe viewer, that a recipe needs a research node. The server decides with
 * {@code RecipeHelperMixin} by recipe id; this condition is added to the client's copy of the recipe so
 * players see why a recipe is locked (and when it is not). Evaluated on a server it agrees with the mixin.
 */
public class ResearchGateCondition extends RecipeCondition<ResearchGateCondition> {

    // spotless:off
    public static final Codec<ResearchGateCondition> CODEC = RecordCodecBuilder.create(instance -> RecipeCondition.isReverse(instance).and(
            ResourceLocation.CODEC.fieldOf("node").forGetter(ResearchGateCondition::node)
    ).apply(instance, ResearchGateCondition::new));
    // spotless:on

    private final ResourceLocation node;

    public ResearchGateCondition() {
        this(new ResourceLocation("gtna", "unset"));
    }

    public ResearchGateCondition(ResourceLocation node) {
        this(false, node);
    }

    public ResearchGateCondition(boolean isReverse, ResourceLocation node) {
        super(isReverse);
        this.node = node;
    }

    public ResourceLocation node() {
        return node;
    }

    /**
     * The conditions a recipe viewer should list for a recipe: the recipe's own plus one research line per
     * node that gates it. Never changes the recipe, which can be shared with the server and may hold an
     * immutable list.
     */
    public static List<RecipeCondition<?>> withResearch(List<RecipeCondition<?>> own, ResourceLocation recipeId) {
        if (recipeId == null) return own;
        List<ResourceLocation> gates = ClientKnowledge.gatesFor(recipeId);
        if (gates.isEmpty()) return own;
        List<RecipeCondition<?>> shown = new ArrayList<>(own);
        gates.forEach(node -> shown.add(new ResearchGateCondition(node)));
        return shown;
    }

    @Override
    public RecipeConditionType<ResearchGateCondition> getType() {
        return GTNARecipeConditions.RESEARCH_GATE;
    }

    /** Longest node name shown in the recipe viewer; longer names run under the machine icon. */
    static final int VIEWER_NAME_LIMIT = 24;

    /**
     * The recipe viewer line, kept short and coloured so it reads at a glance: red "Research: X" while the
     * node is locked, green once unlocked. The viewer prints this as a plain string, so the colour is a
     * formatting code inside the text rather than a component style.
     */
    @Override
    public Component getTooltips() {
        String name = KnowledgeNode.displayName(node).getString();
        if (name.length() > VIEWER_NAME_LIMIT) name = name.substring(0, VIEWER_NAME_LIMIT - 1) + "\u2026";
        boolean unlocked = ClientKnowledge.isUnlocked(node);
        String line = Component.translatable("gtna.research.viewer", name).getString();
        return Component.literal((unlocked ? "\u00a7a" : "\u00a7c") + line);
    }

    @Override
    protected boolean testCondition(@NotNull GTRecipe recipe, @NotNull RecipeLogic recipeLogic) {
        var machine = recipeLogic.getMachine();
        if (!(machine.getLevel() instanceof ServerLevel level)) return true;
        var owner = machine.getOwnerUUID();
        return owner != null && KnowledgeService.isUnlocked(level.getServer(), KnowledgeScope.of(owner), node);
    }

    @Override
    public ResearchGateCondition createTemplate() {
        return new ResearchGateCondition();
    }
}
