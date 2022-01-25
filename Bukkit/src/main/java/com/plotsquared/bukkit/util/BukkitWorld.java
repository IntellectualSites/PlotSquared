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
 *               Copyright (C) 2014 - 2022 IntellectualSites
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
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.plotsquared.bukkit.util;

import com.google.common.collect.Maps;
import com.plotsquared.core.location.World;
import org.bukkit.Bukkit;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.Map;
import java.util.Objects;

public class BukkitWorld implements World<org.bukkit.World> {

    private static final Map<String, BukkitWorld> worldMap = Maps.newHashMap();
    private static final boolean HAS_MIN_Y;

    static {
        boolean temp;
        try {
            org.bukkit.World.class.getMethod("getMinHeight");
            temp = true;
        } catch (NoSuchMethodException e) {
            temp = false;
        }
        HAS_MIN_Y = temp;
    }

    private final org.bukkit.World world;

    private BukkitWorld(final org.bukkit.World world) {
        this.world = world;
    }

    /**
     * Get a new {@link BukkitWorld} from a world name
     *
     * @param worldName World name
     * @return World instance
     */
    public static @NonNull BukkitWorld of(final @NonNull String worldName) {
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
    public static @NonNull BukkitWorld of(final org.bukkit.World world) {
        BukkitWorld bukkitWorld = worldMap.get(world.getName());
        if (bukkitWorld != null && bukkitWorld.getPlatformWorld().equals(world)) {
            return bukkitWorld;
        }
        bukkitWorld = new BukkitWorld(world);
        worldMap.put(world.getName(), bukkitWorld);
        return bukkitWorld;
    }

    /**
     * Get the min world height from a Bukkit {@link org.bukkit.World}. Inclusive
     *
     * @since TODO
     */
    public static int getMinWorldHeight(org.bukkit.World world) {
        return HAS_MIN_Y ? world.getMinHeight() : 0;
    }

    /**
     * Get the max world height from a Bukkit {@link org.bukkit.World}. Exclusive
     *
     * @since TODO
     */
    public static int getMaxWorldHeight(org.bukkit.World world) {
        return HAS_MIN_Y ? world.getMaxHeight() : 256;
    }

    @Override
    public org.bukkit.World getPlatformWorld() {
        return this.world;
    }

    @Override
    public @NonNull String getName() {
        return this.world.getName();
    }

    @Override
    public int getMinHeight() {
        return getMinWorldHeight(world);
    }

    @Override
    public int getMaxHeight() {
        return getMaxWorldHeight(world);
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final BukkitWorld that = (BukkitWorld) o;
        return world.equals(that.world);
    }

    @Override
    public int hashCode() {
        return world.hashCode();
    }

    /**
     * @deprecated This method is not meant to be invoked or overridden, with no replacement.
     */
    @Deprecated(forRemoval = true, since = "TODO")
    protected boolean canEqual(final Object other) {
        return other instanceof BukkitWorld;
    }

    public String toString() {
        return "BukkitWorld(world=" + this.world + ")";
    }

}
