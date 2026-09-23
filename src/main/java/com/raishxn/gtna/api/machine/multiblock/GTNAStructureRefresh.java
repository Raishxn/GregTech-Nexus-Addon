package com.raishxn.gtna.api.machine.multiblock;

import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiPart;
import com.gregtechceu.gtceu.api.machine.multiblock.MultiblockControllerMachine;
import com.gregtechceu.gtceu.api.pattern.MultiblockState;
import com.gregtechceu.gtceu.api.pattern.MultiblockWorldSavedData;

import net.minecraft.server.level.ServerLevel;

import java.util.HashSet;
import java.util.Set;

/** Rebuilds a controller's part list and block-change mapping after a module changes. */
public final class GTNAStructureRefresh {

    private GTNAStructureRefresh() {}

    /** Returns whether the main structure matches after the refresh. Must be called on the server thread. */
    public static boolean refresh(MultiblockControllerMachine controller, boolean force) {
        if (!(controller.getLevel() instanceof ServerLevel serverLevel)) {
            return false;
        }
        MultiblockState state = controller.getMultiblockState();
        if (!force && controller.isFormed() && controller.checkPatternWithLock() && partsMatch(controller, state)) {
            return true;
        }

        MultiblockWorldSavedData savedData = MultiblockWorldSavedData.getOrCreate(serverLevel);
        savedData.removeMapping(state);
        if (controller.isFormed()) {
            controller.onStructureInvalid();
        }
        if (controller.checkPatternWithLock()) {
            controller.setFlipped(state.isNeededFlip());
            controller.onStructureFormed();
            savedData.addMapping(state);
            savedData.removeAsyncLogic(controller);
            return true;
        }
        savedData.addAsyncLogic(controller);
        return false;
    }

    private static boolean partsMatch(MultiblockControllerMachine controller, MultiblockState state) {
        Object matched = state.getMatchContext().get("parts");
        if (!(matched instanceof Set<?> matchParts)) {
            return controller.getParts().isEmpty();
        }
        Set<IMultiPart> currentParts = new HashSet<>(controller.getParts());
        return currentParts.equals(matchParts);
    }
}
