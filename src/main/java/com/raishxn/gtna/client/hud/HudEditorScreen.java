package com.raishxn.gtna.client.hud;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * The GTNA HUD editor (GTOCore {@code HUDScreen} parity, trimmed to a single overlay list).
 *
 * <p>
 * Opened with the GTNA keybind. It draws every enabled {@link IMoveableHud}, lets the player drag
 * them around (the position is written back to the client config on release) and toggle them. The
 * game keeps running behind it, so the HUD can be positioned against the real scene.
 */
@OnlyIn(Dist.CLIENT)
public class HudEditorScreen extends Screen {

    private static final List<IMoveableHud> HUDS = new ArrayList<>();

    private IMoveableHud activeHud;

    public HudEditorScreen() {
        super(Component.translatable("gtna.hud.editor.title"));
    }

    public static void register(IMoveableHud hud) {
        if (!HUDS.contains(hud)) {
            HUDS.add(hud);
        }
    }

    @Override
    protected void init() {
        super.init();
        addRenderableWidget(Button.builder(toggleLabel(), button -> {
            for (IMoveableHud hud : HUDS) {
                hud.setEnabled(!hud.isEnabled());
            }
            button.setMessage(toggleLabel());
        }).bounds(6, 6, 150, 20).build());
        addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, button -> onClose())
                .bounds(width - 66, 6, 60, 20).build());
    }

    private static Component toggleLabel() {
        boolean anyEnabled = HUDS.stream().anyMatch(IMoveableHud::isEnabled);
        return Component.translatable("gtna.hud.editor.toggle",
                Component.translatable(anyEnabled ? "gtna.hud.editor.on" : "gtna.hud.editor.off"));
    }

    @Override
    public void render(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        // Keep the world visible so the HUD can be positioned against it (GTOCore HUDScreen parity):
        // no full-screen dim, just a strip behind the labels.
        graphics.fill(0, 26, width, 56, 0x80000000);

        for (IMoveableHud hud : HUDS) {
            if (hud.isEnabled()) {
                hud.render(graphics, width, height);
                IMoveableHud.drawOutline(graphics, hud.getAnchorBounds(width, height), 0xFF7FDBFF);
            }
        }

        graphics.drawCenteredString(font, title, width / 2, 30, 0xFFFFFFFF);
        graphics.drawCenteredString(font, Component.translatable("gtna.hud.editor.hint"), width / 2, 44,
                0xFFAAAAAA);

        super.render(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    public void renderBackground(@NotNull GuiGraphics graphics) {
        // Intentionally empty: the editor must not dim the world behind the HUD.
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (super.mouseClicked(mouseX, mouseY, button)) {
            return true;
        }
        for (IMoveableHud hud : HUDS) {
            if (hud.isEnabled() && hud.mouseClicked(mouseX, mouseY, button)) {
                activeHud = hud;
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (activeHud != null && activeHud.mouseDragged(mouseX, mouseY, button, dragX, dragY)) {
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (activeHud != null) {
            activeHud.mouseReleased(mouseX, mouseY, button);
            activeHud = null;
            return true;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
