package com.plotsquared.core.plot.flag.implementations;

import com.plotsquared.core.config.Captions;
import com.plotsquared.core.plot.flag.types.NonNegativeIntegerFlag;
import org.jetbrains.annotations.NotNull;

public class EntityCapFlag extends NonNegativeIntegerFlag<EntityCapFlag> {
    public static final EntityCapFlag ENTITY_CAP_UNLIMITED = new EntityCapFlag(Integer.MAX_VALUE);

    protected EntityCapFlag(int value) {
        super(value, Captions.FLAG_DESCRIPTION_ENTITY_CAP);
    }

    @Override protected EntityCapFlag flagOf(@NotNull Integer value) {
        return new EntityCapFlag(value);
    }
}
