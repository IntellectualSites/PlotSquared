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
package com.plotsquared.core.util.placeholders;

import com.google.common.base.Preconditions;
import com.plotsquared.core.player.PlotPlayer;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * A placeholder is a keyed value that gets replaced by a {@link PlotPlayer player}-specific value at runtime
 */
public abstract class Placeholder {

    private final String key;

    public Placeholder(final @NonNull String key) {
        this.key = Preconditions.checkNotNull(key, "Key may not be null");
    }

    /**
     * Get the value of the placeholder for a particular player
     *
     * @param player Player
     * @return Placeholder value. Return {@code ""} if no placeholder value can be returned
     */
    public @NonNull
    abstract String getValue(final @NonNull PlotPlayer<?> player);

    /**
     * Get the placeholder key
     *
     * @return Placeholder key
     */
    public @NonNull
    final String getKey() {
        return this.key;
    }

}
