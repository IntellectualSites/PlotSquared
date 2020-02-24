package com.github.intellectualsites.plotsquared.plot.flags.implementations;

import com.github.intellectualsites.plotsquared.plot.config.Captions;
import com.github.intellectualsites.plotsquared.plot.flags.types.BooleanFlag;
import org.jetbrains.annotations.NotNull;

public class TamedAttackFlag extends BooleanFlag<TamedAttackFlag> {

    public static final TamedAttackFlag TAMED_ATTACK_TRUE = new TamedAttackFlag(true);
    public static final TamedAttackFlag TAMED_ATTACK_FALSE = new TamedAttackFlag(false);

    private TamedAttackFlag(boolean value) {
        super(value, Captions.FLAG_DESCRIPTION_TAMED_ATTACK);
    }

    @Override protected TamedAttackFlag flagOf(@NotNull Boolean value) {
        return value ? TAMED_ATTACK_TRUE : TAMED_ATTACK_FALSE;
    }

}
