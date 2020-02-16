package com.github.intellectualsites.plotsquared.plot.flags.implementations;

import com.github.intellectualsites.plotsquared.plot.config.Captions;
import com.github.intellectualsites.plotsquared.plot.flags.types.BooleanFlag;
import org.jetbrains.annotations.NotNull;

public class RedstoneFlag extends BooleanFlag<RedstoneFlag> {

    public static final RedstoneFlag REDSTONE_TRUE = new RedstoneFlag(true);
    public static final RedstoneFlag REDSTONE_FALSE = new RedstoneFlag(false);

    private RedstoneFlag(boolean value) {
        super(value, Captions.FLAG_DESCRIPTION_REDSTONE);
    }

    @Override protected RedstoneFlag flagOf(@NotNull Boolean value) {
        return value ? REDSTONE_TRUE : REDSTONE_FALSE;
    }

}
