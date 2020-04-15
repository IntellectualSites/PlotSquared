package com.plotsquared.core.plot.flag.implementations;

import com.plotsquared.core.config.Captions;
import com.plotsquared.core.plot.flag.types.BooleanFlag;
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
