package com.plotsquared.sponge.uuid;

import com.intellectualcrafters.plot.object.OfflinePlotPlayer;
import com.intellectualcrafters.plot.object.PlotPlayer;
import com.intellectualcrafters.plot.util.UUIDHandler;
import com.intellectualcrafters.plot.uuid.UUIDWrapper;
import com.plotsquared.sponge.SpongeMain;
import com.plotsquared.sponge.object.SpongePlayer;

import java.util.UUID;
import java.util.concurrent.ExecutionException;

public class SpongeOnlineUUIDWrapper extends UUIDWrapper {
    
    @Override
    public UUID getUUID(final PlotPlayer player) {
        return ((SpongePlayer) player).player.getUniqueId();
    }
    
    @Override
    public UUID getUUID(final OfflinePlotPlayer player) {
        return player.getUUID();
    }
    
    @Override
    public UUID getUUID(final String name) {
        try {
            return SpongeMain.THIS.getResolver().get(name, true).get().getUniqueId();
        } catch (final Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    
    @Override
    public OfflinePlotPlayer getOfflinePlayer(final UUID uuid) {
        String name;
        try {
            name = SpongeMain.THIS.getResolver().get(uuid, true).get().getName().orElse(null);
        } catch (InterruptedException | ExecutionException ignored) {
            name = null;
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
    
    @Override
    public OfflinePlotPlayer[] getOfflinePlayers() {
        throw new UnsupportedOperationException("NOT IMPLEMENTED YET");
    }
    
    @Override
    public OfflinePlotPlayer getOfflinePlayer(String name) {
        throw new UnsupportedOperationException("NOT IMPLEMENTED YET");
    }
    
}
