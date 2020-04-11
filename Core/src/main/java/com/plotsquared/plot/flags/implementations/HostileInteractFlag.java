package com.plotsquared.plot.flags.implementations;

import com.plotsquared.config.Captions;
import com.plotsquared.plot.flags.types.BooleanFlag;
import org.jetbrains.annotations.NotNull;

public class HostileInteractFlag extends BooleanFlag<HostileInteractFlag> {

    public static final HostileInteractFlag HOSTILE_INTERACT_TRUE = new HostileInteractFlag(true);
    public static final HostileInteractFlag HOSTILE_INTERACT_FALSE = new HostileInteractFlag(false);

    private HostileInteractFlag(boolean value) {
        super(value, Captions.FLAG_DESCRIPTION_HOSTILE_INTERACT);
    }

    @Override protected HostileInteractFlag flagOf(@NotNull Boolean value) {
        return value ? HOSTILE_INTERACT_TRUE : HOSTILE_INTERACT_FALSE;
    }

}
