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
package com.plotsquared.core.util.task;

import com.google.common.base.Objects;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

/**
 * Task timings
 */
public final class TaskTime {

    private final long time;
    private final TaskUnit unit;

    private TaskTime(@Nonnegative final long time, @Nonnull final TaskUnit unit) {
        this.time = time;
        this.unit = unit;
    }

    /**
     * Create a new task time in seconds
     * 
     * @param seconds Seconds
     * @return Created task time instance
     */
    @Nonnull public static TaskTime seconds(@Nonnegative final long seconds) {
        return new TaskTime(seconds * 1000L, TaskUnit.MILLISECONDS);
    }
    
    /**
     * Create a new task time in server ticks
     *
     * @param ticks Server ticks
     * @return Created task time instance
     */
    @Nonnull public static TaskTime ticks(@Nonnegative final long ticks) {
        return new TaskTime(ticks, TaskUnit.TICKS);
    }

    /**
     * Create a new task time in milliseconds
     *
     * @param ms Milliseconds
     * @return Created task time instance
     */
    @Nonnull public static TaskTime ms(@Nonnegative final long ms) {
        return new TaskTime(ms, TaskUnit.MILLISECONDS);
    }

    /**
     * Get the task time
     *
     * @return Task time
     */
    @Nonnegative public long getTime() {
        return this.time;
    }

    /**
     * Get the time unit
     *
     * @return Time unit
     */
    @Nonnull public TaskUnit getUnit() {
        return this.unit;
    }

    @Override public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final TaskTime taskTime = (TaskTime) o;
        return getTime() == taskTime.getTime() && getUnit() == taskTime.getUnit();
    }

    @Override public int hashCode() {
        return Objects.hashCode(getTime(), getUnit());
    }


    public enum TaskUnit {
        TICKS, MILLISECONDS
    }
    
    
    public interface TimeConverter {

        /**
         * Convert from milliseconds to server ticks
         * 
         * @param ms Milliseconds
         * @return Server ticks
         */
        @Nonnegative long msToTicks(@Nonnegative final long ms);

        /**
         * Convert from server ticks to milliseconds
         * 
         * @param ticks Server ticks
         * @return Milliseconds
         */
        @Nonnegative long ticksToMs(@Nonnegative final long ticks);

        /**
         * Convert the task time to server ticks
         * 
         * @param taskTime Task time
         * @return Server ticks
         */
        @Nonnegative default long toTicks(@Nonnull final TaskTime taskTime) {
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
        @Nonnegative default long toMs(@Nonnull final TaskTime taskTime) {
            if (taskTime.getUnit() == TaskUnit.MILLISECONDS) {
                return taskTime.getTime();
            } else {
                return this.ticksToMs(taskTime.getTime());
            }
        }
        
    }

}
