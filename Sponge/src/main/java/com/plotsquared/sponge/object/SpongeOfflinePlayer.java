package com.plotsquared.sponge.object;

import com.intellectualcrafters.plot.object.OfflinePlotPlayer;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.living.player.User;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public class SpongeOfflinePlayer implements OfflinePlotPlayer {

    private User user;

    public SpongeOfflinePlayer(User user) {
        this.user = user;
    }
    @Override public UUID getUUID() {
        return user.getUniqueId();
    }

    @Override public long getLastPlayed() {
        Optional<Instant> instant = user.get(Keys.LAST_DATE_PLAYED);
        return instant.map(Instant::toEpochMilli).orElse(0L);
    }

    @Override public boolean isOnline() {
        return user.isOnline();
    }

    @Override public String getName() {
        return user.getName();
    }
}
