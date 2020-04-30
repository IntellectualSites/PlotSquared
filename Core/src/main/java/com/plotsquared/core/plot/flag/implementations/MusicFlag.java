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
import com.plotsquared.core.util.ItemUtil;
import com.sk89q.worldedit.world.item.ItemType;
import com.sk89q.worldedit.world.item.ItemTypes;
import org.jetbrains.annotations.NotNull;

public class MusicFlag extends PlotFlag<ItemType, MusicFlag> {

    public static final MusicFlag MUSIC_FLAG_NONE = new MusicFlag(ItemTypes.AIR);

    /**
     * Construct a new flag instance.
     *
     * @param value Flag value
     */
    protected MusicFlag(ItemType value) {
        super(value, Captions.FLAG_CATEGORY_MUSIC, Captions.FLAG_DESCRIPTION_MUSIC);
    }

    @Override public MusicFlag parse(@NotNull String input) throws FlagParseException {
        if (!input.isEmpty() && !input.contains("music_disc_")) {
            input = "music_disc_" + input;
        }
        final ItemType itemType = ItemUtil.get(input);
        if (itemType != null && itemType.getId() != null && (itemType == ItemTypes.AIR || itemType
            .getId().contains("music_disc_"))) {
            return new MusicFlag(ItemUtil.get(input));
        } else {
            throw new FlagParseException(this, input, Captions.FLAG_ERROR_MUSIC);
        }
    }

    @Override public MusicFlag merge(@NotNull ItemType newValue) {
        if (getValue().equals(ItemTypes.AIR)) {
            return new MusicFlag(newValue);
        } else if (newValue.equals(ItemTypes.AIR)) {
            return this;
        } else {
            return new MusicFlag(newValue);
        }
    }

    @Override public String toString() {
        return getValue().getId();
    }

    @Override public String getExample() {
        return "ward";
    }

    @Override protected MusicFlag flagOf(@NotNull ItemType value) {
        return new MusicFlag(value);
    }

}
