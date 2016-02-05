package com.plotsquared.sponge.util;

import java.util.UUID;

import org.apache.commons.lang.NotImplementedException;

import com.intellectualcrafters.plot.PS;
import com.intellectualcrafters.plot.object.OfflinePlotPlayer;
import com.intellectualcrafters.plot.object.PlotPlayer;
import com.intellectualcrafters.plot.util.EconHandler;
import com.intellectualcrafters.plot.util.UUIDHandler;
import com.plotsquared.sponge.SpongeMain;
import com.plotsquared.sponge.object.SpongePlayer;

public class SpongeEconHandler extends EconHandler {
    
    private Object TE_SERVICE;
    private Object EL_SERVICE;

    public SpongeEconHandler() {
        try {
            Class<?> clazz = Class.forName("com.erigitic.service.TEService");
            this.TE_SERVICE = SpongeMain.THIS.getGame().getServiceManager().provide(clazz).get();

        } catch (Exception e) {
            try {
                Class<?> clazz = Class.forName("me.Flibio.EconomyLite.API.EconomyLiteAPI");
                this.EL_SERVICE = SpongeMain.THIS.getGame().getServiceManager().provide(clazz).get();
            } catch (Exception e2) {
                PS.log("No economy service found! (EconomyLite, TotalEconomy)");
            }
        }
    }

    @Override
    public void withdrawMoney(PlotPlayer player, double amount) {
        UUID uuid = ((SpongePlayer) player).player.getUniqueId();
    }
    
    @Override
    public void depositMoney(PlotPlayer player, double amount) {
        UUID uuid = ((SpongePlayer) player).player.getUniqueId();
    }
    
    @Override
    public void depositMoney(OfflinePlotPlayer player, double amount) {
        UUID uuid = player.getUUID();
        
    }
    
    @Override
    public void setPermission(String world, String player, String perm, boolean value) {
        // TODO Auto-generated method stub
        throw new NotImplementedException("TODO/WIP/NOT IMPLEMENTED!");
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
