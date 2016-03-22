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

import com.intellectualcrafters.plot.PS;
import com.intellectualcrafters.plot.config.C;
import com.intellectualcrafters.plot.config.Settings;
import com.intellectualcrafters.plot.database.DBFunc;
import com.intellectualcrafters.plot.flag.Flag;
import com.intellectualcrafters.plot.flag.FlagManager;
import com.intellectualcrafters.plot.object.ChunkLoc;
import com.intellectualcrafters.plot.object.ConsolePlayer;
import com.intellectualcrafters.plot.object.Location;
import com.intellectualcrafters.plot.object.Plot;
import com.intellectualcrafters.plot.object.PlotArea;
import com.intellectualcrafters.plot.object.PlotBlock;
import com.intellectualcrafters.plot.object.PlotId;
import com.intellectualcrafters.plot.object.PlotPlayer;
import com.intellectualcrafters.plot.object.PseudoRandom;
import com.intellectualcrafters.plot.object.RegionWrapper;
import com.intellectualcrafters.plot.object.RunnableVal;
import java.io.File;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.regex.Matcher;

/**
 * plot functions
 *
 */
public class MainUtil {

    /**
     *
     * @deprecated
     * @param loc
     * @return
     */
    @Deprecated
    public static PlotId getPlotId(Location loc) {
        PlotArea area = loc.getPlotArea();
        return area == null ? null : area.getPlotManager().getPlotId(area, loc.getX(), loc.getY(), loc.getZ());
    }

    /**
     * If the NMS code for sending chunk updates is functional<br>
     *  - E.g. If using an older version of Bukkit, or before the plugin is updated to 1.5<br>
     *  - Slower fallback code will be used if not.<br>
     */
    public static boolean canSendChunk = false;

