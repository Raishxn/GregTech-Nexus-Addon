package com.raishxn.gtna.client.personalspace.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;

import com.raishxn.gtna.common.world.personalspace.PersonalSpaceBlocks;
import com.raishxn.gtna.common.world.personalspace.PersonalSpaceClientState;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;

/**
 * Port of the original block selector: a button showing the selection that opens, on hover, a searchable grid of
 * the server's allowed blocks. In ADD mode (layers) a click fires the callback and keeps the grid open.
 */
public class WBlockDropdown extends Widget {

    public static final int ITEM_SIZE = 20;
    public static final int SCROLLBAR_WIDTH = 6;
    public static final int SEARCH_BAR_HEIGHT = 16;
    public static final int BG_PAD_LEFT = 6;
    public static final int BG_PAD_TOP = 4;
    public static final int BG_PAD_RIGHT = 6;
    public static final int BG_PAD_BOTTOM = 4;

    private static WBlockDropdown openDropdown;
    private static EditBox searchField;
    private static List<BlockEntry> filteredEntries = new ArrayList<>();
    private static int screenWidth, screenHeight, guiLeftOffset, guiTopOffset;
    private static boolean scrollbarDragging = false;

    /** @param blockName empty string means none/air; {@code displayStack} is empty for air. */
    public record BlockEntry(String blockName, ItemStack displayStack, String tooltip) {}

    private final List<BlockEntry> entries;
    private int scrollOffset = 0;
    private int selectedIndex;
    private final boolean addMode;
    private final Consumer<BlockEntry> onSelect;
    private int guiRelX, guiRelY;
    private final int itemsPerRow;
    private final int maxVisibleRows;
    private String label;

    public WBlockDropdown(Rectangle position, boolean addMode, List<BlockEntry> entries, int initialIndex,
                          Consumer<BlockEntry> onSelect) {
        this.position = position;
        this.addMode = addMode;
        this.entries = entries;
        this.selectedIndex = Math.max(0, Math.min(initialIndex, entries.size() - 1));
        this.onSelect = onSelect;
        this.itemsPerRow = Math.max(1, PersonalSpaceClientState.dropdownColumns());
        this.maxVisibleRows = Math.max(1, PersonalSpaceClientState.dropdownRows());
    }

    private int totalRows(List<BlockEntry> display) {
        return (int) Math.ceil((double) display.size() / itemsPerRow);
    }

    private int visibleRows(List<BlockEntry> display) {
        return Math.min(totalRows(display), maxVisibleRows);
    }

    private boolean needsScrollbar(List<BlockEntry> display) {
        return totalRows(display) > maxVisibleRows;
    }

    private int dropdownWidth(List<BlockEntry> display) {
        return itemsPerRow * ITEM_SIZE + (needsScrollbar(display) ? SCROLLBAR_WIDTH : 0);
    }

    private int gridHeight(List<BlockEntry> display) {
        return visibleRows(display) * ITEM_SIZE;
    }

    private int dropdownHeight(List<BlockEntry> display) {
        return SEARCH_BAR_HEIGHT + gridHeight(display);
    }

    private static void updateFilteredEntries() {
        if (openDropdown == null) {
            filteredEntries = new ArrayList<>();
            return;
        }
        String search = searchField != null ? searchField.getValue() : "";
        if (search == null || search.isEmpty()) {
            filteredEntries = new ArrayList<>(openDropdown.entries);
        } else {
            String lower = search.toLowerCase(Locale.ROOT);
            filteredEntries = new ArrayList<>();
            for (BlockEntry entry : openDropdown.entries) {
                if (entry.tooltip() != null && entry.tooltip().toLowerCase(Locale.ROOT).contains(lower) ||
                        entry.blockName().toLowerCase(Locale.ROOT).contains(lower)) {
                    filteredEntries.add(entry);
                }
            }
        }
        openDropdown.scrollOffset = 0;
    }

    public static void setScreenDimensions(int screenW, int screenH, int guiLeft, int guiTop) {
        screenWidth = screenW;
        screenHeight = screenH;
        guiLeftOffset = guiLeft;
        guiTopOffset = guiTop;
    }

