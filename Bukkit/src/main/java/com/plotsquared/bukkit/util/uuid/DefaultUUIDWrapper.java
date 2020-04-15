package com.plotsquared.bukkit.util.uuid;

import com.plotsquared.bukkit.player.BukkitOfflinePlayer;
import com.plotsquared.bukkit.player.BukkitPlayer;
import com.plotsquared.core.player.OfflinePlotPlayer;
import com.plotsquared.core.player.PlotPlayer;
import com.plotsquared.core.util.uuid.UUIDWrapper;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.UUID;

public class DefaultUUIDWrapper extends UUIDWrapper {

    @NotNull @Override public UUID getUUID(PlotPlayer player) {
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
