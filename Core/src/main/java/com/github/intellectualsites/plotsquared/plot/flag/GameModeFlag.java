package com.github.intellectualsites.plotsquared.plot.flag;

import com.github.intellectualsites.plotsquared.plot.config.Captions;
import com.sk89q.worldedit.world.gamemode.GameMode;
import com.sk89q.worldedit.world.gamemode.GameModes;

public class GameModeFlag extends Flag<GameMode> {

    public GameModeFlag(String name) {
        super(Captions.FLAG_CATEGORY_GAMEMODE, name);
    }

    @Override public String valueToString(Object value) {
        return ((GameMode) value).getName();
    }

    @Override public GameMode parseValue(String value) {
        switch (value.toLowerCase()) {
            case "creative":
            case "c":
            case "1":
                return GameModes.CREATIVE;
            case "adventure":
            case "a":
            case "2":
                return GameModes.ADVENTURE;
            case "spectator":
            case "sp":
            case "3":
                return GameModes.SPECTATOR;
            case "survival":
            case "s":
            case "0":
            default:
                return GameModes.SURVIVAL;
        }
    }

    @Override public String getValueDescription() {
        return Captions.FLAG_ERROR_GAMEMODE.getTranslated();
    }
}
