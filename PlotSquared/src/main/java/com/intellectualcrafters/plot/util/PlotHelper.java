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

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import net.milkbowl.vault.economy.Economy;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import com.intellectualcrafters.plot.BukkitMain;
import com.intellectualcrafters.plot.PlotSquared;
import com.intellectualcrafters.plot.config.C;
import com.intellectualcrafters.plot.database.DBFunc;
import com.intellectualcrafters.plot.listeners.PlotListener;
import com.intellectualcrafters.plot.object.BlockLoc;
import com.intellectualcrafters.plot.object.Location;
import com.intellectualcrafters.plot.object.Plot;
import com.intellectualcrafters.plot.object.PlotBlock;
import com.intellectualcrafters.plot.object.PlotId;
import com.intellectualcrafters.plot.object.PlotManager;
import com.intellectualcrafters.plot.object.PlotSettings;
import com.intellectualcrafters.plot.object.PlotWorld;
import com.intellectualcrafters.plot.util.bukkit.BukkitUtil;

/**
 * plot functions
 *
 * @author Citymonstret
 */
@SuppressWarnings({"unused", "javadoc", "deprecation"}) public class PlotHelper {
    public final static HashMap<Plot, Integer> runners = new HashMap<>();
    public static boolean canSendChunk = false;
    public static ArrayList<String> runners_p = new ArrayList<>();
    static long state = 1;
    public static HashMap<String, PlotId> lastPlot = new HashMap<>();
    public static HashMap<String, Integer> worldBorder = new HashMap<>();

    public static int getBorder(String worldname) {
    	if (worldBorder.containsKey(worldname)) {
    		PlotWorld plotworld = PlotSquared.getWorldSettings(worldname);
    		return worldBorder.get(worldname) + 16;
    	}
    	return Integer.MAX_VALUE;
    }
    
    public static void setupBorder(String world) {
    	PlotWorld plotworld = PlotSquared.getWorldSettings(world);
    	if (!plotworld.WORLD_BORDER) {
    		return;
    	}
    	if (!worldBorder.containsKey(world)) {
    		worldBorder.put(world,0);
    	}
    	for (Plot plot : PlotSquared.getPlots(world).values()) {
    		updateWorldBorder(plot);
    	}
    }
    
    public static void createWorld(String world, String generator) {
        
    }
    
    public static PlotId parseId(String arg) {
        try {
            String[] split = arg.split(";");
            return new PlotId(Integer.parseInt(split[0]), Integer.parseInt(split[1])) ;
        }
        catch (Exception e) {
            return null;
        }
    }
    
