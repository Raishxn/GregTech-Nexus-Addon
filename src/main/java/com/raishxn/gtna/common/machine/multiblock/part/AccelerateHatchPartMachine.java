package com.raishxn.gtna.common.machine.multiblock.part;

import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.feature.ITieredMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.part.MultiblockPartMachine;
// import com.raishxn.gtna.config.ConfigHolder; // Removido pois não usamos mais config
import net.minecraft.MethodsReturnNonnullByDefault;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class AccelerateHatchPartMachine extends MultiblockPartMachine implements ITieredMachine {

    private final int tier;

    public AccelerateHatchPartMachine(IMachineBlockEntity holder, int tier, Object... args) {
        super(holder);
        this.tier = tier;
    }

    @Override
    public int getTier() {
        return this.tier;
    }

    /**
     * Calcula a porcentagem da duração da receita.
     * @param machineTier O tier da máquina (ou da receita, dependendo de quem chama).
     * @return Valor entre 1 e 100. (50 = 50% do tempo original).
     */
    public int calcDurationPercentage(int machineTier) {
        // Lógica Fixa:
        // Tier 1 (LV) começa em 50%.
        // A cada tier acima de LV, reduzimos 2% do tempo (fica mais rápido).
        // Exemplo: Tier 14 (MAX) -> 50 - (2 * 13) = 24%.
        int basePercentage = 50 - (2 * (this.getTier() - 1));

        // Regra de Penalidade:
        // Se o tier da máquina for maior que o da escotilha, a eficiência cai 20% por nível de diferença.
        // Isso aumenta a porcentagem de duração (fica mais lento).
        int tierDiff = machineTier - this.getTier();
        if (tierDiff > 0) {
            basePercentage += (20 * tierDiff);
        }

        // Garante que nunca seja menor que 1% (instantâneo) nem maior que 100% (tempo normal)
        return Math.max(1, Math.min(100, basePercentage));
    }
}