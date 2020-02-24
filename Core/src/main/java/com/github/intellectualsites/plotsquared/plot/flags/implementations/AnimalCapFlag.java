package com.github.intellectualsites.plotsquared.plot.flags.implementations;

import com.github.intellectualsites.plotsquared.plot.config.Captions;
import com.github.intellectualsites.plotsquared.plot.flags.types.NonNegativeIntegerFlag;
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
