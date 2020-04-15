package com.plotsquared.core.plot.flag.implementations;

import com.plotsquared.core.config.Captions;
import com.plotsquared.core.plot.flag.types.BooleanFlag;
import org.jetbrains.annotations.NotNull;

public class HideInfoFlag extends BooleanFlag<HideInfoFlag> {

    public static final HideInfoFlag HIDE_INFO_TRUE = new HideInfoFlag(true);
    public static final HideInfoFlag HIDE_INFO_FALSE = new HideInfoFlag(false);

    private HideInfoFlag(boolean value) {
        super(value, Captions.FLAG_DESCRIPTION_HIDE_INFO);
    }

    @Override protected HideInfoFlag flagOf(@NotNull Boolean value) {
        return value ? HIDE_INFO_TRUE : HIDE_INFO_FALSE;
    }

}