    private static int[] dropdownPosition(WBlockDropdown dd, List<BlockEntry> display) {
        int ddW = dd.dropdownWidth(display);
        int ddH = dd.dropdownHeight(display);
        int ddX = dd.guiRelX;
        int ddY = dd.guiRelY + dd.position.height;
        if (guiLeftOffset + ddX + ddW > screenWidth) {
            ddX = dd.guiRelX + dd.position.width - ddW;
            if (guiLeftOffset + ddX < 0) ddX = -guiLeftOffset;
        }
        if (guiTopOffset + ddY + ddH > screenHeight) {
            ddY = dd.guiRelY - ddH;
            if (guiTopOffset + ddY < 0) ddY = -guiTopOffset;
        }
        return new int[] { ddX, ddY };
    }

    public void setLabel(String label) {
        this.label = label;
    }

    /** Position relative to the GUI origin, used to place the open grid. */
    public void setGuiRelativePos(int x, int y) {
        guiRelX = x;
        guiRelY = y;
    }

    public void setSelectedIndex(int index) {
        selectedIndex = Math.max(0, Math.min(index, entries.size() - 1));
    }

    public BlockEntry getSelectedEntry() {
        if (selectedIndex >= 0 && selectedIndex < entries.size()) return entries.get(selectedIndex);
        return entries.isEmpty() ? null : entries.get(0);
    }

    public boolean isOpen() {
        return openDropdown == this;
    }

    private void open() {
        openDropdown = this;
        scrollOffset = 0;
        searchField = new EditBox(Minecraft.getInstance().font, 0, 0, itemsPerRow * ITEM_SIZE,
                SEARCH_BAR_HEIGHT - 2, Component.empty());
        searchField.setMaxLength(256);
        searchField.setFocused(true);
        searchField.setValue("");
        updateFilteredEntries();
    }

    public static void closeDropdown() {
        if (openDropdown != null) {
            openDropdown.scrollOffset = 0;
            openDropdown = null;
            filteredEntries = new ArrayList<>();
            scrollbarDragging = false;
            searchField = null;
        }
    }

    public static boolean isAnyDropdownOpen() {
        return openDropdown != null;
    }

    public static void updateOpenDropdown() {
        if (searchField != null) searchField.tick();
    }

    @Override
    protected void drawImpl(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        Icons icon = enabled ? Icons.BUTTON_NORMAL : Icons.BUTTON_OFF;
        if (enabled && testPoint(mouseX, mouseY)) {
            icon = Icons.BUTTON_HIGHLIGHT;
            if (openDropdown == null) open();
        }
        icon.draw9Patch(graphics, 0, 0, position.width, position.height);
        Font font = Minecraft.getInstance().font;
        BlockEntry selected = getSelectedEntry();
        if (selected != null && !selected.displayStack().isEmpty()) {
            Icons.drawItem(graphics, 1, position.height / 2 - 9, selected.displayStack(), "");
        } else {
            graphics.drawString(font, "-", position.width / 2 - font.width("-") / 2,
                    position.height / 2 - font.lineHeight / 2, 0xFFFFFF, true);
        }
        if (label != null && !label.isEmpty()) {
            graphics.pose().pushPose();
            graphics.pose().translate(0, 0, 200.0F);
            graphics.drawString(font, label, position.width - font.width(label) - 1,
                    position.height - font.lineHeight, 0xFFFFFF, true);
            graphics.pose().popPose();
        }
    }

    @Override
    protected void drawForegroundImpl(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        if (!isAnyDropdownOpen() && testPoint(mouseX, mouseY)) {
            BlockEntry selected = getSelectedEntry();
            if (selected != null && selected.tooltip() != null && !selected.tooltip().isEmpty()) {
                drawTooltip(graphics, mouseX - position.x, mouseY - position.y, selected.tooltip());
            }
        }
    }

