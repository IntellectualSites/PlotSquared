package com.plotsquared.core.plot.flag.implementations;

import com.plotsquared.core.config.Captions;
import com.plotsquared.core.plot.flag.types.BooleanFlag;
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
