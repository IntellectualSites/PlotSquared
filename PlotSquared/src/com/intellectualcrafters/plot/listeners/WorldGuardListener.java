package com.intellectualcrafters.plot.listeners;

import com.intellectualcrafters.plot.Plot;
import com.intellectualcrafters.plot.PlotHelper;
import com.intellectualcrafters.plot.PlotId;
import com.intellectualcrafters.plot.PlotMain;
import com.intellectualcrafters.plot.events.PlayerClaimPlotEvent;
import com.intellectualcrafters.plot.events.PlotDeleteEvent;
import com.sk89q.worldedit.BlockVector;
import com.sk89q.worldguard.domains.DefaultDomain;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

/**
 * Created by Citymonstret on 2014-09-24.
 */
public class WorldGuardListener implements Listener {

    /*
     * TODO recreate WG region when a plot is merged
     *  - It should use the region of the main plot, and delete the other ones.
     */
    
    private PlotMain plugin;

    public WorldGuardListener(PlotMain plugin) {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onPlotClaim(PlayerClaimPlotEvent event) {
        Player player = event.getPlayer();
        Plot plot = event.getPlot();
        RegionManager manager = PlotMain.worldGuard.getRegionManager(plot.getWorld());

        Location location1 = PlotHelper.getPlotBottomLoc(plot.getWorld(), plot.getId());
        Location location2 = PlotHelper.getPlotTopLoc(plot.getWorld(), plot.getId());

        BlockVector vector1 = new BlockVector(location1.getBlockX(), 1, location1.getBlockZ());
        BlockVector vector2 = new BlockVector(location2.getBlockX(), plot.getWorld().getMaxHeight(), location2.getBlockZ());

        ProtectedRegion region = new ProtectedCuboidRegion(plot.getId().x + "-" + plot.getId().y, vector1, vector2);

        DefaultDomain owner = new DefaultDomain();
        owner.addPlayer(PlotMain.worldGuard.wrapPlayer(player));

        region.setOwners(owner);

        manager.addRegion(region);
    }

    @EventHandler
    public void onPlotDelete(PlotDeleteEvent event) {
        PlotId plot = event.getPlotId();
        World world = Bukkit.getWorld(event.getWorld());

        RegionManager manager = PlotMain.worldGuard.getRegionManager(world);
        manager.removeRegion(plot.x + "-" + plot.y);
    }
}
