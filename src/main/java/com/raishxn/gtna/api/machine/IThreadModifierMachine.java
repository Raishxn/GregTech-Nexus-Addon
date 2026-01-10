package com.raishxn.gtna.api.machine;

import com.raishxn.gtna.common.machine.multiblock.part.ThreadPartMachine;
import org.jetbrains.annotations.Nullable;

public interface IThreadModifierMachine {

    default int getAdditionalThread() {
        ThreadPartMachine part = getThreadPartMachine();
        return part != null ? part.getThreadCount() : 0;
    }

    @Nullable
    ThreadPartMachine getThreadPartMachine();
    void setThreadPartMachine(@Nullable ThreadPartMachine threadModifierPart);
}