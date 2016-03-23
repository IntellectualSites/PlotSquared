package com.intellectualcrafters.plot.uuid;

import com.intellectualcrafters.plot.object.OfflinePlotPlayer;
import com.intellectualcrafters.plot.object.PlotPlayer;

import java.util.UUID;

public abstract class UUIDWrapper {

    public abstract UUID getUUID(PlotPlayer player);

    public abstract UUID getUUID(OfflinePlotPlayer player);

    public abstract UUID getUUID(String name);

    public abstract OfflinePlotPlayer getOfflinePlayer(UUID uuid);

    public abstract OfflinePlotPlayer getOfflinePlayer(String name);

    public abstract OfflinePlotPlayer[] getOfflinePlayers();
}
