package com.plotsquared.core.plot.flag.implementations;

import com.plotsquared.core.config.Captions;
import com.plotsquared.core.plot.flag.types.BooleanFlag;
import org.jetbrains.annotations.NotNull;

public class InvincibleFlag extends BooleanFlag<InvincibleFlag> {

    public static final InvincibleFlag INVINCIBLE_TRUE = new InvincibleFlag(true);
    public static final InvincibleFlag INVINCIBLE_FALSE = new InvincibleFlag(false);

    private InvincibleFlag(boolean value) {
        super(value, Captions.FLAG_DESCRIPTION_INVINCIBLE);
    }

    @Override protected InvincibleFlag flagOf(@NotNull Boolean value) {
        return value ? INVINCIBLE_TRUE : INVINCIBLE_FALSE;
    }

}
