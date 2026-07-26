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
package com.plotsquared.bukkit.util;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.jetbrains.annotations.ApiStatus;

import java.util.concurrent.CompletableFuture;

/**
 * Not for external use. May change at any point.
 */
@ApiStatus.Internal
public enum PaperSupport {
    ;
    public static final boolean PAPER;

    static {
        PAPER = hasClass("com.destroystokyo.paper.PaperConfig")
            || hasClass("io.papermc.paper.configuration.Configuration");
    }

    public static boolean isPaper() {
        return PAPER;
    }

    public static CompletableFuture<Boolean> teleportAsync(Entity entity, Location location) {
        if (isPaper()) {
            return entity.teleportAsync(location);
        }
        return CompletableFuture.completedFuture(entity.teleport(location));
    }

    public static CompletableFuture<Boolean> teleportAsync(Entity entity, Location location, PlayerTeleportEvent.TeleportCause cause) {
        if (isPaper()) {
            return entity.teleportAsync(location, cause);
        }
        return CompletableFuture.completedFuture(entity.teleport(location));
    }

    public static CompletableFuture<Chunk> getChunkAtAsync(Location location) {
        return getChunkAtAsync(location.getWorld(), location.getBlockX() >> 4, location.getBlockZ() >> 4, true);
    }

    public static CompletableFuture<Chunk> getChunkAtAsync(World world, int cx, int cz, boolean gen) {
        return getChunkAtAsync(world, cx, cz, gen, false);
    }

    public static CompletableFuture<Chunk> getChunkAtAsync(World world, int cx, int cz, boolean gen, boolean urgent) {
        if (isPaper()) {
            return world.getChunkAtAsync(cx, cz, gen, urgent);
        } else {
            CompletableFuture<Chunk> future = new CompletableFuture<>();
            future.complete(world.getChunkAt(cx, cz, gen));
            return future;
        }
    }

    private static boolean hasClass(String className) {
        try {
            Class.forName(className);
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }
}
