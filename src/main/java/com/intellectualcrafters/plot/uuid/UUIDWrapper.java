package com.intellectualcrafters.plot.uuid;

import java.util.UUID;

import com.intellectualcrafters.plot.object.OfflinePlotPlayer;
import com.intellectualcrafters.plot.object.PlotPlayer;

public abstract class UUIDWrapper {
    public abstract UUID getUUID(PlotPlayer player);

    public abstract UUID getUUID(OfflinePlotPlayer player);

    public abstract UUID getUUID(String name);

    public abstract OfflinePlotPlayer getOfflinePlayer(UUID uuid);
    
    public abstract OfflinePlotPlayer[] getOfflinePlayers();
}
