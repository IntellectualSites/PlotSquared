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

/**
 * A task that can be run and cancelled (if repeating)
 */
public interface PlotSquaredTask extends Runnable {

    /**
     * Get a new {@link NullTask}
     *
     * @return Null task instance
     */
    static NullTask nullTask() {
        return new NullTask();
    }

    /**
     * Run the task. Don't override this, instead
     * implement {@link #runTask()}
     */
    @Override
    default void run() {
        if (isCancelled()) {
            return;
        }
        this.runTask();
    }

    /**
     * Run the task
     */
    void runTask();

    /**
     * Check if the task has been cancelled
     *
     * @return {@code true} if the tasks is cancelled,
     *         {@code false} if not
     */
    boolean isCancelled();

    /**
     * Cancel the task
     */
    void cancel();

    /**
     * Task that does nothing and is always cancelled
     */
    class NullTask implements PlotSquaredTask {

        @Override
        public void runTask() {
        }

        @Override
        public boolean isCancelled() {
            return true;
        }

        @Override
        public void cancel() {
        }

    }

}
