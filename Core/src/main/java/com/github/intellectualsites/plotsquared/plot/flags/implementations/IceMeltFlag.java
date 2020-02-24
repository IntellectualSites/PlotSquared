package com.github.intellectualsites.plotsquared.plot.flags.implementations;

import com.github.intellectualsites.plotsquared.plot.config.Captions;
import com.github.intellectualsites.plotsquared.plot.flags.types.BooleanFlag;
import org.jetbrains.annotations.NotNull;

public class IceMeltFlag extends BooleanFlag<IceMeltFlag> {

    public static final IceMeltFlag ICE_MELT_TRUE = new IceMeltFlag(true);
    public static final IceMeltFlag ICE_MELT_FALSE = new IceMeltFlag(false);

    private IceMeltFlag(boolean value) {
        super(value, Captions.FLAG_DESCRIPTION_ICE_MELT);
    }

    @Override protected IceMeltFlag flagOf(@NotNull Boolean value) {
        return value ? ICE_MELT_TRUE : ICE_MELT_FALSE;
    }

}
