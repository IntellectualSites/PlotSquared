package com.intellectualcrafters.plot.uuid;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.Server;
import org.bukkit.entity.Player;

import com.google.common.base.Charsets;
import com.google.common.collect.BiMap;
import com.intellectualcrafters.plot.PS;
import com.intellectualcrafters.plot.object.BukkitOfflinePlayer;
import com.intellectualcrafters.plot.object.OfflinePlotPlayer;
import com.intellectualcrafters.plot.object.PlotPlayer;
import com.intellectualcrafters.plot.object.StringWrapper;
import com.intellectualcrafters.plot.util.bukkit.UUIDHandler;

public class OfflineUUIDWrapper extends UUIDWrapper {
    private Method getOnline = null;
    private final Object[] arg = new Object[0];

    public OfflineUUIDWrapper() {
        try {
            this.getOnline = Server.class.getMethod("getOnlinePlayers", new Class[0]);
        } catch (final NoSuchMethodException e) {
            e.printStackTrace();
        } catch (final SecurityException e) {
            e.printStackTrace();
        }
    }

    @Override
    public UUID getUUID(final PlotPlayer player) {
        return UUID.nameUUIDFromBytes(("OfflinePlayer:" + player.getName()).getBytes(Charsets.UTF_8));
    }

    @Override
    public UUID getUUID(final OfflinePlotPlayer player) {
        return UUID.nameUUIDFromBytes(("OfflinePlayer:" + player.getName()).getBytes(Charsets.UTF_8));
    }

    public UUID getUUID(final OfflinePlayer player) {
        return UUID.nameUUIDFromBytes(("OfflinePlayer:" + player.getName()).getBytes(Charsets.UTF_8));
    }

    @Override
    public OfflinePlotPlayer getOfflinePlayer(final UUID uuid) {
        final BiMap<UUID, StringWrapper> map = UUIDHandler.getUuidMap().inverse();
        String name;
        try {
            name = map.get(uuid).value;
        } catch (final NullPointerException e) {
            name = null;
        }
        if (name != null) {
            final OfflinePlayer op = Bukkit.getOfflinePlayer(name);
            if (op.hasPlayedBefore()) {
                return new BukkitOfflinePlayer(op);
            }
        }
        for (final OfflinePlayer player : Bukkit.getOfflinePlayers()) {
            if (getUUID(player).equals(uuid)) {
                return new BukkitOfflinePlayer(player);
            }
        }
        return null;
    }

    public Player[] getOnlinePlayers() {
        if (this.getOnline == null) {
            return Bukkit.getOnlinePlayers().toArray(new Player[0]);
        }
        try {
            final Object players = this.getOnline.invoke(Bukkit.getServer(), this.arg);
            if (players instanceof Player[]) {
                return (Player[]) players;
            } else {
                @SuppressWarnings("unchecked")
                final Collection<? extends Player> p = (Collection<? extends Player>) players;
                return p.toArray(new Player[0]);
            }
        } catch (final Exception e) {
            PS.log("Failed to resolve online players");
            this.getOnline = null;
            return Bukkit.getOnlinePlayers().toArray(new Player[0]);
        }
    }

    @Override
    public UUID getUUID(final String name) {
        return UUID.nameUUIDFromBytes(("OfflinePlayer:" + name).getBytes(Charsets.UTF_8));
    }
    
    @Override
    public OfflinePlotPlayer[] getOfflinePlayers() {
        OfflinePlayer[] ops = Bukkit.getOfflinePlayers();
        BukkitOfflinePlayer[] toReturn = new BukkitOfflinePlayer[ops.length] ;
        for (int i = 0; i < ops.length; i++) {
            toReturn[i] = new BukkitOfflinePlayer(ops[i]);
        }
        return toReturn;
    }
}
