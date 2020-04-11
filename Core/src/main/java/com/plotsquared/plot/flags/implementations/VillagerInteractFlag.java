package com.plotsquared.plot.flags.implementations;

import com.plotsquared.config.Captions;
import com.plotsquared.plot.flags.types.BooleanFlag;
import org.jetbrains.annotations.NotNull;

public class VillagerInteractFlag extends BooleanFlag<VillagerInteractFlag> {

    public static final VillagerInteractFlag VILLAGER_INTERACT_TRUE =
        new VillagerInteractFlag(true);
    public static final VillagerInteractFlag VILLAGER_INTERACT_FALSE =
        new VillagerInteractFlag(false);

    private VillagerInteractFlag(boolean value) {
        super(value, Captions.FLAG_DESCRIPTION_VILLAGER_INTERACT);
    }

    @Override protected VillagerInteractFlag flagOf(@NotNull Boolean value) {
        return value ? VILLAGER_INTERACT_TRUE : VILLAGER_INTERACT_FALSE;
    }

}