    /**
     * direction 0 = north, 1 = south, etc:
     *
     * @param id
     * @param direction
     *
     * @return
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
     * Merges all plots in the arraylist (with cost)
     *
     * @param plr
     * @param world
     * @param plotIds
     *
     * @return
     */
    public static boolean mergePlots(final Player plr, final String world, final ArrayList<PlotId> plotIds) {

        final PlotWorld plotworld = PlotSquared.getWorldSettings(world);
        if ((PlotSquared.economy != null) && plotworld.USE_ECONOMY) {
            final double cost = plotIds.size() * plotworld.MERGE_PRICE;
            if (cost > 0d) {
                final Economy economy = PlotSquared.economy;
                if (economy.getBalance(plr) < cost) {
                    PlayerFunctions.sendMessage(plr, C.CANNOT_AFFORD_MERGE, "" + cost);
                    return false;
                }
                economy.withdrawPlayer(plr, cost);
                PlayerFunctions.sendMessage(plr, C.REMOVED_BALANCE, cost + "");
            }
        }
        return mergePlots(world, plotIds, true);
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
    public static boolean mergePlots(final String world, final ArrayList<PlotId> plotIds, boolean removeRoads) {

        if (plotIds.size() < 2) {
            return false;
        }
        final PlotId pos1 = plotIds.get(0);
        final PlotId pos2 = plotIds.get(plotIds.size() - 1);

        final PlotManager manager = PlotSquared.getPlotManager(world);
        final PlotWorld plotworld = PlotSquared.getWorldSettings(world);

        manager.startPlotMerge(plotworld, plotIds);


        boolean result = false;

        for (int x = pos1.x; x <= pos2.x; x++) {
            for (int y = pos1.y; y <= pos2.y; y++) {

                boolean changed = false;

                final boolean lx = x < pos2.x;
                final boolean ly = y < pos2.y;

                final PlotId id = new PlotId(x, y);

                final Plot plot = PlotSquared.getPlots(world).get(id);

                Plot plot2 = null;

                if (removeRoads) {
                    removeSign(plot);

                }
                if (lx) {
                    if (ly) {
                        if (!plot.settings.getMerged(1) || !plot.settings.getMerged(2)) {
                            changed = true;
                            if (removeRoads) {
                                manager.removeRoadSouthEast(plotworld, plot);
                            }
                        }
                    }
                    if (!plot.settings.getMerged(1)) {
                        changed = true;
                        plot2 = PlotSquared.getPlots(world).get(new PlotId(x + 1, y));
                        mergePlot(world, plot, plot2, removeRoads);
                        plot.settings.setMerged(1, true);
                        plot2.settings.setMerged(3, true);
                    }
                }
                if (ly) {
                    if (!plot.settings.getMerged(2)) {
                        changed = true;
                        plot2 = PlotSquared.getPlots(world).get(new PlotId(x, y + 1));
                        mergePlot(world, plot, plot2, removeRoads);
                        plot.settings.setMerged(2, true);
                        plot2.settings.setMerged(0, true);
                    }
                }
            }
        }
        
        for (int x = pos1.x; x <= pos2.x; x++) {
            for (int y = pos1.y; y <= pos2.y; y++) {
                final PlotId id = new PlotId(x, y);
                final Plot plot = PlotSquared.getPlots(world).get(id);
                DBFunc.setMerged(world, plot, plot.settings.getMerged());

            }
        }


        manager.finishPlotMerge(plotworld, plotIds);


        return result;
    }

    /**
     * Merges 2 plots Removes the road inbetween <br> - Assumes the first plot parameter is lower <br> - Assumes neither
     * are a Mega-plot <br> - Assumes plots are directly next to each other <br> - Saves to DB
     *
     * @param world
     * @param lesserPlot
     * @param greaterPlot
     */
    public static void mergePlot(final String world, final Plot lesserPlot, final Plot greaterPlot, boolean removeRoads) {

        final PlotManager manager = PlotSquared.getPlotManager(world);
        final PlotWorld plotworld = PlotSquared.getWorldSettings(world);

        if (lesserPlot.id.x.equals(greaterPlot.id.x)) {
            if (!lesserPlot.settings.getMerged(2)) {
                lesserPlot.settings.setMerged(2, true);
                greaterPlot.settings.setMerged(0, true);
                if (removeRoads) {
                    manager.removeRoadSouth(plotworld, lesserPlot);
                }
            }
        } else {
            if (!lesserPlot.settings.getMerged(1)) {
                lesserPlot.settings.setMerged(1, true);
                greaterPlot.settings.setMerged(3, true);
                if (removeRoads) {
                    manager.removeRoadEast(plotworld, lesserPlot);
                }
            }
        }
    }
    
    

    public static void removeSign(final Plot p) {
        String world = p.world;
        final PlotManager manager = PlotSquared.getPlotManager(world);
        final PlotWorld plotworld = PlotSquared.getWorldSettings(world);
        final Location loc = manager.getSignLoc(plotworld, p);
        BlockManager.setBlocks(world, new int[] { loc.getX()}, new int[] { loc.getY()}, new int[] { loc.getZ()}, new int[] { 0 }, new byte[] { 0 });
    }

    public static void setSign(String name, final Plot p) {

        if (name == null) {
            name = "unknown";
        }
        final PlotManager manager = PlotSquared.getPlotManager(p.world);
        final PlotWorld plotworld = PlotSquared.getWorldSettings(p.world);
        final Location loc = manager.getSignLoc(plotworld, p);
        final String id = p.id.x + ";" + p.id.y;
        String[] lines = new String[] {
                C.OWNER_SIGN_LINE_1.translated().replaceAll("%id%", id),
                C.OWNER_SIGN_LINE_2.translated().replaceAll("%id%", id).replaceAll("%plr%", name),
                C.OWNER_SIGN_LINE_3.translated().replaceAll("%id%", id).replaceAll("%plr%", name),
                C.OWNER_SIGN_LINE_4.translated().replaceAll("%id%", id).replaceAll("%plr%", name)
        };
        BukkitUtil.setSign(p.world, loc.getX(), loc.getY(), loc.getZ(), lines);
    }

    public static String getPlayerName(final UUID uuid) {
        if (uuid == null) {
            return "unknown";
        }
        final OfflinePlayer plr = UUIDHandler.uuidWrapper.getOfflinePlayer(uuid);
        if (!plr.hasPlayedBefore()) {
            return "unknown";
        }
        return plr.getName();
    }

    public static String getStringSized(final int max, final String string) {
        if (string.length() > max) {
            return string.substring(0, max);
        }
        return string;
    }

    public static void autoMerge(final String world, final Plot plot, final Player player) {

        if (plot == null) {
            return;
        }
        if (plot.owner == null) {
            return;
        }
        if (!plot.owner.equals(UUIDHandler.getUUID(player))) {
            return;
        }

        ArrayList<PlotId> plots;
        boolean merge = true;
        int count = 0;
        while (merge) {
            if (count > 16) {
                break;
            }
            count++;
            final PlotId bot = PlayerFunctions.getBottomPlot(world, plot).id;
            final PlotId top = PlayerFunctions.getTopPlot(world, plot).id;
            plots = PlayerFunctions.getPlotSelectionIds(new PlotId(bot.x, bot.y - 1), new PlotId(top.x, top.y));
            if (ownsPlots(world, plots, player, 0)) {
                final boolean result = mergePlots(world, plots, true);
                if (result) {
                    merge = true;
                    continue;
                }
            }
            plots = PlayerFunctions.getPlotSelectionIds(new PlotId(bot.x, bot.y), new PlotId(top.x + 1, top.y));
            if (ownsPlots(world, plots, player, 1)) {
                final boolean result = mergePlots(world, plots, true);
                if (result) {
                    merge = true;
                    continue;
                }
            }
            plots = PlayerFunctions.getPlotSelectionIds(new PlotId(bot.x, bot.y), new PlotId(top.x, top.y + 1));
            if (ownsPlots(world, plots, player, 2)) {
                final boolean result = mergePlots(world, plots, true);
                if (result) {
                    merge = true;
                    continue;
                }
            }
            plots = PlayerFunctions.getPlotSelectionIds(new PlotId(bot.x - 1, bot.y), new PlotId(top.x, top.y));
            if (ownsPlots(world, plots, player, 3)) {
                final boolean result = mergePlots(world, plots, true);
                if (result) {
                    merge = true;
                    continue;
                }
            }
            merge = false;
        }
        update(BukkitUtil.getLocation(player));
    }

    private static boolean ownsPlots(final String world, final ArrayList<PlotId> plots, final Player player, final int dir) {

        final PlotId id_min = plots.get(0);
        final PlotId id_max = plots.get(plots.size() - 1);
        for (final PlotId myid : plots) {
            final Plot myplot = PlotSquared.getPlots(world).get(myid);
            if ((myplot == null) || !myplot.hasOwner() || !(myplot.getOwner().equals(UUIDHandler.getUUID(player)))) {
                return false;
            }
            final PlotId top = PlayerFunctions.getTopPlot(world, myplot).id;
            if (((top.x > id_max.x) && (dir != 1)) || ((top.y > id_max.y) && (dir != 2))) {
                return false;
            }
            final PlotId bot = PlayerFunctions.getBottomPlot(world, myplot).id;
            if (((bot.x < id_min.x) && (dir != 3)) || ((bot.y < id_min.y) && (dir != 0))) {
                return false;
            }
        }
        return true;
    }
    
    public static void update(Location loc) {
        ArrayList<Chunk> chunks = new ArrayList<>();
        final int distance = Bukkit.getViewDistance();
        for (int cx = -distance; cx < distance; cx++) {
            for (int cz = -distance; cz < distance; cz++) {
                Chunk chunk = BukkitUtil.getChunkAt(loc.getWorld(), loc.getX(), loc.getZ());
                chunks.add(chunk);
            }
        }
        AbstractSetBlock.setBlockManager.update(chunks);
    }
    
    public static void updateWorldBorder(Plot plot) {
    	if (!worldBorder.containsKey(plot.world)) {
    		return;
    	}
    	String world = plot.world;
    	PlotManager manager = PlotSquared.getPlotManager(world);
    	PlotWorld plotworld = PlotSquared.getWorldSettings(world);
    	Location bot = manager.getPlotBottomLocAbs(plotworld, plot.id);
		Location top = manager.getPlotTopLocAbs(plotworld, plot.id);
		int border = worldBorder.get(plot.world);
		int botmax = Math.max(Math.abs(bot.getX()), Math.abs(bot.getZ()));
		int topmax = Math.max(Math.abs(top.getX()), Math.abs(top.getZ()));
		int max = Math.max(botmax, topmax);
		if (max > border ) {
			worldBorder.put(plot.world, max);
		}
    }

    /**
     * Create a plot and notify the world border and plot merger
     */
    public static boolean createPlot(final Player player, final Plot plot) {
        if (PlotHelper.worldBorder.containsKey(plot.world)) {
            updateWorldBorder(plot);
        }
        String w = BukkitUtil.getWorld(player);
        UUID uuid = UUIDHandler.getUUID(player);
        Plot p = createPlotAbs(uuid, plot);
        final PlotWorld plotworld = PlotSquared.getWorldSettings(w);
        if (plotworld.AUTO_MERGE) {
            autoMerge(w, p, player);
        }
        return true;
    }
    
    /**
     * Create a plot without notifying the merge function or world border manager 
     */
    public static Plot createPlotAbs(final UUID uuid, final Plot plot) {
        final World w = plot.getWorld();
        final Plot p = new Plot(plot.id, uuid, plot.settings.getBiome(), new ArrayList<UUID>(), new ArrayList<UUID>(), w.getName());
        PlotSquared.updatePlot(p);
        DBFunc.createPlotAndSettings(p);
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
            return new short[]{Short.parseShort(split[0]), Short.parseShort(split[1])};
        }
        return new short[]{Short.parseShort(block), 0};
    }

