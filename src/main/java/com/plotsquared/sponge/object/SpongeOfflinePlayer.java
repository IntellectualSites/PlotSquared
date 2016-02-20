package com.plotsquared.sponge.object;

import com.intellectualcrafters.plot.object.OfflinePlotPlayer;

import java.util.UUID;

public class SpongeOfflinePlayer implements OfflinePlotPlayer {

    @Override public UUID getUUID() {
        return null;
    }

    @Override public long getLastPlayed() {
        return 0;
    }

    @Override public boolean isOnline() {
        return false;
    }

    @Override public String getName() {
        return null;
    }
}
