package com.plotsquared.core.plot.flag.implementations;

import com.plotsquared.core.config.Captions;
import com.plotsquared.core.plot.flag.types.BooleanFlag;
import org.jetbrains.annotations.NotNull;

public class AnimalInteractFlag extends BooleanFlag<AnimalInteractFlag> {

    public static final AnimalInteractFlag ANIMAL_INTERACT_TRUE = new AnimalInteractFlag(true);
    public static final AnimalInteractFlag ANIMAL_INTERACT_FALSE = new AnimalInteractFlag(false);

    private AnimalInteractFlag(boolean value) {
        super(value, Captions.FLAG_DESCRIPTION_ANIMAL_INTERACT);
    }

    @Override protected AnimalInteractFlag flagOf(@NotNull Boolean value) {
        return value ? ANIMAL_INTERACT_TRUE : ANIMAL_INTERACT_FALSE;
    }

}
