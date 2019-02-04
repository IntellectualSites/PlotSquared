package com.github.intellectualsites.plotsquared.plot.commands;

import com.github.intellectualsites.plotsquared.commands.CommandCaller;

public enum RequiredType {
    CONSOLE, PLAYER, NONE;

    public boolean allows(CommandCaller player) {
        switch (this) {
            case NONE:
                return true;
            default:
                return this == player.getSuperCaller();
        }
    }
}
