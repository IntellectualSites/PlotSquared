package com.plotsquared.bukkit.listeners;

import java.util.List;

import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.ExplosionPrimeEvent;
import org.bukkit.util.Vector;

import com.intellectualcrafters.plot.PS;
import com.intellectualcrafters.plot.flag.FlagManager;
import com.intellectualcrafters.plot.object.Location;
import com.intellectualcrafters.plot.object.Plot;
import com.intellectualcrafters.plot.util.MainUtil;
import com.plotsquared.bukkit.util.BukkitUtil;

public class TNTListener implements Listener {
    private double lastRadius;
    
    @EventHandler
    public void onPrime(final ExplosionPrimeEvent event) {
        lastRadius = event.getRadius() + 1;
    }
    
    @EventHandler
    public void onExplode(final EntityExplodeEvent event) {
        final Entity entity = event.getEntity();
        if (entity == null) {
            return;
        }
        final World world = entity.getWorld();
        final String worldname = world.getName();
        if (!PS.get().isPlotWorld(worldname)) {
            return;
        }
        final Plot plot = MainUtil.getPlot(BukkitUtil.getLocation(entity));
        if (plot == null) {
            return;
        }
        
        if (!FlagManager.isPlotFlagTrue(plot, "explosion")) {
            return;
        }
        
        final Location bot = MainUtil.getPlotBottomLoc(plot.world, plot.id);
        final Location top = MainUtil.getPlotTopLoc(plot.world, plot.id);
        
        final List<Entity> nearby = entity.getNearbyEntities(lastRadius, lastRadius, lastRadius);
        for (final Entity near : nearby) {
            if ((near instanceof TNTPrimed) || (near.getType() == EntityType.MINECART_TNT)) {
                final Vector velocity = near.getVelocity();
                final Location loc = BukkitUtil.getLocation(near);
                final Plot nearPlot = MainUtil.getPlot(loc);
                if (!plot.equals(nearPlot)) {
                    near.setVelocity(new Vector(0, 0, 0));
                    continue;
                }
                final double vx = velocity.getX();
                velocity.getX();
                final double vz = velocity.getX();
                
                int dx;
                int dz;
                
                if (vx > 0) {
                    dx = top.getX() - loc.getX();
                } else {
                    dx = bot.getX() - loc.getX();
                }
                if (vz > 0) {
                    dz = top.getZ() - loc.getZ();
                } else {
                    dz = bot.getZ() - loc.getZ();
                }
                
                final double s1 = dx / vx;
                final double s2 = dz / vz;
                final Vector v1 = new Vector(dx, 0, vz * s1);
                final Vector v2 = new Vector(vx * s2, 0, dz);
                
                Vector shortest;
                if (v1.length() < v2.length()) {
                    shortest = v1;
                } else {
                    shortest = v2;
                }
                
                final Location landing = loc.add(shortest.getBlockX() + 1, 0, shortest.getBlockZ() + 1);
                final int ty = MainUtil.getHeighestBlock(worldname, landing.getX(), landing.getZ());
                final int diff = ty - loc.getY();
                final double calcDiff = getY(velocity, Math.sqrt((shortest.getBlockX() * shortest.getBlockX()) + (shortest.getBlockZ() * shortest.getBlockZ())));
                if (calcDiff > diff) {
                    near.setVelocity(new Vector(0, 0, 0));
                }
            }
        }
        event.getEntity().setVelocity(new Vector(0, 0, 0));
    }
    
    public double getY(final Vector velocity, final double x) {
        
        final double g = 16;
        final double l1 = velocity.length();
        final double l2 = Math.sqrt((velocity.getX() * velocity.getX()) + (velocity.getZ() * velocity.getZ()));
        
        final double v = l1 * 20;
        double theta = Math.acos(l2 / l1);
        if (velocity.getY() < 0) {
            theta = -theta;
        }
        final double cos = Math.cos(theta);
        final double yDiff = (x * Math.tan(theta)) - ((g * x * x) / (2 * (v * v * cos * cos)));
        return yDiff;
    }
}
