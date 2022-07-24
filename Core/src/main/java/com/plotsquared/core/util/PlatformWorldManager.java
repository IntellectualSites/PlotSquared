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

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Collection;

/**
 * This class should be implemented by each platform to allow PlotSquared to interact
 * with the world management solution used on the server.
 * <p>
 * Special support for world management plugins such as Multiverse and
 * Hyperverse can be added by extending the platform specific class. This
 * way PlotSquared can hook into different APIs and provide better support for that
 * particular plugin
 */
public interface PlatformWorldManager<T> {

    /**
     * Initialize the platform world manager
     */
    void initialize();

    /**
     * Inform the manager that PlotSquared has created a new world, using
     * a specified generator.
     *
     * @param worldName World name
     * @param generator World generator
     * @return Created world
     */
    @Nullable T handleWorldCreation(
            final @NonNull String worldName,
            final @Nullable String generator
    );

    /**
     * Get the implementation name
     *
     * @return implementation name
     */
    String getName();

    /**
     * Get the names of all worlds on the server
     *
     * @return Worlds
     */
    Collection<String> getWorlds();

}
