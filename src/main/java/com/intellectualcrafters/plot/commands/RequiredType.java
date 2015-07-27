package com.intellectualcrafters.plot.commands;

import com.intellectualcrafters.plot.object.ConsolePlayer;
import com.intellectualcrafters.plot.object.PlotPlayer;

public enum RequiredType {
    CONSOLE,
    PLAYER,
    NONE;
    
    public boolean allows(PlotPlayer player) {
        switch (this) {
            case NONE:
                return true;
            case PLAYER:
                return !ConsolePlayer.isConsole(player);
            case CONSOLE:
                return ConsolePlayer.isConsole(player);
            default:
                return false;
        }
    }
}
