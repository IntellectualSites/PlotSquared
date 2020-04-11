package com.plotsquared.util.uuid;

import com.plotsquared.player.OfflinePlotPlayer;
import com.plotsquared.player.PlotPlayer;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public abstract class UUIDWrapper {

    @NotNull public abstract UUID getUUID(PlotPlayer player);

    public abstract UUID getUUID(OfflinePlotPlayer player);

    public abstract UUID getUUID(String name);

    public abstract OfflinePlotPlayer getOfflinePlayer(UUID uuid);

    public abstract OfflinePlotPlayer getOfflinePlayer(String name);

    public abstract OfflinePlotPlayer[] getOfflinePlayers();
}
