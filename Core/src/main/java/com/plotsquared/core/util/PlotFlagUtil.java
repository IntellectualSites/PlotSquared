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
package com.plotsquared.core.util;

import com.plotsquared.core.plot.PlotArea;
import com.plotsquared.core.plot.flag.PlotFlag;
import com.plotsquared.core.plot.flag.implementations.EntityChangeBlockFlag;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.Objects;

/**
 * Util class for generic methods relating to plot flags.
 *
 * @since 6.10.4
 */
public final class PlotFlagUtil {

    private PlotFlagUtil() {
        //No-op
    }

    /**
     * Check if the value of a {@link PlotFlag} matches the given value. If
     * road flags are disabled for the given plot area, returns false.
     *
     * @param flagClass boolean flag to get value of
     * @param value     boolean value to check flag value against
     * @param <T>       The flag value type
     * @return true if road flag value matches with road flags enabled
     * @since 6.10.4
     */
    public static <T> boolean isAreaRoadFlagsAndFlagEquals(
            PlotArea area, final Class<? extends PlotFlag<T, ?>> flagClass, T value
    ) {
        return area.isRoadFlags() && Objects.equals(area.getRoadFlag(flagClass), value);
    }

    /**
     * Check if an entity may change a block that does not belong to an owned plot, i.e. a block of a road, a wall or
     * an unowned plot. The change is only allowed if the area has road flags and {@code entity-change-block} is set to
     * {@code true} among them. An area without road flags denies the change, like a plot without the flag does.
     *
     * @param area the plot area the block is in
     * @return {@code true} if the entity may change the block
     * @since 7.6.2
     */
    public static boolean mayEntityChangeBlockOnRoad(final @NonNull PlotArea area) {
        return mayEntityChangeBlockOnRoad(area.isRoadFlags(), area.getRoadFlag(EntityChangeBlockFlag.class));
    }

    static boolean mayEntityChangeBlockOnRoad(final boolean roadFlags, final boolean entityChangeBlock) {
        return roadFlags && entityChangeBlock;
    }

}
