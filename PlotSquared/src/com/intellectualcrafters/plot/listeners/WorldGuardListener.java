package com.intellectualcrafters.plot.listeners;

import java.util.ArrayList;
import java.util.Map;
import java.util.UUID;

import com.intellectualcrafters.plot.Plot;
import com.intellectualcrafters.plot.PlotHelper;
import com.intellectualcrafters.plot.PlotId;
import com.intellectualcrafters.plot.PlotMain;
import com.intellectualcrafters.plot.events.PlayerClaimPlotEvent;
import com.intellectualcrafters.plot.events.PlotDeleteEvent;
import com.intellectualcrafters.plot.events.PlotMergeEvent;
import com.intellectualcrafters.plot.events.PlotUnlinkEvent;
import com.sk89q.worldedit.BlockVector;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.domains.DefaultDomain;
import com.sk89q.worldguard.protection.flags.Flag;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

/**
 * Created by Citymonstret on 2014-09-24.
 */
public class WorldGuardListener implements Listener {
    public WorldGuardListener(PlotMain plugin) {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler(priority=EventPriority.MONITOR, ignoreCancelled=true)
    public void onMerge(PlotMergeEvent event)  {
        Plot main = event.getPlot();
        ArrayList<PlotId> plots = event.getPlots();
        World world = event.getWorld();
        RegionManager manager = PlotMain.worldGuard.getRegionManager(world);
        for (PlotId plot:plots) {
            if (!plot.equals(main))
                manager.removeRegion(plot.x + "-" + plot.y);
        }
        ProtectedRegion region = manager.getRegion(main.id.x + "-" + main.id.y);
        DefaultDomain owner = region.getOwners();
        Map<Flag<?>, Object> flags = region.getFlags();
        DefaultDomain members = region.getMembers();
        manager.removeRegion(main.id.x + "-" + main.id.y);
        
        Location location1 = PlotHelper.getPlotBottomLocAbs(world, plots.get(0));
        Location location2 = PlotHelper.getPlotTopLocAbs(world, plots.get(plots.size()-1));

        BlockVector vector1 = new BlockVector(location1.getBlockX(), 1, location1.getBlockZ());
        BlockVector vector2 = new BlockVector(location2.getBlockX(), world.getMaxHeight(), location2.getBlockZ());
        ProtectedRegion rg = new ProtectedCuboidRegion(main.id.x + "-" + main.id.y, vector1, vector2);
        
        rg.setFlags(flags);

        rg.setOwners(owner);
        
        rg.setMembers(members);

        manager.addRegion(rg);
    }
    
    @EventHandler(priority=EventPriority.MONITOR, ignoreCancelled=true)
    public void onUnlink(PlotUnlinkEvent event)  {
        World w = event.getWorld();
        ArrayList<PlotId> plots = event.getPlots();
        Plot main = PlotMain.getPlots(w).get(plots.get(0));
        
        RegionManager manager = PlotMain.worldGuard.getRegionManager(w);
        ProtectedRegion region = manager.getRegion(main.id.x + "-" + main.id.y);
        
        DefaultDomain owner = region.getOwners();
        Map<Flag<?>, Object> flags = region.getFlags();
        DefaultDomain members = region.getMembers();
        
        manager.removeRegion(main.id.x + "-" + main.id.y);
        for (int i = 1;i<plots.size();i++) {
            PlotId id = plots.get(i);
            Location location1 = PlotHelper.getPlotBottomLocAbs(w, id);
            Location location2 = PlotHelper.getPlotTopLocAbs(w, id);

            BlockVector vector1 = new BlockVector(location1.getBlockX(), 1, location1.getBlockZ());
            BlockVector vector2 = new BlockVector(location2.getBlockX(), w.getMaxHeight(), location2.getBlockZ());
            ProtectedRegion rg = new ProtectedCuboidRegion(id.x + "-" + id.y, vector1, vector2);
            
            rg.setFlags(flags);

            rg.setOwners(owner);
            
            rg.setMembers(members);

            manager.addRegion(rg);
        }
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
