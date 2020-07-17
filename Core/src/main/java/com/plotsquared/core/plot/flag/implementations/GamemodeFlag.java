/*
 *       _____  _       _    _____                                _
 *      |  __ \| |     | |  / ____|                              | |
 *      | |__) | | ___ | |_| (___   __ _ _   _  __ _ _ __ ___  __| |
 *      |  ___/| |/ _ \| __|\___ \ / _` | | | |/ _` | '__/ _ \/ _` |
 *      | |    | | (_) | |_ ____) | (_| | |_| | (_| | | |  __/ (_| |
 *      |_|    |_|\___/ \__|_____/ \__, |\__,_|\__,_|_|  \___|\__,_|
 *                                    | |
 *                                    |_|
 *            PlotSquared plot management system for Minecraft
 *                  Copyright (C) 2020 IntellectualSites
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.plotsquared.core.plot.flag.implementations;

import com.plotsquared.core.configuration.Captions;
import com.plotsquared.core.plot.flag.FlagParseException;
import com.plotsquared.core.plot.flag.PlotFlag;
import com.sk89q.worldedit.world.gamemode.GameMode;
import com.sk89q.worldedit.world.gamemode.GameModes;

import javax.annotation.Nonnull;

public class GamemodeFlag extends PlotFlag<GameMode, GamemodeFlag> {

    public static final GameMode DEFAULT = new GameMode("default");
    public static final GamemodeFlag GAMEMODE_FLAG_CREATIVE = new GamemodeFlag(GameModes.CREATIVE);
    public static final GamemodeFlag GAMEMODE_FLAG_ADVENTURE =
        new GamemodeFlag(GameModes.ADVENTURE);
    public static final GamemodeFlag GAMEMODE_FLAG_SPECTATOR =
        new GamemodeFlag(GameModes.SPECTATOR);
    public static final GamemodeFlag GAMEMODE_FLAG_SURVIVAL = new GamemodeFlag(GameModes.SURVIVAL);
    public static final GamemodeFlag GAMEMODE_FLAG_DEFAULT = new GamemodeFlag(DEFAULT);

    static {
        GameModes.register(DEFAULT);
    }

    /**
     * Construct a new flag instance.
     *
     * @param value Flag value
     */
    protected GamemodeFlag(@Nonnull GameMode value) {
        super(value, Captions.FLAG_CATEGORY_GAMEMODE, Captions.FLAG_DESCRIPTION_GAMEMODE);
    }

    @Override public GamemodeFlag parse(@Nonnull String input) throws FlagParseException {
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

    @Override public GamemodeFlag merge(@Nonnull GameMode newValue) {
        return flagOf(newValue);
    }

    @Override public String toString() {
        return getValue().getId();
    }

    @Override public String getExample() {
        return "survival";
    }

    @Override protected GamemodeFlag flagOf(@Nonnull GameMode value) {
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
