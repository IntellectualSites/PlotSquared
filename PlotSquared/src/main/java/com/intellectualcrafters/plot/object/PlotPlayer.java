package com.intellectualcrafters.plot.object;

import com.intellectualcrafters.plot.BukkitMain;
import com.intellectualcrafters.plot.util.bukkit.UUIDHandler;

import java.util.UUID;

/**
 * Created 2015-02-20 for PlotSquared
 *
 * @author Citymonstret
 */
public interface PlotPlayer {
    public Location getLocation();

    public UUID getUUID();

    public boolean hasPermission(final String perm);

    public void sendMessage(final String message);
    
    public void teleport(final Location loc);
    
    public boolean isOp();
    
    public boolean isOnline();
    
    public String getName();
}