    @Override
    protected boolean mouseClickedImpl(int x, int y, int button) {
        if (isOpen()) closeDropdown();
        else open();
        return true;
    }

    /** Draws the open grid; mouse coordinates are relative to the GUI origin. */
    public static void drawOpenDropdown(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        if (openDropdown == null) return;
        WBlockDropdown dd = openDropdown;
        List<BlockEntry> display = filteredEntries;
        int totalRows = dd.totalRows(display);
        int visibleRows = dd.visibleRows(display);
        int ddW = dd.dropdownWidth(display);
        int ddH = dd.dropdownHeight(display);
        int gridHeight = dd.gridHeight(display);
        int[] pos = dropdownPosition(dd, display);
        int ddX = pos[0];
        int ddY = pos[1];

        boolean overTrigger = mouseX >= dd.guiRelX && mouseX < dd.guiRelX + dd.position.width &&
                mouseY >= dd.guiRelY && mouseY < dd.guiRelY + dd.position.height;
        boolean overDropdown = mouseX >= ddX - BG_PAD_LEFT && mouseX < ddX + ddW + BG_PAD_RIGHT &&
                mouseY >= ddY - BG_PAD_TOP && mouseY < ddY + ddH + BG_PAD_BOTTOM;
        if (!overTrigger && !overDropdown && !scrollbarDragging) {
            closeDropdown();
            return;
        }

        Font font = Minecraft.getInstance().font;
        graphics.pose().pushPose();
        graphics.pose().translate(ddX, ddY, 300.0F);
        Icons.GUI_BG.draw9Patch(graphics, -BG_PAD_LEFT, -BG_PAD_TOP, ddW + BG_PAD_LEFT + BG_PAD_RIGHT,
                ddH + BG_PAD_TOP + BG_PAD_BOTTOM);
        if (searchField != null) {
            searchField.setX(0);
            searchField.setY(0);
            searchField.setWidth(ddW);
            searchField.render(graphics, mouseX - ddX, mouseY - ddY, partialTicks);
        }

        int itemsPerRow = dd.itemsPerRow;
        int startRow = dd.scrollOffset;
        int endRow = Math.min(startRow + visibleRows, totalRows);
        int gridWidth = itemsPerRow * ITEM_SIZE;
        for (int row = startRow; row < endRow; row++) {
            for (int col = 0; col < itemsPerRow; col++) {
                int index = row * itemsPerRow + col;
                if (index >= display.size()) break;
                BlockEntry entry = display.get(index);
                int cellX = col * ITEM_SIZE;
                int cellY = SEARCH_BAR_HEIGHT + (row - startRow) * ITEM_SIZE;
                boolean hovered = mouseX - ddX >= cellX && mouseX - ddX < cellX + ITEM_SIZE &&
                        mouseY - ddY >= cellY && mouseY - ddY < cellY + ITEM_SIZE;
                boolean selected = false;
                if (!dd.addMode && dd.selectedIndex >= 0 && dd.selectedIndex < dd.entries.size()) {
                    selected = entry.blockName().equals(dd.entries.get(dd.selectedIndex).blockName());
                }
                Icons cell = selected ? Icons.BUTTON_PRESSED_NORMAL :
                        hovered ? Icons.BUTTON_HIGHLIGHT : Icons.BUTTON_NORMAL;
                cell.draw9Patch(graphics, cellX, cellY, ITEM_SIZE, ITEM_SIZE);
                if (!entry.displayStack().isEmpty()) {
                    Icons.drawItem(graphics, cellX + 1, cellY + 1, entry.displayStack(), "");
                } else {
                    graphics.drawString(font, "-", cellX + ITEM_SIZE / 2 - font.width("-") / 2,
                            cellY + ITEM_SIZE / 2 - font.lineHeight / 2, 0xFFFFFF, true);
                }
            }
        }
        if (dd.needsScrollbar(display)) {
            graphics.fill(gridWidth, SEARCH_BAR_HEIGHT, gridWidth + SCROLLBAR_WIDTH, SEARCH_BAR_HEIGHT + gridHeight,
                    0xFF222222);
            int maxScroll = totalRows - visibleRows;
            int thumbHeight = Math.max(8, (int) (gridHeight * ((float) visibleRows / totalRows)));
            int thumbY = SEARCH_BAR_HEIGHT +
                    (maxScroll > 0 ? (int) ((float) dd.scrollOffset / maxScroll * (gridHeight - thumbHeight)) : 0);
            graphics.fill(gridWidth + 1, thumbY, gridWidth + SCROLLBAR_WIDTH - 1, thumbY + thumbHeight, 0xFF888888);
        }
        graphics.pose().popPose();

        int localX = mouseX - ddX;
        int localY = mouseY - ddY - SEARCH_BAR_HEIGHT;
        if (localX >= 0 && localX < gridWidth && localY >= 0 && localY < gridHeight) {
            int index = (dd.scrollOffset + localY / ITEM_SIZE) * itemsPerRow + localX / ITEM_SIZE;
            if (index >= 0 && index < display.size()) {
                String tooltip = display.get(index).tooltip();
                if (tooltip != null && !tooltip.isEmpty()) {
                    graphics.pose().pushPose();
                    graphics.pose().translate(0, 0, 300.0F);
                    drawTooltip(graphics, mouseX, mouseY, tooltip);
                    graphics.pose().popPose();
                }
            }
        }
    }

