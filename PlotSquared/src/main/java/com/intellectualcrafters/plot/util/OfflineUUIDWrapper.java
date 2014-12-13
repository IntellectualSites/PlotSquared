package com.intellectualcrafters.plot.util;

import java.util.UUID;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import com.google.common.base.Charsets;

public class OfflineUUIDWrapper extends UUIDWrapper {

    @Override
    public UUID getUUID(Player player) {
        return UUID.nameUUIDFromBytes(("OfflinePlayer:" + player.getName()).getBytes(Charsets.UTF_8));
    }

    @Override
    public UUID getUUID(OfflinePlayer player) {
        return UUID.nameUUIDFromBytes(("OfflinePlayer:" + player.getName()).getBytes(Charsets.UTF_8));
    }
    
}
