package com.github.intellectualsites.plotsquared.plot.flags.implementations;

import com.github.intellectualsites.plotsquared.plot.config.Captions;
import com.github.intellectualsites.plotsquared.plot.flags.types.BooleanFlag;
import org.jetbrains.annotations.NotNull;

public class HostileAttackFlag extends BooleanFlag<HostileAttackFlag> {

    public static final HostileAttackFlag HOSTILE_ATTACK_TRUE = new HostileAttackFlag(true);
    public static final HostileAttackFlag HOSTILE_ATTACK_FALSE = new HostileAttackFlag(false);

    private HostileAttackFlag(boolean value) {
        super(value, Captions.FLAG_DESCRIPTION_HOSTILE_ATTACK);
    }

    @Override protected HostileAttackFlag flagOf(@NotNull Boolean value) {
        return value ? HOSTILE_ATTACK_TRUE : HOSTILE_ATTACK_FALSE;
    }

}
