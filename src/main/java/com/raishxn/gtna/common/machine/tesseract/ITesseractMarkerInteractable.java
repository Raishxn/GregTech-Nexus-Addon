package com.raishxn.gtna.common.machine.tesseract;

import net.minecraft.world.entity.player.Player;

import java.util.List;

public interface ITesseractMarkerInteractable {

    boolean onMarkerInteract(Player player, List<TesseractDirectedTarget> targets);
}
