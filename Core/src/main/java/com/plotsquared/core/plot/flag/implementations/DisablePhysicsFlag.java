package com.plotsquared.core.plot.flag.implementations;

import com.plotsquared.core.config.Captions;
import com.plotsquared.core.plot.flag.types.BooleanFlag;
import org.jetbrains.annotations.NotNull;

public class DisablePhysicsFlag extends BooleanFlag<DisablePhysicsFlag> {

    public static final DisablePhysicsFlag DISABLE_PHYSICS_TRUE = new DisablePhysicsFlag(true);
    public static final DisablePhysicsFlag DISABLE_PHYSICS_FALSE = new DisablePhysicsFlag(false);

    private DisablePhysicsFlag(boolean value) {
        super(value, Captions.FLAG_DESCRIPTION_DISABLE_PHYSICS);
    }

    @Override protected DisablePhysicsFlag flagOf(@NotNull Boolean value) {
        return value ? DISABLE_PHYSICS_TRUE : DISABLE_PHYSICS_FALSE;
    }

}
