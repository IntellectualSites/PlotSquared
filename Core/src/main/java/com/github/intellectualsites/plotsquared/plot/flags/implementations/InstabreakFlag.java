package com.github.intellectualsites.plotsquared.plot.flags.implementations;

import com.github.intellectualsites.plotsquared.plot.config.Captions;
import com.github.intellectualsites.plotsquared.plot.flags.types.BooleanFlag;
import org.jetbrains.annotations.NotNull;

public class InstabreakFlag extends BooleanFlag<InstabreakFlag> {

    public static final InstabreakFlag INSTABREAK_TRUE = new InstabreakFlag(true);
    public static final InstabreakFlag INSTABREAK_FALSE = new InstabreakFlag(false);

    private InstabreakFlag(boolean value) {
        super(value, Captions.FLAG_DESCRIPTION_INSTABREAK);
    }

    @Override protected InstabreakFlag flagOf(@NotNull Boolean value) {
        return value ? INSTABREAK_TRUE : INSTABREAK_FALSE;
    }

}
