package com.github.intellectualsites.plotsquared.commands;

public enum RequiredType {
    CONSOLE, PLAYER, NONE;

    public boolean allows(CommandCaller player) {
        if (this == RequiredType.NONE) {
            return true;
        }
        return this == player.getSuperCaller();
    }
}
