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
import com.intellectualcrafters.plot.object.StringWrapper;
import com.intellectualcrafters.plot.util.UUIDHandler;

public class OfflineUUIDWrapper extends UUIDWrapper {

    private Method getOnline = null;
    private Object[] arg = new Object[0];

    public OfflineUUIDWrapper() {
        try {
            this.getOnline = Server.class.getMethod("getOnlinePlayers", new Class[0]);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }
    
    @Override
    public UUID getUUID(final Player player) {
        return UUID.nameUUIDFromBytes(("OfflinePlayer:" + player.getName()).getBytes(Charsets.UTF_8));
    }

    @Override
    public UUID getUUID(final OfflinePlayer player) {
        return UUID.nameUUIDFromBytes(("OfflinePlayer:" + player.getName()).getBytes(Charsets.UTF_8));
    }

    @Override
    public OfflinePlayer getOfflinePlayer(final UUID uuid) {
        final BiMap<UUID, StringWrapper> map = UUIDHandler.getUuidMap().inverse();
        String name;
        try {
            name = map.get(uuid).value;
        } catch (NullPointerException e) {
            name = null;
        }
        if (name != null) {
            OfflinePlayer op = Bukkit.getOfflinePlayer(name);
            if (op.hasPlayedBefore()) {
                return op;
            }
        }
        for (final OfflinePlayer player : Bukkit.getOfflinePlayers()) {
            if (getUUID(player).equals(uuid)) {
                return player;
            }
        }
        return null;
    }

    public Player[] getOnlinePlayers() {
        if (getOnline == null) {
            return Bukkit.getOnlinePlayers().toArray(new Player[0]);
        }
        try {
            Object players = getOnline.invoke(Bukkit.getServer(), arg);
            if (players instanceof Player[]) {
                return (Player[]) players;
            }
            else {
                @SuppressWarnings("unchecked")
                Collection<? extends Player> p = (Collection<? extends Player>) players;
                return p.toArray(new Player[0]);
            }
        }
        catch (Exception e) {
            System.out.print("Failed to resolve online players");
            getOnline = null;
            return Bukkit.getOnlinePlayers().toArray(new Player[0]);
        }
    }
    
    @Override
    public Player getPlayer(final UUID uuid) {
        for (final Player player : getOnlinePlayers()) {
            if (getUUID(player).equals(uuid)) {
                return player;
            }
        }
        return null;
    }

    @Override
    public UUID getUUID(String name) {
        return UUID.nameUUIDFromBytes(("OfflinePlayer:" + name).getBytes(Charsets.UTF_8));
    }

}
