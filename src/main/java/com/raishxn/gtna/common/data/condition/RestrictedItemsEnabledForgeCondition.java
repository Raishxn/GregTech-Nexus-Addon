package com.raishxn.gtna.common.data.condition;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.common.crafting.conditions.ICondition;
import net.minecraftforge.common.crafting.conditions.IConditionSerializer;

import com.google.gson.JsonObject;
import com.raishxn.gtna.GTNACORE;
import com.raishxn.gtna.config.ConfigHolder;

public class RestrictedItemsEnabledForgeCondition implements ICondition {

    private static final ResourceLocation ID = GTNACORE.id("restricted_items_enabled");
    public static final RestrictedItemsEnabledForgeCondition INSTANCE = new RestrictedItemsEnabledForgeCondition();

    private RestrictedItemsEnabledForgeCondition() {}

    @Override
    public ResourceLocation getID() {
        return ID;
    }

    @Override
    public boolean test(IContext context) {
        return ConfigHolder.areRestrictedRecipesEnabled();
    }

    public static void register() {
        CraftingHelper.register(Serializer.INSTANCE);
    }

    public static class Serializer implements IConditionSerializer<RestrictedItemsEnabledForgeCondition> {

        public static final Serializer INSTANCE = new Serializer();

        @Override
        public void write(JsonObject json, RestrictedItemsEnabledForgeCondition value) {}

        @Override
        public RestrictedItemsEnabledForgeCondition read(JsonObject json) {
            return INSTANCE_VALUE();
        }

        @Override
        public ResourceLocation getID() {
            return ID;
        }

        private RestrictedItemsEnabledForgeCondition INSTANCE_VALUE() {
            return RestrictedItemsEnabledForgeCondition.INSTANCE;
        }
    }
}
