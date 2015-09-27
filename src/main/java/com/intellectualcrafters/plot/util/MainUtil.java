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
package com.intellectualcrafters.plot.util;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import com.intellectualcrafters.plot.PS;
import com.intellectualcrafters.plot.config.C;
import com.intellectualcrafters.plot.config.Settings;
import com.intellectualcrafters.plot.database.DBFunc;
import com.intellectualcrafters.plot.flag.Flag;
import com.intellectualcrafters.plot.object.BlockLoc;
import com.intellectualcrafters.plot.object.ChunkLoc;
import com.intellectualcrafters.plot.object.Location;
import com.intellectualcrafters.plot.object.Plot;
import com.intellectualcrafters.plot.object.PlotBlock;
import com.intellectualcrafters.plot.object.PlotCluster;
import com.intellectualcrafters.plot.object.PlotId;
import com.intellectualcrafters.plot.object.PlotManager;
import com.intellectualcrafters.plot.object.PlotPlayer;
import com.intellectualcrafters.plot.object.PlotWorld;
import com.intellectualcrafters.plot.object.PseudoRandom;
import com.intellectualcrafters.plot.object.RegionWrapper;
import com.intellectualcrafters.plot.object.RunnableVal;
import com.plotsquared.listener.PlotListener;

/**
 * plot functions
 *
 */
public class MainUtil {
    public final static HashMap<Plot, Integer> runners = new HashMap<>();
    public static boolean canSendChunk = false;
    public static boolean canSetFast = true;
    public static ArrayList<String> runners_p = new ArrayList<>();
    public static HashMap<String, PlotId> lastPlot = new HashMap<>();
    public static HashMap<String, Integer> worldBorder = new HashMap<>();
    public static PseudoRandom random = new PseudoRandom();
    
    public static short[][] x_loc;
    public static short[][] y_loc;
    public static short[][] z_loc;
    
    /**
     * This cache is used for world generation and just saves a bit of calculation time when checking if something is in the plot area.
     */
    public static void initCache() {
        if (x_loc == null) {
            x_loc = new short[16][4096];
            y_loc = new short[16][4096];
            z_loc = new short[16][4096];
            for (int i = 0; i < 16; i++) {
                final int i4 = i << 4;
                for (int j = 0; j < 4096; j++) {
                    final int y = (i4) + (j >> 8);
                    final int a = (j - ((y & 0xF) << 8));
                    final int z1 = (a >> 4);
                    final int x1 = a - (z1 << 4);
                    x_loc[i][j] = (short) x1;
                    y_loc[i][j] = (short) y;
                    z_loc[i][j] = (short) z1;
                }
            }
        }
    }
    
    /**
     * Attempt to find the largest rectangular region in a plot (as plots can form non rectangular shapes) 
     * @param plot
     * @return
     */
    public static RegionWrapper getLargestRegion(Plot plot) {
        HashSet<RegionWrapper> regions = getRegions(plot);
        RegionWrapper max = null;
        int area = 0;
        for (RegionWrapper region : regions) {
            int current = (region.maxX - region.minX + 1) * (region.maxZ - region.minZ + 1);
            if (current > area) {
                max = region;
                area = current;
            }
        }
        return max;
    }
    
    /**
     * This will combine each plot into effective rectangular regions
     * @param plot
     * @return
     */
    public static HashSet<RegionWrapper> getRegions(Plot origin) {
        if (regions_cache != null && connected_cache != null && connected_cache.contains(origin)) {
            return regions_cache;
        }
        if (!origin.isMerged()) {
            final Location pos1 = MainUtil.getPlotBottomLocAbs(origin.world, origin.id);  
            final Location pos2 = MainUtil.getPlotTopLocAbs(origin.world, origin.id);
            connected_cache = new HashSet<>(Arrays.asList(origin));
            regions_cache = new HashSet<>(1);
            regions_cache.add(new RegionWrapper(pos1.getX(), pos2.getX(), pos1.getY(), pos2.getY(), pos1.getZ(), pos2.getZ()));
            return regions_cache;
        }
        
//        Create a list of ALL edges from your rectangles. One rectangle has 4 edges.
//        Let the Edge be a class with properly defined compareTo() and equals().
//        Sort the edges list (uses comapreTo).
//        Iterate through the list. If the same edge is present in the list TWICE, remove them both from the list.
//        The remaining edges are the edges of your polygon.
        
        
        HashSet<Plot> plots = getConnectedPlots(origin);
        regions_cache = new HashSet<>();
        HashSet<PlotId> visited = new HashSet<>();
        ArrayList<PlotId> ids;
        for (Plot current : plots) {
            if (visited.contains(current.id)) {
                continue;
            }
            boolean merge = true;
            boolean tmp = true;
            PlotId bot = new PlotId(current.id.x, current.id.y);
            PlotId top = new PlotId(current.id.x, current.id.y);
            while (merge) {
                merge = false;
                ids = getPlotSelectionIds(new PlotId(bot.x, bot.y - 1), new PlotId(top.x, bot.y - 1));
                tmp = true;
                for (PlotId id : ids) {
                    Plot plot = MainUtil.getPlotAbs(origin.world, id);
                    if (plot == null || !plot.getMerged(2) || (visited.contains(plot.id))) {
                        tmp = false;
                    }
                }
                if (tmp) {
                    merge = true;
                    bot.y--;
                }
                ids = getPlotSelectionIds(new PlotId(top.x + 1, bot.y), new PlotId(top.x + 1, top.y));
                tmp = true;
                for (PlotId id : ids) {
                    Plot plot = MainUtil.getPlotAbs(origin.world, id);
                    if (plot == null || !plot.getMerged(3) || (visited.contains(plot.id))) {
                        tmp = false;
                    }
                }
                if (tmp) {
                    merge = true;
                    top.x++;
                }
                ids = getPlotSelectionIds(new PlotId(bot.x, top.y + 1), new PlotId(top.x, top.y + 1));
                tmp = true;
                for (PlotId id : ids) {
                    Plot plot = MainUtil.getPlotAbs(origin.world, id);
                    if (plot == null || !plot.getMerged(0) || (visited.contains(plot.id))) {
                        tmp = false;
                    }
                }
                if (tmp) {
                    merge = true;
                    top.y++;
                }
                ids = getPlotSelectionIds(new PlotId(bot.x - 1, bot.y), new PlotId(bot.x - 1, top.y));
                tmp = true;
                for (PlotId id : ids) {
                    Plot plot = MainUtil.getPlotAbs(origin.world, id);
                    if (plot == null || !plot.getMerged(1) || (visited.contains(plot.id))) {
                        tmp = false;
                    }
                }
                if (tmp) {
                    merge = true;
                    bot.x--;
                }
            }
            Location gtopabs = getPlotAbs(origin.world, top).getTopAbs();
            Location gbotabs = getPlotAbs(origin.world, bot).getBottomAbs();
            for (PlotId id : getPlotSelectionIds(bot, top)) {
                visited.add(id);
            }
            for (int x = bot.x; x <= top.x; x++) {
                Plot plot = getPlotAbs(current.world, new PlotId(x, top.y));
                if (plot.getMerged(2)) {
                    // south wedge
                    Location toploc = getPlotTopLoc_(plot);
                    Location botabs = plot.getBottomAbs();
                    Location topabs = plot.getTopAbs();
                    regions_cache.add(new RegionWrapper(botabs.getX(), topabs.getX(), topabs.getZ() + 1, toploc.getZ()));
                    if (plot.getMerged(5)) {
                        regions_cache.add(new RegionWrapper(topabs.getX() + 1, toploc.getX(), topabs.getZ() + 1, toploc.getZ()));
                        // intersection
                    }
                }
            }
            
            for (int y = bot.y; y <= top.y; y++) {
                Plot plot = getPlotAbs(current.world, new PlotId(top.x, y));
                if (plot.getMerged(1)) {
                    // east wedge
                    Location toploc = getPlotTopLoc_(plot);
                    Location botabs = plot.getBottomAbs();
                    Location topabs = plot.getTopAbs();
                    regions_cache.add(new RegionWrapper(topabs.getX() + 1, toploc.getX(), botabs.getZ(), topabs.getZ()));
                    if (plot.getMerged(5)) {
                        regions_cache.add(new RegionWrapper(topabs.getX() + 1, toploc.getX(), topabs.getZ() + 1, toploc.getZ()));
                        // intersection
                    }
                }
            }
            regions_cache.add(new RegionWrapper(gbotabs.getX(), gtopabs.getX(), gbotabs.getZ(), gtopabs.getZ()));
        }
        return regions_cache;
    }
    
    public static int hash(boolean[] array) {
        if (array.length == 4) {
            if (!array[0] && !array[1] && !array[2] && !array[3]) {
                return 0;
            }
            return ((array[0] ? 1 : 0) << 3) + ((array[1] ? 1 : 0) << 2) + ((array[2] ? 1 : 0) << 1) + (array[3] ? 1 : 0);
        }
        int n = 0;
        for (int j = 0; j < array.length; ++j) {
            n = (n << 1) + (array[j] ? 1 : 0);
        }
        return n;
    }
    
    public static boolean isPlotArea(final Location location) {
        final PlotWorld plotworld = PS.get().getPlotWorld(location.getWorld());
        if (plotworld == null) {
            return false;
        }
        if (plotworld.TYPE == 2) {
            return ClusterManager.getCluster(location) != null;
        }
        return true;
    }
    
    public static String getName(final UUID owner) {
        if (owner == null) {
            return C.NONE.s();
        }
        final String name = UUIDHandler.getName(owner);
        if (name == null) {
            return C.UNKNOWN.s();
        }
        return name;
    }
    
    public static List<PlotPlayer> getPlayersInPlot(final Plot plot) {
        final ArrayList<PlotPlayer> players = new ArrayList<>();
        for (final PlotPlayer pp : UUIDHandler.getPlayers().values()) {
            if (plot.equals(pp.getCurrentPlot())) {
                players.add(pp);
            }
        }
        return players;
    }
    
    public static void reEnterPlot(final Plot plot) {
        TaskManager.runTaskLater(new Runnable() {
            @Override
            public void run() {
                for (final PlotPlayer pp : getPlayersInPlot(plot)) {
                    PlotListener.plotExit(pp, plot);
                    PlotListener.plotEntry(pp, plot);
                }
            }
        }, 1);
    }
    
