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

package com.plotsquared.core.location;

import org.jetbrains.annotations.NotNull;

/**
 * PlotSquared representation of a platform world
 *
 * @param <T> Platform world type
 */
public interface World<T> {

    /**
     * Get the platform world represented by this world
     *
     * @return Platform world
     */
    @NotNull T getPlatformWorld();

    /**
     * Get the name of the world
     *
     * @return World name
     */
    @NotNull String getName();

    /**
     * Get a {@link NullWorld} implementation
     *
     * @return NullWorld instance
     */
    static <T> NullWorld<T> nullWorld() {
        return new NullWorld<>();
    }

    class NullWorld<T> implements World<T> {

        private NullWorld() {
        }

        @NotNull @Override public T getPlatformWorld() {
            throw new UnsupportedOperationException("Cannot get platform world from NullWorld");
        }

        @Override public @NotNull String getName() {
            return "";
        }

        @Override public boolean equals(final Object obj) {
            return obj instanceof NullWorld;
        }

        @Override public int hashCode() {
            return "null".hashCode();
        }

    }

}
