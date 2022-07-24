/*
 * PlotSquared, a land and world management plugin for Minecraft.
 * Copyright (C) IntellectualSites <https://intellectualsites.com>
 * Copyright (C) IntellectualSites team and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.plotsquared.core.plot.flag.implementations;

import com.plotsquared.core.configuration.caption.TranslatableCaption;
import com.plotsquared.core.plot.flag.FlagParseException;
import com.plotsquared.core.plot.flag.PlotFlag;
import com.sk89q.worldedit.world.gamemode.GameMode;
import com.sk89q.worldedit.world.gamemode.GameModes;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.Arrays;
import java.util.Collection;

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
    protected GamemodeFlag(@NonNull GameMode value) {
        super(
                value,
                TranslatableCaption.of("flags.flag_category_gamemode"),
                TranslatableCaption.of("flags.flag_description_gamemode")
        );
    }

    @Override
    public GamemodeFlag parse(@NonNull String input) throws FlagParseException {
        return switch (input) {
            case "creative", "c", "1" -> flagOf(GameModes.CREATIVE);
            case "adventure", "a", "2" -> flagOf(GameModes.ADVENTURE);
            case "spectator", "sp", "3" -> flagOf(GameModes.SPECTATOR);
            case "survival", "s", "0" -> flagOf(GameModes.SURVIVAL);
            default -> flagOf(DEFAULT);
        };
    }

    @Override
    public GamemodeFlag merge(@NonNull GameMode newValue) {
        return flagOf(newValue);
    }

    @Override
    public String toString() {
        return getValue().getId();
    }

    @Override
    public String getExample() {
        return "survival";
    }

    @Override
    protected GamemodeFlag flagOf(@NonNull GameMode value) {
        return switch (value.getId()) {
            case "creative" -> GAMEMODE_FLAG_CREATIVE;
            case "adventure" -> GAMEMODE_FLAG_ADVENTURE;
            case "spectator" -> GAMEMODE_FLAG_SPECTATOR;
            case "survival" -> GAMEMODE_FLAG_SURVIVAL;
            default -> GAMEMODE_FLAG_DEFAULT;
        };
    }

    @Override
    public Collection<String> getTabCompletions() {
        return Arrays.asList("survival", "creative", "adventure", "spectator");
    }

}
