package com.intellectualcrafters.plot.uuid;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import com.plotsquared.bukkit.object.BukkitOfflinePlayer;
import com.plotsquared.bukkit.object.BukkitPlayer;
import com.intellectualcrafters.plot.object.OfflinePlotPlayer;
import com.intellectualcrafters.plot.object.PlotPlayer;

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
        OfflinePlayer[] ops = Bukkit.getOfflinePlayers();
        BukkitOfflinePlayer[] toReturn = new BukkitOfflinePlayer[ops.length] ;
        for (int i = 0; i < ops.length; i++) {
            toReturn[i] = new BukkitOfflinePlayer(ops[i]);
        }
        return toReturn;
    }
}
