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
package com.plotsquared.core.util.placeholders;

import com.google.common.base.Preconditions;
import com.plotsquared.core.player.PlotPlayer;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.jetbrains.annotations.NotNull;

/**
 * A placeholder is a keyed value that gets replaced by a {@link PlotPlayer player}-specific value at runtime
 */
@EqualsAndHashCode(of = "key") @ToString(of = "key")
public abstract class Placeholder {

    private final String key;

    public Placeholder(@NotNull final String key) {
        this.key = Preconditions.checkNotNull(key, "Key may not be null");
    }

    /**
     * Get the value of the placeholder for a particular player
     *
     * @param player Player
     * @return Placeholder value. Return {@code ""} if no placeholder value can be returned
     */
    @NotNull public abstract String getValue(@NotNull final PlotPlayer<?> player);

    /**
     * Get the placeholder key
     *
     * @return Placeholder key
     */
    @NotNull public final String getKey() {
        return this.key;
    }

}
