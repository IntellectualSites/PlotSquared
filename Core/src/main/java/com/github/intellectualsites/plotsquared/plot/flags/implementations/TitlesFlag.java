package com.github.intellectualsites.plotsquared.plot.flags.implementations;

import com.github.intellectualsites.plotsquared.plot.config.Captions;
import com.github.intellectualsites.plotsquared.plot.flags.types.BooleanFlag;
import org.jetbrains.annotations.NotNull;

public class TitlesFlag extends BooleanFlag<TitlesFlag> {

    public static final TitlesFlag TITLES_TRUE = new TitlesFlag(true);
    public static final TitlesFlag TITLES_FALSE = new TitlesFlag(false);

    private TitlesFlag(boolean value) {
        super(value, Captions.FLAG_DESCRIPTION_TITLES);
    }

    @Override protected TitlesFlag flagOf(@NotNull Boolean value) {
        return value ? TITLES_TRUE : TITLES_FALSE;
    }

}
