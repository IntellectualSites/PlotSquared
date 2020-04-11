package com.plotsquared.plot.flags.implementations;

import com.plotsquared.config.Captions;
import com.plotsquared.plot.flags.types.NonNegativeIntegerFlag;
import org.jetbrains.annotations.NotNull;

public class AnimalCapFlag extends NonNegativeIntegerFlag<AnimalCapFlag> {
    public static final AnimalCapFlag ANIMAL_CAP_UNLIMITED = new AnimalCapFlag(Integer.MAX_VALUE);

    protected AnimalCapFlag(int value) {
        super(value, Captions.FLAG_DESCRIPTION_ANIMAL_CAP);
    }

    @Override protected AnimalCapFlag flagOf(@NotNull Integer value) {
        return new AnimalCapFlag(value);
    }
}
