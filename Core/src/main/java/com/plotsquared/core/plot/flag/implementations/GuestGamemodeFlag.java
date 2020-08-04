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

import com.plotsquared.core.configuration.caption.TranslatableCaption;
import com.plotsquared.core.plot.flag.FlagParseException;
import com.plotsquared.core.plot.flag.PlotFlag;
import com.sk89q.worldedit.world.gamemode.GameMode;
import com.sk89q.worldedit.world.gamemode.GameModes;

import javax.annotation.Nonnull;

public class GuestGamemodeFlag extends PlotFlag<GameMode, GuestGamemodeFlag> {

    public static final GuestGamemodeFlag GUEST_GAMEMODE_FLAG_CREATIVE =
        new GuestGamemodeFlag(GameModes.CREATIVE);
    public static final GuestGamemodeFlag GUEST_GAMEMODE_FLAG_ADVENTURE =
        new GuestGamemodeFlag(GameModes.ADVENTURE);
    public static final GuestGamemodeFlag GUEST_GAMEMODE_FLAG_SPECTATOR =
        new GuestGamemodeFlag(GameModes.SPECTATOR);
    public static final GuestGamemodeFlag GUEST_GAMEMODE_FLAG_SURVIVAL =
        new GuestGamemodeFlag(GameModes.SURVIVAL);
    public static final GuestGamemodeFlag GUEST_GAMEMODE_FLAG_DEFAULT =
        new GuestGamemodeFlag(GamemodeFlag.DEFAULT);

    /**
     * Construct a new flag instance.
     *
     * @param value Flag value
     */
    protected GuestGamemodeFlag(@Nonnull GameMode value) {
        super(value, TranslatableCaption.of("flags.flag_category_gamemode"), TranslatableCaption.of("flags.flag_description_guest_gamemode"));
    }

    @Override public GuestGamemodeFlag parse(@Nonnull String input) throws FlagParseException {
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
                return flagOf(GamemodeFlag.DEFAULT);
        }
    }

    @Override public GuestGamemodeFlag merge(@Nonnull GameMode newValue) {
        return flagOf(newValue);
    }

    @Override public String toString() {
        return getValue().getId();
    }

    @Override public String getExample() {
        return "survival";
    }

    @Override protected GuestGamemodeFlag flagOf(@Nonnull GameMode value) {
        switch (value.getId()) {
            case "creative":
                return GUEST_GAMEMODE_FLAG_CREATIVE;
            case "adventure":
                return GUEST_GAMEMODE_FLAG_ADVENTURE;
            case "spectator":
                return GUEST_GAMEMODE_FLAG_SPECTATOR;
            case "survival":
                return GUEST_GAMEMODE_FLAG_SURVIVAL;
            default:
                return GUEST_GAMEMODE_FLAG_DEFAULT;
        }
    }

}
