package com.plotsquared.bukkit.object;

import java.util.UUID;

import org.bukkit.OfflinePlayer;

import com.intellectualcrafters.plot.object.OfflinePlotPlayer;

public class BukkitOfflinePlayer implements OfflinePlotPlayer
{

    public final OfflinePlayer player;

    /**
     * Please do not use this method. Instead use BukkitUtil.getPlayer(Player), as it caches player objects.
     * @param player
     */
    public BukkitOfflinePlayer(final OfflinePlayer player)
    {
        this.player = player;
    }

    @Override
    public UUID getUUID()
    {
        return player.getUniqueId();
    }

    @Override
    public long getLastPlayed()
    {
        return player.getLastPlayed();
    }

    @Override
    public boolean isOnline()
    {
        return player.isOnline();
    }

    @Override
    public String getName()
    {
        return player.getName();
    }
}
