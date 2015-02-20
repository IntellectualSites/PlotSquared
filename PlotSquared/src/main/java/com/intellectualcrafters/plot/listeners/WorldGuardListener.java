////////////////////////////////////////////////////////////////////////////////////////////////////
// PlotSquared - A plot manager and world generator for the Bukkit API                             /
// Copyright (c) 2014 IntellectualSites/IntellectualCrafters                                       /
//                                                                                                 /
// This program is free software; you can redistribute it and/or modify                            /
// it under the terms of the GNU General Public License as published by                            /
// the Free Software Foundation; either version 3 of the License, or                               /
// (at your option) any later version.                                                             /
//                                                                                                 /
// This program is distributed in the hope that it will be useful,                                 /
// but WITHOUT ANY WARRANTY; without even the implied warranty of                                  /
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the                                   /
// GNU General Public License for more details.                                                    /
//                                                                                                 /
// You should have received a copy of the GNU General Public License                               /
// along with this program; if not, write to the Free Software Foundation,                         /
// Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301  USA                               /
//                                                                                                 /
// You can contact us via: support@intellectualsites.com                                           /
////////////////////////////////////////////////////////////////////////////////////////////////////
package com.intellectualcrafters.plot.listeners;

import java.util.ArrayList;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import com.intellectualcrafters.plot.PlotSquared;
import com.intellectualcrafters.plot.events.PlayerClaimPlotEvent;
import com.intellectualcrafters.plot.events.PlayerPlotHelperEvent;
import com.intellectualcrafters.plot.events.PlayerPlotTrustedEvent;
import com.intellectualcrafters.plot.events.PlotDeleteEvent;
import com.intellectualcrafters.plot.events.PlotMergeEvent;
import com.intellectualcrafters.plot.events.PlotUnlinkEvent;
import com.intellectualcrafters.plot.object.Plot;
import com.intellectualcrafters.plot.object.PlotId;
import com.intellectualcrafters.plot.util.PlotHelper;
import com.intellectualcrafters.plot.util.bukkit.UUIDHandler;
import com.sk89q.worldedit.BlockVector;
import com.sk89q.worldguard.domains.DefaultDomain;
import com.sk89q.worldguard.protection.flags.DefaultFlag;
import com.sk89q.worldguard.protection.flags.Flag;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

/**
 * Created 2014-09-24 for PlotSquared
 *
 * @author Citymonstret
 * @author Empire92
 */
public class WorldGuardListener implements Listener {
    public final ArrayList<String> str_flags;
    public final ArrayList<Flag<?>> flags;
    
    public WorldGuardListener(final PlotSquared plugin) {
        this.str_flags = new ArrayList<>();
        this.flags = new ArrayList<>();
        for (final Flag<?> flag : DefaultFlag.getFlags()) {
            this.str_flags.add(flag.getName());
            this.flags.add(flag);
        }
    }
    
    public void changeOwner(final Player requester, final UUID owner, final World world, final Plot plot) {
        try {
            final RegionManager manager = PlotSquared.worldGuard.getRegionManager(world);
            final ProtectedRegion region = manager.getRegion(plot.id.x + "-" + plot.id.y);
            final DefaultDomain owners = new DefaultDomain();
            owners.addPlayer(UUIDHandler.getName(owner));
            region.setOwners(owners);
        } catch (final Exception e) {
        }
    }
    
    public void removeFlag(final Player requester, final World world, final Plot plot, final String key) {
        final boolean op = requester.isOp();
        requester.setOp(true);
        try {
            final RegionManager manager = PlotSquared.worldGuard.getRegionManager(world);
            manager.getRegion(plot.id.x + "-" + plot.id.y);
            for (final Flag<?> flag : this.flags) {
                if (flag.getName().equalsIgnoreCase(key)) {
                    requester.performCommand("region flag " + (plot.id.x + "-" + plot.id.y) + " " + key);
                }
            }
        } catch (final Exception e) {
            requester.setOp(op);
        } finally {
            requester.setOp(op);
        }
    }
    
