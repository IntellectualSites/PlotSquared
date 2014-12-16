package com.intellectualcrafters.plot.uuid;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

public class DefaultUUIDWrapper extends UUIDWrapper {

    @Override
    public UUID getUUID(Player player) {
        return player.getUniqueId();
    }
    
    @Override
    public UUID getUUID(OfflinePlayer player) {
        return player.getUniqueId();
    }

    @Override
    public OfflinePlayer getOfflinePlayer(UUID uuid) {
        return Bukkit.getOfflinePlayer(uuid);
    }

    @Override
    public Player getPlayer(UUID uuid) {
        return Bukkit.getPlayer(uuid);
    }
    
}
