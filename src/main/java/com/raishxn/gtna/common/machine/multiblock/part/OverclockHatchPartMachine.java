package com.raishxn.gtna.common.machine.multiblock.part;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.feature.ITieredMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.part.MultiblockPartMachine;
import net.minecraft.MethodsReturnNonnullByDefault;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class OverclockHatchPartMachine extends MultiblockPartMachine implements ITieredMachine {

    private final int tier;

    public OverclockHatchPartMachine(IMachineBlockEntity holder, int tier, Object... args) {
        super(holder);
        this.tier = tier;
    }

    @Override
    public int getTier() {
        return this.tier;
    }

    /**
     * Calcula o multiplicador de duração baseado no tier da máquina controladora.
     * @param controllerTier O tier da voltagem atual da máquina (ex: UV, UHV...)
     * @return Um multiplicador entre 0.5 (UV) e 0.125 (MAX).
     */
    public double getOverclockMultiplier(int controllerTier) {
        int minTier = GTValues.UV;  // Tier inicial (UV)
        int maxTier = GTValues.MAX; // Tier final (MAX/OpV)

        // Multiplicadores
        double startMult = 0.5;   // 50%
        double endMult = 0.125;   // 12.5%

        // 1. Se for menor que UV, retorna o base (ou lida conforme sua preferência, aqui travo em 0.5)
        if (controllerTier <= minTier) {
            return startMult;
        }

        // 2. Se for maior ou igual ao MAX, retorna o limite máximo de velocidade
        if (controllerTier >= maxTier) {
            return endMult;
        }
        // Calcula onde estamos na escala (0.0 = UV, 1.0 = MAX)
        double progress = (double) (controllerTier - minTier) / (maxTier - minTier);

        // Fórmula: Inicio - (Progresso * (Diferença))
        // Ex: Se progresso for 0.5 (metade), vai reduzir metade da diferença entre 0.5 e 0.125
        return startMult - (progress * (startMult - endMult));
    }
    /*// Dentro do seu RecipeLogic ou checkRecipe
var overclockHatch = machine.getParts().stream()
    .filter(p -> p instanceof OverclockHatchPartMachine)
    .findFirst();
double durationMult = 0.55; // Padrão "Nerfado" sem o hatch
if (overclockHatch.isPresent()) {
    // Se o hatch existe, pegamos a lógica dele
    int currentVoltageTier = GTValues.getTier(machine.getAvailableVoltage());
    durationMult = ((OverclockHatchPartMachine) overclockHatch.get()).getOverclockMultiplier(currentVoltageTier);
}
// Aplica o multiplicador
recipeDuration = (int) (recipeDuration * durationMult);*/
}