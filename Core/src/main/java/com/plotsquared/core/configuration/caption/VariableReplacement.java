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
package com.plotsquared.core.configuration.caption;

import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * Key-value pair used as replacement of variables in {@link com.plotsquared.core.configuration.Caption captions}
 */
@ToString
@EqualsAndHashCode
public final class VariableReplacement {

    private final String key;
    private final String value;

    private VariableReplacement(@NotNull final String key, @NotNull final String value) {
        this.key = Objects.requireNonNull(key, "Key may not be null");
        this.value = Objects.requireNonNull(value, "Value may not be null");
    }

    /**
     * Create a new variable replacement from a key-value pair
     *
     * @param key   Replacement key
     * @param value Replacement value
     * @return Replacement instance
     */
    @NotNull public static VariableReplacement keyed(@NotNull final String key,
        @NotNull final String value) {
        return new VariableReplacement(key, value);
    }

    /**
     * Get the replacement key
     *
     * @return Replacement key
     */
    @NotNull public String getKey() {
        return this.key;
    }

    /**
     * Get the replacement value
     *
     * @return Replacement value
     */
    @NotNull public String getValue() {
        return this.value;
    }

}
