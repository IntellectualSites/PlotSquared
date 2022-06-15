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
package com.plotsquared.bukkit.util.task;

import com.plotsquared.core.util.task.TaskTime;
import org.bukkit.Bukkit;
import org.checkerframework.checker.index.qual.NonNegative;

/**
 * Time converter that uses the server MSPT count to convert between
 * different time units
 */
public final class PaperTimeConverter implements TaskTime.TimeConverter {

    private static final long MIN_MS_PER_TICKS = 50L;

    @Override
    public long msToTicks(@NonNegative final long ms) {
        return Math.max(1L, (long) (ms / Math.max(MIN_MS_PER_TICKS, Bukkit.getAverageTickTime())));
    }

    @Override
    public long ticksToMs(@NonNegative final long ticks) {
        return Math.max(1L, (long) (ticks * Math.max(MIN_MS_PER_TICKS, Bukkit.getAverageTickTime())));
    }

}
