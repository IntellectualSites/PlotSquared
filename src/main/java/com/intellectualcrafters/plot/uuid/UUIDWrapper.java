package com.intellectualcrafters.plot.uuid;

import java.util.UUID;

import com.intellectualcrafters.plot.object.OfflinePlotPlayer;
import com.intellectualcrafters.plot.object.PlotPlayer;

public abstract class UUIDWrapper {
    public abstract UUID getUUID(final PlotPlayer player);
    
    public abstract UUID getUUID(final OfflinePlotPlayer player);
    
    public abstract UUID getUUID(final String name);
    
    public abstract OfflinePlotPlayer getOfflinePlayer(final UUID uuid);
    
    public abstract OfflinePlotPlayer getOfflinePlayer(final String name);

    public abstract OfflinePlotPlayer[] getOfflinePlayers();
}
