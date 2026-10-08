package com.raishxn.gtna.common.data.multiblock;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;

import java.util.ArrayList;
import java.util.List;

/** Shared catalog snapshots and viewer layouts; preview omissions never change simulation outputs. */
public final class EyeOfHarmonyDisplay {

    private EyeOfHarmonyDisplay() {}

    private static final int PAGE_SIZE = 99;

    public record Product(CompoundTag tag, boolean fluid) {

        public long amount() {
            return tag.getLong("remaining");
        }
    }

    public record Page(EyeOfHarmonyCatalog.Catalog catalog, List<Product> products, int number, int total,
                       int rows) {}

    public static List<Page> pages(EyeOfHarmonyCatalog.Catalog catalog) {
        return pages(catalog, PAGE_SIZE);
    }

    /** Reflows a registered viewer page to the space actually allocated by the viewer. */
    public static List<Page> reflow(Page page, int pageSize) {
        if (pageSize < 9 || pageSize > 99 || pageSize % 9 != 0)
            throw new IllegalArgumentException("Page size must be 9–99 in rows of nine");
        List<Page> pages = new ArrayList<>();
        int count = (page.products().size() + pageSize - 1) / pageSize;
        for (int i = 0; i < count; i++) {
            pages.add(new Page(page.catalog(), List.copyOf(page.products().subList(i * pageSize,
                    Math.min(page.products().size(), (i + 1) * pageSize))), i + 1, count, pageSize / 9));
        }
        return List.copyOf(pages);
    }

    /** One EMI recipe indexes the complete catalog, regardless of the preview capacity. */
    public static Page singlePage(EyeOfHarmonyCatalog.Catalog catalog) {
        var all = pages(catalog).stream().flatMap(page -> page.products().stream()).toList();
        return new Page(catalog, all, 1, 1, 11);
    }

    /** Fixed single-page preview; the warning reports products outside the two visible regions. */
    public static Page singlePreview(Page source, int itemRows, int fluidRows) {
        var first = separated(source, itemRows, fluidRows).get(0);
        return new Page(source.catalog(), first.products(), 1, 1, first.rows());
    }

    /** Each EMI page has independent item and fluid regions, without duplicating indexed outputs. */
    public static List<Page> separatedPages(EyeOfHarmonyCatalog.Catalog catalog, int itemRows, int fluidRows) {
        var all = pages(catalog).stream().flatMap(page -> page.products().stream()).toList();
        return separated(new Page(catalog, all, 1, 1, 11), itemRows, fluidRows);
    }

    public static List<Page> separated(Page source, int itemRows, int fluidRows) {
        if (itemRows < 1 || itemRows > 9 || fluidRows < 1 || fluidRows > 2)
            throw new IllegalArgumentException("Item grid: 1–9 rows; fluid grid: 1–2 rows");
        var items = source.products().stream().filter(product -> !product.fluid()).toList();
        var fluids = source.products().stream().filter(Product::fluid).toList();
        int itemCapacity = itemRows * 9;
        int fluidCapacity = fluidRows * 9;
        int count = Math.max((items.size() + itemCapacity - 1) / itemCapacity,
                (fluids.size() + fluidCapacity - 1) / fluidCapacity);
        List<Page> result = new ArrayList<>();
        for (int i = 0; i < Math.max(1, count); i++) {
            List<Product> products = new ArrayList<>();
            products.addAll(items.subList(Math.min(items.size(), i * itemCapacity),
                    Math.min(items.size(), (i + 1) * itemCapacity)));
            products.addAll(fluids.subList(Math.min(fluids.size(), i * fluidCapacity),
                    Math.min(fluids.size(), (i + 1) * fluidCapacity)));
            result.add(new Page(source.catalog(), List.copyOf(products), i + 1, Math.max(1, count),
                    itemRows + fluidRows));
        }
        return List.copyOf(result);
    }

    public static List<Page> pages(EyeOfHarmonyCatalog.Catalog catalog, int pageSize) {
        if (pageSize < 9 || pageSize > 99 || pageSize % 9 != 0)
            throw new IllegalArgumentException("Page size must be 9–99 in rows of nine");
        List<Product> products = new ArrayList<>();
        for (String kind : List.of("items", "fluids")) {
            for (Tag tag : catalog.products().getList(kind, Tag.TAG_COMPOUND)) {
                products.add(new Product(((CompoundTag) tag).copy(), kind.equals("fluids")));
            }
        }
        List<Page> pages = new ArrayList<>();
        int count = (products.size() + pageSize - 1) / pageSize;
        for (int i = 0; i < count; i++) {
            pages.add(new Page(catalog, List.copyOf(products.subList(i * pageSize,
                    Math.min(products.size(), (i + 1) * pageSize))), i + 1, count, pageSize / 9));
        }
        return List.copyOf(pages);
    }
}
