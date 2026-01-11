package com.raishxn.gtna.common.machine.multiblock.part;

import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiController;
import com.gregtechceu.gtceu.api.machine.multiblock.part.TieredIOPartMachine;
import com.raishxn.gtna.api.machine.IThreadModifierMachine;
import com.lowdragmc.lowdraglib.gui.widget.LabelWidget;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

public class ThreadPartMachine extends TieredIOPartMachine {

    private final int threadCount;

    public ThreadPartMachine(IMachineBlockEntity holder, int tier, Object... args) {
        // IO.NONE pois o hatch não transporta itens, apenas lógica
        super(holder, tier, IO.NONE);
        // Exemplo: Tier 1 (LV) = +1 thread, Tier 2 (MV) = +2 threads, etc.
        // Você pode ajustar essa fórmula conforme o balanceamento desejado.
        this.threadCount = (tier + 1);
    }

    public int getThreadCount() {
        return this.threadCount;
    }

    @Override
    public void addedToController(IMultiController controller) {
        super.addedToController(controller);
        if (controller instanceof IThreadModifierMachine threadMachine) {
            threadMachine.setThreadPartMachine(this);
            // O GTLAdditions faz isso, mas nós não precisamos se nossa Logic
            // sempre verificar 'getMaxThreads()' no tick.
        }
    }

    @Override
    public void removedFromController(IMultiController controller) {
        super.removedFromController(controller);
        // Remove a referência ao quebrar o hatch
        if (controller instanceof IThreadModifierMachine threadMachine) {
            if (threadMachine.getThreadPartMachine() == this) {
                threadMachine.setThreadPartMachine(null);
            }
        }
    }

    @Override
    public Widget createUIWidget() {
        var group = new WidgetGroup(0, 0, 100, 20);
        group.addWidget(new LabelWidget(5, 5, () ->
                // Use §b para cor Aqua e concatene a string diretamente
                "Threads: §b+" + this.getThreadCount()
        ));
        return group;
    }
}