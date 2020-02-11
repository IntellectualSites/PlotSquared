package com.github.intellectualsites.plotsquared.plot.flags.implementations;

import com.github.intellectualsites.plotsquared.plot.config.Captions;
import com.github.intellectualsites.plotsquared.plot.flags.types.BooleanFlag;
import org.jetbrains.annotations.NotNull;

public class ExplosionFlag extends BooleanFlag<ExplosionFlag> {
    public static final ExplosionFlag EXPLOSION_TRUE = new ExplosionFlag(true);
    public static final ExplosionFlag EXPLOSION_FALSE = new ExplosionFlag(false);

    private ExplosionFlag(boolean value) {
        super(value, Captions.FLAG_DESCRIPTION_EXPLOSION);
    }

    @Override public String getExample() {
        return "true";
    }

    @Override protected ExplosionFlag flagOf(@NotNull Boolean value) {
        return value ? EXPLOSION_TRUE : EXPLOSION_FALSE;
    }
}