    /** @return whether the open grid consumed the click; coordinates are relative to the GUI origin. */
    public static boolean handleOpenDropdownClick(int mouseX, int mouseY, int button) {
        if (openDropdown == null) return false;
        WBlockDropdown dd = openDropdown;
        List<BlockEntry> display = filteredEntries;
        int gridWidth = dd.itemsPerRow * ITEM_SIZE;
        int gridHeight = dd.gridHeight(display);
        int ddW = dd.dropdownWidth(display);
        int ddH = dd.dropdownHeight(display);
        int[] pos = dropdownPosition(dd, display);
        int localX = mouseX - pos[0];
        int localY = mouseY - pos[1];
        if (localX >= -BG_PAD_LEFT && localX < ddW + BG_PAD_RIGHT && localY >= -BG_PAD_TOP &&
                localY < ddH + BG_PAD_BOTTOM) {
            if (localY >= 0 && localY < SEARCH_BAR_HEIGHT) {
                if (searchField != null) {
                    if (button == 1) searchField.setValue("");
                    else searchField.mouseClicked(localX, localY, button);
                    searchField.setFocused(true);
                    updateFilteredEntries();
                }
                return true;
            }
            if (dd.needsScrollbar(display) && localX >= gridWidth && localX < gridWidth + SCROLLBAR_WIDTH &&
                    localY >= SEARCH_BAR_HEIGHT && localY < SEARCH_BAR_HEIGHT + gridHeight) {
                scrollbarDragging = true;
                updateScrollFromDrag(dd, display, localY);
                return true;
            }
            int gridY = localY - SEARCH_BAR_HEIGHT;
            if (localX >= 0 && localX < gridWidth && gridY >= 0 && gridY < gridHeight) {
                int index = (dd.scrollOffset + gridY / ITEM_SIZE) * dd.itemsPerRow + localX / ITEM_SIZE;
                if (index >= 0 && index < display.size()) {
                    BlockEntry entry = display.get(index);
                    if (!dd.addMode) {
                        int original = dd.entries.indexOf(entry);
                        dd.selectedIndex = Math.max(original, 0);
                    }
                    if (dd.onSelect != null) dd.onSelect.accept(entry);
                    dd.clickSound();
                    if (!dd.addMode) closeDropdown();
                    return true;
                }
            }
            return true;
        }
        boolean overTrigger = mouseX >= dd.guiRelX && mouseX < dd.guiRelX + dd.position.width &&
                mouseY >= dd.guiRelY && mouseY < dd.guiRelY + dd.position.height;
        if (!overTrigger) closeDropdown();
        return false;
    }

