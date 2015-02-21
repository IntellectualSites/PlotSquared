package com.intellectualcrafters.plot.uuid;

import java.util.UUID;

import org.bukkit.Bukkit;
import com.intellectualcrafters.plot.object.BukkitOfflinePlayer;
import com.intellectualcrafters.plot.object.PlotPlayer;
import com.intellectualcrafters.plot.object.BukkitPlayer;;

public class DefaultUUIDWrapper extends UUIDWrapper {
    @Override
    public UUID getUUID(final PlotPlayer player) {
        return ((BukkitPlayer) player).player.getUniqueId();
    }
    
    @Override
    public UUID getUUID(final BukkitOfflinePlayer player) {
        return player.getUUID();
    }
    
    @Override
    public BukkitOfflinePlayer getOfflinePlayer(final UUID uuid) {
        return new BukkitOfflinePlayer(Bukkit.getOfflinePlayer(uuid));
    }
    
    @Override
    public UUID getUUID(final String name) {
        return Bukkit.getOfflinePlayer(name).getUniqueId();
    }
}
