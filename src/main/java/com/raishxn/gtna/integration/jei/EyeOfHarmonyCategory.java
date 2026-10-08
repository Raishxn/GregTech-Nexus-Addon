package com.raishxn.gtna.integration.jei;

import com.gregtechceu.gtceu.common.data.GTMaterials;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

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

/**
 * Same layout as the EMI category: hydrogen, planet and helium on top, a 9-wide item grid, a separate fluid grid
 * below it, then the program information. Pages are portions of one program, not separate crafts.
 */
public final class EyeOfHarmonyCategory implements IRecipeCategory<Page> {

    public static final RecipeType<Page> TYPE = RecipeType.create("gtna", "eye_of_harmony", Page.class);
    private final IDrawable icon;
    static final int FLUID_ROWS = 2;
    /** Eight info lines plus a two-line warning. */
    private static final int INFO_HEIGHT = 106;
    /** Full EMI-like page: 9 item rows; {@code JeiRecipeGuiHeightMixin} grows JEI's window to fit it. */
    private final int itemRows = 9;
    public static final int FULL_HEIGHT = 52 + (9 + FLUID_ROWS) * 18 + INFO_HEIGHT;

    public int itemRows() {
        return itemRows;
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
        return 198;
    }

    @Override
    public int getHeight() {
        // Gas row, page line, both grids, then the info lines and the warning: fits inside JEI's window.
        return 52 + (itemRows + FLUID_ROWS) * 18 + INFO_HEIGHT;
    }

    @Override
    public IDrawable getIcon() {
        return icon;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, Page page, IFocusGroup focuses) {
        var program = page.catalog().program();
        builder.addSlot(RecipeIngredientRole.INPUT, 18, 3).setStandardSlotBackground()
                .addFluidStack(GTMaterials.Hydrogen.getFluid(), program.hydrogen())
                .addRichTooltipCallback((slot, tooltip) -> tooltip
                        .add(Component.translatable("gtna.eoh.jei.amount_fluid", exact(program.hydrogen()))));
        builder.addSlot(RecipeIngredientRole.CATALYST, 90, 3).setStandardSlotBackground()
                .addItemStack(page.catalog().definition().planetStack());
        builder.addSlot(RecipeIngredientRole.INPUT, 162, 3).setStandardSlotBackground()
                .addFluidStack(GTMaterials.Helium.getFluid(), program.helium())
                .addRichTooltipCallback((slot, tooltip) -> tooltip
                        .add(Component.translatable("gtna.eoh.jei.amount_fluid", exact(program.helium()))));
        int item = 0, fluidIndex = 0;
        for (Product product : page.products()) {
            int index = product.fluid() ? fluidIndex++ : item++;
            int y = product.fluid() ? 46 + itemRows * 18 + index / 9 * 18 : 40 + index / 9 * 18;
            var slot = builder.addSlot(RecipeIngredientRole.OUTPUT, 18 + index % 9 * 18, y)
                    .setStandardSlotBackground().setOverlay(new AmountOverlay(product.amount(), product.fluid()), 0, 0)
                    .addRichTooltipCallback((view, tooltip) -> tooltip.add(Component.translatable(
                            product.fluid() ? "gtna.eoh.jei.amount_fluid" : "gtna.eoh.jei.amount_item",
                            exact(product.amount()))));
            if (product.fluid()) {
                // Fill the fluid icon fully; exact long quantities remain in the custom tooltip.
                var fluid = FluidStack.loadFluidStackFromNBT(product.tag());
                // Billions of mB overflow JEI's fill ratio (a thin strip); draw a full slot.
                slot.addFluidStack(fluid.getFluid(), 1000).setFluidRenderer(1000, false, 16, 16);
            } else slot.addItemStack(ItemStack.of(product.tag()));
        }
        // Empty slots keep both grids visible, as in the EMI layout.
        for (int i = item; i < itemRows * 9; i++)
            builder.addSlot(RecipeIngredientRole.RENDER_ONLY, 18 + i % 9 * 18, 40 + i / 9 * 18)
                    .setStandardSlotBackground();
        for (int i = fluidIndex; i < FLUID_ROWS * 9; i++)
            builder.addSlot(RecipeIngredientRole.RENDER_ONLY, 18 + i % 9 * 18, 46 + itemRows * 18 + i / 9 * 18)
                    .setStandardSlotBackground();
    }

    @Override
    public void draw(Page page, IRecipeSlotsView slots, GuiGraphics graphics, double mouseX, double mouseY) {
        com.raishxn.gtna.client.EyeOfHarmonyRecipePresentation.drawEmiInfo(page, graphics);
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
