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

}
