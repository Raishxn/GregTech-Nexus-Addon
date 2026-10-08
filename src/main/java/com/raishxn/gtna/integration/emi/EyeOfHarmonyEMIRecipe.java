package com.raishxn.gtna.integration.emi;

import com.gregtechceu.gtceu.common.data.GTMaterials;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

import com.raishxn.gtna.GTNACORE;
import com.raishxn.gtna.client.EyeOfHarmonyRecipePresentation;
import com.raishxn.gtna.common.data.multiblock.EyeOfHarmonyDisplay;
import com.raishxn.gtna.common.data.multiblock.EyeOfHarmonyDisplay.Page;
import com.raishxn.gtna.common.data.multiblock.EyeOfHarmonyDisplay.Product;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.SlotWidget;
import dev.emi.emi.api.widget.WidgetHolder;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

/** One planetary preview with complete output indexing; not a transferable crafting recipe. */
public record EyeOfHarmonyEMIRecipe(Page page) implements EmiRecipe {

    @Override
    public EmiRecipeCategory getCategory() {
        return EyeOfHarmonyEMIPlugin.CATEGORY;
    }

    @Override
    public ResourceLocation getId() {
        return GTNACORE
                .id("eye_of_harmony/" + page.catalog().definition().planet().getPath() + "/page_" + page.number());
    }

    @Override
    public int getDisplayWidth() {
        return 198;
    }

    @Override
    public int getDisplayHeight() {
        return EyeOfHarmonyRecipePresentation.emiInfoHeight(page) + page.rows() * 18;
    }

    @Override
    public boolean supportsRecipeTree() {
        return false;
    }

    @Override
    public boolean hideCraftable() {
        return true;
    }

    @Override
    public List<EmiIngredient> getInputs() {
        return List.of(EmiStack.of(GTMaterials.Hydrogen.getFluid(), page.catalog().program().hydrogen()),
                EmiStack.of(GTMaterials.Helium.getFluid(), page.catalog().program().helium()));
    }

    @Override
    public List<EmiIngredient> getCatalysts() {
        return List.of(EmiStack.of(page.catalog().definition().planetStack()));
    }

    @Override
    public List<EmiStack> getOutputs() {
        return page.products().stream().map(EyeOfHarmonyEMIRecipe::stack).toList();
    }

    private static EmiStack stack(Product product) {
        if (!product.fluid()) return EmiStack.of(ItemStack.of(product.tag()), product.amount());
        var fluid = FluidStack.loadFluidStackFromNBT(product.tag());
        return EmiStack.of(fluid.getFluid(), fluid.getTag(), product.amount());
    }

    @Override
    public void addWidgets(WidgetHolder widgets) {
        int rows = Math.max(2, Math.min(11,
                (widgets.getHeight() - EyeOfHarmonyRecipePresentation.emiInfoHeight(page)) / 18));
        int fluidRows = Math.min(2, rows - 1);
        int itemRows = Math.min(9, rows - fluidRows);
        var visible = EyeOfHarmonyDisplay.singlePreview(page, itemRows, fluidRows);
        widgets.addSlot(getCatalysts().get(0), 90, 3).catalyst(true);
        widgets.add(productSlot(() -> new Product(com.raishxn.gtna.common.data.multiblock.EyeOfHarmonyCatalog
                .fluid(GTMaterials.Hydrogen.getFluid(), page.catalog().program().hydrogen()), true), 18, 3));
        widgets.add(productSlot(() -> new Product(com.raishxn.gtna.common.data.multiblock.EyeOfHarmonyCatalog
                .fluid(GTMaterials.Helium.getFluid(), page.catalog().program().helium()), true), 162, 3));
        for (boolean fluid : new boolean[] { false, true }) {
            int capacity = (fluid ? fluidRows : itemRows) * 9;
            int startY = fluid ? 46 + itemRows * 18 : 40;
            for (int i = 0; i < capacity; i++) {
                int slot = i;
                widgets.add(productSlot(() -> {
                    var products = visible.products().stream()
                            .filter(product -> product.fluid() == fluid).toList();
                    return slot < products.size() ? products.get(slot) : null;
                }, 18 + i % 9 * 18, startY + i / 9 * 18));
            }
        }
        widgets.addDrawable(0, 0, getDisplayWidth(), widgets.getHeight(), (graphics, mouseX, mouseY, delta) -> {
            EyeOfHarmonyRecipePresentation.drawEmiInfo(visible, graphics);
        });
    }

    private SlotWidget productSlot(java.util.function.Supplier<Product> product, int slotX, int slotY) {
        return new SlotWidget(EmiStack.EMPTY, slotX, slotY) {

            @Override
            public EmiIngredient getStack() {
                var current = product.get();
                return current == null ? EmiStack.EMPTY : stack(current);
            }

            @Override
            public void drawStack(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
                getStack().render(graphics, x + 1, y + 1, delta, EmiIngredient.RENDER_ICON);
            }

            @Override
            public void drawOverlay(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
                super.drawOverlay(graphics, mouseX, mouseY, delta);
                var current = product.get();
                if (current == null) return;
                // Item rendering queues geometry; flush it before drawing the quantity above the icon.
                graphics.flush();
                var font = Minecraft.getInstance().font;
                String amount = current.fluid() ? EyeOfHarmonyRecipePresentation.compactBuckets(current.amount()) :
                        EyeOfHarmonyRecipePresentation.compact(current.amount());
                float scale = Math.min(0.5F, 16F / font.width(amount));
                graphics.pose().pushPose();
                graphics.pose().translate(x + 17 - font.width(amount) * scale, y + 17 - 8 * scale, 200);
                graphics.pose().scale(scale, scale, 1);
                graphics.drawString(font, amount, 0, 0, 0xffffffff, true);
                graphics.pose().popPose();
            }

            @Override
            public List<ClientTooltipComponent> getTooltip(int mouseX, int mouseY) {
                var tooltip = super.getTooltip(mouseX, mouseY);
                var current = product.get();
                if (current == null) return tooltip;
                tooltip.add(ClientTooltipComponent.create(Component.translatable("gtna.eoh.jei.base")
                        .getVisualOrderText()));
                tooltip.add(ClientTooltipComponent.create((current.fluid() ?
                        Component.translatable("gtna.eoh.jei.amount_buckets",
                                EyeOfHarmonyRecipePresentation.exactBuckets(current.amount()),
                                NumberFormat.getIntegerInstance(Locale.US).format(current.amount())) :
                        Component.translatable("gtna.eoh.jei.amount_item",
                                NumberFormat.getIntegerInstance(Locale.US).format(current.amount())))
                        .getVisualOrderText()));
                return tooltip;
            }
        }.recipeContext(this);
    }
}
