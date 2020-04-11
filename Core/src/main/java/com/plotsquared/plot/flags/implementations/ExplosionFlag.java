package com.plotsquared.plot.flags.implementations;

import com.plotsquared.config.Captions;
import com.plotsquared.plot.flags.types.BooleanFlag;
import org.jetbrains.annotations.NotNull;

public class ExplosionFlag extends BooleanFlag<ExplosionFlag> {

    public static final ExplosionFlag EXPLOSION_TRUE = new ExplosionFlag(true);
    public static final ExplosionFlag EXPLOSION_FALSE = new ExplosionFlag(false);

    private ExplosionFlag(boolean value) {
        super(value, Captions.FLAG_DESCRIPTION_EXPLOSION);
    }

    @Override protected ExplosionFlag flagOf(@NotNull Boolean value) {
        return value ? EXPLOSION_TRUE : EXPLOSION_FALSE;
    }

}
