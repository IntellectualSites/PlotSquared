package com.plotsquared.sponge;

import java.util.UUID;
import java.util.concurrent.ExecutionException;

import org.spongepowered.api.GameProfile;
import org.spongepowered.api.entity.player.Player;
import org.spongepowered.api.service.profile.GameProfileResolver;

import com.google.common.base.Charsets;
import com.google.inject.Inject;
import com.intellectualcrafters.plot.object.OfflinePlotPlayer;
import com.intellectualcrafters.plot.object.PlotPlayer;
import com.intellectualcrafters.plot.util.UUIDHandler;
import com.intellectualcrafters.plot.uuid.UUIDWrapper;

public class SpongeLowerOfflineUUIDWrapper extends UUIDWrapper {

    public SpongeLowerOfflineUUIDWrapper() {
        // Anything?
    }

    @Override
    public UUID getUUID(final PlotPlayer player) {
        return getUUID(player.getName());
    }

    @Override
    public UUID getUUID(final OfflinePlotPlayer player) {
        return getUUID(player.getName());
    }

    @Override
    public OfflinePlotPlayer getOfflinePlayer(final UUID uuid) {
        String name = UUIDHandler.getName(uuid);
        if (name == null) {
            try {
                GameProfile profile = SpongeMain.THIS.getResolver().get(uuid).get();
                if (profile != null) {
                    name = profile.getName();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (name == null) {
            for (GameProfile profile : SpongeMain.THIS.getResolver().getCachedProfiles()) {
                if (getUUID(profile.getName()).equals(uuid)) {
                    name = profile.getName();
                    break;
                }
            }
        }
        final String username = name;
        return new OfflinePlotPlayer() {
            @Override
            public boolean isOnline() {
                return UUIDHandler.getPlayer(uuid) != null;
            }
            
            @Override
            public UUID getUUID() {
                return uuid;
            }
            
            @Override
            public String getName() {
                return username;
            }
            
            @Override
            public long getLastPlayed() {
             // TODO FIXME
                throw new UnsupportedOperationException("NOT IMPLEMENTED YET");
            }
        };
    }

    public Player[] getOnlinePlayers() {
        return SpongeMain.THIS.getServer().getOnlinePlayers().toArray(new Player[0]);
    }

    @Override
    public UUID getUUID(final String name) {
        return UUID.nameUUIDFromBytes(("OfflinePlayer:" + name.toLowerCase()).getBytes(Charsets.UTF_8));
    }
    
    @Override
    public OfflinePlotPlayer[] getOfflinePlayers() {
     // TODO FIXME
        throw new UnsupportedOperationException("NOT IMPLEMENTED YET");
    }
}
