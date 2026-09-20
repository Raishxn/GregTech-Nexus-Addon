package com.raishxn.gtna.client;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import org.jetbrains.annotations.Nullable;

/**
 * Client-only indirection for common code that needs the local player.
 *
 * <p>
 * Referencing {@code net.minecraft.client.Minecraft} — or the {@code LocalPlayer} it returns —
 * directly from a class that is loaded on a dedicated server makes Forge's {@code RuntimeDistCleaner}
 * reject that class during verification, and mod loading fails outright (verified with
 * {@code ./gradlew runGameTestServer}: the mod did not load on a dedicated server at all). This
 * helper keeps the client types out of the common class: callers obtain the player through
 * {@code DistExecutor.unsafeCallWhenOn(Dist.CLIENT, () -> ClientPlayerLookup::localPlayer)} and get
 * {@code null} on the server.
 */
@OnlyIn(Dist.CLIENT)
public final class ClientPlayerLookup {

    private ClientPlayerLookup() {}

    /** @return the local player, or {@code null} when there is no client world yet. */
    @Nullable
    public static Player localPlayer() {
        Minecraft minecraft = Minecraft.getInstance();
        return minecraft == null ? null : minecraft.player;
    }
}
