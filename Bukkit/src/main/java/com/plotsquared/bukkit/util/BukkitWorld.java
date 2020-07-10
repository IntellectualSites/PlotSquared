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
package com.plotsquared.bukkit.util;

import com.google.common.collect.Maps;
import com.plotsquared.core.location.World;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

@EqualsAndHashCode @ToString public class BukkitWorld implements World<org.bukkit.World> {

    private static final Map<String, BukkitWorld> worldMap = Maps.newHashMap();

    private final org.bukkit.World world;

    private BukkitWorld(@NotNull final org.bukkit.World world) {
        this.world = world;
    }

    /**
     * Get a new {@link BukkitWorld} from a world name
     *
     * @param worldName World name
     * @return World instance
     */
    @NotNull public static BukkitWorld of(@NotNull final String worldName) {
        final org.bukkit.World bukkitWorld = Bukkit.getWorld(worldName);
        if (bukkitWorld == null) {
            throw new IllegalArgumentException(String.format("There is no world with the name '%s'", worldName));
        }
        return of(bukkitWorld);
    }

    /**
     * Get a new {@link BukkitWorld} from a Bukkit world
     *
     * @param world Bukkit world
     * @return World instance
     */
    @NotNull public static BukkitWorld of(@NotNull final org.bukkit.World world) {
        BukkitWorld bukkitWorld = worldMap.get(world.getName());
        if (bukkitWorld != null && bukkitWorld.getPlatformWorld().equals(world)) {
            return bukkitWorld;
        }
        bukkitWorld = new BukkitWorld(world);
        worldMap.put(world.getName(), bukkitWorld);
        return bukkitWorld;
    }

    @NotNull @Override public org.bukkit.World getPlatformWorld() {
        return this.world;
    }

    @Override @NotNull public String getName() {
        return this.world.getName();
    }

}
