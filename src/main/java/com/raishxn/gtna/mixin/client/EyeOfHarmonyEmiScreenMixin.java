package com.raishxn.gtna.mixin.client;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import com.raishxn.gtna.GTNACORE;
import dev.emi.emi.config.EmiConfig;
import dev.emi.emi.screen.RecipeTab;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

/** Optional EMI: size only the selected EOH tab, restoring ordinary size on every other tab. */
@Pseudo
@Mixin(targets = "dev.emi.emi.screen.RecipeScreen", remap = false)
public abstract class EyeOfHarmonyEmiScreenMixin extends Screen {

    @Shadow
    private List<RecipeTab> tabs;
    @Shadow
    private int tab;
    @Shadow
    int backgroundHeight;
    @Shadow
    int y;

    protected EyeOfHarmonyEmiScreenMixin(Component title) {
        super(title);
    }

    @Inject(method = "setPage",
            at = @At(value = "INVOKE",
                     target = "Ldev/emi/emi/screen/RecipeTab;getPageCount()I",
                     ordinal = 0))
    private void gtna$sizeSelectedPlanetaryTab(int tabPage, int selectedTab, int page, CallbackInfo ci) {
        var selected = tabs.get(tab);
        int available = height - 52 - EmiConfig.verticalMargin;
        int wanted = EmiConfig.maximumRecipeScreenHeight;
        if (selected.category.getId().equals(GTNACORE.id("eye_of_harmony"))) {
            // EMI reserves 46 px for navigation and up to 23 px for bottom workstations.
            wanted = selected.getPage(0).stream().mapToInt(display -> display.recipe.getDisplayHeight() + 69)
                    .max().orElse(wanted);
        }
        int resized = Math.min(wanted, available);
        if (resized != backgroundHeight) {
            backgroundHeight = resized;
            y = (height - backgroundHeight) / 2 + 1;
            selected.bakePages(backgroundHeight);
        }
    }
}
