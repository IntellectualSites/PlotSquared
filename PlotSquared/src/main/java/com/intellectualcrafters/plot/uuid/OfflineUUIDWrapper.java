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
    public UUID getUUID(Player player) {
        return UUID.nameUUIDFromBytes(("OfflinePlayer:" + player.getName()).getBytes(Charsets.UTF_8));
    }

    @Override
    public UUID getUUID(OfflinePlayer player) {
        return UUID.nameUUIDFromBytes(("OfflinePlayer:" + player.getName()).getBytes(Charsets.UTF_8));
    }

    @Override
    public OfflinePlayer getOfflinePlayer(UUID uuid) {
        BiMap<UUID, StringWrapper> map = UUIDHandler.getUuidMap().inverse();
        String name = map.get(uuid).value;
        if (name != null) {
            return Bukkit.getOfflinePlayer(name);
        }
        else {
            for (OfflinePlayer player : Bukkit.getOfflinePlayers()) {
                if (getUUID(player).equals(uuid)) {
                    return player;
                }
            }
        }
        return Bukkit.getOfflinePlayer(uuid.toString());
    }

    @Override
    public Player getPlayer(UUID uuid) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (getUUID(player).equals(uuid)) {
                return player;
            }
        }
        return null;
    }
    
    
    
}
