package com.intellectualcrafters.plot.uuid;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import com.google.common.base.Charsets;
import com.google.common.collect.BiMap;
import com.intellectualcrafters.plot.object.StringWrapper;
import com.intellectualcrafters.plot.util.UUIDHandler;

public class OfflineUUIDWrapper extends UUIDWrapper {

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

    @Override
    public Player getPlayer(final UUID uuid) {
        for (final Player player : Bukkit.getOnlinePlayers()) {
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
