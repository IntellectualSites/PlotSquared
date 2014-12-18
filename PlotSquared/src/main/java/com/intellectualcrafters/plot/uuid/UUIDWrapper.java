package com.intellectualcrafters.plot.uuid;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.UUID;

public abstract class UUIDWrapper {
    public abstract UUID getUUID(Player player);

    public abstract UUID getUUID(OfflinePlayer player);

    public abstract OfflinePlayer getOfflinePlayer(UUID uuid);

    public abstract Player getPlayer(UUID uuid);
}
