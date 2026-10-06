package com.raishxn.gtna.gametest;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.common.data.GTItems;

import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraftforge.common.util.FakePlayerFactory;

import com.mojang.authlib.GameProfile;
import com.raishxn.gtna.GTNACORE;
import com.raishxn.gtna.common.data.GTNAItems;
import com.raishxn.gtna.common.item.DepositRecorderBehavior;
import com.raishxn.gtna.config.VoidFluidDrillConfig;

import java.util.UUID;

import static com.raishxn.gtna.gametest.GTNAFluidDrillingGameTests.*;

/** Optional FTB test body: the registered GameTest holder must load without FTB. */
final class FTBFluidDiscoveryTests {

    @SuppressWarnings("unchecked")
    static void run(GameTestHelper h) throws ReflectiveOperationException {
        var manager = (dev.ftb.mods.ftbteams.data.TeamManagerImpl) dev.ftb.mods.ftbteams.api.FTBTeamsAPI.api()
                .getManager();
        // FTB exposes a read-only view; install isolated offline identities in its backing map.
        var knownField = manager.getClass().getDeclaredField("knownPlayers");
        knownField.setAccessible(true);
        var known = (java.util.Map<UUID, dev.ftb.mods.ftbteams.api.Team>) knownField.get(manager);
        UUID first = UUID.randomUUID(), second = UUID.randomUUID(), outsider = UUID.randomUUID();
        var party = new dev.ftb.mods.ftbteams.data.PartyTeam(manager, UUID.randomUUID());
        var firstTeam = new dev.ftb.mods.ftbteams.data.PlayerTeam(manager, first);
        var secondTeam = new dev.ftb.mods.ftbteams.data.PlayerTeam(manager, second);
        var outsideTeam = new dev.ftb.mods.ftbteams.data.PlayerTeam(manager, outsider);
        // Real FTB model/resolver objects with isolated test identities, not connected clients.
        firstTeam.setEffectiveTeam(party);
        secondTeam.setEffectiveTeam(party);
        known.put(first, firstTeam);
        known.put(second, secondTeam);
        known.put(outsider, outsideTeam);
        try {
            var member = FakePlayerFactory.get(h.getLevel(), new GameProfile(second, "TeamFluidMember"));
            var stranger = FakePlayerFactory.get(h.getLevel(), new GameProfile(outsider, "OutsideFluidPlayer"));
            var data = card(h, first, VoidFluidDrillConfig.get().program("oil"));
            h.assertTrue(DepositRecorderBehavior.owner(member).equals(party.getId()),
                    "discovery ownership must use the effective FTB party, not the personal player team");
            h.assertTrue(DepositRecorderBehavior.validCard(h.getLevel(), data,
                    DepositRecorderBehavior.owner(member), "minecraft:overworld", "gtceu:oil"),
                    "a second party member must share the first member's discovery");
            h.assertTrue(!DepositRecorderBehavior.validCard(h.getLevel(), data,
                    DepositRecorderBehavior.owner(stranger), "minecraft:overworld", "gtceu:oil"),
                    "outsiders must not share discovery certificates");
            var behavior = new DepositRecorderBehavior();
            for (var player : java.util.List.of(member, stranger)) {
                player.setItemInHand(InteractionHand.MAIN_HAND, GTNAItems.DEPOSIT_RECORDER.asStack());
                player.setItemInHand(InteractionHand.OFF_HAND, data.copy());
                player.getInventory().setItem(1, GTItems.TOOL_DATA_STICK.asStack());
            }
            h.assertTrue(behavior.use(GTNAItems.DEPOSIT_RECORDER.get(), h.getLevel(), member,
                    InteractionHand.MAIN_HAND).getResult() == InteractionResult.SUCCESS,
                    "party members must be able to copy shared data");
            h.assertTrue(behavior.use(GTNAItems.DEPOSIT_RECORDER.get(), h.getLevel(), stranger,
                    InteractionHand.MAIN_HAND).getResult() == InteractionResult.FAIL &&
                    stranger.getInventory().getItem(1).getCount() == 1,
                    "outsider copying must fail without consuming its stick");
            var rig = build(h, GTValues.EV, second);
            insert(h, data, 100);
            secondTeam.setEffectiveTeam(secondTeam);
            ticks(h, rig, 30);
            h.assertTrue(inputFluid(h) == 100 && output(h) == 0,
                    "leaving the party must revoke production using its shared discovery");
            secondTeam.setEffectiveTeam(party);
            rig.getRecipeLogic().markLastRecipeDirty();
            ticks(h, rig, 450);
            h.assertTrue(inputFluid(h) == 0 && output(h) == 2000,
                    "rejoining the party must restore shared production");
            GTNACORE.LOGGER.info("FTB effective party discovery, copying and membership gates: PASS");
            h.succeed();
        } finally {
            known.remove(first);
            known.remove(second);
            known.remove(outsider);
        }
    }
}