    public static void plotTask(Plot plot, RunnableVal<Plot> run) {
        if (!plot.isMerged()) {
            run.value = plot;
            run.run();
            return;
        }
        for (Plot current : getConnectedPlots(plot)) {
            run.value = current;
            run.run();
            if (run.value == null) {
                break;
            }
        }
    }
    
    public static List<Plot> getPlotsBySearch(final String search) {
        final String[] split = search.split(" ");
        final int size = split.length * 2;
        
        final List<UUID> uuids = new ArrayList<>();
        PlotId id = null;
        String world = null;
        String alias = null;
        
        for (final String term : split) {
            try {
                UUID uuid = UUIDHandler.getUUID(term, null);
                if (uuid == null) {
                    uuid = UUID.fromString(term);
                }
                if (uuid != null) {
                    uuids.add(uuid);
                    continue;
                }
            } catch (final Exception e) {
                id = PlotId.fromString(term);
                if (id != null) {
                    continue;
                }
                for (final String pw : PS.get().getPlotWorlds()) {
                    if (pw.equalsIgnoreCase(term)) {
                        world = pw;
                        break;
                    }
                }
                if (world == null) {
                    alias = term;
                }
            }
        }
        
        final ArrayList<ArrayList<Plot>> plotList = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            plotList.add(new ArrayList<Plot>());
        }
        
        for (final Plot plot : PS.get().getPlots()) {
            int count = 0;
            if (uuids.size() > 0) {
                for (final UUID uuid : uuids) {
                    if (plot.isOwner(uuid)) {
                        count += 2;
                    } else if (plot.isAdded(uuid)) {
                        count++;
                    }
                }
            }
            if (id != null) {
                if (plot.id.equals(id)) {
                    count++;
                }
            }
            if ((world != null) && plot.world.equals(world)) {
                count++;
            }
            if ((alias != null) && alias.equals(plot.getAlias())) {
                count += 2;
            }
            if (count != 0) {
                plotList.get(count - 1).add(plot);
            }
        }
        
