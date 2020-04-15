package com.plotsquared.core.plot.flag.implementations;

import com.plotsquared.core.config.Captions;
import com.plotsquared.core.plot.flag.types.BooleanFlag;
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
