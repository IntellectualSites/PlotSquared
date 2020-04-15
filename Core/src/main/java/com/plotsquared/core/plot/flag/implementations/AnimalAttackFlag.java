package com.plotsquared.core.plot.flag.implementations;

import com.plotsquared.core.config.Captions;
import com.plotsquared.core.plot.flag.types.BooleanFlag;
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
