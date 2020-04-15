package com.plotsquared.core.plot.flag.implementations;

import com.plotsquared.core.config.Captions;
import com.plotsquared.core.plot.flag.types.BooleanFlag;
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
