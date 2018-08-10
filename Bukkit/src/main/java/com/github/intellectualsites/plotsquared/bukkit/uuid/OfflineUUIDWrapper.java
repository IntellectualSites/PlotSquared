package com.github.intellectualsites.plotsquared.bukkit.uuid;

import com.github.intellectualsites.plotsquared.bukkit.object.BukkitOfflinePlayer;
import com.github.intellectualsites.plotsquared.plot.PS;
import com.github.intellectualsites.plotsquared.plot.object.OfflinePlotPlayer;
import com.github.intellectualsites.plotsquared.plot.object.PlotPlayer;
import com.github.intellectualsites.plotsquared.plot.object.StringWrapper;
import com.github.intellectualsites.plotsquared.plot.util.UUIDHandler;
import com.github.intellectualsites.plotsquared.plot.uuid.UUIDWrapper;
import com.google.common.base.Charsets;
import com.google.common.collect.BiMap;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.Server;
import org.bukkit.entity.Player;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.UUID;

public class OfflineUUIDWrapper extends UUIDWrapper {

    private final Object[] arg = new Object[0];
    private Method getOnline = null;

    public OfflineUUIDWrapper() {
        try {
            this.getOnline = Server.class.getMethod("getOnlinePlayers");
        } catch (NoSuchMethodException | SecurityException e) {
            e.printStackTrace();
        }
    }

    @Override public UUID getUUID(PlotPlayer player) {
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
            return onlinePlayers.toArray(new Player[onlinePlayers.size()]);
        }
        try {
            Object players = this.getOnline.invoke(Bukkit.getServer(), this.arg);
            if (players instanceof Player[]) {
                return (Player[]) players;
            } else {
                @SuppressWarnings("unchecked") Collection<? extends Player> p =
                    (Collection<? extends Player>) players;
                return p.toArray(new Player[p.size()]);
            }
        } catch (IllegalAccessException | InvocationTargetException | IllegalArgumentException ignored) {
            PS.debug("Failed to resolve online players");
            this.getOnline = null;
            Collection<? extends Player> onlinePlayers = Bukkit.getOnlinePlayers();
            return onlinePlayers.toArray(new Player[onlinePlayers.size()]);
        }
    }

    @Override public UUID getUUID(String name) {
        return UUID.nameUUIDFromBytes(("OfflinePlayer:" + name).getBytes(Charsets.UTF_8));
    }

    @Override public OfflinePlotPlayer[] getOfflinePlayers() {
        OfflinePlayer[] ops = Bukkit.getOfflinePlayers();
        BukkitOfflinePlayer[] toReturn = new BukkitOfflinePlayer[ops.length];
        for (int i = 0; i < ops.length; i++) {
            toReturn[i] = new BukkitOfflinePlayer(ops[i]);
        }
        return toReturn;
    }

    @Override public OfflinePlotPlayer getOfflinePlayer(String name) {
        return new BukkitOfflinePlayer(Bukkit.getOfflinePlayer(name));
    }
}
