package com.plotsquared.plot.flags.implementations;

import com.plotsquared.config.Captions;
import com.plotsquared.plot.flags.types.BooleanFlag;
import org.jetbrains.annotations.NotNull;

public class GrassGrowFlag extends BooleanFlag<GrassGrowFlag> {

    public static final GrassGrowFlag GRASS_GROW_TRUE = new GrassGrowFlag(true);
    public static final GrassGrowFlag GRASS_GROW_FALSE = new GrassGrowFlag(false);

    private GrassGrowFlag(boolean value) {
        super(value, Captions.FLAG_DESCRIPTION_GRASS_GROW);
    }

    @Override protected GrassGrowFlag flagOf(@NotNull Boolean value) {
        return value ? GRASS_GROW_TRUE : GRASS_GROW_FALSE;
    }

}
