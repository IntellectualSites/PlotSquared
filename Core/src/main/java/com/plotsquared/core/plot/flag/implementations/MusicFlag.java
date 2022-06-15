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
import com.plotsquared.core.util.ItemUtil;
import com.sk89q.worldedit.world.item.ItemType;
import com.sk89q.worldedit.world.item.ItemTypes;
import org.checkerframework.checker.nullness.qual.NonNull;

public class MusicFlag extends PlotFlag<ItemType, MusicFlag> {

    public static final MusicFlag MUSIC_FLAG_NONE = new MusicFlag(ItemTypes.AIR);

    /**
     * Construct a new flag instance.
     *
     * @param value Flag value
     */
    protected MusicFlag(ItemType value) {
        super(value, TranslatableCaption.of("flags.flag_category_music"), TranslatableCaption.of("flags.flag_description_music"));
    }

    @Override
    public MusicFlag parse(@NonNull String input) throws FlagParseException {
        if (!input.isEmpty() && !input.contains("music_disc_")) {
            input = "music_disc_" + input;
        }
        final ItemType itemType = ItemUtil.get(input);
        if (itemType != null && itemType.getId() != null && (itemType == ItemTypes.AIR || itemType
                .getId().contains("music_disc_"))) {
            return new MusicFlag(ItemUtil.get(input));
        } else {
            throw new FlagParseException(this, input, TranslatableCaption.of("flags.flag_error_music"));
        }
    }

    @Override
    public MusicFlag merge(@NonNull ItemType newValue) {
        if (getValue().equals(ItemTypes.AIR)) {
            return new MusicFlag(newValue);
        } else if (newValue.equals(ItemTypes.AIR)) {
            return this;
        } else {
            return new MusicFlag(newValue);
        }
    }

    @Override
    public String toString() {
        return getValue().getId();
    }

    @Override
    public String getExample() {
        return "ward";
    }

    @Override
    protected MusicFlag flagOf(@NonNull ItemType value) {
        return new MusicFlag(value);
    }

}
