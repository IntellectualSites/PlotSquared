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
package com.plotsquared.core.configuration.caption;

import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * A {@link Caption} that can be identified by a namespace-key pair
 */
public interface NamespacedCaption extends Caption {

    /**
     * Get the key that identifies this caption
     *
     * @return Caption key
     */
    @NonNull String getKey();

    /**
     * Get the namespace that this caption belongs to
     *
     * @return Namespace
     */
    @NonNull String getNamespace();

}