    public static boolean handleOpenDropdownScroll(int mouseX, int mouseY, double delta) {
        if (openDropdown == null) return false;
        WBlockDropdown dd = openDropdown;
        List<BlockEntry> display = filteredEntries;
        if (!dd.needsScrollbar(display)) return false;
        int[] pos = dropdownPosition(dd, display);
        boolean overTrigger = mouseX >= dd.guiRelX && mouseX < dd.guiRelX + dd.position.width &&
                mouseY >= dd.guiRelY && mouseY < dd.guiRelY + dd.position.height;
        boolean overDropdown = mouseX >= pos[0] && mouseX < pos[0] + dd.dropdownWidth(display) && mouseY >= pos[1] &&
                mouseY < pos[1] + dd.dropdownHeight(display);
        if (!overTrigger && !overDropdown) return false;
        int maxScroll = dd.totalRows(display) - dd.visibleRows(display);
        if (delta > 0) dd.scrollOffset = Math.max(0, dd.scrollOffset - 1);
        else if (delta < 0) dd.scrollOffset = Math.min(maxScroll, dd.scrollOffset + 1);
        return true;
    }

    public static boolean handleOpenDropdownDrag(int mouseX, int mouseY) {
        if (openDropdown == null || !scrollbarDragging) return false;
        int[] pos = dropdownPosition(openDropdown, filteredEntries);
        updateScrollFromDrag(openDropdown, filteredEntries, mouseY - pos[1]);
        return true;
    }

    public static boolean handleOpenDropdownMouseUp() {
        if (!scrollbarDragging) return false;
        scrollbarDragging = false;
        return true;
    }

    private static void updateScrollFromDrag(WBlockDropdown dd, List<BlockEntry> display, int localY) {
        int totalRows = dd.totalRows(display);
        int visibleRows = dd.visibleRows(display);
        int gridHeight = dd.gridHeight(display);
        int maxScroll = totalRows - visibleRows;
        if (maxScroll <= 0) return;
        int thumbHeight = Math.max(8, (int) (gridHeight * ((float) visibleRows / totalRows)));
        int range = gridHeight - thumbHeight;
        if (range <= 0) return;
        float ratio = Math.max(0, Math.min(1, (float) (localY - SEARCH_BAR_HEIGHT - thumbHeight / 2) / range));
        dd.scrollOffset = Math.round(ratio * maxScroll);
    }

    public static boolean handleOpenDropdownKeyPressed(int key, int scanCode, int modifiers) {
        if (openDropdown == null || searchField == null) return false;
        String old = searchField.getValue();
        boolean consumed = searchField.keyPressed(key, scanCode, modifiers);
        if (!old.equals(searchField.getValue())) updateFilteredEntries();
        return consumed;
    }

    public static boolean handleOpenDropdownCharTyped(char character, int modifiers) {
        if (openDropdown == null || searchField == null) return false;
        String old = searchField.getValue();
        boolean consumed = searchField.charTyped(character, modifiers);
        if (!old.equals(searchField.getValue())) updateFilteredEntries();
        return consumed;
    }

    /** Entries for allowed block IDs; the "none" entry (empty name) is used by boundary, gap and center. */
    public static List<BlockEntry> buildEntries(Collection<String> blockIds, boolean includeNoneOption) {
        List<BlockEntry> entries = new ArrayList<>();
        if (includeNoneOption) entries.add(new BlockEntry("", ItemStack.EMPTY, "-"));
        for (String id : blockIds) {
            Block block = PersonalSpaceBlocks.block(id);
            if (block == null) continue;
            ItemStack stack = new ItemStack(block.asItem());
            String name = stack.isEmpty() ? block.getName().getString() : stack.getHoverName().getString();
            entries.add(new BlockEntry(id, stack, name + " (" + id + ")"));
        }
        return entries;
    }

    public static int findEntryIndex(List<BlockEntry> entries, String blockName) {
        if (blockName == null || blockName.isEmpty()) return 0;
        for (int index = 0; index < entries.size(); index++) {
            if (blockName.equals(entries.get(index).blockName())) return index;
        }
        return 0;
    }
}
