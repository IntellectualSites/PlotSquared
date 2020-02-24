package com.github.intellectualsites.plotsquared.plot.flags.implementations;

import com.github.intellectualsites.plotsquared.plot.config.Captions;
import com.github.intellectualsites.plotsquared.plot.flags.types.BooleanFlag;
import org.jetbrains.annotations.NotNull;

public class PvpFlag extends BooleanFlag<PvpFlag> {

    public static final PvpFlag PVP_TRUE = new PvpFlag(true);
    public static final PvpFlag PVP_FALSE = new PvpFlag(false);

    private PvpFlag(boolean value) {
        super(value, Captions.FLAG_DESCRIPTION_PVP);
    }

    @Override protected PvpFlag flagOf(@NotNull Boolean value) {
        return value ? PVP_TRUE : PVP_FALSE;
    }

}
