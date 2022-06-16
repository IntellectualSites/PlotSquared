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
package com.plotsquared.core.util.task;

import com.google.common.base.Objects;
import org.checkerframework.checker.index.qual.NonNegative;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * Task timings
 */
public final class TaskTime {

    private final long time;
    private final TaskUnit unit;

    private TaskTime(@NonNegative final long time, final @NonNull TaskUnit unit) {
        this.time = time;
        this.unit = unit;
    }

    /**
     * Create a new task time in seconds
     *
     * @param seconds Seconds
     * @return Created task time instance
     */
    public static @NonNull TaskTime seconds(@NonNegative final long seconds) {
        return new TaskTime(seconds * 1000L, TaskUnit.MILLISECONDS);
    }

    /**
     * Create a new task time in server ticks
     *
     * @param ticks Server ticks
     * @return Created task time instance
     */
    public static @NonNull TaskTime ticks(@NonNegative final long ticks) {
        return new TaskTime(ticks, TaskUnit.TICKS);
    }

    /**
     * Create a new task time in milliseconds
     *
     * @param ms Milliseconds
     * @return Created task time instance
     */
    public static @NonNull TaskTime ms(@NonNegative final long ms) {
        return new TaskTime(ms, TaskUnit.MILLISECONDS);
    }

    /**
     * Get the task time
     *
     * @return Task time
     */
    @NonNegative
    public long getTime() {
        return this.time;
    }

    /**
     * Get the time unit
     *
     * @return Time unit
     */
    public @NonNull TaskUnit getUnit() {
        return this.unit;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final TaskTime taskTime = (TaskTime) o;
        return getTime() == taskTime.getTime() && getUnit() == taskTime.getUnit();
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getTime(), getUnit());
    }


    public enum TaskUnit {
        TICKS,
        MILLISECONDS
    }


    public interface TimeConverter {

        /**
         * Convert from milliseconds to server ticks
         *
         * @param ms Milliseconds
         * @return Server ticks
         */
        @NonNegative
        long msToTicks(@NonNegative final long ms);

        /**
         * Convert from server ticks to milliseconds
         *
         * @param ticks Server ticks
         * @return Milliseconds
         */
        @NonNegative
        long ticksToMs(@NonNegative final long ticks);

        /**
         * Convert the task time to server ticks
         *
         * @param taskTime Task time
         * @return Server ticks
         */
        @NonNegative
        default long toTicks(final @NonNull TaskTime taskTime) {
            if (taskTime.getUnit() == TaskUnit.TICKS) {
                return taskTime.getTime();
            } else {
                return this.msToTicks(taskTime.getTime());
            }
        }

        /**
         * Convert the task time to milliseconds
         *
         * @param taskTime Task time
         * @return Milliseconds
         */
        @NonNegative
        default long toMs(final @NonNull TaskTime taskTime) {
            if (taskTime.getUnit() == TaskUnit.MILLISECONDS) {
                return taskTime.getTime();
            } else {
                return this.ticksToMs(taskTime.getTime());
            }
        }

    }

}
