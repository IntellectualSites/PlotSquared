package com.plotsquared.sponge.uuid;

import com.google.common.base.Charsets;
import com.intellectualcrafters.plot.object.OfflinePlotPlayer;
import com.intellectualcrafters.plot.object.PlotPlayer;
import com.intellectualcrafters.plot.util.UUIDHandler;
import com.intellectualcrafters.plot.uuid.UUIDWrapper;
import com.plotsquared.sponge.SpongeMain;
import org.spongepowered.api.profile.GameProfile;

import java.util.UUID;

public class SpongeLowerOfflineUUIDWrapper extends UUIDWrapper {
    
    public SpongeLowerOfflineUUIDWrapper() {
        // Anything?
    }
    
    @Override
    public UUID getUUID(PlotPlayer player) {
        return getUUID(player.getName());
    }
    
    @Override
    public UUID getUUID(OfflinePlotPlayer player) {
        return getUUID(player.getName());
    }
    
    @Override
    public OfflinePlotPlayer getOfflinePlayer(UUID uuid) {
        String name = UUIDHandler.getName(uuid);
        if (name == null) {
            try {
                GameProfile profile = SpongeMain.THIS.getResolver().get(uuid).get();
                if (profile != null) {
                    name = profile.getName().orElse(null);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (name == null) {
            for (GameProfile profile : SpongeMain.THIS.getResolver().getCache().getProfiles()) {
                String tmp = profile.getName().orElse(null);
                if (tmp != null) {
                    if (getUUID(name).equals(uuid)) {
                        name = tmp;
                        break;
                    }
                }
            }
        }
        String username = name;
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

    @Override
    public UUID getUUID(String name) {
        return UUID.nameUUIDFromBytes(("OfflinePlayer:" + name.toLowerCase()).getBytes(Charsets.UTF_8));
    }
    
    @Override
    public OfflinePlotPlayer[] getOfflinePlayers() {
        // TODO FIXME
        throw new UnsupportedOperationException("NOT IMPLEMENTED YET");
    }
    
    @Override
    public OfflinePlotPlayer getOfflinePlayer(String name) {
        throw new UnsupportedOperationException("NOT IMPLEMENTED YET");
    }
}
