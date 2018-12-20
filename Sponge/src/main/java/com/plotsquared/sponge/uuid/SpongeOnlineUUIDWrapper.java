package com.plotsquared.sponge.uuid;

import com.intellectualcrafters.plot.object.OfflinePlotPlayer;
import com.intellectualcrafters.plot.object.PlotPlayer;
import com.intellectualcrafters.plot.uuid.UUIDWrapper;
import com.plotsquared.sponge.object.SpongeOfflinePlayer;
import com.plotsquared.sponge.object.SpongePlayer;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.service.user.UserStorageService;
import org.spongepowered.api.util.Identifiable;

import java.util.Optional;
import java.util.UUID;

public class SpongeOnlineUUIDWrapper extends UUIDWrapper {

    private UserStorageService userStorageService;
    public SpongeOnlineUUIDWrapper() {
        Optional<UserStorageService> userStorage = Sponge.getServiceManager().provide(UserStorageService.class);
        userStorage.ifPresent(userStorageService -> this.userStorageService = userStorageService);

    }

    @Override
    public UUID getUUID(PlotPlayer player) {
        return ((SpongePlayer) player).player.getUniqueId();
    }
    
    @Override
    public UUID getUUID(OfflinePlotPlayer player) {
        return player.getUUID();
    }
    
    @Override
    public UUID getUUID(String name) {
        Optional<Player> player = Sponge.getServer().getPlayer(name);
        if (player.isPresent()) {
            return player.get().getUniqueId();
        }
        Optional<User> user = userStorageService.get(name);
        return user.map(Identifiable::getUniqueId).orElse(null);
    }
    
    @Override
    public OfflinePlotPlayer getOfflinePlayer(UUID uuid) {
        Optional<Player> player = Sponge.getServer().getPlayer(uuid);
        if (player.isPresent()) {
            return new SpongeOfflinePlayer(player.get());
        }
        Optional<User> user = userStorageService.get(uuid);
        return user.map(SpongeOfflinePlayer::new).orElse(null);
    }
    
    @Override
    public OfflinePlotPlayer[] getOfflinePlayers() {
        throw new UnsupportedOperationException("NOT IMPLEMENTED YET");
    }

    @Override
    public OfflinePlotPlayer getOfflinePlayer(String name) {
        Optional<Player> player = Sponge.getServer().getPlayer(name);
        if (player.isPresent()) {
            return new SpongeOfflinePlayer(player.get());
        }
        Optional<User> user = userStorageService.get(name);
        return user.map(SpongeOfflinePlayer::new).orElse(null);
    }
    
}
