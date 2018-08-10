package com.github.intellectualsites.plotsquared.nukkit.uuid;

import com.github.intellectualsites.plotsquared.plot.object.OfflinePlotPlayer;
import com.github.intellectualsites.plotsquared.plot.object.PlotPlayer;
import com.github.intellectualsites.plotsquared.plot.uuid.UUIDWrapper;
import com.google.common.base.Charsets;

import java.util.UUID;

public class LowerOfflineUUIDWrapper extends UUIDWrapper {

    @Override public UUID getUUID(PlotPlayer player) {
        return UUID.nameUUIDFromBytes(
            ("OfflinePlayer:" + player.getName().toLowerCase()).getBytes(Charsets.UTF_8));
    }

    @Override public UUID getUUID(OfflinePlotPlayer player) {
        return UUID.nameUUIDFromBytes(
            ("OfflinePlayer:" + player.getName().toLowerCase()).getBytes(Charsets.UTF_8));
    }

    @Override public UUID getUUID(String name) {
        return UUID
            .nameUUIDFromBytes(("OfflinePlayer:" + name.toLowerCase()).getBytes(Charsets.UTF_8));
    }

    @Override public OfflinePlotPlayer getOfflinePlayer(UUID uuid) {
        return null;
    }

    @Override public OfflinePlotPlayer getOfflinePlayer(String name) {
        return null;
    }

    @Override public OfflinePlotPlayer[] getOfflinePlayers() {
        return new OfflinePlotPlayer[0];
    }

}
