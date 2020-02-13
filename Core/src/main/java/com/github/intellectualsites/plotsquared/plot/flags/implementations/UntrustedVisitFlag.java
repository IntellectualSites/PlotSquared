package com.github.intellectualsites.plotsquared.plot.flags.implementations;

import com.github.intellectualsites.plotsquared.plot.config.Captions;
import com.github.intellectualsites.plotsquared.plot.flags.types.BooleanFlag;
import org.jetbrains.annotations.NotNull;

public class UntrustedVisitFlag extends BooleanFlag<UntrustedVisitFlag> {

    public static final UntrustedVisitFlag UNTRUSTED_VISIT_FLAG_TRUE = new UntrustedVisitFlag(true);

    protected UntrustedVisitFlag(boolean value) {
        super(value, Captions.FLAG_DESCRIPTION_UNTRUSTED);
    }

    @Override public String getExample() {
        return "";
    }

    @Override protected UntrustedVisitFlag flagOf(@NotNull Boolean value) {
        return new UntrustedVisitFlag(value);
    }
}
