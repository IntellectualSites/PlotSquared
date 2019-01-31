package com.github.intellectualsites.plotsquared.plot.uuid;

import com.github.intellectualsites.plotsquared.plot.object.OfflinePlotPlayer;
import com.github.intellectualsites.plotsquared.plot.object.PlotPlayer;

import java.util.UUID;

public interface UUIDWrapper {

    UUID getUUID(PlotPlayer player);

    UUID getUUID(OfflinePlotPlayer player);

    UUID getUUID(String name);

    OfflinePlotPlayer getOfflinePlayer(UUID uuid);

    OfflinePlotPlayer getOfflinePlayer(String name);

    OfflinePlotPlayer[] getOfflinePlayers();
}
