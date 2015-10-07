package com.plotsquared.sponge.util;

import com.intellectualcrafters.plot.object.OfflinePlotPlayer;
import com.intellectualcrafters.plot.object.PlotPlayer;
import com.intellectualcrafters.plot.util.EconHandler;
import com.intellectualcrafters.plot.util.UUIDHandler;
import com.plotsquared.sponge.object.SpongePlayer;

public class SpongeEconHandler extends EconHandler {
    
    @Override
    public void withdrawMoney(PlotPlayer player, double amount) {
        // TODO Auto-generated method stub
        
    }
    
    @Override
    public void depositMoney(PlotPlayer player, double amount) {
        // TODO Auto-generated method stub
        
    }
    
    @Override
    public void depositMoney(OfflinePlotPlayer player, double amount) {
        // TODO Auto-generated method stub
        
    }
    
    @Override
    public void setPermission(String world, String player, String perm, boolean value) {
        // TODO Auto-generated method stub
    }
    
    @Override
    public boolean hasPermission(String world, String player, String perm) {
        SpongePlayer obj = (SpongePlayer) UUIDHandler.getPlayer(player);
        if (obj != null) {
            return obj.player.hasPermission(perm);
        }
        // TODO offline
        return false;
    }
    
}