    public static void clearAllEntities(final String world, final Plot plot, final boolean tile) {
        final List<Entity> entities = BukkitUtil.getEntities(world);
        for (final Entity entity : entities) {
            final PlotId id = PlayerFunctions.getPlot(entity.getLocation());
            if (plot.id.equals(id)) {
                if (entity instanceof Player) {
                    final Player player = (Player) entity;
                    BukkitMain.teleportPlayer(player, BukkitUtil.getLocation(entity), plot);
                    PlotListener.plotExit(player, plot);
                } else {
                    entity.remove();
                }
            }
        }
    }

    /**
     * Clear a plot. Use null player if no player is present
     * @param player
     * @param world
     * @param plot
     * @param isDelete
     */
    public static void clear(final Player player, final String world, final Plot plot, final boolean isDelete) {

        if (runners.containsKey(plot)) {
            PlayerFunctions.sendMessage(null, C.WAIT_FOR_TIMER);
            return;
        }
        final PlotManager manager = PlotSquared.getPlotManager(world);

        final Location pos1 = PlotHelper.getPlotBottomLoc(world, plot.id).add(1, 0, 1);

        final int prime = 31;
        int h = 1;
        h = (prime * h) + pos1.getX();
        h = (prime * h) + pos1.getZ();
        state = h;
        
        final long start = System.currentTimeMillis();
        final Location location = PlotHelper.getPlotHomeDefault(plot);
        PlotWorld plotworld = PlotSquared.getWorldSettings(world);
        runners.put(plot, 1);
        if (plotworld.TERRAIN != 0) {
            final Location pos2 = PlotHelper.getPlotTopLoc(world, plot.id);
            ChunkManager.regenerateRegion(pos1, pos2, new Runnable() {
                @Override
                public void run() {
                    if (player != null && player.isOnline()) {
                        PlayerFunctions.sendMessage(player, C.CLEARING_DONE.s().replaceAll("%time%", "" + ((System.currentTimeMillis() - start))));
                    }
                    runners.remove(plot);
                }
            });
            return;
        }
        Runnable run = new Runnable() {
            @Override
            public void run() {
                PlotHelper.setBiome(world, plot, Biome.FOREST);
                runners.remove(plot);
                if (player != null && player.isOnline()) {
                    PlayerFunctions.sendMessage(player, C.CLEARING_DONE.s().replaceAll("%time%", "" + ((System.currentTimeMillis() - start))));
                }
                update(location);
            }
        };
        manager.clearPlot(world, plotworld, plot, isDelete, run);
    }

