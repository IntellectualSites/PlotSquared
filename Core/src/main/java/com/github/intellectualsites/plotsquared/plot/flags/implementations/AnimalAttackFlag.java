package com.github.intellectualsites.plotsquared.plot.flags.implementations;

import com.github.intellectualsites.plotsquared.plot.config.Captions;
import com.github.intellectualsites.plotsquared.plot.flags.types.BooleanFlag;
import org.jetbrains.annotations.NotNull;

public class AnimalAttackFlag extends BooleanFlag<AnimalAttackFlag> {

    public static final AnimalAttackFlag ANIMAL_ATTACK_TRUE = new AnimalAttackFlag(true);
    public static final AnimalAttackFlag ANIMAL_ATTACK_FALSE = new AnimalAttackFlag(false);

    private AnimalAttackFlag(boolean value) {
        super(value, Captions.FLAG_DESCRIPTION_ANIMAL_ATTACK);
    }

    @Override protected AnimalAttackFlag flagOf(@NotNull Boolean value) {
        return value ? ANIMAL_ATTACK_TRUE : ANIMAL_ATTACK_FALSE;
    }

}
