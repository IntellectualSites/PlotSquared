package com.plotsquared.bukkit.uuid;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import com.intellectualcrafters.plot.object.OfflinePlotPlayer;
import com.intellectualcrafters.plot.object.PlotPlayer;
import com.intellectualcrafters.plot.uuid.UUIDWrapper;
import com.plotsquared.bukkit.object.BukkitOfflinePlayer;
import com.plotsquared.bukkit.object.BukkitPlayer;

public class DefaultUUIDWrapper extends UUIDWrapper {
    @Override
    public UUID getUUID(final PlotPlayer player) {
        return ((BukkitPlayer) player).player.getUniqueId();
    }
    
    @Override
    public UUID getUUID(final OfflinePlotPlayer player) {
        return player.getUUID();
    }
    
    @Override
    public OfflinePlotPlayer getOfflinePlayer(final UUID uuid) {
        return new BukkitOfflinePlayer(Bukkit.getOfflinePlayer(uuid));
    }
    
    @Override
    public UUID getUUID(final String name) {
        return Bukkit.getOfflinePlayer(name).getUniqueId();
    }
    
    @Override
    public OfflinePlotPlayer[] getOfflinePlayers() {
        final OfflinePlayer[] ops = Bukkit.getOfflinePlayers();
        final BukkitOfflinePlayer[] toReturn = new BukkitOfflinePlayer[ops.length];
        for (int i = 0; i < ops.length; i++) {
            toReturn[i] = new BukkitOfflinePlayer(ops[i]);
        }
        return toReturn;
    }
    
    @Override
    public OfflinePlotPlayer getOfflinePlayer(String name) {
        return new BukkitOfflinePlayer(Bukkit.getOfflinePlayer(name));
    }
}
