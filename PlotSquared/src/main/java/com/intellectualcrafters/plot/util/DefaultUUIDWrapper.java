package com.intellectualcrafters.plot.util;

import java.util.UUID;

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
    
}