        final List<Plot> plots = new ArrayList<Plot>();
        for (int i = plotList.size() - 1; i >= 0; i--) {
            if (plotList.get(i).size() > 0) {
                plots.addAll(plotList.get(i));
            }
        }
        return plots;
    }
    
    public static Plot getPlotFromString(final PlotPlayer player, final String arg, final boolean message) {
        if (arg == null) {
            if (player == null) {
                if (message) {
                    MainUtil.sendMessage(player, C.NOT_VALID_PLOT_WORLD);
                }
                return null;
            }
            return getPlotAbs(player.getLocation());
        }
        String worldname = null;
        PlotId id = null;
        if (player != null) {
            worldname = player.getLocation().getWorld();
        }
        final String[] split = arg.split(";|,");
        if (split.length == 3) {
            worldname = split[0];
            id = PlotId.fromString(split[1] + ";" + split[2]);
        } else if (split.length == 2) {
            id = PlotId.fromString(arg);
        } else {
            if (worldname == null) {
                if (PS.get().getPlotWorlds().size() == 0) {
                    if (message) {
                        MainUtil.sendMessage(player, C.NOT_VALID_PLOT_WORLD);
                    }
                    return null;
                }
                worldname = PS.get().getPlotWorlds().iterator().next();
            }
            for (final Plot p : PS.get().getPlotsInWorld(worldname)) {
                final String name = p.getAlias();
                if ((name.length() != 0) && name.equalsIgnoreCase(arg)) {
                    return p;
                }
            }
            for (final String world : PS.get().getPlotWorlds()) {
                if (!world.endsWith(worldname)) {
                    for (final Plot p : PS.get().getPlotsInWorld(world)) {
                        final String name = p.getAlias();
                        if ((name.length() != 0) && name.equalsIgnoreCase(arg)) {
                            return p;
                        }
                    }
                }
            }
        }
        if ((worldname == null) || !PS.get().isPlotWorld(worldname)) {
            if (message) {
                MainUtil.sendMessage(player, C.NOT_VALID_PLOT_WORLD);
            }
            return null;
        }
        if (id == null) {
            if (message) {
                MainUtil.sendMessage(player, C.NOT_VALID_PLOT_ID);
            }
            return null;
        }
        return getPlotAbs(worldname, id);
    }
    
    /**
     * Merges all plots in the arraylist (with cost)
     *
     * @param world
     * @param plotIds
     *
     * @return boolean
     */
    public static boolean mergePlots(final PlotPlayer player, final String world, final ArrayList<PlotId> plotIds) {
        final PlotWorld plotworld = PS.get().getPlotWorld(world);
        if ((EconHandler.manager != null) && plotworld.USE_ECONOMY) {
            final double cost = plotIds.size() * plotworld.MERGE_PRICE;
            if (cost > 0d) {
                if (EconHandler.manager.getMoney(player) < cost) {
                    MainUtil.sendMessage(player, C.CANNOT_AFFORD_MERGE, "" + cost);
                    return false;
                }
                EconHandler.manager.withdrawMoney(player, cost);
                MainUtil.sendMessage(player, C.REMOVED_BALANCE, cost + "");
            }
        }
        return MainUtil.mergePlots(world, plotIds, true, true);
    }
    
    /**
     * Unlink the plot and all connected plots
     * @param plot
     * @param createRoad
     * @return
     */
    public static boolean unlinkPlot(final Plot plot, final boolean createRoad, boolean createSign) {
        if (!plot.isMerged()) {
            return false;
        }
        HashSet<Plot> plots = getConnectedPlots(plot);
        ArrayList<PlotId> ids = new ArrayList<>(plots.size());
        for (Plot current : plots) {
            current.setHome(null);
            ids.add(current.id);
        }
        final boolean result = EventUtil.manager.callUnlink(plot.world, ids);
        if (!result) {
            return false;
        }
        plot.removeSign();
        final PlotManager manager = PS.get().getPlotManager(plot.world);
        final PlotWorld plotworld = PS.get().getPlotWorld(plot.world);
        manager.startPlotUnlink(plotworld, ids);
        if ((plotworld.TERRAIN != 3) && createRoad) {
            for (Plot current : plots) {
                if (current.getMerged(1)) {
                    manager.createRoadEast(plotworld, current);
                    if (current.getMerged(2)) {
                        manager.createRoadSouth(plotworld, current);
                        if (current.getMerged(5)) {
                            manager.createRoadSouthEast(plotworld, current);
                        }
                    }
                }
                else if (current.getMerged(2)) {
                    manager.createRoadSouth(plotworld, current);
                }
            }
        }
        for (Plot current : plots) {
            boolean[] merged = new boolean[] { false, false, false, false };
            current.setMerged(merged);
            if (createSign) {
                MainUtil.setSign(getName(current.owner), current);
            }
        }
        manager.finishPlotUnlink(plotworld, ids);
        return true;
    }
    
    public static boolean isPlotAreaAbs(final Location location) {
        final PlotWorld plotworld = PS.get().getPlotWorld(location.getWorld());
        if (plotworld == null) {
            return false;
        }
        if (plotworld.TYPE == 2) {
            return ClusterManager.getClusterAbs(location) != null;
        }
        return true;
    }
    
    public static boolean isPlotRoad(final Location location) {
        final PlotWorld plotworld = PS.get().getPlotWorld(location.getWorld());
        if (plotworld.TYPE == 2) {
            final PlotCluster cluster = ClusterManager.getCluster(location);
            if (cluster == null) {
                return false;
            }
        }
        final PlotManager manager = PS.get().getPlotManager(location.getWorld());
        return manager.getPlotId(plotworld, location.getX(), location.getY(), location.getZ()) == null;
    }
    
    public static boolean isPlotArea(final Plot plot) {
        final PlotWorld plotworld = PS.get().getPlotWorld(plot.world);
        if (plotworld.TYPE == 2) {
            return plot.getCluster() != null;
        }
        return true;
    }
    
    public static boolean enteredPlot(final Location l1, final Location l2) {
        final PlotId p1 = MainUtil.getPlotId(l1);
        final PlotId p2 = MainUtil.getPlotId(l2);
        return (p2 != null) && ((p1 == null) || !p1.equals(p2));
    }
    
    public static boolean leftPlot(final Location l1, final Location l2) {
        final PlotId p1 = MainUtil.getPlotId(l1);
        final PlotId p2 = MainUtil.getPlotId(l2);
        return (p1 != null) && ((p2 == null) || !p1.equals(p2));
    }
    
    /**
     * Get the number of plots for a player
     *
     * @param plr
     *
     * @return int plot count
     */
    public static int getPlayerPlotCount(final String world, final PlotPlayer plr) {
        final UUID uuid = plr.getUUID();
        int count = 0;
        for (final Plot plot : PS.get().getPlotsInWorld(world)) {
            if (plot.hasOwner() && plot.owner.equals(uuid) && (!Settings.DONE_COUNTS_TOWARDS_LIMIT || !plot.getFlags().containsKey("done"))) {
                count++;
            }
        }
        return count;
    }
    
    /**
     * Get a player's total number of plots that count towards their limit
     * @param plr
     * @return
     */
    public static int getPlayerPlotCount(final PlotPlayer plr) {
        if (!Settings.GLOBAL_LIMIT) {
            return getPlayerPlotCount(plr.getLocation().getWorld(), plr);
        }
        int count = 0;
        for (final String world : PS.get().getPlotWorldsString()) {
            count += getPlayerPlotCount(world, plr);
        }
        return count;
    }
    
    public static Plot getPlot(Plot plot) {
        if (plot == null) {
            return null;
        }
        return plot.getBasePlot(false);
    }
    
    public static Plot getPlot(Location loc) {
        return getPlot(getPlotAbs(loc));
    }
    
    public static Plot getPlot(String world, PlotId id) {
        if (id == null) {
            return null;
        }
        return getPlot(getPlotAbs(world, id));
    }
    
    public static Location getDefaultHome(Plot plot) {
        plot = plot.getBasePlot(false);
        final PlotWorld plotworld = PS.get().getPlotWorld(plot.world);
        if (plotworld.DEFAULT_HOME != null) {
            final PlotManager manager = PS.get().getPlotManager(plot.world);
            final int x;
            final int z;
            Location bot = plot.getBottomAbs();
            if ((plotworld.DEFAULT_HOME.x == Integer.MAX_VALUE) && (plotworld.DEFAULT_HOME.z == Integer.MAX_VALUE)) {
                Location top = plot.getTopAbs();
                x = ((top.getX() - bot.getX()) / 2) + bot.getX();
                z = ((top.getZ() - bot.getZ()) / 2) + bot.getZ();
            } else {
                x = bot.getX() + plotworld.DEFAULT_HOME.x;
                z = bot.getZ() + plotworld.DEFAULT_HOME.z;
            }
            final int y = Math.max(getHeighestBlock(plot.world, x, z), manager.getSignLoc(PS.get().getPlotWorld(plot.world), plot).getY());
            return new Location(plot.world, x, y + 1, z);
        }
        Location bot = plot.getBottomAbs();
        Location top = plot.getTopAbs();
        final int x = ((top.getX() - bot.getX()) / 2) + bot.getX();
        final int z = bot.getZ() - 1;
        final PlotManager manager = PS.get().getPlotManager(plot.world);
        final int y = Math.max(getHeighestBlock(plot.world, x, z), manager.getSignLoc(PS.get().getPlotWorld(plot.world), plot).getY());
        return new Location(plot.world, x, y + 1, z);
    }
    
    public static boolean teleportPlayer(final PlotPlayer player, final Location from, Plot plot) {
        plot = plot.getBasePlot(false);
        final boolean result = EventUtil.manager.callTeleport(player, from, plot);
        
        if (result) {
            final Location location;
            if (PS.get().getPlotWorld(plot.world).HOME_ALLOW_NONMEMBER || plot.isAdded(player.getUUID())) {
                location = MainUtil.getPlotHome(plot);
            } else {
                location = getDefaultHome(plot);
            }
            if ((Settings.TELEPORT_DELAY == 0) || Permissions.hasPermission(player, "plots.teleport.delay.bypass")) {
                sendMessage(player, C.TELEPORTED_TO_PLOT);
                player.teleport(location);
                return true;
            }
            sendMessage(player, C.TELEPORT_IN_SECONDS, Settings.TELEPORT_DELAY + "");
            final String name = player.getName();
            TaskManager.TELEPORT_QUEUE.add(name);
            TaskManager.runTaskLater(new Runnable() {
                @Override
                public void run() {
                    if (!TaskManager.TELEPORT_QUEUE.contains(name)) {
                        sendMessage(player, C.TELEPORT_FAILED);
                        return;
                    }
                    TaskManager.TELEPORT_QUEUE.remove(name);
                    if (!player.isOnline()) {
                        return;
                    }
                    sendMessage(player, C.TELEPORTED_TO_PLOT);
                    player.teleport(location);
                }
            }, Settings.TELEPORT_DELAY * 20);
            return true;
        }
        return result;
    }
    
    public static int getBorder(final String worldname) {
        if (worldBorder.containsKey(worldname)) {
            final int border = worldBorder.get(worldname) + 16;
            if (border == 0) {
                return Integer.MAX_VALUE;
            } else {
                return border;
            }
        }
        return Integer.MAX_VALUE;
    }
    
    public static void setupBorder(final String world) {
        final PlotWorld plotworld = PS.get().getPlotWorld(world);
        if (!plotworld.WORLD_BORDER) {
            return;
        }
        if (!worldBorder.containsKey(world)) {
            worldBorder.put(world, 0);
        }
        for (final Plot plot : PS.get().getPlotsInWorld(world)) {
            updateWorldBorder(plot);
        }
    }
    
    public static void update(final String world, final ChunkLoc loc) {
        BlockUpdateUtil.setBlockManager.update(world, Arrays.asList(loc));
    }
    
    public static void update(final Plot plot) {
        TaskManager.runTaskAsync(new Runnable() {
            @Override
            public void run() {
                final HashSet<ChunkLoc> chunks = new HashSet<>();
                for (RegionWrapper region : getRegions(plot)) {
                    for (int x = region.minX >> 4; x <= region.maxX >> 4; x++) {
                        for (int z = region.minZ >> 4; z <= region.maxZ >> 4; z++) {
                            chunks.add(new ChunkLoc(x, z));
                        }
                    }
                }
                BlockUpdateUtil.setBlockManager.update(plot.world, chunks);
            }
        });
    }
    
    public static void createWorld(final String world, final String generator) {}
    
    public static PlotId parseId(final String arg) {
        try {
            final String[] split = arg.split(";");
            return new PlotId(Integer.parseInt(split[0]), Integer.parseInt(split[1]));
        } catch (final Exception e) {
            return null;
        }
    }
    
    /**
     * direction 0 = north, 1 = south, etc:
     *
     * @param id
     * @param direction
     *
     * @return PlotId relative
     */
    public static PlotId getPlotIdRelative(final PlotId id, final int direction) {
        switch (direction) {
            case 0:
                return new PlotId(id.x, id.y - 1);
            case 1:
                return new PlotId(id.x + 1, id.y);
            case 2:
                return new PlotId(id.x, id.y + 1);
            case 3:
                return new PlotId(id.x - 1, id.y);
        }
        return id;
    }
    
    /**
     * Get a list of plot ids within a selection
     * @param pos1
     * @param pos2
     * @return
     */
    public static ArrayList<PlotId> getPlotSelectionIds(final PlotId pos1, final PlotId pos2) {
        final ArrayList<PlotId> myplots = new ArrayList<>();
        for (int x = pos1.x; x <= pos2.x; x++) {
            for (int y = pos1.y; y <= pos2.y; y++) {
                myplots.add(new PlotId(x, y));
            }
        }
        return myplots;
    }
    
    /**
     * Get a set of owned plots within a selection (chooses the best algorithm based on selection size.<br>
     * i.e. A selection of billions of plots will work fine
     * @param pos1
     * @param pos2
     * @return
     */
    public static HashSet<Plot> getPlotSelectionOwned(final String world, final PlotId pos1, final PlotId pos2) {
        final int size = ((1 + pos2.x) - pos1.x) * ((1 + pos2.y) - pos1.y);
        final HashSet<Plot> result = new HashSet<>();
        if (PS.get().isPlotWorld(world)) {
            if (size < 16 || size < PS.get().getAllPlotsRaw().get(world).size()) {
                for (final PlotId pid : MainUtil.getPlotSelectionIds(pos1, pos2)) {
                    final Plot plot = MainUtil.getPlotAbs(world, pid);
                    if (plot.hasOwner()) {
                        if ((plot.id.x > pos1.x) || (plot.id.y > pos1.y) || (plot.id.x < pos2.x) || (plot.id.y < pos2.y)) {
                            result.add(plot);
                        }
                    }
                }
            } else {
                for (final Plot plot : PS.get().getPlotsInWorld(world)) {
                    if ((plot.id.x > pos1.x) || (plot.id.y > pos1.y) || (plot.id.x < pos2.x) || (plot.id.y < pos2.y)) {
                        result.add(plot);
                    }
                }
            }
        }
        return result;
    }
    
    /**
     * Completely merges a set of plots<br> <b>(There are no checks to make sure you supply the correct
     * arguments)</b><br> - Misuse of this method can result in unusable plots<br> - the set of plots must belong to one
     * owner and be rectangular<br> - the plot array must be sorted in ascending order<br> - Road will be removed where
     * required<br> - changes will be saved to DB<br>
     *
     * @param world
     * @param plotIds
     *
     * @return boolean (success)
     */
    public static boolean mergePlots(final String world, final ArrayList<PlotId> plotIds, final boolean removeRoads, final boolean updateDatabase) {
        if (plotIds.size() < 2) {
            return false;
        }
        final PlotId pos1 = plotIds.get(0);
        final PlotId pos2 = plotIds.get(plotIds.size() - 1);
        final PlotManager manager = PS.get().getPlotManager(world);
        final PlotWorld plotworld = PS.get().getPlotWorld(world);
        
        final boolean result = EventUtil.manager.callMerge(world, getPlotAbs(world, pos1), plotIds);
        if (!result) {
            return false;
        }
        
        final HashSet<UUID> trusted = new HashSet<UUID>();
        final HashSet<UUID> members = new HashSet<UUID>();
        final HashSet<UUID> denied = new HashSet<UUID>();
        
        manager.startPlotMerge(plotworld, plotIds);
        for (int x = pos1.x; x <= pos2.x; x++) {
            for (int y = pos1.y; y <= pos2.y; y++) {
                final PlotId id = new PlotId(x, y);
                final Plot plot = PS.get().getPlot(world, id);
                trusted.addAll(plot.getTrusted());
                members.addAll(plot.getMembers());
                denied.addAll(plot.getDenied());
                if (removeRoads) {
                    removeSign(plot);
                }
            }
        }
        members.removeAll(trusted);
        denied.removeAll(trusted);
        denied.removeAll(members);
        for (int x = pos1.x; x <= pos2.x; x++) {
            for (int y = pos1.y; y <= pos2.y; y++) {
                final boolean lx = x < pos2.x;
                final boolean ly = y < pos2.y;
                final PlotId id = new PlotId(x, y);
                final Plot plot = PS.get().getPlot(world, id);
                plot.setTrusted(trusted);
                plot.setMembers(members);
                plot.setDenied(denied);
                Plot plot2 = null;
                if (lx) {
                    if (ly) {
                        if (!plot.getMerged(1) || !plot.getMerged(2)) {
                            if (removeRoads) {
                                MainUtil.removeRoadSouthEast(plotworld, plot);
                            }
                        }
                    }
                    if (!plot.getMerged(1)) {
                        plot2 = PS.get().getPlot(world, new PlotId(x + 1, y));
                        mergePlot(world, plot, plot2, removeRoads);
                    }
                }
                if (ly) {
                    if (!plot.getMerged(2)) {
                        plot2 = PS.get().getPlot(world, new PlotId(x, y + 1));
                        mergePlot(world, plot, plot2, removeRoads);
                    }
                }
            }
        }
        manager.finishPlotMerge(plotworld, plotIds);
        return true;
    }
    
    public static void removeRoadSouthEast(final PlotWorld plotworld, final Plot plot) {
        if ((plotworld.TYPE != 0) && (plotworld.TERRAIN > 1)) {
            if (plotworld.TERRAIN == 3) {
                return;
            }
            final PlotId id = plot.id;
            final PlotId id2 = new PlotId(id.x + 1, id.y + 1);
            final Location pos1 = getPlotTopLocAbs(plot.world, id).add(1, 0, 1);
            final Location pos2 = getPlotBottomLocAbs(plot.world, id2).subtract(1, 0, 1);
            pos1.setY(0);
            pos2.setY(256);
            ChunkManager.manager.regenerateRegion(pos1, pos2, null);
        } else {
            PS.get().getPlotManager(plot.world).removeRoadSouthEast(plotworld, plot);
        }
    }
    
    public static void removeRoadEast(final PlotWorld plotworld, final Plot plot) {
        if ((plotworld.TYPE != 0) && (plotworld.TERRAIN > 1)) {
            if (plotworld.TERRAIN == 3) {
                return;
            }
            final PlotId id = plot.id;
            final PlotId id2 = new PlotId(id.x + 1, id.y);
            final Location bot = getPlotBottomLocAbs(plot.world, id2);
            final Location top = getPlotTopLocAbs(plot.world, id);
            final Location pos1 = new Location(plot.world, top.getX(), 0, bot.getZ());
            final Location pos2 = new Location(plot.world, bot.getX(), 256, top.getZ());
            ChunkManager.manager.regenerateRegion(pos1, pos2, null);
        } else {
            PS.get().getPlotManager(plot.world).removeRoadEast(plotworld, plot);
        }
    }
    
    public static void removeRoadSouth(final PlotWorld plotworld, final Plot plot) {
        if ((plotworld.TYPE != 0) && (plotworld.TERRAIN > 1)) {
            if (plotworld.TERRAIN == 3) {
                return;
            }
            final PlotId id = plot.id;
            final PlotId id2 = new PlotId(id.x, id.y + 1);
            final Location bot = getPlotBottomLocAbs(plot.world, id2);
            final Location top = getPlotTopLocAbs(plot.world, id);
            final Location pos1 = new Location(plot.world, bot.getX(), 0, top.getZ());
            final Location pos2 = new Location(plot.world, top.getX(), 256, bot.getZ());
            ChunkManager.manager.regenerateRegion(pos1, pos2, null);
        } else {
            PS.get().getPlotManager(plot.world).removeRoadSouth(plotworld, plot);
        }
    }
    
    /**
     * Merges 2 plots Removes the road inbetween <br>- Assumes plots are directly next to each other <br> - saves to DB
     *
     * @param world
     * @param lesserPlot
     * @param greaterPlot
     */
    public static void mergePlot(final String world, Plot lesserPlot, Plot greaterPlot, final boolean removeRoads) {
        final PlotWorld plotworld = PS.get().getPlotWorld(world);
        if (lesserPlot.id.x.equals(greaterPlot.id.x)) {
            if (lesserPlot.id.y > greaterPlot.id.y) {
                Plot tmp = lesserPlot;
                lesserPlot = greaterPlot;
                greaterPlot = tmp;
            }
            if (!lesserPlot.getMerged(2)) {
                lesserPlot.setMerged(2, true);
                greaterPlot.setMerged(0, true);
                mergeData(lesserPlot, greaterPlot);
                if (removeRoads) {
                    if (lesserPlot.getMerged(5)) {
                        removeRoadSouthEast(plotworld, lesserPlot);
                    }
                    MainUtil.removeRoadSouth(plotworld, lesserPlot);
                    Plot other = getPlotAbs(world, getPlotIdRelative(lesserPlot.id, 3));
                    if (other.getMerged(2) && other.getMerged(1)) {
                        MainUtil.removeRoadSouthEast(plotworld, other);
                        mergePlot(world, greaterPlot, getPlotAbs(world, getPlotIdRelative(greaterPlot.id, 3)), removeRoads);
                    }
                }
            }
        } else {
            if (lesserPlot.id.x > greaterPlot.id.x) {
                Plot tmp = lesserPlot;
                lesserPlot = greaterPlot;
                greaterPlot = tmp;
            }
            if (!lesserPlot.getMerged(1)) {
                lesserPlot.setMerged(1, true);
                greaterPlot.setMerged(3, true);
                mergeData(lesserPlot, greaterPlot);
                if (removeRoads) {
                    MainUtil.removeRoadEast(plotworld, lesserPlot);
                    if (lesserPlot.getMerged(5)) {
                        removeRoadSouthEast(plotworld, lesserPlot);
                    }
                    Plot other = getPlotAbs(world, getPlotIdRelative(lesserPlot.id, 0));
                    if (other.getMerged(2) && other.getMerged(1)) {
                        MainUtil.removeRoadSouthEast(plotworld, other);
                        mergePlot(world, greaterPlot, getPlotAbs(world, getPlotIdRelative(greaterPlot.id, 0)), removeRoads);
                    }
                }
            }
        }
    }
    
    public static void mergeData(Plot a, Plot b) {
        HashMap<String, Flag> flags1 = a.getFlags();
        HashMap<String, Flag> flags2 = b.getFlags();
        if ((flags1.size() != 0 || flags2.size() != 0) && !flags1.equals(flags2)) {
            boolean greater = flags1.size() > flags2.size();
            if (greater) {
                flags1.putAll(flags2);
            }
            else {
                flags2.putAll(flags1);
            }
            HashSet<Flag> net = new HashSet<>((greater ? flags1 : flags2).values());
            a.setFlags(net);
            b.setFlags(net);
        }
        for (UUID uuid : a.getTrusted()) {
            b.addTrusted(uuid);
        }
        for (UUID uuid : b.getTrusted()) {
            a.addTrusted(uuid);
        }
        
        for (UUID uuid : a.getMembers()) {
            b.addMember(uuid);
        }
        for (UUID uuid : b.getMembers()) {
            a.addMember(uuid);
        }
        
        for (UUID uuid : a.getDenied()) {
            b.addDenied(uuid);
        }
        for (UUID uuid : b.getDenied()) {
            a.addDenied(uuid);
        }
    }
    
    public static void removeSign(final Plot p) {
        if (!PS.get().isMainThread(Thread.currentThread())) {
            TaskManager.runTask(new Runnable() {
                @Override
                public void run() {
                    removeSign(p);
                }
            });
            return;
        }
        final String world = p.world;
        final PlotManager manager = PS.get().getPlotManager(world);
        final PlotWorld plotworld = PS.get().getPlotWorld(world);
        if (!plotworld.ALLOW_SIGNS) {
            return;
        }
        final Location loc = manager.getSignLoc(plotworld, p);
        BlockManager.setBlocks(world, new int[] { loc.getX() }, new int[] { loc.getY() }, new int[] { loc.getZ() }, new int[] { 0 }, new byte[] { 0 });
    }
    
    public static void setSign(final Plot p) {
        if (p.owner == null) {
            setSign(null, p);
            return;
        }
        setSign(UUIDHandler.getName(p.owner), p);
    }
    
    public static void setSign(final String name, final Plot p) {
        if (!PS.get().isMainThread(Thread.currentThread())) {
            TaskManager.runTask(new Runnable() {
                @Override
                public void run() {
                    setSign(name, p);
                }
            });
            return;
        }
        final String rename = name == null ? "unknown" : name;
        final PlotManager manager = PS.get().getPlotManager(p.world);
        final PlotWorld plotworld = PS.get().getPlotWorld(p.world);
        if (plotworld.ALLOW_SIGNS) {
            final Location loc = manager.getSignLoc(plotworld, p);
            final String id = p.id.x + ";" + p.id.y;
            final String[] lines = new String[] {
            C.OWNER_SIGN_LINE_1.formatted().replaceAll("%id%", id),
            C.OWNER_SIGN_LINE_2.formatted().replaceAll("%id%", id).replaceAll("%plr%", rename),
            C.OWNER_SIGN_LINE_3.formatted().replaceAll("%id%", id).replaceAll("%plr%", rename),
            C.OWNER_SIGN_LINE_4.formatted().replaceAll("%id%", id).replaceAll("%plr%", rename) };
            BlockManager.setSign(p.world, loc.getX(), loc.getY(), loc.getZ(), lines);
        }
    }
    
    public static Location[] getCorners(String world, RegionWrapper region) {
        Location pos1 = new Location(world, region.minX, region.minY, region.minZ);
        Location pos2 = new Location(world, region.maxX, region.maxY, region.maxZ);
        return new Location[] { pos1, pos2 };
    }
    
    /**
     * Returns the top and bottom connected plot.<br>
     *  - If the plot is not connected, it will return itself for the top/bottom<br>
     *  - the returned IDs will not necessarily correspond to claimed plots if the connected plots do not form a rectangular shape 
     * @param plot
     * @return new PlotId[] { bottom, top }
     */
    public static Location[] getCorners(Plot plot) {
        if (!plot.isMerged()) {
            return new Location[] { plot.getBottomAbs(), plot.getTopAbs() };
        }
        return getCorners(plot.world, getRegions(plot));
    }
    
    public static Location[] getCorners(String world, Collection<RegionWrapper> regions) {
        Location min = null;
        Location max = null;
        for (RegionWrapper region : regions) {
            Location[] corners = getCorners(world, region);
            if (min == null) {
                min = corners[0];
                max = corners[1];
                continue;
            }
            Location pos1 = corners[0];
            Location pos2 = corners[1];
            if (pos2.getX() > max.getX()) {
                max.setX(pos2.getX());
            }
            if (pos1.getX() < min.getX()) {
                min.setX(pos1.getX());
            }
            if (pos2.getZ() > max.getZ()) {
                max.setZ(pos2.getZ());
            }
            if (pos1.getZ() < min.getZ()) {
                min.setZ(pos1.getZ());
            }
        }
        return new Location[] { min, max };
    }
    
    public static PlotId[] getCornerIds(Plot plot) {
        if (!plot.isMerged()) {
            return new PlotId[] { plot.id, plot.id };
        }
        PlotId min = new PlotId(plot.id.x, plot.id.y);
        PlotId max = new PlotId(plot.id.x, plot.id.y);
        for (Plot current : getConnectedPlots(plot)) {
            if (current.id.x < min.x) {
                min.x = current.id.x;
            }
            else if (current.id.x > max.x) {
                max.x = current.id.x;
            }
            if (current.id.y < min.y) {
                min.y = current.id.y;
            }
            else if (current.id.y > max.y) {
                max.y = current.id.y;
            }
        }
        return new PlotId[] { min, max };
    }
    
    public static boolean autoMerge(final Plot plot, int dir, int max, final UUID uuid, final boolean removeRoads) {
        if (plot == null) {
            return false;
        }
        if (plot.owner == null) {
            return false;
        }
        HashSet<Plot> visited = new HashSet<>();
        HashSet<PlotId> merged = new HashSet<>();
        ArrayDeque<Plot> frontier = new ArrayDeque<>(getConnectedPlots(plot));
        Plot current;
        boolean toReturn = false;
        while ((current = frontier.poll()) != null && max > 0) {
            if (visited.contains(current)) {
                continue;
            }
            visited.add(current);
            if (max > 0 && (dir == -1 || dir == 0) && !current.getMerged(0)) {
                Plot other = getPlotAbs(current.world, getPlotIdRelative(current.id, 0));
                if (other.isOwner(uuid)) {
                    frontier.addAll(other.getConnectedPlots());
                    mergePlot(current.world, current, other, removeRoads);
                    merged.add(current.id);
                    merged.add(other.id);
                    toReturn = true;
                    max--;
                }
            }
            if (max > 0 && (dir == -1 || dir == 1) && !current.getMerged(1)) {
                Plot other = getPlotAbs(current.world, getPlotIdRelative(current.id, 1));
                if (other.isOwner(uuid)) {
                    frontier.addAll(other.getConnectedPlots());
                    mergePlot(current.world, current, other, removeRoads);
                    merged.add(current.id);
                    merged.add(other.id);
                    toReturn = true;
                    max--;
                }
            }
            if (max > 0 && (dir == -1 || dir == 2) && !current.getMerged(2)) {
                Plot other = getPlotAbs(current.world, getPlotIdRelative(current.id, 2));
                if (other.isOwner(uuid)) {
                    frontier.addAll(other.getConnectedPlots());
                    mergePlot(current.world, current, other, removeRoads);
                    merged.add(current.id);
                    merged.add(other.id);
                    toReturn = true;
                    max--;
                }
            }
            if (max > 0 && (dir == -1 || dir == 3) && !current.getMerged(3)) {
                Plot other = getPlotAbs(current.world, getPlotIdRelative(current.id, 3));
                if (other.isOwner(uuid)) {
                    frontier.addAll(other.getConnectedPlots());
                    mergePlot(current.world, current, other, removeRoads);
                    merged.add(current.id);
                    merged.add(other.id);
                    toReturn = true;
                    max--;
                }
            }
        }
        PlotManager manager = PS.get().getPlotManager(plot.world);
        ArrayList<PlotId> ids = new ArrayList<>(merged);
        if (removeRoads) {
            manager.finishPlotMerge(plot.getWorld(), ids);
        }
        return toReturn;
    }
    
    public static void updateWorldBorder(final Plot plot) {
        if (!worldBorder.containsKey(plot.world)) {
            return;
        }
        final String world = plot.world;
        final PlotManager manager = PS.get().getPlotManager(world);
        final PlotWorld plotworld = PS.get().getPlotWorld(world);
        final PlotId id = new PlotId(Math.abs(plot.id.x) + 1, Math.abs(plot.id.x) + 1);
        final Location bot = manager.getPlotBottomLocAbs(plotworld, id);
        final Location top = manager.getPlotTopLocAbs(plotworld, id);
        final int border = worldBorder.get(plot.world);
        final int botmax = Math.max(Math.abs(bot.getX()), Math.abs(bot.getZ()));
        final int topmax = Math.max(Math.abs(top.getX()), Math.abs(top.getZ()));
        final int max = Math.max(botmax, topmax);
        if (max > border) {
            worldBorder.put(plot.world, max);
        }
    }
    
    /**
     * Create a plot and notify the world border and plot merger
     */
    public static boolean createPlot(final UUID uuid, final Plot plot) {
        if (MainUtil.worldBorder.containsKey(plot.world)) {
            updateWorldBorder(plot);
        }
        final String w = plot.world;
        if (PS.get().getPlot(plot.world, plot.id) != null) {
            return true;
        }
        final Plot p = new Plot(w, plot.id, uuid);
        if (p.owner == null) {
            return false;
        }
        PS.get().updatePlot(p);
        DBFunc.createPlotAndSettings(p, new Runnable() {
            @Override
            public void run() {
                final PlotWorld plotworld = PS.get().getPlotWorld(plot.world);
                if (plotworld.AUTO_MERGE) {
                    autoMerge(p, -1, Integer.MAX_VALUE, uuid, true);
                }
            }
        });
        return true;
    }
    
    /**
     * Create a plot without notifying the merge function or world border manager
     */
    public static Plot createPlotAbs(final UUID uuid, final Plot plot) {
        final String w = plot.world;
        Plot p = PS.get().getPlot(plot.world, plot.id);
        if (p != null) {
            return p;
        }
        p = new Plot(w, plot.id, uuid);
        if (p.owner == null) {
            return null;
        }
        PS.get().updatePlot(p);
        DBFunc.createPlotAndSettings(p, null);
        return p;
    }
    
    public static String createId(final int x, final int z) {
        return x + ";" + z;
    }
    
    public static int square(final int x) {
        return x * x;
    }
    
    public static short[] getBlock(final String block) {
        if (block.contains(":")) {
            final String[] split = block.split(":");
            return new short[] { Short.parseShort(split[0]), Short.parseShort(split[1]) };
        }
        return new short[] { Short.parseShort(block), 0 };
    }
    
    /**
     * Clear a plot and associated sections: [sign, entities, border]
     *
     * @param plot
     * @param isDelete
     * @param whenDone
     */
    public static boolean clearAsPlayer(final Plot plot, final boolean isDelete, final Runnable whenDone) {
        if (plot.getRunning() != 0) {
            return false;
        }
        clear(plot, isDelete, whenDone);
        return true;
    }
    
    public static int[] countEntities(Plot plot) {
        int[] count = new int[5];
        for (Plot current : getConnectedPlots(plot)) {
            int[] result = ChunkManager.manager.countEntities(current);
            count[0] += result[0];
            count[1] += result[1];
            count[2] += result[2];
            count[3] += result[3];
            count[4] += result[4];
        }
        return count;
    }
    
    public static boolean delete(final Plot plot, final Runnable whenDone) {
        // Plot is not claimed
        if (!plot.hasOwner()) {
            return false;
        }
        final HashSet<Plot> plots = getConnectedPlots(plot);
        clear(plot, true, new Runnable() {
            @Override
            public void run() {
                for (Plot current : plots) {
                    current.unclaim();
                }
                TaskManager.runTask(whenDone);
            }
        });
        return true;
    }
    
    public static boolean clear(final Plot plot, final boolean isDelete, final Runnable whenDone) {
        if (!EventUtil.manager.callClear(plot.world, plot.id)) {
            return false;
        }
        final HashSet<Plot> plots = getConnectedPlots(plot);
        final ArrayDeque<Plot> queue = new ArrayDeque<>(plots);
        removeSign(plot);
        MainUtil.unlinkPlot(plot, true, !isDelete);
        final PlotManager manager = PS.get().getPlotManager(plot.world);
        final PlotWorld plotworld = PS.get().getPlotWorld(plot.world);
        Runnable run = new Runnable() {
            @Override
            public void run() {
                if (queue.size() == 0) {
                    final AtomicInteger finished = new AtomicInteger(0);
                    final Runnable run = new Runnable() {
                        @Override
                        public void run() {
                            if (finished.incrementAndGet() >= plots.size()) {
                                TaskManager.runTask(whenDone);
                            }
                        }
                    };
                    if (isDelete) {
                        for (Plot current : plots) {
                            manager.unclaimPlot(plotworld, current, run);
                        }
                    }
                    else {
                        for (Plot current : plots) {
                            manager.claimPlot(plotworld, current);
                            SetBlockQueue.addNotify(run);
                        }
                    }
                    return;
                }
                Plot current = queue.poll();
                if ((plotworld.TERRAIN != 0) || Settings.FAST_CLEAR) {
                    ChunkManager.manager.regenerateRegion(current.getBottomAbs(), current.getTopAbs(), this);
                    return;
                }
                manager.clearPlot(plotworld, current, this);
            }
        };
        run.run();
        return true;
    }
    
    public static void setCuboid(final String world, final Location pos1, final Location pos2, final PlotBlock[] blocks) {
        if (blocks.length == 1) {
            setSimpleCuboid(world, pos1, pos2, blocks[0]);
            return;
        }
        final int length = (pos2.getX() - pos1.getX()) * (pos2.getY() - pos1.getY()) * (pos2.getZ() - pos1.getZ());
        final int[] xl = new int[length];
        final int[] yl = new int[length];
        final int[] zl = new int[length];
        final int[] ids = new int[length];
        final byte[] data = new byte[length];
        int index = 0;
        for (int y = pos1.getY(); y <= pos2.getY(); y++) {
            for (int x = pos1.getX(); x <= pos2.getX(); x++) {
                for (int z = pos1.getZ(); z <= pos2.getZ(); z++) {
                    final int i = random.random(blocks.length);
                    xl[index] = x;
                    yl[index] = y;
                    zl[index] = z;
                    final PlotBlock block = blocks[i];
                    ids[index] = block.id;
                    data[index] = block.data;
                    index++;
                }
            }
        }
        BlockManager.setBlocks(world, xl, yl, zl, ids, data);
    }
    
    public static void setCuboidAsync(final String world, final Location pos1, final Location pos2, final PlotBlock[] blocks) {
        if (blocks.length == 1) {
            setSimpleCuboidAsync(world, pos1, pos2, blocks[0]);
            return;
        }
        for (int y = pos1.getY(); y <= Math.min(255, pos2.getY()); y++) {
            for (int x = pos1.getX(); x <= pos2.getX(); x++) {
                for (int z = pos1.getZ(); z <= pos2.getZ(); z++) {
                    final int i = random.random(blocks.length);
                    final PlotBlock block = blocks[i];
                    SetBlockQueue.setBlock(world, x, y, z, block);
                }
            }
        }
    }
    
    public static void setSimpleCuboid(final String world, final Location pos1, final Location pos2, final PlotBlock newblock) {
        final int length = (pos2.getX() - pos1.getX()) * (pos2.getY() - pos1.getY()) * (pos2.getZ() - pos1.getZ());
        final int[] xl = new int[length];
        final int[] yl = new int[length];
        final int[] zl = new int[length];
        final int[] ids = new int[length];
        final byte[] data = new byte[length];
        int index = 0;
        for (int y = pos1.getY(); y <= Math.min(255, pos2.getY()); y++) {
            for (int x = pos1.getX(); x <= pos2.getX(); x++) {
                for (int z = pos1.getZ(); z <= pos2.getZ(); z++) {
                    xl[index] = x;
                    yl[index] = y;
                    zl[index] = z;
                    ids[index] = newblock.id;
                    data[index] = newblock.data;
                    index++;
                }
            }
        }
        BlockManager.setBlocks(world, xl, yl, zl, ids, data);
    }
    
    public static void setSimpleCuboidAsync(final String world, final Location pos1, final Location pos2, final PlotBlock newblock) {
        for (int y = pos1.getY(); y <= Math.min(255, pos2.getY()); y++) {
            for (int x = pos1.getX(); x <= pos2.getX(); x++) {
                for (int z = pos1.getZ(); z <= pos2.getZ(); z++) {
                    SetBlockQueue.setBlock(world, x, y, z, newblock);
                }
            }
        }
    }
    
    public static void setBiome(final Plot plot, final String biome, final Runnable whenDone) {
        final ArrayDeque<RegionWrapper> regions = new ArrayDeque<>(getRegions(plot));
        Runnable run = new Runnable() {
            @Override
            public void run() {
                if (regions.size() == 0) {
                    update(plot);
                    TaskManager.runTask(whenDone);
                    return;
                }
                RegionWrapper region = regions.poll();
                Location pos1 = new Location(plot.world, region.minX, region.minY, region.minZ);
                Location pos2 = new Location(plot.world, region.maxX, region.maxY, region.maxZ);
                ChunkManager.chunkTask(pos1, pos2, new RunnableVal<int[]>() {
                    @Override
                    public void run() {
                        final ChunkLoc loc = new ChunkLoc(value[0], value[1]);
                        ChunkManager.manager.loadChunk(plot.world, loc, false);
                        setBiome(plot.world, value[2], value[3], value[4], value[5], biome);
                        ChunkManager.manager.unloadChunk(plot.world, loc, true, true);
                    }
                }, this, 5);
                
            }
        };
        run.run();
    }
    
    public static void setBiome(final String world, final int p1x, final int p1z, final int p2x, final int p2z, final String biome) {
        final int length = ((p2x - p1x) + 1) * ((p2z - p1z) + 1);
        final int[] xl = new int[length];
        final int[] zl = new int[length];
        int index = 0;
        for (int x = p1x; x <= p2x; x++) {
            for (int z = p1z; z <= p2z; z++) {
                xl[index] = x;
                zl[index] = z;
                index++;
            }
        }
        BlockManager.setBiomes(world, xl, zl, biome);
    }
    
    public static int getHeighestBlock(final String world, final int x, final int z) {
        final int result = BlockManager.manager.getHeighestBlock(world, x, z);
        if (result == 0) {
            return 64;
        }
        return result;
    }
    
    /**
     * Get plot home
     *
     * @param w      World in which the plot is located
     * @param plotid Plot ID
     *
     * @return Home Location
     */
    public static Location getPlotHome(final String w, final PlotId plotid) {
        final Plot plot = getPlotAbs(w, plotid).getBasePlot(false);
        final BlockLoc home = plot.getPosition();
        PS.get().getPlotManager(w);
        if ((home == null) || ((home.x == 0) && (home.z == 0))) {
            return getDefaultHome(plot);
        } else {
            Location bot = plot.getBottomAbs();
            final Location loc = new Location(bot.getWorld(), bot.getX() + home.x, bot.getY() + home.y, bot.getZ() + home.z);
            if (BlockManager.manager.getBlock(loc).id != 0) {
                loc.setY(Math.max(getHeighestBlock(w, loc.getX(), loc.getZ()), bot.getY()));
            }
            return loc;
        }
    }
    
    /**
     * Get the plot home
     *
     * @param plot Plot Object
     *
     * @return Plot Home Location
     *
     */
    public static Location getPlotHome(final Plot plot) {
        return getPlotHome(plot.world, plot.id);
    }
    
    /**
     * Gets the top plot location of a plot (all plots are treated as small plots) - To get the top loc of a mega plot
     * use getPlotTopLoc(...)
     *
     * @param world
     * @param id
     *
     * @return Location top
     */
    public static Location getPlotTopLocAbs(final String world, final PlotId id) {
        final PlotWorld plotworld = PS.get().getPlotWorld(world);
        final PlotManager manager = PS.get().getPlotManager(world);
        return manager.getPlotTopLocAbs(plotworld, id);
    }
    
    /**
     * Gets the bottom plot location of a plot (all plots are treated as small plots) - To get the top loc of a mega
     * plot use getPlotBottomLoc(...)
     *
     * @param world
     * @param id
     *
     * @return Location bottom
     */
    public static Location getPlotBottomLocAbs(final String world, final PlotId id) {
        final PlotWorld plotworld = PS.get().getPlotWorld(world);
        final PlotManager manager = PS.get().getPlotManager(world);
        return manager.getPlotBottomLocAbs(plotworld, id);
    }
    
    /**
     * Gets the top loc of a plot (if mega, returns top loc of that mega plot) - If you would like each plot treated as
     * a small plot use getPlotTopLocAbs(...)
     *
     * @param world
     * @param id
     *
     * @return Location top of mega plot
     */
    public static Location getPlotTopLoc_(Plot plot) {
        Location top = getPlotTopLocAbs(plot.world, plot.id);
        if (!plot.isMerged()) {
            return top;
        }
        PlotId id;
        if (plot.getMerged(2)) {
            id = getPlotIdRelative(plot.id, 2);
            top.setZ(getPlotBottomLocAbs(plot.world, id).getZ() - 1);
        }
        if (plot.getMerged(1)) {
            id = getPlotIdRelative(plot.id, 1);
            top.setX(getPlotBottomLocAbs(plot.world, id).getX() - 1);
        }
        return top;
    }
    
    public static Location getPlotBottomLoc_(Plot plot) {
        Location bot = getPlotBottomLocAbs(plot.world, plot.id);
        if (!plot.isMerged()) {
            return bot;
        }
        PlotId id;
        if (plot.getMerged(0)) {
            id = getPlotIdRelative(plot.id, 0);
            bot.setZ(getPlotTopLocAbs(plot.world, id).getZ() + 1);
        }
        if (plot.getMerged(3)) {
            id = getPlotIdRelative(plot.id, 3);
            bot.setX(getPlotTopLocAbs(plot.world, id).getX() + 1);
        }
        return bot;
    }
    
    public static boolean canClaim(final PlotPlayer player, final String world, final PlotId pos1, final PlotId pos2) {
        for (int x = pos1.x; x <= pos2.x; x++) {
            for (int y = pos1.y; y <= pos2.y; y++) {
                final PlotId id = new PlotId(x, y);
                final Plot plot = getPlotAbs(world, id);
                if (!canClaim(player, plot)) {
                    return false;
                }
            }
        }
        return true;
    }
    
    public static boolean canClaim(final PlotPlayer player, final Plot plot) {
        if (plot == null) {
            return false;
        }
        if (Settings.ENABLE_CLUSTERS) {
            final PlotCluster cluster = plot.getCluster();
            if (cluster != null) {
                if (!cluster.isAdded(player.getUUID()) && !Permissions.hasPermission(player, "plots.admin.command.claim")) {
                    return false;
                }
            }
        }
        return plot.owner == null;
    }
    
    public static boolean isUnowned(final String world, final PlotId pos1, final PlotId pos2) {
        for (int x = pos1.x; x <= pos2.x; x++) {
            for (int y = pos1.y; y <= pos2.y; y++) {
                final PlotId id = new PlotId(x, y);
                if (PS.get().getPlot(world, id) != null) {
                    if (PS.get().getPlot(world, id).owner != null) {
                        return false;
                    }
                }
            }
        }
        return true;
    }
    
    public static boolean swapData(Plot p1, Plot p2, final Runnable whenDone) {
        if ((p1 == null) || (p1.owner == null)) {
            if ((p2 != null) && (p2.owner != null)) {
                moveData(p2, p1, whenDone);
                return true;
            }
            return false;
        }
        if ((p2 == null) || (p2.owner == null)) {
            if ((p1 != null) && (p1.owner != null)) {
                moveData(p1, p2, whenDone);
                return true;
            }
            return false;
        }
        // Swap cached
        final PlotId temp = new PlotId(p1.id.x.intValue(), p1.id.y.intValue());
        p1.id.x = p2.id.x.intValue();
        p1.id.y = p2.id.y.intValue();
        p2.id.x = temp.x;
        p2.id.y = temp.y;
        final Map<String, ConcurrentHashMap<PlotId, Plot>> raw = PS.get().getAllPlotsRaw();
        raw.get(p1.world).remove(p1.id);
        raw.get(p2.world).remove(p2.id);
        p1.id.recalculateHash();
        p2.id.recalculateHash();
        raw.get(p1.world).put(p1.id, p1);
        raw.get(p2.world).put(p2.id, p2);
        // Swap database
        DBFunc.dbManager.swapPlots(p2, p1);
        TaskManager.runTaskLater(whenDone, 1);
        return true;
    }
    
    public static boolean moveData(final Plot pos1, final Plot pos2, final Runnable whenDone) {
        if (pos1.owner == null) {
            PS.debug(pos2 + " is unowned (single)");
            TaskManager.runTask(whenDone);
            return false;
        }
        if (pos2.hasOwner()) {
            PS.debug(pos2 + " is unowned (multi)");
            TaskManager.runTask(whenDone);
            return false;
        }
        final Map<String, ConcurrentHashMap<PlotId, Plot>> raw = PS.get().getAllPlotsRaw();
        raw.get(pos1.world).remove(pos1.id);
        pos1.id.x = (int) pos2.id.x;
        pos1.id.y = (int) pos2.id.y;
        pos1.id.recalculateHash();
        raw.get(pos2.world).put(pos1.id, pos1);
        DBFunc.movePlot(pos1, pos2);
        TaskManager.runTaskLater(whenDone, 1);
        return true;
    }

    public static boolean move(final Plot origin, final Plot destination, final Runnable whenDone, boolean allowSwap) {
        PlotId offset = new PlotId(destination.id.x - origin.id.x, destination.id.y - origin.id.y);
        Location db = destination.getBottomAbs();
        Location ob = origin.getBottomAbs();
        final int offsetX = db.getX() - ob.getX();
        final int offsetZ = db.getZ() - ob.getZ();
        if (origin.owner == null) {
            TaskManager.runTaskLater(whenDone, 1);
            return false;
        }
        boolean occupied = false;
        HashSet<Plot> plots = MainUtil.getConnectedPlots(origin);
        for (Plot plot : plots) {
            Plot other = MainUtil.getPlotAbs(destination.world, new PlotId(plot.id.x + offset.x, plot.id.y + offset.y));
            if (other.owner != null) {
                if (!allowSwap) {
                    TaskManager.runTaskLater(whenDone, 1);
                    return false;
                }
                occupied = true;
            }
        }
        final ArrayDeque<RegionWrapper> regions = new ArrayDeque<>(getRegions(origin));
        // move / swap data
        for (Plot plot : plots) {
            Plot other = MainUtil.getPlotAbs(destination.world, new PlotId(plot.id.x + offset.x, plot.id.y + offset.y));
            swapData(plot, other, null);
        }
        // copy terrain
        Runnable move = new Runnable() {
            @Override
            public void run() {
                if (regions.size() == 0) {
                    TaskManager.runTask(whenDone);
                    return;
                }
                final Runnable task = this;
                RegionWrapper region = regions.poll();
                Location[] corners = getCorners(origin.world, region);
                final Location pos1 = corners[0];
                final Location pos2 = corners[1];
                Location newPos = pos1.clone().add(offsetX, 0, offsetZ);
                newPos.setWorld(destination.world);
                ChunkManager.manager.copyRegion(pos1, pos2, newPos, new Runnable() {
                    @Override
                    public void run() {
                        ChunkManager.manager.regenerateRegion(pos1, pos2, task);
                    }
                });
            }
        };
        Runnable swap = new Runnable() {
            @Override
            public void run() {
                if (regions.size() == 0) {
                    TaskManager.runTask(whenDone);
                    return;
                }
                RegionWrapper region = regions.poll();
                Location[] corners = getCorners(origin.world, region);
                Location pos1 = corners[0];
                Location pos2 = corners[1];
                Location pos3 = pos1.clone().add(offsetX, 0, offsetZ);
                Location pos4 = pos2.clone().add(offsetX, 0, offsetZ);
                pos3.setWorld(destination.world);
                pos4.setWorld(destination.world);
                ChunkManager.manager.swap(pos1, pos2, pos3, pos4, this);
            }
        };
        if (occupied) {
            swap.run();
        }
        else {
            move.run();
        }
        return true;
    }