    /**
     * Cache of mapping x,y,z coordinates to the chunk array<br>
     *  - Used for efficent world generation<br>
     */
    public static short[][] x_loc;
    public static short[][] y_loc;
    public static short[][] z_loc;
    public static short[][][] CACHE_I = null;
    public static short[][][] CACHE_J = null;

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
                    final int y = i4 + (j >> 8);
                    final int a = j - ((y & 0xF) << 8);
                    final int z1 = a >> 4;
                    final int x1 = a - (z1 << 4);
                    x_loc[i][j] = (short) x1;
                    y_loc[i][j] = (short) y;
                    z_loc[i][j] = (short) z1;
                }
            }
        }
        if (CACHE_I == null) {
            CACHE_I = new short[256][16][16];
            CACHE_J = new short[256][16][16];
            for (int x = 0; x < 16; x++) {
                for (int z = 0; z < 16; z++) {
                    for (int y = 0; y < 256; y++) {
                        final short i = (short) (y >> 4);
                        final short j = (short) ((y & 0xF) << 8 | z << 4 | x);
                        CACHE_I[y][x][z] = i;
                        CACHE_J[y][x][z] = j;
                    }
                }
            }
        }
    }

    public static void upload(UUID uuid, String file, String extension, final RunnableVal<OutputStream> writeTask, final RunnableVal<URL> whenDone) {
        if (writeTask == null) {
            PS.debug("&cWrite task cannot be null");
            TaskManager.runTask(whenDone);
            return;
        }
        final String filename;
        final String website;
        if (uuid == null) {
            uuid = UUID.randomUUID();
            website = Settings.WEB_URL + "upload.php?" + uuid;
            filename = "plot." + extension;
        } else {
            website = Settings.WEB_URL + "save.php?" + uuid;
            filename = file + "." + extension;
        }
        final URL url;
        try {
            url = new URL(Settings.WEB_URL + "?key=" + uuid + "&ip=" + Settings.WEB_IP + "&type=" + extension);
        } catch (MalformedURLException e) {
            e.printStackTrace();
            whenDone.run();
            return;
        }
        TaskManager.runTaskAsync(new Runnable() {
            @Override
            public void run() {
                try {
                    final String boundary = Long.toHexString(System.currentTimeMillis());
                    final URLConnection con = new URL(website).openConnection();
                    con.setDoOutput(true);
                    con.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
                    try (OutputStream output = con.getOutputStream();
                         PrintWriter writer = new PrintWriter(new OutputStreamWriter(output, StandardCharsets.UTF_8), true)) {
                        final String CRLF = "\r\n";
                        writer.append("--" + boundary).append(CRLF);
                        writer.append("Content-Disposition: form-data; name=\"param\"").append(CRLF);
                        writer.append("Content-Type: text/plain; charset=" + StandardCharsets.UTF_8.displayName()).append(CRLF);
                        final String param = "value";
                        writer.append(CRLF).append(param).append(CRLF).flush();
                        writer.append("--" + boundary).append(CRLF);
                        writer.append("Content-Disposition: form-data; name=\"schematicFile\"; filename=\"" + filename + "\"").append(CRLF);
                        writer.append("Content-Type: " + URLConnection.guessContentTypeFromName(filename)).append(CRLF);
                        writer.append("Content-Transfer-Encoding: binary").append(CRLF);
                        writer.append(CRLF).flush();
                        writeTask.value = output;
                        writeTask.run();
                        output.flush();
                        writer.append(CRLF).flush();
                        writer.append("--" + boundary + "--").append(CRLF).flush();
                    }
//                    try (Reader response = new InputStreamReader(con.getInputStream(), StandardCharsets.UTF_8)) {
//                        final char[] buffer = new char[256];
//                        final StringBuilder result = new StringBuilder();
//                        while (true) {
//                            final int r = response.read(buffer);
//                            if (r < 0) {
//                                break;
//                            }
//                            result.append(buffer, 0, r);
//                        }
//                        if (!result.toString().startsWith("Success")) {
//                            PS.debug(result);
//                        }
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
                    final int responseCode = ((HttpURLConnection) con).getResponseCode();
                    if (responseCode == 200) {
                        whenDone.value = url;
                    }
                    TaskManager.runTask(whenDone);
                } catch (Exception e) {
                    e.printStackTrace();
                    TaskManager.runTask(whenDone);
                }
            }
        });
    }

    /**
     * Resets the biome if it was modified
     * @param area
     * @param pos1
     * @param pos2
     * @return true if any changes were made
     */
    public static boolean resetBiome(PlotArea area, Location pos1, Location pos2) {
        String biome = WorldUtil.IMP.getBiomeList()[area.PLOT_BIOME];
        if (!StringMan.isEqual(WorldUtil.IMP.getBiome(area.worldname, (pos1.getX() + pos2.getX()) / 2, (pos1.getZ() + pos2.getZ()) / 2), biome)) {
            MainUtil.setBiome(area.worldname, pos1.getX(), pos1.getZ(), pos2.getX(), pos2.getZ(), biome);
            return true;
        }
        return false;
    }

    public static long timeToSec(String string) {
        if (MathMan.isInteger(string)) {
            return Long.parseLong(string);
        }
        if (string == null) {
            return 0;
        }
        string = string.toLowerCase().trim().toLowerCase();
        if (string.equalsIgnoreCase("false")) {
            return 0;
        }
        String[] split = string.split(" ");
        long time = 0;
        for (int i = 0; i < split.length; i++) {
            String value = split[i];
            int nums = Integer.parseInt(value.replaceAll("[^\\d]", ""));
            String letters = value.replaceAll("[^a-z]", "");
            switch (letters) {
                case "week":
                case "weeks":
                case "wks":
                case "w": {
                    time += 604800 * nums;
                }
                case "days":
                case "day":
                case "d": {
                    time += 86400 * nums;
                }
                case "hour":
                case "hr":
                case "hrs":
                case "hours":
                case "h": {
                    time += 3600 * nums;
                }
                case "minutes":
                case "minute":
                case "mins":
                case "min":
                case "m": {
                    time += 60 * nums;
                }
                case "seconds":
                case "second":
                case "secs":
                case "sec":
                case "s":{
                    time += nums;
                }
            }
        }
        return time;
    }

    /**
     * Hashcode of a boolean array.<br>
     *  - Used for traversing mega plots quickly.
     * @param array
     * @return hashcode
     */
    public static int hash(boolean[] array) {
        if (array.length == 4) {
            if (!array[0] && !array[1] && !array[2] && !array[3]) {
                return 0;
            }
            return ((array[0] ? 1 : 0) << 3) + ((array[1] ? 1 : 0) << 2) + ((array[2] ? 1 : 0) << 1) + (array[3] ? 1 : 0);
        }
        int n = 0;
        for (boolean anArray : array) {
            n = (n << 1) + (anArray ? 1 : 0);
        }
        return n;
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
     * Get the name from a UUID<br>
     * @param owner
     * @return The player's name, None, Everyone or Unknown
     */
    public static String getName(final UUID owner) {
        if (owner == null) {
            return C.NONE.s();
        } else if (owner.equals(DBFunc.everyone)) {
            return C.EVERYONE.s();
        }
        final String name = UUIDHandler.getName(owner);
        if (name == null) {
            return C.UNKNOWN.s();
        }
        return name;
    }

    /**
     * Get the corner locations for a list of regions<br>
     * @see Plot#getCorners()
     * @param world
     * @param regions
     * @return
     */
    public static Location[] getCorners(String world, Collection<RegionWrapper> regions) {
        Location min = null;
        Location max = null;
        for (RegionWrapper region : regions) {
            Location[] corners = region.getCorners(world);
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

    /**
     * Fuzzy plot search with spaces separating terms<br>
     *  - Terms: type, alias, world, owner, trusted, member
     * @param search
     * @return
     */
    public static List<Plot> getPlotsBySearch(final String search) {
        final String[] split = search.split(" ");
        final int size = split.length * 2;

        final List<UUID> uuids = new ArrayList<>();
        PlotId id = null;
        PlotArea area = null;
        String alias = null;

        for (final String term : split) {
            try {
                UUID uuid = UUIDHandler.getUUID(term, null);
                if (uuid == null) {
                    uuid = UUID.fromString(term);
                }
                uuids.add(uuid);
            } catch (final Exception e) {
                id = PlotId.fromString(term);
                if (id != null) {
                    continue;
                }
                area = PS.get().getPlotAreaByString(term);
                if (area == null) {
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
            if (!uuids.isEmpty()) {
                for (final UUID uuid : uuids) {
                    if (plot.isOwner(uuid)) {
                        count += 2;
                    } else if (plot.isAdded(uuid)) {
                        count++;
                    }
                }
            }
            if (id != null) {
                if (plot.getId().equals(id)) {
                    count++;
                }
            }
            if (area != null && plot.getArea().equals(area)) {
                count++;
            }
            if (alias != null && alias.equals(plot.getAlias())) {
                count += 2;
            }
            if (count != 0) {
                plotList.get(count - 1).add(plot);
            }
        }

        final List<Plot> plots = new ArrayList<>();
        for (int i = plotList.size() - 1; i >= 0; i--) {
            if (!plotList.get(i).isEmpty()) {
                plots.addAll(plotList.get(i));
            }
        }
        return plots;
    }

    /**
     * Get the plot from a string<br>
     * @param player Provides a context for what world to search in. Prefixing the term with 'world_name;' will override this context.
     * @param arg The search term
     * @param message If a message should be sent to the player if a plot cannot be found
     * @return The plot if only 1 result is found, or null
     */
    public static Plot getPlotFromString(final PlotPlayer player, final String arg, final boolean message) {
        if (arg == null) {
            if (player == null) {
                if (message) {
                    MainUtil.sendMessage(player, C.NOT_VALID_PLOT_WORLD);
                }
                return null;
            }
            return player.getCurrentPlot();
        }
        PlotArea area;
        if (player != null) {
            area = PS.get().getPlotAreaByString(arg);
            if (area == null) {
                area = player.getApplicablePlotArea();
            }
        } else {
            area = ConsolePlayer.getConsole().getApplicablePlotArea();
        }
        final String[] split = arg.split(";|,");
        PlotId id;
        if (split.length == 4) {
            area = PS.get().getPlotAreaByString(split[0] + ";" + split[1]);
            id = PlotId.fromString(split[2] + ";" + split[3]);
        } else if (split.length == 3) {
            area = PS.get().getPlotAreaByString(split[0]);
            id = PlotId.fromString(split[1] + ";" + split[2]);
        } else if (split.length == 2) {
            id = PlotId.fromString(arg);
        } else {
            Collection<Plot> plots;
            if (area == null) {
                plots = PS.get().getPlots();
            } else {
                plots = area.getPlots();
            }
            for (final Plot p : plots) {
                final String name = p.getAlias();
                if (!name.isEmpty() && StringMan.isEqualIgnoreCase(name, arg)) {
                    return p;
                }
            }
            if (message) {
                MainUtil.sendMessage(player, C.NOT_VALID_PLOT_ID);
            }
            return null;
        }
        if (id == null) {
            if (message) {
                MainUtil.sendMessage(player, C.NOT_VALID_PLOT_ID);
            }
            return null;
        }
        if (area == null) {
            if (message) {
                MainUtil.sendMessage(player, C.NOT_VALID_PLOT_WORLD);
            }
            return null;
        }
        return area.getPlotAbs(id);
    }

    /**
     * Resend the chunk at a location
     * @param world
     * @param loc
     */
    public static void update(final String world, final ChunkLoc loc) {
        SetQueue.IMP.queue.sendChunk(world, Collections.singletonList(loc));
    }

    public static File getFile(File base, String path) {
        if (Paths.get(path).isAbsolute()) {
            return new File(path);
        }
        return new File(base, path);
    }

    /**
     * Set a cuboid in the world to a set of blocks.
     * @param world
     * @param pos1
     * @param pos2
     * @param blocks If multiple blocks are provided, the result will be a random mix
     */
    public static void setCuboid(final String world, final Location pos1, final Location pos2, final PlotBlock[] blocks) {
        if (blocks.length == 1) {
            setSimpleCuboid(world, pos1, pos2, blocks[0]);
            return;
        }
        for (int y = pos1.getY(); y <= pos2.getY(); y++) {
            for (int x = pos1.getX(); x <= pos2.getX(); x++) {
                for (int z = pos1.getZ(); z <= pos2.getZ(); z++) {
                    final int i = PseudoRandom.random.random(blocks.length);
                    final PlotBlock block = blocks[i];
                    SetQueue.IMP.setBlock(world, x, y, z, block);
                }
            }
        }
        while (SetQueue.IMP.forceChunkSet());
    }

    /**
     * Set a cuboid asynchronously to a set of blocks
     * @param world
     * @param pos1
     * @param pos2
     * @param blocks
     */
    public static void setCuboidAsync(final String world, final Location pos1, final Location pos2, final PlotBlock[] blocks) {
        if (blocks.length == 1) {
            setSimpleCuboidAsync(world, pos1, pos2, blocks[0]);
            return;
        }
        for (int y = pos1.getY(); y <= Math.min(255, pos2.getY()); y++) {
            for (int x = pos1.getX(); x <= pos2.getX(); x++) {
                for (int z = pos1.getZ(); z <= pos2.getZ(); z++) {
                    final int i = PseudoRandom.random.random(blocks.length);
                    final PlotBlock block = blocks[i];
                    SetQueue.IMP.setBlock(world, x, y, z, block);
                }
            }
        }
    }

    /**
     * Set a cuboid to a block
     * @param world
     * @param pos1
     * @param pos2
     * @param newblock
     */
    public static void setSimpleCuboid(final String world, final Location pos1, final Location pos2, final PlotBlock newblock) {
        for (int y = pos1.getY(); y <= pos2.getY(); y++) {
            for (int x = pos1.getX(); x <= pos2.getX(); x++) {
                for (int z = pos1.getZ(); z <= pos2.getZ(); z++) {
                    SetQueue.IMP.setBlock(world, x, y, z, newblock);
                }
            }
        }
        while (SetQueue.IMP.forceChunkSet());
    }

    /**
     * Set a cuboid asynchronously to a block
     * @param world
     * @param pos1
     * @param pos2
     * @param newblock
     */
    public static void setSimpleCuboidAsync(final String world, final Location pos1, final Location pos2, final PlotBlock newblock) {
        for (int y = pos1.getY(); y <= Math.min(255, pos2.getY()); y++) {
            for (int x = pos1.getX(); x <= pos2.getX(); x++) {
                for (int z = pos1.getZ(); z <= pos2.getZ(); z++) {
                    SetQueue.IMP.setBlock(world, x, y, z, newblock);
                }
            }
        }
    }

    /**
     * Synchronously set the biome in a selection
     * @param world
     * @param p1x
     * @param p1z
     * @param p2x
     * @param p2z
     * @param biome
     */
    public static void setBiome(final String world, final int p1x, final int p1z, final int p2x, final int p2z, final String biome) {
        RegionWrapper region = new RegionWrapper(p1x, p2x, p1z, p2z);
        WorldUtil.IMP.setBiomes(world, region, biome);
    }

    /**
     * Get the highest block at a location
     * @param world
     * @param x
     * @param z
     * @return
     */
    public static int getHeighestBlock(final String world, final int x, final int z) {
        final int result = WorldUtil.IMP.getHighestBlock(world, x, z);
        if (result == 0) {
            return 64;
        }
        return result;
    }

    /**
     * Send a message to the player
     *
     * @param plr Player to receive message
     * @param msg Message to send
     *
     * @return true Can be used in things such as commands (return PlayerFunctions.sendMessage(...))
     */
    public static boolean sendMessage(final PlotPlayer plr, final String msg) {
        return sendMessage(plr, msg, true);
    }

    /**
     * Send a message to console
     * @param caption
     * @param args
     */
    public static void sendConsoleMessage(final C caption, final String... args) {
        sendMessage(null, caption, args);
    }

    /**
     * Send a message to a player
     * @param plr Can be null to represent console, or use ConsolePlayer.getConsole()
     * @param msg
     * @param prefix If the message should be prefixed with the configured prefix
     * @return
     */
    public static boolean sendMessage(final PlotPlayer plr, final String msg, final boolean prefix) {
        if (!msg.isEmpty()) {
            if (plr == null) {
                ConsolePlayer.getConsole().sendMessage((prefix ? C.PREFIX.s() : "") + msg);
            } else {
                plr.sendMessage((prefix ? C.PREFIX.s() : "") + C.color(msg));
            }
        }
        return true;
    }

    /**
     * Send a message to the player
     *
     * @param plr Player to receive message
     * @param c   Caption to send
     *
     * @return boolean success
     */
    public static boolean sendMessage(final PlotPlayer plr, final C c, final String... args) {
        return sendMessage(plr, c, (Object[]) args);
    }

    /**
     * Send a message to the player
     *
     * @param plr Player to receive message
     * @param c   Caption to send
     *
     * @return boolean success
     */
    public static boolean sendMessage(final PlotPlayer plr, final C c, final Object... args) {
        if (c.s().isEmpty()) {
            return true;
        }
        TaskManager.runTaskAsync(new Runnable() {
            @Override
            public void run() {
                String m = C.format(c, args);
                if (plr == null) {
                    ConsolePlayer.getConsole().sendMessage(m);
                } else {
                    plr.sendMessage(m);
                }
            }
        });
        return true;
    }

    /**
     * If rating categories are enabled, get the average rating by category.<br>
     *  - The index corresponds to the index of the category in the config
     * @param plot
     * @return
     */
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
        if (rating == null || rating.isEmpty()) {
            return ratings;
        }
        for (final Entry<UUID, Integer> entry : rating.entrySet()) {
            int current = entry.getValue();
            if (Settings.RATING_CATEGORIES == null || Settings.RATING_CATEGORIES.isEmpty()) {
                ratings[0] += current;
            } else {
                for (int i = 0; i < Settings.RATING_CATEGORIES.size(); i++) {
                    ratings[i] += current % 10 - 1;
                    current /= 10;
                }
            }
        }
        for (int i = 0; i < size; i++) {
            ratings[i] /= rating.size();
        }
        return ratings;
    }

    /**
     * Format a string with plot information:<br>
     * @param info
     * @param plot
     * @param player
     * @param full
     * @param whenDone
     */
    public static void format(String info, final Plot plot, final PlotPlayer player, final boolean full, final RunnableVal<String> whenDone) {
        final int num = plot.getConnectedPlots().size();
        final String alias = !plot.getAlias().isEmpty() ? plot.getAlias() : C.NONE.s();
        final Location bot = plot.getCorners()[0];
        final String biome = WorldUtil.IMP.getBiome(plot.getArea().worldname, bot.getX(), bot.getZ());
        final String trusted = getPlayerList(plot.getTrusted());
        final String members = getPlayerList(plot.getMembers());
        final String denied = getPlayerList(plot.getDenied());

        final Flag descriptionFlag = FlagManager.getPlotFlagRaw(plot, "description");
        final String description = descriptionFlag == null ? C.NONE.s() : descriptionFlag.getValueString();

        final String flags;
        if (!StringMan.join(FlagManager.getPlotFlags(plot.getArea(), plot.getSettings(), true).values(), "").isEmpty()) {
            flags = StringMan.replaceFromMap(
                    "$2" + StringMan.join(FlagManager.getPlotFlags(plot.getArea(), plot.getSettings(), true).values(), "$1, $2"), C.replacements);
        } else {
            flags = StringMan.replaceFromMap("$2" + C.NONE.s(), C.replacements);
        }
        final boolean build = plot.isAdded(player.getUUID());

        final String owner = plot.getOwners().isEmpty() ? "unowned" : getPlayerList(plot.getOwners());

        info = info.replaceAll("%id%", plot.getId().toString());
        info = info.replaceAll("%alias%", alias);
        info = info.replaceAll("%num%", num + "");
        info = info.replaceAll("%desc%", description);
        info = info.replaceAll("%biome%", biome);
        info = info.replaceAll("%owner%", owner);
        info = info.replaceAll("%members%", members);
        info = info.replaceAll("%player%", player.getName());
        info = info.replaceAll("%trusted%", trusted);
        info = info.replaceAll("%helpers%", members);
        info = info.replaceAll("%denied%", denied);
        info = info.replaceAll("%flags%", Matcher.quoteReplacement(flags));
        info = info.replaceAll("%build%", build + "");
        info = info.replaceAll("%desc%", "No description set.");
        if (info.contains("%rating%")) {
            final String newInfo = info;
            TaskManager.runTaskAsync(new Runnable() {
                @Override
                public void run() {
                    int max = 10;
                    if (Settings.RATING_CATEGORIES != null && !Settings.RATING_CATEGORIES.isEmpty()) {
                        max = 8;
                    }
                    String info;
                    if (full && Settings.RATING_CATEGORIES != null && Settings.RATING_CATEGORIES.size() > 1) {
                        String rating = "";
                        String prefix = "";
                        final double[] ratings = MainUtil.getAverageRatings(plot);
                        for (int i = 0; i < ratings.length; i++) {
                            rating += prefix + Settings.RATING_CATEGORIES.get(i) + "=" + String.format("%.1f", ratings[i]);
                            prefix = ",";
                        }
                        info = newInfo.replaceAll("%rating%", rating);
                    } else {
                        info = newInfo.replaceAll("%rating%", String.format("%.1f", plot.getAverageRating()) + "/" + max);
                    }
                    whenDone.run(info);
                }
            });
            return;
        }
        whenDone.run(info);
    }

    /**
     * Get a list of names given a list of uuids.<br>
     * - Uses the format {@link C#PLOT_USER_LIST} for the returned string
     * @param uuids
     * @return
     */
    public static String getPlayerList(final Collection<UUID> uuids) {
        final ArrayList<UUID> l = new ArrayList<>(uuids);
        if (l.size() < 1) {
            return C.NONE.s();
        }
        final String c = C.PLOT_USER_LIST.s();
        final StringBuilder list = new StringBuilder();
        for (int x = 0; x < l.size(); x++) {
            if (x + 1 == l.size()) {
                list.append(c.replace("%user%", getName(l.get(x))).replace(",", ""));
            } else {
                list.append(c.replace("%user%", getName(l.get(x))));
            }
        }
        return list.toString();
    }

    public static void getPersistentMeta(final UUID uuid, final String key, final RunnableVal<byte[]> result) {
        PlotPlayer pp = UUIDHandler.getPlayer(uuid);
        if (pp != null) {
            result.run(pp.getPersistentMeta(key));
        } else {
            DBFunc.dbManager.getPersistentMeta(uuid, new RunnableVal<Map<String, byte[]>>() {
                @Override
                public void run(Map<String, byte[]> value) {
                    result.run(value.get(key));
                }
            });
        }
    }
}
