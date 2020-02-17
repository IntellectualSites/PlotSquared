package com.github.intellectualsites.plotsquared.plot.flags.implementations;

import com.github.intellectualsites.plotsquared.plot.config.Captions;
import com.github.intellectualsites.plotsquared.plot.flags.FlagParseException;
import com.github.intellectualsites.plotsquared.plot.flags.PlotFlag;
import com.sk89q.worldedit.world.gamemode.GameMode;
import com.sk89q.worldedit.world.gamemode.GameModes;
import org.jetbrains.annotations.NotNull;

public class GamemodeFlag extends PlotFlag<GameMode, GamemodeFlag> {

    public static final GameMode DEFAULT = new GameMode("default");
    static {
        GameModes.register(DEFAULT);
    }

    public static final GamemodeFlag GAMEMODE_FLAG_CREATIVE = new GamemodeFlag(GameModes.CREATIVE);
    public static final GamemodeFlag GAMEMODE_FLAG_ADVENTURE = new GamemodeFlag(GameModes.ADVENTURE);
    public static final GamemodeFlag GAMEMODE_FLAG_SPECTATOR = new GamemodeFlag(GameModes.SPECTATOR);
    public static final GamemodeFlag GAMEMODE_FLAG_SURVIVAL = new GamemodeFlag(GameModes.SURVIVAL);
    public static final GamemodeFlag GAMEMODE_FLAG_DEFAULT = new GamemodeFlag(DEFAULT);

    /**
     * Construct a new flag instance.
     *
     * @param value           Flag value
     */
    protected GamemodeFlag(@NotNull GameMode value) {
        super(value, Captions.FLAG_CATEGORY_GAMEMODE, Captions.FLAG_DESCRIPTION_GAMEMODE);
    }

    @Override public GamemodeFlag parse(@NotNull String input) throws FlagParseException {
        switch (input) {
            case "creative":
            case "c":
            case "1":
                return flagOf(GameModes.CREATIVE);
            case "adventure":
            case "a":
            case "2":
                return flagOf(GameModes.ADVENTURE);
            case "spectator":
            case "sp":
            case "3":
                return flagOf(GameModes.SPECTATOR);
            case "survival":
            case "s":
            case "0":
                return flagOf(GameModes.SURVIVAL);
            default:
                return flagOf(DEFAULT);
        }
    }

    @Override public GamemodeFlag merge(@NotNull GameMode newValue) {
        return flagOf(newValue);
    }

    @Override public String toString() {
        return getValue().getId();
    }

    @Override public String getExample() {
        return "survival";
    }

    @Override protected GamemodeFlag flagOf(@NotNull GameMode value) {
        switch (value.getId()) {
            case "creative":
                return GAMEMODE_FLAG_CREATIVE;
            case "adventure":
                return GAMEMODE_FLAG_ADVENTURE;
            case "spectator":
                return GAMEMODE_FLAG_SPECTATOR;
            case "survival":
                return GAMEMODE_FLAG_SURVIVAL;
            default:
                return GAMEMODE_FLAG_DEFAULT;
        }
    }

}