//        final com.intellectualcrafters.plot.object.Location bot1 = MainUtil.getPlotBottomLoc(plot1.world, plot1.id);
//        final com.intellectualcrafters.plot.object.Location bot2 = MainUtil.getPlotBottomLoc(plot2.world, plot2.id);
//        final Location top = MainUtil.getPlotTopLoc(plot1.world, plot1.id);
//        if (plot1.owner == null) {
//            PS.debug(plot2 + " is unowned (single)");
//            TaskManager.runTask(whenDone);
//            return false;
//        }
//        final Plot pos1 = getBottomPlot(plot1);
//        final Plot pos2 = getTopPlot(plot1);
//        final PlotId size = MainUtil.getSize(plot1);
//        if (!MainUtil.isUnowned(plot2.world, plot2.id, new PlotId((plot2.id.x + size.x) - 1, (plot2.id.y + size.y) - 1))) {
//            PS.debug(plot2 + " is unowned (multi)");
//            TaskManager.runTask(whenDone);
//            return false;
//        }
//        final int offset_x = plot2.id.x - pos1.id.x;
//        final int offset_y = plot2.id.y - pos1.id.y;
//        final ArrayList<PlotId> selection = getPlotSelectionIds(pos1.id, pos2.id);
//        for (final PlotId id : selection) {
//            final String worldOriginal = plot1.world;
//            final PlotId idOriginal = new PlotId(id.x, id.y);
//            final Plot plot = PS.get().getPlot(plot1.world, id);
//            final Map<String, ConcurrentHashMap<PlotId, Plot>> raw = PS.get().getAllPlotsRaw();
//            raw.get(plot1.world).remove(id);
//            plot.id.x += offset_x;
//            plot.id.y += offset_y;
//            plot.id.recalculateHash();
//            raw.get(plot2.world).put(plot.id, plot);
//            DBFunc.movePlot(getPlot(worldOriginal, idOriginal), getPlot(plot2.world, new PlotId(id.x + offset_x, id.y + offset_y)));
//        }
//        ChunkManager.manager.copyRegion(bot1, top, bot2, new Runnable() {
//            @Override
//            public void run() {
//                final Location bot = bot1.clone().add(1, 0, 1);
//                ChunkManager.manager.regenerateRegion(bot, top, null);
//                TaskManager.runTaskLater(whenDone, 1);
//            }
//        });
//        return true;
//    }
    
    public static boolean copy(final Plot origin, final Plot destination, final Runnable whenDone) {
        PlotId offset = new PlotId(destination.id.x - origin.id.x, destination.id.y - origin.id.y);
        Location db = destination.getBottomAbs();
        Location ob = origin.getBottomAbs();
        final int offsetX = db.getX() - ob.getX();
        final int offsetZ = db.getZ() - ob.getZ();
        if (origin.owner == null) {
            TaskManager.runTaskLater(whenDone, 1);
            return false;
        }
        HashSet<Plot> plots = MainUtil.getConnectedPlots(origin);
        for (Plot plot : plots) {
            Plot other = MainUtil.getPlotAbs(destination.world, new PlotId(plot.id.x + offset.x, plot.id.y + offset.y));
            if (other.owner != null) {
                TaskManager.runTaskLater(whenDone, 1);
                return false;
            }
        }
        // copy data
        for (Plot plot : plots) {
            Plot other = MainUtil.getPlotAbs(destination.world , new PlotId(plot.id.x + offset.x, plot.id.y + offset.y));
            other = createPlotAbs(plot.owner, other);
            if ((plot.getFlags() != null) && (plot.getFlags().size() > 0)) {
                other.getSettings().flags = plot.getFlags();
                DBFunc.setFlags(other, plot.getFlags().values());
            }
            if (plot.isMerged()) {
                other.setMerged(plot.getMerged());
            }
            if ((plot.members != null) && (plot.members.size() > 0)) {
                other.members = plot.members;
                for (final UUID member : other.members) {
                    DBFunc.setMember(other, member);
                }
            }
            if ((plot.trusted != null) && (plot.trusted.size() > 0)) {
                other.trusted = plot.trusted;
                for (final UUID trusted : other.trusted) {
                    DBFunc.setTrusted(other, trusted);
                }
            }
            if ((plot.denied != null) && (plot.denied.size() > 0)) {
                other.denied = plot.denied;
                for (final UUID denied : other.denied) {
                    DBFunc.setDenied(other, denied);
                }
            }
            PS.get().updatePlot(other);
        }
        // copy terrain
        final ArrayDeque<RegionWrapper> regions = new ArrayDeque<>(getRegions(origin));
        Runnable run = new Runnable() {
            @Override
            public void run() {
                if (regions.size() == 0) {
                    TaskManager.runTask(whenDone);
                    return;
                }
                RegionWrapper region = regions.poll();
                Location[] corners = getCorners(origin.world, region);
                Location pos1 = corners[0];
                Location pos2 = corners[1];
                Location newPos = pos1.clone().add(offsetX, 0, offsetZ);
                newPos.setWorld(destination.world);
                ChunkManager.manager.copyRegion(pos1, pos2, newPos, this);
            }
        };
        run.run();
        return true;
    }
    
    /**
     * Send a message to the player
     *
     * @param plr Player to recieve message
     * @param msg Message to send
     *
     * @return true Can be used in things such as commands (return PlayerFunctions.sendMessage(...))
     */
    public static boolean sendMessage(final PlotPlayer plr, final String msg) {
        return sendMessage(plr, msg, true);
    }
    
    public static void sendConsoleMessage(final String msg) {
        sendMessage(null, msg);
    }
    
    public static void sendConsoleMessage(final C caption, final String... args) {
        sendMessage(null, caption, args);
    }
    
    public static boolean sendMessage(final PlotPlayer plr, final String msg, final boolean prefix) {
        if ((msg.length() > 0) && !msg.equals("")) {
            if (plr == null) {
                PS.log((prefix ? C.PREFIX.s() : "") + msg);
            } else {
                plr.sendMessage((prefix ? C.PREFIX.s() : "") + C.color(msg));
            }
        }
        return true;
    }
    
    public static String[] wordWrap(final String rawString, final int lineLength) {
        if (rawString == null) {
            return new String[] { "" };
        }
        if ((rawString.length() <= lineLength) && (!rawString.contains("\n"))) {
            return new String[] { rawString };
        }
        final char[] rawChars = (rawString + ' ').toCharArray();
        StringBuilder word = new StringBuilder();
        StringBuilder line = new StringBuilder();
        final ArrayList<String> lines = new ArrayList();
        int lineColorChars = 0;
        for (int i = 0; i < rawChars.length; i++) {
            final char c = rawChars[i];
            if (c == '\u00A7') {
                word.append('\u00A7' + (rawChars[(i + 1)]));
                lineColorChars += 2;
                i++;
            } else if ((c == ' ') || (c == '\n')) {
                if ((line.length() == 0) && (word.length() > lineLength)) {
                    for (final String partialWord : word.toString().split("(?<=\\G.{" + lineLength + "})")) {
                        lines.add(partialWord);
                    }
                } else if (((line.length() + word.length()) - lineColorChars) == lineLength) {
                    line.append(word);
                    lines.add(line.toString());
                    line = new StringBuilder();
                    lineColorChars = 0;
                } else if (((line.length() + 1 + word.length()) - lineColorChars) > lineLength) {
                    for (final String partialWord : word.toString().split("(?<=\\G.{" + lineLength + "})")) {
                        lines.add(line.toString());
                        line = new StringBuilder(partialWord);
                    }
                    lineColorChars = 0;
                } else {
                    if (line.length() > 0) {
                        line.append(' ');
                    }
                    line.append(word);
                }
                word = new StringBuilder();
                if (c == '\n') {
                    lines.add(line.toString());
                    line = new StringBuilder();
                }
            } else {
                word.append(c);
            }
        }
        if (line.length() > 0) {
            lines.add(line.toString());
        }
        if ((lines.get(0).length() == 0) || (lines.get(0).charAt(0) != '\u00A7')) {
            lines.set(0, "\u00A7f" + lines.get(0));
        }
        for (int i = 1; i < lines.size(); i++) {
            final String pLine = lines.get(i - 1);
            final String subLine = lines.get(i);
            
            final char color = pLine.charAt(pLine.lastIndexOf('\u00A7') + 1);
            if ((subLine.length() == 0) || (subLine.charAt(0) != '\u00A7')) {
                lines.set(i, '\u00A7' + (color) + subLine);
            }
        }
        return lines.toArray(new String[lines.size()]);
    }
    
    /**
     * Send a message to the player
     *
     * @param plr Player to recieve message
     * @param c   Caption to send
     *
     * @return boolean success
     */
    public static boolean sendMessage(final PlotPlayer plr, final C c, final String... args) {
        if (c.s().length() > 1) {
            String msg = c.s();
            if ((args != null) && (args.length > 0)) {
                msg = C.format(c, args);
            }
            if (plr == null) {
                PS.log(msg);
            } else {
                sendMessage(plr, msg, c.usePrefix());
            }
        }
        return true;
    }
    
    /**
     * Send a message to the player
     *
     * @param plr Player to recieve message
     * @param c   Caption to send
     *
     * @return boolean success
     */
    public static boolean sendMessage(final PlotPlayer plr, final C c, final Object... args) {
        if (c.s().length() > 1) {
            String msg = c.s();
            if ((args != null) && (args.length > 0)) {
                msg = C.format(c, args);
            }
            if (plr == null) {
                PS.log(msg);
            } else {
                sendMessage(plr, msg, c.usePrefix());
            }
        }
        return true;
    }
    
    /**
     * @deprecated raw access is deprecated
     */
    public static HashSet<Plot> connected_cache;
    public static HashSet<RegionWrapper> regions_cache;
    
    public static HashSet<Plot> getConnectedPlots(Plot plot) {
        if (plot == null) {
            return null;
        }
        if (plot.settings == null) {
            return new HashSet<>(Arrays.asList(plot));
        }
        boolean[] merged = plot.getMerged();
        int hash = hash(merged);
        if (hash == 0) {
            return new HashSet<>(Arrays.asList(plot));
        }
        if (connected_cache != null && connected_cache.contains(plot)) {
            return connected_cache;
        }
        regions_cache = null;
        connected_cache = new HashSet<Plot>();
        ArrayDeque<Plot> frontier = new ArrayDeque<>();
        HashSet<Object> queuecache = new HashSet<>();
        connected_cache.add(plot);
        Plot tmp;
        if (merged[0]) {
            tmp = getPlotAbs(plot.world, getPlotIdRelative(plot.id, 0));
            if (!tmp.getMerged(2)) {
                // invalid merge
                PS.debug("Fixing invalid merge: " + plot);
                tmp.getSettings().setMerged(2, true);
                DBFunc.setMerged(tmp, tmp.settings.getMerged());
            }
            queuecache.add(tmp);
            frontier.add(tmp);
        }
        if (merged[1]) {
            tmp = getPlotAbs(plot.world, getPlotIdRelative(plot.id, 1));
            if (!tmp.getMerged(3)) {
                // invalid merge
                PS.debug("Fixing invalid merge: " + plot);
                tmp.getSettings().setMerged(3, true);
                DBFunc.setMerged(tmp, tmp.settings.getMerged());
            }
            queuecache.add(tmp);
            frontier.add(tmp);
        }
        if (merged[2]) {
            tmp = getPlotAbs(plot.world, getPlotIdRelative(plot.id, 2));
            if (!tmp.getMerged(0)) {
                // invalid merge
                PS.debug("Fixing invalid merge: " + plot);
                tmp.getSettings().setMerged(0, true);
                DBFunc.setMerged(tmp, tmp.settings.getMerged());
            }
            queuecache.add(tmp);
            frontier.add(tmp);
        }
        if (merged[3]) {
            tmp = getPlotAbs(plot.world, getPlotIdRelative(plot.id, 3));
            if (!tmp.getMerged(1)) {
                // invalid merge
                PS.debug("Fixing invalid merge: " + plot);
                tmp.getSettings().setMerged(1, true);
                DBFunc.setMerged(tmp, tmp.settings.getMerged());
            }
            queuecache.add(tmp);
            frontier.add(tmp);
        }
        Plot current;
        while ((current = frontier.poll()) != null) {
            if (current.settings == null) {
                // Invalid plot
                // merged onto unclaimed plot
                PS.debug("Ignoring invalid merged plot: " + current + " | " + current.owner);
                continue;
            }
            connected_cache.add(current);
            queuecache.remove(current);
            merged = current.getMerged();
            if (merged[0]) {
                tmp = getPlotAbs(current.world, getPlotIdRelative(current.id, 0));
                if (!queuecache.contains(tmp) && !connected_cache.contains(tmp)) {
                    queuecache.add(tmp);
                    frontier.add(tmp);
                }
            }
            if (merged[1]) {
                tmp = getPlotAbs(current.world, getPlotIdRelative(current.id, 1));
                if (!queuecache.contains(tmp) && !connected_cache.contains(tmp)) {
                    queuecache.add(tmp);
                    frontier.add(tmp);
                }
            }
            if (merged[2]) {
                tmp = getPlotAbs(current.world, getPlotIdRelative(current.id, 2));
                if (!queuecache.contains(tmp) && !connected_cache.contains(tmp)) {
                    queuecache.add(tmp);
                    frontier.add(tmp);
                }
            }
            if (merged[3]) {
                tmp = getPlotAbs(current.world, getPlotIdRelative(current.id, 3));
                if (!queuecache.contains(tmp) && !connected_cache.contains(tmp)) {
                    queuecache.add(tmp);
                    frontier.add(tmp);
                }
            }
        }
        return connected_cache;
    }
    
    /**
     * Fetches the plot from the main class
     */
    public static Plot getPlotAbs(final String world, final PlotId id) {
        if (id == null) {
            return null;
        }
        final Plot plot = PS.get().getPlot(world, id);
        if (plot != null) {
            return plot;
        }
        return new Plot(world, id, null);
    }
    
    /**
     * Gets all the connected plots
     */
    public static HashSet<Plot> getPlots(final String world, final PlotId id) {
        if (id == null) {
            return null;
        }
        final Plot plot = PS.get().getPlot(world, id);
        if (plot != null) {
            return getConnectedPlots(plot);
        }
        return new HashSet<>(Arrays.asList(new Plot(world, id, null)));
    }
    
    /**
     * Returns the plot id at a location (mega plots are considered)
     * @param loc
     * @return PlotId PlotId observed id
     */
    public static PlotId getPlotId(final Location loc) {
        final String world = loc.getWorld();
        final PlotManager manager = PS.get().getPlotManager(world);
        if (manager == null) {
            return null;
        }
        final PlotWorld plotworld = PS.get().getPlotWorld(world);
        final PlotId id = manager.getPlotId(plotworld, loc.getX(), loc.getY(), loc.getZ());
        if ((id != null) && (plotworld.TYPE == 2)) {
            if (ClusterManager.getCluster(world, id) == null) {
                return null;
            }
        }
        return id;
    }
    
    /**
     * Get the maximum number of plots a player is allowed
     *
     * @param p
     * @return int
     */
    public static int getAllowedPlots(final PlotPlayer p) {
        return Permissions.hasPermissionRange(p, "plots.plot", Settings.MAX_PLOTS);
    }
    
    public static Plot getPlotAbs(final Location loc) {
        final PlotId id = getPlotId(loc);
        if (id == null) {
            return null;
        }
        return getPlotAbs(loc.getWorld(), id);
    }
    
    public static Set<Plot> getPlots(final Location loc) {
        final PlotId id = getPlotId(loc);
        if (id == null) {
            return null;
        }
        return getPlots(loc.getWorld(), id);
    }
    
    public static double getAverageRating(final Plot plot) {
        HashMap<UUID, Integer> rating;
        if (plot.getSettings().ratings != null) {
            rating = plot.getSettings().ratings;
        } else if (Settings.CACHE_RATINGS) {
            rating = new HashMap<>();
        } else {
            rating = DBFunc.getRatings(plot);
        }
        if ((rating == null) || (rating.size() == 0)) {
            return 0;
        }
        double val = 0;
        int size = 0;
        for (final Entry<UUID, Integer> entry : rating.entrySet()) {
            int current = entry.getValue();
            if ((Settings.RATING_CATEGORIES == null) || (Settings.RATING_CATEGORIES.size() == 0)) {
                val += current;
                size++;
            } else {
                for (int i = 0; i < Settings.RATING_CATEGORIES.size(); i++) {
                    val += (current % 10) - 1;
                    current /= 10;
                    size++;
                }
            }
        }
        return val / size;
    }
    
    public static double[] getAverageRatings(final Plot plot) {
        HashMap<UUID, Integer> rating;
        if (plot.getSettings().ratings != null) {
            rating = plot.getSettings().ratings;
        } else if (Settings.CACHE_RATINGS) {
            rating = new HashMap<>();
        } else {
            rating = DBFunc.getRatings(plot);
        }
        int size = 1;
        if (Settings.RATING_CATEGORIES != null) {
            size = Math.max(1, Settings.RATING_CATEGORIES.size());
        }
        final double[] ratings = new double[size];
        if ((rating == null) || (rating.size() == 0)) {
            return ratings;
        }
        for (final Entry<UUID, Integer> entry : rating.entrySet()) {
            int current = entry.getValue();
            if ((Settings.RATING_CATEGORIES == null) || (Settings.RATING_CATEGORIES.size() == 0)) {
                ratings[0] += current;
            } else {
                for (int i = 0; i < Settings.RATING_CATEGORIES.size(); i++) {
                    ratings[i] += (current % 10) - 1;
                    current /= 10;
                }
            }
        }
        for (int i = 0; i < size; i++) {
            ratings[i] /= rating.size();
        }
        return ratings;
    }
    
    public static boolean setComponent(final Plot plot, final String component, final PlotBlock[] blocks) {
        return PS.get().getPlotManager(plot.world).setComponent(PS.get().getPlotWorld(plot.world), plot.id, component, blocks);
    }
}
