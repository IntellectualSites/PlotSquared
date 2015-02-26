package com.intellectualcrafters.plot.uuid;

import java.util.UUID;

import com.intellectualcrafters.plot.object.BukkitOfflinePlayer;
import com.intellectualcrafters.plot.object.PlotPlayer;

public abstract class UUIDWrapper {
    public abstract UUID getUUID(PlotPlayer player);

    public abstract UUID getUUID(BukkitOfflinePlayer player);

    public abstract UUID getUUID(String name);

    public abstract BukkitOfflinePlayer getOfflinePlayer(UUID uuid);
}
