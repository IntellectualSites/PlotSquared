package com.plotsquared.core.plot.flag.implementations;

import com.plotsquared.core.config.Captions;
import com.plotsquared.core.plot.flag.types.BooleanFlag;
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
