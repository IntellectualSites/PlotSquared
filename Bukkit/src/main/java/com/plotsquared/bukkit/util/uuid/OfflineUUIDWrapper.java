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
package com.plotsquared.bukkit.util.uuid;

import com.google.common.base.Charsets;
import com.google.common.collect.BiMap;
import com.plotsquared.bukkit.player.BukkitOfflinePlayer;
import com.plotsquared.core.PlotSquared;
import com.plotsquared.core.player.OfflinePlotPlayer;
import com.plotsquared.core.player.PlotPlayer;
import com.plotsquared.core.util.StringWrapper;
import com.plotsquared.core.util.uuid.UUIDHandler;
import com.plotsquared.core.util.uuid.UUIDWrapper;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.UUID;

public class OfflineUUIDWrapper implements UUIDWrapper {

    private final Object[] arg = new Object[0];
    private Method getOnline = null;

    public OfflineUUIDWrapper() {
        try {
            this.getOnline = Server.class.getMethod("getOnlinePlayers");
        } catch (NoSuchMethodException | SecurityException e) {
            e.printStackTrace();
        }
    }

    @NotNull @Override public UUID getUUID(PlotPlayer player) {
        return UUID
            .nameUUIDFromBytes(("OfflinePlayer:" + player.getName()).getBytes(Charsets.UTF_8));
    }

    @Override public UUID getUUID(OfflinePlotPlayer player) {
        return UUID
            .nameUUIDFromBytes(("OfflinePlayer:" + player.getName()).getBytes(Charsets.UTF_8));
    }

    public UUID getUUID(OfflinePlayer player) {
        return UUID
            .nameUUIDFromBytes(("OfflinePlayer:" + player.getName()).getBytes(Charsets.UTF_8));
    }

    @Override public OfflinePlotPlayer getOfflinePlayer(UUID uuid) {
        BiMap<UUID, StringWrapper> map = UUIDHandler.getUuidMap().inverse();
        String name = null;
        if (map.containsKey(uuid)) {
            name = map.get(uuid).value;
        }
        if (name != null) {
            OfflinePlayer op = Bukkit.getOfflinePlayer(name);
            if (op.hasPlayedBefore()) {
                return new BukkitOfflinePlayer(op);
            }
        }
        for (OfflinePlayer player : Bukkit.getOfflinePlayers()) {
            if (getUUID(player).equals(uuid)) {
                return new BukkitOfflinePlayer(player);
            }
        }
        return null;
    }

    public Player[] getOnlinePlayers() {
        if (this.getOnline == null) {
            Collection<? extends Player> onlinePlayers = Bukkit.getOnlinePlayers();
            return onlinePlayers.toArray(new Player[0]);
        }
        try {
            Object players = this.getOnline.invoke(Bukkit.getServer(), this.arg);
            if (players instanceof Player[]) {
                return (Player[]) players;
            } else {
                @SuppressWarnings("unchecked") Collection<? extends Player> p =
                    (Collection<? extends Player>) players;
                return p.toArray(new Player[0]);
            }
        } catch (IllegalAccessException | InvocationTargetException | IllegalArgumentException ignored) {
            PlotSquared.debug("Failed to resolve online players");
            this.getOnline = null;
            Collection<? extends Player> onlinePlayers = Bukkit.getOnlinePlayers();
            return onlinePlayers.toArray(new Player[0]);
        }
    }

    @Override public UUID getUUID(String name) {
        return UUID.nameUUIDFromBytes(("OfflinePlayer:" + name).getBytes(Charsets.UTF_8));
    }

    @Override public OfflinePlotPlayer[] getOfflinePlayers() {
        OfflinePlayer[] ops = Bukkit.getOfflinePlayers();
        return Arrays.stream(ops).map(BukkitOfflinePlayer::new).toArray(BukkitOfflinePlayer[]::new);
    }

    @Override public OfflinePlotPlayer getOfflinePlayer(String name) {
        return new BukkitOfflinePlayer(Bukkit.getOfflinePlayer(name));
    }
}
