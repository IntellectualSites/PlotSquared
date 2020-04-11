package com.plotsquared.plot.flags.implementations;

import com.plotsquared.config.Captions;
import com.plotsquared.plot.flags.types.BooleanFlag;
import org.jetbrains.annotations.NotNull;

public class UntrustedVisitFlag extends BooleanFlag<UntrustedVisitFlag> {

    public static final UntrustedVisitFlag UNTRUSTED_VISIT_FLAG_TRUE = new UntrustedVisitFlag(true);

    protected UntrustedVisitFlag(boolean value) {
        super(value, Captions.FLAG_DESCRIPTION_UNTRUSTED);
    }

    @Override protected UntrustedVisitFlag flagOf(@NotNull Boolean value) {
        return new UntrustedVisitFlag(value);
    }

}
