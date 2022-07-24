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
    protected GuestGamemodeFlag(@NonNull GameMode value) {
        super(
                value,
                TranslatableCaption.of("flags.flag_category_gamemode"),
                TranslatableCaption.of("flags.flag_description_guest_gamemode")
        );
    }

    @Override
    public GuestGamemodeFlag parse(@NonNull String input) throws FlagParseException {
        return switch (input) {
            case "creative", "c", "1" -> flagOf(GameModes.CREATIVE);
            case "adventure", "a", "2" -> flagOf(GameModes.ADVENTURE);
            case "spectator", "sp", "3" -> flagOf(GameModes.SPECTATOR);
            case "survival", "s", "0" -> flagOf(GameModes.SURVIVAL);
            default -> flagOf(GamemodeFlag.DEFAULT);
        };
    }

    @Override
    public GuestGamemodeFlag merge(@NonNull GameMode newValue) {
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
    protected GuestGamemodeFlag flagOf(@NonNull GameMode value) {
        return switch (value.getId()) {
            case "creative" -> GUEST_GAMEMODE_FLAG_CREATIVE;
            case "adventure" -> GUEST_GAMEMODE_FLAG_ADVENTURE;
            case "spectator" -> GUEST_GAMEMODE_FLAG_SPECTATOR;
            case "survival" -> GUEST_GAMEMODE_FLAG_SURVIVAL;
            default -> GUEST_GAMEMODE_FLAG_DEFAULT;
        };
    }

}
