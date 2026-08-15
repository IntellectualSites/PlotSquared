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
import com.plotsquared.core.plot.flag.types.BooleanFlag;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * @since 7.3.7
 */

public class TileDropFlag extends BooleanFlag<TileDropFlag> {

    public static final TileDropFlag TILE_DROP_TRUE = new TileDropFlag(true);
    public static final TileDropFlag TILE_DROP_FALSE = new TileDropFlag(false);

    private TileDropFlag(boolean value) {
        super(value, TranslatableCaption.of("flags.flag_description_tile_drop"));
    }

    @Override
    protected TileDropFlag flagOf(@NonNull Boolean value) {
        return value ? TILE_DROP_TRUE : TILE_DROP_FALSE;
    }

}
