package com.plotsquared.bukkit.util.bukkit;

import com.intellectualcrafters.plot.object.PlotPlayer;
import com.intellectualcrafters.plot.util.PlayerManager;
import com.plotsquared.bukkit.object.BukkitPlayer;

public class BukkitPlayerManager extends PlayerManager {
    
    @Override
    public void kickPlayer(PlotPlayer player, String reason) {
        ((BukkitPlayer) player).player.kickPlayer(reason);
    }
    
}
