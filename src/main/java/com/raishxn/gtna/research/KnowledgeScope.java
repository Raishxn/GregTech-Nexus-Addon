package com.raishxn.gtna.research;

import com.gregtechceu.gtceu.common.machine.owner.MachineOwner;

import net.minecraft.world.entity.player.Player;

import com.raishxn.gtna.common.item.DepositRecorderBehavior;

import java.util.UUID;

/**
 * Who owns research progress: the player's effective FTB team when FTB Teams is loaded and the player
 * is in one, otherwise the player. Same rule as fluid discoveries, so the two systems agree.
 */
public final class KnowledgeScope {

    private KnowledgeScope() {}

    public static UUID of(Player player) {
        return of(player.getUUID());
    }

    public static UUID of(UUID playerId) {
        return DepositRecorderBehavior.scope(MachineOwner.getOwner(playerId), playerId);
    }
}
