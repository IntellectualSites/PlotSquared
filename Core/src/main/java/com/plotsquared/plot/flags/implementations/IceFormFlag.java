package com.plotsquared.plot.flags.implementations;

import com.plotsquared.config.Captions;
import com.plotsquared.plot.flags.types.BooleanFlag;
import org.jetbrains.annotations.NotNull;

public class IceFormFlag extends BooleanFlag<IceFormFlag> {

    public static final IceFormFlag ICE_FORM_TRUE = new IceFormFlag(true);
    public static final IceFormFlag ICE_FORM_FALSE = new IceFormFlag(false);

    private IceFormFlag(boolean value) {
        super(value, Captions.FLAG_DESCRIPTION_ICE_FORM);
    }

    @Override protected IceFormFlag flagOf(@NotNull Boolean value) {
        return value ? ICE_FORM_TRUE : ICE_FORM_FALSE;
    }

}
