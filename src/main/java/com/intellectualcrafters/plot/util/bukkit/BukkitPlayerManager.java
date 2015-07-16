package com.intellectualcrafters.plot.util.bukkit;

import com.intellectualcrafters.plot.object.BukkitPlayer;
import com.intellectualcrafters.plot.object.PlotPlayer;
import com.intellectualcrafters.plot.util.PlayerManager;

public class BukkitPlayerManager extends PlayerManager {
    
    @Override
    public void kickPlayer(PlotPlayer player, String reason) {
        ((BukkitPlayer) player).player.kickPlayer(reason);
    }
    
}
