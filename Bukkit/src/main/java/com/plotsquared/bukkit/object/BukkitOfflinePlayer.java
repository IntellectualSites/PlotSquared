package com.plotsquared.bukkit.object;

import com.intellectualcrafters.plot.object.OfflinePlotPlayer;
import org.bukkit.OfflinePlayer;

import java.util.UUID;

public class BukkitOfflinePlayer implements OfflinePlotPlayer {

    public final OfflinePlayer player;

    /**
     * Please do not use this method. Instead use BukkitUtil.getPlayer(Player),
     * as it caches player objects.
     *
     * @param player
     */
    public BukkitOfflinePlayer(OfflinePlayer player) {
        this.player = player;
    }

    @Override
    public UUID getUUID() {
        return this.player.getUniqueId();
    }

    @Override
    public long getLastPlayed() {
        return this.player.getLastPlayed();
    }

    @Override
    public boolean isOnline() {
        return this.player.isOnline();
    }

    @Override
    public String getName() {
        return this.player.getName();
    }
}
