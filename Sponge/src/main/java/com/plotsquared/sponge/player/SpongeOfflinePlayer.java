package com.plotsquared.sponge.player;

import com.plotsquared.core.player.OfflinePlotPlayer;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.UUID;

//TODO Implement
public class SpongeOfflinePlayer implements @Nullable
        OfflinePlotPlayer {

    @Override
    public boolean hasPermission(@Nullable final String world, @NonNull final String permission) {
        return false;
    }

    @Override
    public boolean hasKeyedPermission(@Nullable final String world, @NonNull final String permission, @NonNull final String key) {
        return false;
    }

    @Override
    public UUID getUUID() {
        return null;
    }

    @Override
    public long getLastPlayed() {
        return 0;
    }

    @Override
    public String getName() {
        return null;
    }
}
