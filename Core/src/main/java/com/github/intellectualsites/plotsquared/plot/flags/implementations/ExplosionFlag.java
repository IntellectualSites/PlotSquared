package com.github.intellectualsites.plotsquared.plot.flags.implementations;

import com.github.intellectualsites.plotsquared.plot.config.Captions;
import com.github.intellectualsites.plotsquared.plot.flags.types.BooleanFlag;

public class ExplosionFlag extends BooleanFlag {

    public ExplosionFlag() {
        super(Captions.FLAG_DESCRIPTION_EXPLOSION);
    }

    @Override public String getExample() {
        return "true";
    }

}
