package com.github.intellectualsites.plotsquared.plot.flags.implementations;

import com.github.intellectualsites.plotsquared.plot.config.Captions;
import com.github.intellectualsites.plotsquared.plot.flags.types.BooleanFlag;
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
