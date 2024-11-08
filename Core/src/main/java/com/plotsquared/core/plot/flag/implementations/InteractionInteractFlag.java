package com.plotsquared.core.plot.flag.implementations;

import com.plotsquared.core.configuration.caption.TranslatableCaption;
import com.plotsquared.core.plot.flag.types.BooleanFlag;
import org.checkerframework.checker.nullness.qual.NonNull;

public class InteractionInteractFlag extends BooleanFlag<InteractionInteractFlag> {

    public static final InteractionInteractFlag INTERACTION_INTERACT_TRUE = new InteractionInteractFlag(true);
    public static final InteractionInteractFlag INTERACTION_INTERACT_FALSE = new InteractionInteractFlag(false);

    private InteractionInteractFlag(boolean value) {
        super(value, TranslatableCaption.of("flags.flag_description_interaction_interact"));
    }

    @Override
    protected InteractionInteractFlag flagOf(@NonNull Boolean value) {
        return value ? INTERACTION_INTERACT_TRUE : INTERACTION_INTERACT_FALSE;
    }

}
