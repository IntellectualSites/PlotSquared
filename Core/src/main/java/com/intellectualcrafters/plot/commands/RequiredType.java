package com.intellectualcrafters.plot.commands;

import com.plotsquared.general.commands.CommandCaller;

public enum RequiredType {
    CONSOLE, PLAYER, NONE;
    
    public boolean allows(final CommandCaller player) {
        switch (this) {
            case NONE:
                return true;
            default:
                return this == player.getSuperCaller();
        }
    }
}