    /**
     * Clear a plot and associated sections: [sign, entities, border]
     *
     * @param requester
     * @param plot
     */
    public static void clear(final Player requester, final Plot plot, final boolean isDelete) {
        if (requester == null) {
            clearAllEntities(plot.getWorld(), plot, false);
            clear(requester, plot.getWorld(), plot, isDelete);
            removeSign(plot.getWorld(), plot);
            return;
        }
        if (runners.containsKey(plot)) {
            PlayerFunctions.sendMessage(requester, C.WAIT_FOR_TIMER);
            return;
        }

        PlayerFunctions.sendMessage(requester, C.CLEARING_PLOT);

        world = requester.getWorld();

        clearAllEntities(world, plot, false);
        clear(requester, world, plot, isDelete);
        removeSign(world, plot);
    }

        for (int y = pos1.getBlockY(); y < pos2.getBlockY(); y++) {
            for (int x = pos1.getX(); x < pos2.getX(); x++) {
                for (int z = pos1.getZ(); z < pos2.getZ(); z++) {
                    final Block block = world.getBlockAt(x, y, z);
                    if (!((block.getTypeId() == newblock.id) && (block.getData() == newblock.data))) {
                        setBlock(world, x, y, z, newblock.id, newblock.data);
                    }
                }
            }
        }
    }

    public static void setCuboid(final String world, final Location pos1, final Location pos2, final PlotBlock[] blocks) {

        if (blocks.length == 1) {
            setCuboid(world, pos1, pos2, blocks[0]);
            return;
        }
        for (int y = pos1.getBlockY(); y < pos2.getBlockY(); y++) {
            for (int x = pos1.getX(); x < pos2.getX(); x++) {
                for (int z = pos1.getZ(); z < pos2.getZ(); z++) {
                    final int i = random(blocks.length);
                    final PlotBlock newblock = blocks[i];
                    final Block block = world.getBlockAt(x, y, z);
                    if (!((block.getTypeId() == newblock.id) && (block.getData() == newblock.data))) {
                        setBlock(world, x, y, z, newblock.id, newblock.data);
                    }
                }
            }
        }
    }

    public static void setSimpleCuboid(final String world, final Location pos1, final Location pos2, final PlotBlock newblock) {

        for (int y = pos1.getBlockY(); y < pos2.getBlockY(); y++) {
            for (int x = pos1.getX(); x < pos2.getX(); x++) {
                for (int z = pos1.getZ(); z < pos2.getZ(); z++) {
                    final Block block = world.getBlockAt(x, y, z);
                    if (!((block.getTypeId() == newblock.id))) {
                        setBlock(world, x, y, z, newblock.id, (byte) 0);
                    }
                }
            }
        }
    }

    public static void setBiome(final String world, final Plot plot, final Biome b) {

        final int bottomX = getPlotBottomLoc(world, plot.id).getX();
        final int topX = getPlotTopLoc(world, plot.id).getX() + 1;
        final int bottomZ = getPlotBottomLoc(world, plot.id).getZ();
        final int topZ = getPlotTopLoc(world, plot.id).getZ() + 1;

        final Block block = world.getBlockAt(getPlotBottomLoc(world, plot.id).add(1, 1, 1));
        final Biome biome = block.getBiome();

        if (biome.equals(b)) {
            return;
        }

        for (int x = bottomX; x <= topX; x++) {
            for (int z = bottomZ; z <= topZ; z++) {
                final Block blk = world.getBlockAt(x, 0, z);
                final Biome c = blk.getBiome();
                if (c.equals(b)) {
                    x += 15;
                    continue;
                }
                blk.setBiome(b);
            }
        }
    }

    public static int getHeighestBlock(final String world, final int x, final int z) {

        boolean safe = false;
        int id;
        for (int i = 1; i < world.getMaxHeight(); i++) {
            id = world.getBlockAt(x, i, z).getTypeId();
            if (id == 0) {
                if (safe) {
                    return i;
                }
                safe = true;
            }
        }
        return 64;
    }

    /**
     * Get plot home
     *
     * @param w      World in which the plot is located
     * @param plotid Plot ID
     *
     * @return Home Location
     */
    public static Location getPlotHome(final World w, final PlotId plotid) {
        Plot plot = getPlot(w, plotid);
        BlockLoc home = plot.settings.getPosition();
        final Location bot = getPlotBottomLoc(w, plotid);
    	PlotManager manager = PlotSquared.getPlotManager(w);
        if (home == null || (home.x == 0 && home.z == 0)) {
            final Location top = getPlotTopLoc(w, plotid);
            final int x = ((top.getX() - bot.getX())/2) + bot.getX();
            final int z = ((top.getZ() - bot.getZ())/2) + bot.getZ();
            final int y = Math.max(getHeighestBlock(w, x, z), manager.getSignLoc(w, PlotSquared.getWorldSettings(w), plot).getBlockY());
            return new Location(w, x, y, z);
        }
        else {
        	final int y = Math.max(getHeighestBlock(w, home.x, home.z), home.y);
            return bot.add(home.x, y, home.z);
        }
    }

    /**
     * Retrieve the location of the default plot home position
     *
     * @param plot Plot
     *
     * @return the location
     */
    public static Location getPlotHomeDefault(final Plot plot) {
        final Location l = getPlotBottomLoc(plot.getWorld(), plot.getId()).subtract(0, 0, 0);
        l.setY(getHeighestBlock(plot.getWorld(), l.getX(), l.getZ()));
        return l;
    }

    /**
     * Get the plot home
     *
     * @param w    World
     * @param plot Plot Object
     *
     * @return Plot Home Location
     *
     * @see #getPlotHome(org.bukkit.World, com.intellectualcrafters.plot.object.PlotId)
     */
    public static Location getPlotHome(final World w, final Plot plot) {
        return getPlotHome(w, plot.id);
    }

    /**
     * Refresh the plot chunks
     *
     * @param world World in which the plot is located
     * @param plot  Plot Object
     */
    public static void refreshPlotChunks(final String world, final Plot plot) {

        final int bottomX = getPlotBottomLoc(world, plot.id).getX();
        final int topX = getPlotTopLoc(world, plot.id).getX();
        final int bottomZ = getPlotBottomLoc(world, plot.id).getZ();
        final int topZ = getPlotTopLoc(world, plot.id).getZ();

        final int minChunkX = (int) Math.floor((double) bottomX / 16);
        final int maxChunkX = (int) Math.floor((double) topX / 16);
        final int minChunkZ = (int) Math.floor((double) bottomZ / 16);
        final int maxChunkZ = (int) Math.floor((double) topZ / 16);

        final ArrayList<Chunk> chunks = new ArrayList<>();

        for (int x = minChunkX; x <= maxChunkX; x++) {
            for (int z = minChunkZ; z <= maxChunkZ; z++) {
                if (canSendChunk) {
                    final Chunk chunk = world.getChunkAt(x, z);
                    chunks.add(chunk);
                } else {
                    world.refreshChunk(x, z);
                }
            }
        }
        try {
            SendChunk.sendChunk(chunks);
        } catch (final Throwable e) {
            canSendChunk = false;
            for (int x = minChunkX; x <= maxChunkX; x++) {
                for (int z = minChunkZ; z <= maxChunkZ; z++) {
                    world.refreshChunk(x, z);
                }
            }
        }
    }

    /**
     * Gets the top plot location of a plot (all plots are treated as small plots) - To get the top loc of a mega plot
     * use getPlotTopLoc(...)
     *
     * @param world
     * @param id
     *
     * @return
     */
    public static Location getPlotTopLocAbs(final String world, final PlotId id) {

        final PlotWorld plotworld = PlotSquared.getWorldSettings(world);
        final PlotManager manager = PlotSquared.getPlotManager(world);
        return manager.getPlotTopLocAbs(plotworld, id);
    }

    /**
     * Gets the bottom plot location of a plot (all plots are treated as small plots) - To get the top loc of a mega
     * plot use getPlotBottomLoc(...)
     *
     * @param world
     * @param id
     *
     * @return
     */
    public static Location getPlotBottomLocAbs(final String world, final PlotId id) {

        final PlotWorld plotworld = PlotSquared.getWorldSettings(world);
        final PlotManager manager = PlotSquared.getPlotManager(world);
        return manager.getPlotBottomLocAbs(plotworld, id);
    }

    /**
     * Obtains the width of a plot (x width)
     *
     * @param world
     * @param id
     *
     * @return
     */
    public static int getPlotWidth(final String world, final PlotId id) {

        return getPlotTopLoc(world, id).getX() - getPlotBottomLoc(world, id).getX();
    }

    /**
     * Gets the top loc of a plot (if mega, returns top loc of that mega plot) - If you would like each plot treated as
     * a small plot use getPlotTopLocAbs(...)
     *
     * @param world
     * @param id
     *
     * @return
     */
    public static Location getPlotTopLoc(final String world, PlotId id) {

        final Plot plot = PlotSquared.getPlots(world).get(id);
        if (plot != null) {
            id = PlayerFunctions.getTopPlot(world, plot).id;
        }
        final PlotWorld plotworld = PlotSquared.getWorldSettings(world);
        final PlotManager manager = PlotSquared.getPlotManager(world);
        return manager.getPlotTopLocAbs(plotworld, id);
    }

    /**
     * Gets the bottom loc of a plot (if mega, returns bottom loc of that mega plot) - If you would like each plot
     * treated as a small plot use getPlotBottomLocAbs(...)
     *
     * @param world
     * @param id
     *
     * @return
     */
    public static Location getPlotBottomLoc(final String world, PlotId id) {

        final Plot plot = PlotSquared.getPlots(world).get(id);
        if (plot != null) {
            id = PlayerFunctions.getBottomPlot(world, plot).id;
        }
        final PlotWorld plotworld = PlotSquared.getWorldSettings(world);
        final PlotManager manager = PlotSquared.getPlotManager(world);
        return manager.getPlotBottomLocAbs(plotworld, id);
    }
    
    public static boolean isUnowned(final String world, final PlotId pos1, final PlotId pos2) {

        for (int x = pos1.x; x <= pos2.x; x++) {
            for (int y = pos1.y; y <= pos2.y; y++) {
                final PlotId id = new PlotId(x, y);
                if (PlotSquared.getPlots(world).get(id) != null) {
                    if (PlotSquared.getPlots(world).get(id).owner != null) {
                        return false;
                    }
                }
            }
        }
        return true;
    }
    
    public static boolean move(final String world, final PlotId current, PlotId newPlot, final Runnable whenDone) {
        String worldname = world.getName();
        final com.intellectualcrafters.plot.object.Location bot1 = PlotHelper.getPlotBottomLoc(worldname, current);
        com.intellectualcrafters.plot.object.Location bot2 = PlotHelper.getPlotBottomLoc(worldname, newPlot);
        final Location top = PlotHelper.getPlotTopLoc(worldname, current);
        final Plot currentPlot = PlotHelper.getPlot(worldname, current);
        if (currentPlot.owner == null) {
            return false;
        }
        Plot pos1 = PlayerFunctions.getBottomPlot(worldname, currentPlot);
        Plot pos2 = PlayerFunctions.getTopPlot(worldname, currentPlot);

        PlotId size = PlotHelper.getSize(world, currentPlot);
        if (!PlotHelper.isUnowned(world, newPlot, new PlotId(newPlot.x + size.x - 1, newPlot.y + size.y - 1))) {
            return false;
        }
        
        int offset_x = newPlot.x - pos1.id.x;
        int offset_y = newPlot.y - pos1.id.y;
        final ArrayList<PlotId> selection = PlayerFunctions.getPlotSelectionIds(pos1.id, pos2.id);
        String worldname = world.getName();
        for (PlotId id : selection) { 
            DBFunc.movePlot(world.getName(), new PlotId(id.x, id.y), new PlotId(id.x + offset_x, id.y + offset_y));
            Plot plot = PlotSquared.getPlots(worldname).get(id);
            PlotSquared.getPlots(worldname).remove(id);
            plot.id.x += offset_x;
            plot.id.y += offset_y;
            PlotSquared.getPlots(worldname).put(plot.id, plot);
        }
        ChunkManager.copyRegion(bot1, top, bot2, new Runnable() {
            @Override
            public void run() {
                Location bot = bot1.clone().add(1, 0, 1);
                ChunkManager.regenerateRegion(bot, top, null);
                TaskManager.runTaskLater(whenDone, 1);
            }
        });
        return true;
    }
    
    public static PlotId getSize(String world, Plot plot) {

        PlotSettings settings = plot.settings;
        if (!settings.isMerged()) {
            return new PlotId(1,1);
        }
        Plot top = PlayerFunctions.getTopPlot(world, plot);
        Plot bot = PlayerFunctions.getBottomPlot(world, plot);
        return new PlotId(top.id.x - bot.id.x + 1, top.id.y - bot.id.y + 1);
    }
    
    /**
     * Fetches the plot from the main class
     *
     * @param world
     * @param id
     *
     * @return
     */
    public static Plot getPlot(final String world, final PlotId id) {

        if (id == null) {
            return null;
        }
        if (PlotSquared.getPlots(world).containsKey(id)) {
            return PlotSquared.getPlots(world).get(id);
        }
        return new Plot(id, null, Biome.FOREST, new ArrayList<UUID>(), new ArrayList<UUID>(), world.getName());
    }

    /**
     * Returns the plot at a given location
     *
     * @param loc
     *
     * @return
     */
    public static Plot getCurrentPlot(final Location loc) {
        final PlotId id = PlayerFunctions.getPlot(loc);
        if (id == null) {
            return null;
        }
        if (PlotSquared.getPlots(loc.getWorld()).containsKey(id)) {
            return PlotSquared.getPlots(loc.getWorld()).get(id);
        }
        return new Plot(id, null, Biome.FOREST, new ArrayList<UUID>(), new ArrayList<UUID>(), loc.getWorld().getName());
    }
}
