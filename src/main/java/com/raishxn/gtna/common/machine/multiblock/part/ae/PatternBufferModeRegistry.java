package com.raishxn.gtna.common.machine.multiblock.part.ae;

import com.gregtechceu.gtceu.api.machine.feature.IRecipeLogicMachine;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiController;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;

import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.ParametersAreNonnullByDefault;

/**
 * Registry of the recipe-type "modes" a Pattern Buffer slot can be pinned to.
 *
 * <p>
 * Owns the cached list of recipe types exposed by the controller(s) this buffer is attached to
 * (kept in the machine's synced {@code availableModeIds} field so clients can render the selector
 * without a round-trip) and the label formatting used by the UI. Pure presentation (the selector
 * button, cycling and hover tooltips) stays in the machine/UI; this class only answers "which
 * modes exist and what are they called".
 */
@ParametersAreNonnullByDefault
final class PatternBufferModeRegistry {

    private final GTNAMEPatternBufferPartMachine machine;

    PatternBufferModeRegistry(GTNAMEPatternBufferPartMachine machine) {
        this.machine = machine;
    }

    record ModeOption(String id, String label) {}

    /**
     * Modes offered by the selector: "auto" first, then every recipe type currently exposed by the
     * controller(s), then any legacy/derived mode pinned on the selected slot so it can never be
     * silently dropped from the list.
     */
    List<ModeOption> getAvailableModeOptions() {
        List<ModeOption> options = new ArrayList<>();
        options.add(new ModeOption("", Component.translatable("gtna.machine.pattern_buffer.mode.auto").getString()));

        Set<String> seen = new LinkedHashSet<>();
        for (String modeId : getCachedAvailableModeIds()) {
            if (seen.add(modeId)) {
                options.add(new ModeOption(modeId, formatModeLabel(modeId)));
            }
        }
        if (machine.isFormed() && !machine.getControllers().isEmpty()) {
            IMultiController controller = machine.getControllers().first();
            if (controller instanceof IRecipeLogicMachine recipeMachine) {
                GTRecipeType[] recipeTypes = recipeMachine.getRecipeTypes();
                if (recipeTypes == null || recipeTypes.length == 0) {
                    recipeTypes = new GTRecipeType[] { recipeMachine.getRecipeType() };
                }
                for (GTRecipeType recipeType : recipeTypes) {
                    if (recipeType == null || recipeType.registryName == null) {
                        continue;
                    }
                    String id = recipeType.registryName.toString();
                    if (seen.add(id)) {
                        options.add(new ModeOption(id, formatModeLabel(recipeType)));
                    }
                }
            }
        }

        GTNAPatternBufferSlotConfig config = machine.getSelectedConfig();
        if (config != null && !config.getPreferredModeId().isBlank() && seen.add(config.getPreferredModeId())) {
            options.add(new ModeOption(config.getPreferredModeId(),
                    Component.translatable("gtna.machine.pattern_buffer.mode.legacy",
                            compactDisplay(config.getPreferredModeId(), 18)).getString()));
        }
        if (config != null && config.getPreferredModeId().isBlank() && !config.getDerivedModeId().isBlank() &&
                seen.add(config.getDerivedModeId())) {
            options.add(new ModeOption(config.getDerivedModeId(), formatModeLabel(config.getDerivedModeId())));
        }
        return options;
    }

    List<String> getCachedAvailableModeIds() {
        String availableModeIds = machine.getAvailableModeIds();
        if (availableModeIds == null || availableModeIds.isBlank()) {
            return List.of();
        }
        List<String> ids = new ArrayList<>();
        for (String token : availableModeIds.split("\\|")) {
            String trimmed = token == null ? "" : token.trim();
            if (!trimmed.isBlank()) {
                ids.add(trimmed);
            }
        }
        return ids;
    }

    /** Recomputes the synced recipe-type list from the current controller(s). */
    void refreshAvailableModesCache() {
        Set<String> ids = new LinkedHashSet<>();
        if (machine.isFormed() && !machine.getControllers().isEmpty()) {
            for (IMultiController controller : machine.getControllers()) {
                if (!(controller instanceof IRecipeLogicMachine recipeMachine)) {
                    continue;
                }
                GTRecipeType[] recipeTypes = recipeMachine.getRecipeTypes();
                if (recipeTypes == null || recipeTypes.length == 0) {
                    recipeTypes = new GTRecipeType[] { recipeMachine.getRecipeType() };
                }
                for (GTRecipeType recipeType : recipeTypes) {
                    if (recipeType != null && recipeType.registryName != null) {
                        ids.add(recipeType.registryName.toString());
                    }
                }
            }
        }
        machine.setAvailableModeIds(String.join("|", ids));
    }

    static String compactDisplay(String value, int maxLength) {
        if (value == null || value.isBlank()) {
            return "-";
        }
        return value.length() <= maxLength ? value : value.substring(0, Math.max(0, maxLength - 3)) + "...";
    }

    static String formatModeLabel(GTRecipeType recipeType) {
        if (recipeType == null || recipeType.registryName == null) {
            return "-";
        }
        return formatModeLabel(recipeType.registryName.toString());
    }

    static String formatModeLabel(String modeId) {
        if (modeId == null || modeId.isBlank()) {
            return "-";
        }
        String path = modeId;
        int namespaceSeparator = path.indexOf(':');
        if (namespaceSeparator >= 0 && namespaceSeparator + 1 < path.length()) {
            path = path.substring(namespaceSeparator + 1);
        }
        String[] parts = path.split("[/_]");
        StringBuilder builder = new StringBuilder();
        for (String part : parts) {
            if (part.isBlank()) {
                continue;
            }
            if (builder.length() > 0) {
                builder.append(' ');
            }
            builder.append(Character.toUpperCase(part.charAt(0)));
            if (part.length() > 1) {
                builder.append(part.substring(1));
            }
        }
        return builder.length() == 0 ? path : builder.toString();
    }
}
