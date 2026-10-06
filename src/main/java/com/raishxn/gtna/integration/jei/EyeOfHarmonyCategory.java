package com.raishxn.gtna.integration.jei;

import com.gregtechceu.gtceu.common.data.GTMaterials;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

import com.raishxn.gtna.common.data.GTNAEyeOfHarmonyContent;
import com.raishxn.gtna.common.data.GTNAMachines;
import com.raishxn.gtna.common.data.multiblock.EyeOfHarmonyDisplay.Page;
import com.raishxn.gtna.common.data.multiblock.EyeOfHarmonyDisplay.Product;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;

import java.util.Locale;

import static com.raishxn.gtna.client.EyeOfHarmonyRecipePresentation.exact;

/** All products are indexed by JEI; pages are portions of one program, not separate crafts. */
public final class EyeOfHarmonyCategory implements IRecipeCategory<Page> {

    public static final RecipeType<Page> TYPE = RecipeType.create("gtna", "eye_of_harmony", Page.class);
    private final IDrawable icon;
    private final int rows = com.raishxn.gtna.client.EyeOfHarmonyRecipePresentation.rowsForScreen();

    public int pageSize() {
        return rows * 9;
    }

    public EyeOfHarmonyCategory(IGuiHelper helper) {
        icon = helper.createDrawableIngredient(VanillaTypes.ITEM_STACK, GTNAMachines.EYE_OF_HARMONY.asStack());
    }

    @Override
    public RecipeType<Page> getRecipeType() {
        return TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("gtna.eoh.jei.title");
    }

    @Override
    public int getWidth() {
        return 180;
    }

    @Override
    public int getHeight() {
        return 144 + rows * 18;
    }

    @Override
    public IDrawable getIcon() {
        return icon;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, Page page, IFocusGroup focuses) {
        builder.addSlot(RecipeIngredientRole.CATALYST, 8, 4).setStandardSlotBackground()
                .addItemStack(GTNAEyeOfHarmonyContent.OVERWORLD_PLANET.asStack());
        var program = page.catalog().program();
        builder.addSlot(RecipeIngredientRole.INPUT, 134, 4).setStandardSlotBackground()
                .addFluidStack(GTMaterials.Hydrogen.getFluid(), program.hydrogen())
                .addRichTooltipCallback((slot, tooltip) -> tooltip
                        .add(Component.translatable("gtna.eoh.jei.amount_fluid", exact(program.hydrogen()))));
        builder.addSlot(RecipeIngredientRole.INPUT, 152, 4).setStandardSlotBackground()
                .addFluidStack(GTMaterials.Helium.getFluid(), program.helium())
                .addRichTooltipCallback((slot, tooltip) -> tooltip
                        .add(Component.translatable("gtna.eoh.jei.amount_fluid", exact(program.helium()))));
        for (int i = 0; i < page.products().size(); i++) {
            Product product = page.products().get(i);
            var slot = builder.addSlot(RecipeIngredientRole.OUTPUT, 8 + (i % 9) * 18, 32 + (i / 9) * 18)
                    .setStandardSlotBackground().setOverlay(new AmountOverlay(product.amount(), product.fluid()), 0, 0)
                    .addRichTooltipCallback((view, tooltip) -> tooltip.add(Component.translatable(
                            product.fluid() ? "gtna.eoh.jei.amount_fluid" : "gtna.eoh.jei.amount_item",
                            exact(product.amount()))));
            if (product.fluid()) {
                // Fill the fluid icon fully; exact long quantities remain in the custom tooltip.
                var fluid = FluidStack.loadFluidStackFromNBT(product.tag());
                slot.addFluidStack(fluid.getFluid(), product.amount())
                        .setFluidRenderer(product.amount(), false, 16, 16);
            } else slot.addItemStack(ItemStack.of(product.tag()));
        }
    }

    @Override
    public void draw(Page page, IRecipeSlotsView slots, GuiGraphics graphics, double mouseX, double mouseY) {
        com.raishxn.gtna.client.EyeOfHarmonyRecipePresentation.drawInfo(page, graphics);
    }

    private record AmountOverlay(long amount, boolean fluid) implements IDrawable {

        @Override
        public int getWidth() {
            return 16;
        }

        @Override
        public int getHeight() {
            return 16;
        }

        @Override
        public void draw(GuiGraphics graphics, int x, int y) {
            String label = fluid ? com.raishxn.gtna.client.EyeOfHarmonyRecipePresentation.compactBuckets(amount) :
                    amount >= 1_000_000_000 ? String.format(Locale.ROOT, "%.1fG", amount / 1e9) :
                            amount >= 1_000_000 ? String.format(Locale.ROOT, "%.1fM", amount / 1e6) :
                                    amount >= 1_000 ? String.format(Locale.ROOT, "%.1fk", amount / 1e3) :
                                            Long.toString(amount);
            var font = Minecraft.getInstance().font;
            graphics.pose().pushPose();
            graphics.pose().translate(x + 16, y + 12, 0);
            graphics.pose().scale(.5F, .5F, 1);
            graphics.drawString(font, label, -font.width(label), 0, 0xffffffff, true);
            graphics.pose().popPose();
        }
    }
}
