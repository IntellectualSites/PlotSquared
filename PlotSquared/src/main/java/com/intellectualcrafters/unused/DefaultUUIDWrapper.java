package com.intellectualcrafters.unused;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.UUID;

public class DefaultUUIDWrapper extends UUIDWrapper {

    @Override
    public UUID getUUID(final Player player) {
        return player.getUniqueId();
    }

    @Override
    public UUID getUUID(final OfflinePlayer player) {
        return player.getUniqueId();
    }

    @Override
    public OfflinePlayer getOfflinePlayer(final UUID uuid) {
        return Bukkit.getOfflinePlayer(uuid);
    }

    @Override
    public Player getPlayer(final UUID uuid) {
        return Bukkit.getPlayer(uuid);
    }

}
