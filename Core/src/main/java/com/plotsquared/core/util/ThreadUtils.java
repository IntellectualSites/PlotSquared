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

import com.plotsquared.core.PlotSquared;

public final class ThreadUtils {

    private ThreadUtils() {
        throw new UnsupportedOperationException(
                "This is a utility class and cannot be instantiated");
    }

    /**
     * Throws {@link IllegalStateException} if the method
     * is called from the server main thread
     *
     * @param message Message describing the issue
     */
    public static void catchSync(final String message) {
        if (PlotSquared.get().isMainThread(Thread.currentThread())) {
            throw new IllegalStateException(message);
        }
    }

    /**
     * Throws {@link IllegalStateException} if the method
     * is not called from the server main thread
     *
     * @param message Message describing the issue
     */
    public static void catchAsync(final String message) {
        if (!PlotSquared.get().isMainThread(Thread.currentThread())) {
            throw new IllegalStateException(message);
        }
    }

}
