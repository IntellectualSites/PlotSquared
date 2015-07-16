package com.intellectualcrafters.plot.util;

import com.intellectualcrafters.plot.object.PlotPlayer;

public abstract class PlayerManager {
    public static PlayerManager manager;
    
    public abstract void kickPlayer(PlotPlayer player, String reason);
}
