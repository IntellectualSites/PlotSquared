package com.plotsquared.bukkit.listeners;

import com.intellectualcrafters.plot.PS;
import com.intellectualcrafters.plot.flag.FlagManager;
import com.intellectualcrafters.plot.object.Location;
import com.intellectualcrafters.plot.object.Plot;
import com.intellectualcrafters.plot.util.MainUtil;
import com.plotsquared.bukkit.util.bukkit.BukkitUtil;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.ExplosionPrimeEvent;
import org.bukkit.util.Vector;

import java.util.List;

public class TNTListener implements Listener {
    private double lastRadius;
    
    @EventHandler
    public void onPrime(ExplosionPrimeEvent event) {
        lastRadius = event.getRadius() + 1;
    }
    @EventHandler
    public void onExplode(EntityExplodeEvent event) {
        Entity entity = event.getEntity();
        if (entity == null) {
            return;
        }
        World world = entity.getWorld();
        String worldname = world.getName();
        if (!PS.get().isPlotWorld(worldname)) {
            return;
        }
        Plot plot = MainUtil.getPlot(BukkitUtil.getLocation(entity));
        if (plot == null) {
            return;
        }
        
        if (!FlagManager.isPlotFlagTrue(plot, "explosion")) {
            return;
        }
        
        Location bot = MainUtil.getPlotBottomLoc(plot.world, plot.id);
        Location top = MainUtil.getPlotTopLoc(plot.world, plot.id);
        
        List<Entity> nearby = entity.getNearbyEntities(lastRadius, lastRadius, lastRadius);
        for (Entity near : nearby) {
            if (near instanceof TNTPrimed || near.getType() == EntityType.MINECART_TNT) {
                Vector velocity = near.getVelocity();
                Location loc = BukkitUtil.getLocation(near);
                Plot nearPlot = MainUtil.getPlot(loc);
                if (!plot.equals(nearPlot)) {
                    near.setVelocity(new Vector(0, 0, 0));
                    continue;
                }
                double vx = velocity.getX();
                double vy = velocity.getX();
                double vz = velocity.getX();
                
                int dx;
                int dz;
                
                if (vx > 0) {
                    dx = top.getX() - loc.getX();
                }
                else {
                    dx = bot.getX() - loc.getX();
                }
                if (vz > 0) {
                    dz = top.getZ() - loc.getZ();
                }
                else {
                    dz = bot.getZ() - loc.getZ();
                }
                
                double s1 = dx / vx;
                double s2 = dz / vz;
                Vector v1 = new Vector(dx, 0, vz * s1);
                Vector v2 = new Vector(vx * s2, 0, dz);
                
                Vector shortest;
                if (v1.length() < v2.length()) {
                    shortest = v1;
                }
                else {
                    shortest = v2;
                }
                
                Location landing = loc.add(shortest.getBlockX() + 1, 0, shortest.getBlockZ() + 1);
                int ty = BukkitUtil.getHeighestBlock(worldname, landing.getX(), landing.getZ());
                int diff = ty - loc.getY();
                double calcDiff = getY(velocity, Math.sqrt(shortest.getBlockX() * shortest.getBlockX() + shortest.getBlockZ() * shortest.getBlockZ()));
                if (calcDiff > diff) {
                    near.setVelocity(new Vector(0, 0, 0));
                }
            }
        }
        event.getEntity().setVelocity(new Vector(0, 0, 0));
    }
    
    public double getY(Vector velocity, double x) {
        
        double g = 16;
        double l1 = velocity.length();
        double l2 = Math.sqrt(velocity.getX() * velocity.getX() + velocity.getZ() * velocity.getZ());
        
        double v = l1 * 20;
        double theta = Math.acos(l2/l1);
        if (velocity.getY() < 0) {
            theta = -theta;
        }
        double cos = Math.cos(theta);
        double yDiff = (x * Math.tan(theta)) - ((g * x * x) / (2 * (v * v * cos * cos)));
        return yDiff;
    }
}