    public void addFlag(final Player requester, final World world, final Plot plot, final String key, final String value) {
        final boolean op = requester.isOp();
        requester.setOp(true);
        try {
            final RegionManager manager = PlotSquared.worldGuard.getRegionManager(world);
            manager.getRegion(plot.id.x + "-" + plot.id.y);
            for (final Flag<?> flag : this.flags) {
                if (flag.getName().equalsIgnoreCase(key)) {
                    requester.performCommand("region flag " + (plot.id.x + "-" + plot.id.y) + " " + key + " " + value);
                }
            }
        } catch (final Exception e) {
            requester.setOp(op);
        } finally {
            requester.setOp(op);
        }
    }
    
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onMerge(final PlotMergeEvent event) {
        final Plot main = event.getPlot();
        final ArrayList<PlotId> plots = event.getPlots();
        final World world = event.getWorld();
        final RegionManager manager = PlotSquared.worldGuard.getRegionManager(world);
        for (final PlotId plot : plots) {
            if (!plot.equals(main.getId())) {
                manager.removeRegion(plot.x + "-" + plot.y);
            }
        }
        final ProtectedRegion region = manager.getRegion(main.id.x + "-" + main.id.y);
        final DefaultDomain owner = region.getOwners();
        final Map<Flag<?>, Object> flags = region.getFlags();
        final DefaultDomain members = region.getMembers();
        manager.removeRegion(main.id.x + "-" + main.id.y);
        final Location location1 = PlotHelper.getPlotBottomLocAbs(world, plots.get(0));
        final Location location2 = PlotHelper.getPlotTopLocAbs(world, plots.get(plots.size() - 1));
        final BlockVector vector1 = new BlockVector(location1.getBlockX(), 1, location1.getBlockZ());
        final BlockVector vector2 = new BlockVector(location2.getBlockX(), world.getMaxHeight(), location2.getBlockZ());
        final ProtectedRegion rg = new ProtectedCuboidRegion(main.id.x + "-" + main.id.y, vector1, vector2);
        rg.setFlags(flags);
        rg.setOwners(owner);
        rg.setMembers(members);
        manager.addRegion(rg);
    }
    
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onUnlink(final PlotUnlinkEvent event) {
        try {
            final World w = event.getWorld();
            final ArrayList<PlotId> plots = event.getPlots();
            final Plot main = PlotSquared.getPlots(w).get(plots.get(0));
            final RegionManager manager = PlotSquared.worldGuard.getRegionManager(w);
            final ProtectedRegion region = manager.getRegion(main.id.x + "-" + main.id.y);
            final DefaultDomain owner = region.getOwners();
            final Map<Flag<?>, Object> flags = region.getFlags();
            final DefaultDomain members = region.getMembers();
            manager.removeRegion(main.id.x + "-" + main.id.y);
            for (int i = 1; i < plots.size(); i++) {
                final PlotId id = plots.get(i);
                final Location location1 = PlotHelper.getPlotBottomLocAbs(w, id);
                final Location location2 = PlotHelper.getPlotTopLocAbs(w, id);
                final BlockVector vector1 = new BlockVector(location1.getBlockX(), 1, location1.getBlockZ());
                final BlockVector vector2 = new BlockVector(location2.getBlockX(), w.getMaxHeight(), location2.getBlockZ());
                final ProtectedRegion rg = new ProtectedCuboidRegion(id.x + "-" + id.y, vector1, vector2);
                rg.setFlags(flags);
                rg.setOwners(owner);
                rg.setMembers(members);
                manager.addRegion(rg);
            }
        } catch (final Exception e) {
        }
    }
    
    @EventHandler
    public void onPlotClaim(final PlayerClaimPlotEvent event) {
        try {
            final Player player = event.getPlayer();
            final Plot plot = event.getPlot();
            final RegionManager manager = PlotSquared.worldGuard.getRegionManager(plot.world);
            final Location location1 = PlotHelper.getPlotBottomLoc(plot.world, plot.getId());
            final Location location2 = PlotHelper.getPlotTopLoc(plot.world, plot.getId());
            final BlockVector vector1 = new BlockVector(location1.getBlockX(), 1, location1.getBlockZ());
            final BlockVector vector2 = new BlockVector(location2.getBlockX(), plot.world.getMaxHeight(), location2.getBlockZ());
            final ProtectedRegion region = new ProtectedCuboidRegion(plot.getId().x + "-" + plot.getId().y, vector1, vector2);
            final DefaultDomain owner = new DefaultDomain();
            owner.addPlayer(PlotSquared.worldGuard.wrapPlayer(player));
            region.setOwners(owner);
            manager.addRegion(region);
        } catch (final Exception e) {
        }
    }
    
    @EventHandler
    public void onPlotDelete(final PlotDeleteEvent event) {
        try {
            final PlotId plot = event.getPlotId();
            final World world = Bukkit.getWorld(event.getWorld());
            final RegionManager manager = PlotSquared.worldGuard.getRegionManager(world);
            manager.removeRegion(plot.x + "-" + plot.y);
        } catch (final Exception e) {
        }
    }
    
    public void addUser(final Player requester, final UUID user, final World world, final Plot plot) {
        final RegionManager manager = PlotSquared.worldGuard.getRegionManager(world);
        final ProtectedRegion region = manager.getRegion(plot.id.x + "-" + plot.id.y);
        final DefaultDomain members = region.getMembers();
        members.addPlayer(UUIDHandler.getName(user));
        region.setMembers(members);
    }
    
    public void removeUser(final Player requester, final UUID user, final World world, final Plot plot) {
        final RegionManager manager = PlotSquared.worldGuard.getRegionManager(world);
        final ProtectedRegion region = manager.getRegion(plot.id.x + "-" + plot.id.y);
        final DefaultDomain members = region.getMembers();
        members.removePlayer(UUIDHandler.getName(user));
        region.setMembers(members);
    }
    
    @EventHandler
    public void onPlotHelper(final PlayerPlotHelperEvent event) {
        if (event.wasAdded()) {
            addUser(event.getInitiator(), event.getPlayer(), event.getInitiator().getWorld(), event.getPlot());
        } else {
            removeUser(event.getInitiator(), event.getPlayer(), event.getInitiator().getWorld(), event.getPlot());
        }
    }
    
    @EventHandler
    public void onPlotTrusted(final PlayerPlotTrustedEvent event) {
        if (event.wasAdded()) {
            addUser(event.getInitiator(), event.getPlayer(), event.getInitiator().getWorld(), event.getPlot());
        } else {
            removeUser(event.getInitiator(), event.getPlayer(), event.getInitiator().getWorld(), event.getPlot());
        }
    }
    
    @EventHandler
    public void onPlotDenied(final PlayerPlotTrustedEvent event) {
        if (event.wasAdded()) {
            removeUser(event.getInitiator(), event.getPlayer(), event.getInitiator().getWorld(), event.getPlot());
        }
    }
}
