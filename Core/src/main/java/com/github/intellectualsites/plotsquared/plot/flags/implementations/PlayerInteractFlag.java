package com.github.intellectualsites.plotsquared.plot.flags.implementations;

import com.github.intellectualsites.plotsquared.plot.config.Captions;
import com.github.intellectualsites.plotsquared.plot.flags.types.BooleanFlag;
import org.jetbrains.annotations.NotNull;

public class PlayerInteractFlag extends BooleanFlag<PlayerInteractFlag> {

    public static final PlayerInteractFlag PLAYER_INTERACT_TRUE = new PlayerInteractFlag(true);
    public static final PlayerInteractFlag PLAYER_INTERACT_FALSE = new PlayerInteractFlag(false);

    private PlayerInteractFlag(boolean value) {
        super(value, Captions.FLAG_DESCRIPTION_PLAYER_INTERACT);
    }

    @Override protected PlayerInteractFlag flagOf(@NotNull Boolean value) {
        return value ? PLAYER_INTERACT_TRUE : PLAYER_INTERACT_FALSE;
    }

}
