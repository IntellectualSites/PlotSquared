package com.github.intellectualsites.plotsquared.bukkit.uuid;

import com.github.intellectualsites.plotsquared.bukkit.object.BukkitOfflinePlayer;
import com.github.intellectualsites.plotsquared.bukkit.object.BukkitPlayer;
import com.github.intellectualsites.plotsquared.plot.object.OfflinePlotPlayer;
import com.github.intellectualsites.plotsquared.plot.object.PlotPlayer;
import com.github.intellectualsites.plotsquared.plot.uuid.UUIDWrapper;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import java.util.Arrays;
import java.util.UUID;

public class DefaultUUIDWrapper implements UUIDWrapper {

    @Override public UUID getUUID(PlotPlayer player) {
        return ((BukkitPlayer) player).player.getUniqueId();
    }

    @Override public UUID getUUID(OfflinePlotPlayer player) {
        return player.getUUID();
    }

    @Override public OfflinePlotPlayer getOfflinePlayer(UUID uuid) {
        return new BukkitOfflinePlayer(Bukkit.getOfflinePlayer(uuid));
    }

    @Override public UUID getUUID(String name) {
        return Bukkit.getOfflinePlayer(name).getUniqueId();
    }

    @Override public OfflinePlotPlayer[] getOfflinePlayers() {
        OfflinePlayer[] ops = Bukkit.getOfflinePlayers();
        return Arrays.stream(ops).map(BukkitOfflinePlayer::new).toArray(BukkitOfflinePlayer[]::new);
    }

    @Override public OfflinePlotPlayer getOfflinePlayer(String name) {
        return new BukkitOfflinePlayer(Bukkit.getOfflinePlayer(name));
    }
}
