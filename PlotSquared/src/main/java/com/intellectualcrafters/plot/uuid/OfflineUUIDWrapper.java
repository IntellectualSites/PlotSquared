package com.intellectualcrafters.plot.uuid;

import com.google.common.base.Charsets;
import com.google.common.collect.BiMap;
import com.intellectualcrafters.plot.object.StringWrapper;
import com.intellectualcrafters.plot.util.UUIDHandler;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.UUID;

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
        final String name = map.get(uuid).value;
        if (name != null) {
            return Bukkit.getOfflinePlayer(name);
        } else {
            for (final OfflinePlayer player : Bukkit.getOfflinePlayers()) {
                if (getUUID(player).equals(uuid)) {
                    return player;
                }
            }
        }
        return Bukkit.getOfflinePlayer(uuid.toString());
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

}
