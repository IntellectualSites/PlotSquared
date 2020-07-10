/*
 *       _____  _       _    _____                                _
 *      |  __ \| |     | |  / ____|                              | |
 *      | |__) | | ___ | |_| (___   __ _ _   _  __ _ _ __ ___  __| |
 *      |  ___/| |/ _ \| __|\___ \ / _` | | | |/ _` | '__/ _ \/ _` |
 *      | |    | | (_) | |_ ____) | (_| | |_| | (_| | | |  __/ (_| |
 *      |_|    |_|\___/ \__|_____/ \__, |\__,_|\__,_|_|  \___|\__,_|
 *                                    | |
 *                                    |_|
 *            PlotSquared plot management system for Minecraft
 *                  Copyright (C) 2020 IntellectualSites
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.plotsquared.core.util;

import com.plotsquared.core.PlotSquared;
import com.plotsquared.core.command.Like;
import com.plotsquared.core.configuration.Caption;
import com.plotsquared.core.configuration.CaptionUtility;
import com.plotsquared.core.configuration.Captions;
import com.plotsquared.core.configuration.ConfigurationSection;
import com.plotsquared.core.configuration.Settings;
import com.plotsquared.core.database.DBFunc;
import com.plotsquared.core.location.Location;
import com.plotsquared.core.player.ConsolePlayer;
import com.plotsquared.core.player.PlotPlayer;
import com.plotsquared.core.plot.Plot;
import com.plotsquared.core.plot.PlotArea;
import com.plotsquared.core.plot.PlotAreaTerrainType;
import com.plotsquared.core.plot.PlotAreaType;
import com.plotsquared.core.plot.PlotId;
import com.plotsquared.core.plot.expiration.ExpireManager;
import com.plotsquared.core.plot.flag.PlotFlag;
import com.plotsquared.core.plot.flag.implementations.DescriptionFlag;
import com.plotsquared.core.plot.flag.implementations.ServerPlotFlag;
import com.plotsquared.core.plot.flag.types.DoubleFlag;
import com.plotsquared.core.util.net.AbstractDelegateOutputStream;
import com.plotsquared.core.util.query.PlotQuery;
import com.plotsquared.core.util.task.RunnableVal;
import com.plotsquared.core.util.task.TaskManager;
import com.plotsquared.core.uuid.UUIDMapping;
import com.sk89q.worldedit.math.BlockVector2;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.world.biome.BiomeType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.Scanner;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.IntConsumer;
import java.util.function.IntFunction;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * plot functions
 */
public class MainUtil {

    private static final DecimalFormat FLAG_DECIMAL_FORMAT = new DecimalFormat("0");

    static {
        FLAG_DECIMAL_FORMAT.setMaximumFractionDigits(340);
    }

    /**
     * Cache of mapping x,y,z coordinates to the chunk array<br>
     * - Used for efficient world generation<br>
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
                int i4 = i << 4;
                for (int j = 0; j < 4096; j++) {
                    int y = i4 + (j >> 8);
                    int a = j - ((y & 0xF) << 8);
                    int z1 = a >> 4;
                    int x1 = a - (z1 << 4);
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
                        short i = (short) (y >> 4);
                        short j = (short) ((y & 0xF) << 8 | z << 4 | x);
                        CACHE_I[y][x][z] = i;
                        CACHE_J[y][x][z] = j;
                    }
                }
            }
        }
    }

    public static void upload(UUID uuid, String file, String extension,
        final RunnableVal<OutputStream> writeTask, final RunnableVal<URL> whenDone) {
        if (writeTask == null) {
            PlotSquared.debug("&cWrite task cannot be null");
            TaskManager.runTask(whenDone);
            return;
        }
        final String filename;
        final String website;
        if (uuid == null) {
            uuid = UUID.randomUUID();
            website = Settings.Web.URL + "upload.php?" + uuid;
            filename = "plot." + extension;
        } else {
            website = Settings.Web.URL + "save.php?" + uuid;
            filename = file + '.' + extension;
        }
        final URL url;
        try {
            url = new URL(Settings.Web.URL + "?key=" + uuid + "&type=" + extension);
        } catch (MalformedURLException e) {
            e.printStackTrace();
            whenDone.run();
            return;
        }
        TaskManager.runTaskAsync(() -> {
            try {
                String boundary = Long.toHexString(System.currentTimeMillis());
                URLConnection con = new URL(website).openConnection();
                con.setDoOutput(true);
                con.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
                try (OutputStream output = con.getOutputStream();
                    PrintWriter writer = new PrintWriter(
                        new OutputStreamWriter(output, StandardCharsets.UTF_8), true)) {
                    String CRLF = "\r\n";
                    writer.append("--" + boundary).append(CRLF);
                    writer.append("Content-Disposition: form-data; name=\"param\"").append(CRLF);
                    writer.append(
                        "Content-Type: text/plain; charset=" + StandardCharsets.UTF_8.displayName())
                        .append(CRLF);
                    String param = "value";
                    writer.append(CRLF).append(param).append(CRLF).flush();
                    writer.append("--" + boundary).append(CRLF);
                    writer.append(
                        "Content-Disposition: form-data; name=\"schematicFile\"; filename=\""
                            + filename + '"').append(CRLF);
                    writer
                        .append("Content-Type: " + URLConnection.guessContentTypeFromName(filename))
                        .append(CRLF);
                    writer.append("Content-Transfer-Encoding: binary").append(CRLF);
                    writer.append(CRLF).flush();
                    writeTask.value = new AbstractDelegateOutputStream(output) {
                        @Override public void close() {
                        } // Don't close
                    };
                    writeTask.run();
                    output.flush();
                    writer.append(CRLF).flush();
                    writer.append("--" + boundary + "--").append(CRLF).flush();
                }
                String content;
                try (Scanner scanner = new Scanner(con.getInputStream()).useDelimiter("\\A")) {
                    content = scanner.next().trim();
                }
                if (!content.startsWith("<")) {
                    PlotSquared.debug(content);
                }
                int responseCode = ((HttpURLConnection) con).getResponseCode();
                if (responseCode == 200) {
                    whenDone.value = url;
                }
                TaskManager.runTask(whenDone);
            } catch (IOException e) {
                e.printStackTrace();
                TaskManager.runTask(whenDone);
            }
        });
    }

    /**
     * Resets the biome if it was modified
     *
     * @param area
     * @param pos1
     * @param pos2
     * @return true if any changes were made
     */
    public static boolean resetBiome(PlotArea area, Location pos1, Location pos2) {
        BiomeType biome = area.getPlotBiome();
        if (!Objects.equals(WorldUtil.IMP
            .getBiomeSynchronous(area.getWorldName(), (pos1.getX() + pos2.getX()) / 2,
                (pos1.getZ() + pos2.getZ()) / 2), biome)) {
            MainUtil
                .setBiome(area.getWorldName(), pos1.getX(), pos1.getZ(), pos2.getX(), pos2.getZ(),
                    biome);
            return true;
        }
        return false;
    }

    public static String secToTime(long time) {
        StringBuilder toreturn = new StringBuilder();
        if (time >= 33868800) {
            int years = (int) (time / 33868800);
            time -= years * 33868800;
            toreturn.append(years + "y ");
        }
        if (time >= 604800) {
            int weeks = (int) (time / 604800);
            time -= weeks * 604800;
            toreturn.append(weeks + "w ");
        }
        if (time >= 86400) {
            int days = (int) (time / 86400);
            time -= days * 86400;
            toreturn.append(days + "d ");
        }
        if (time >= 3600) {
            int hours = (int) (time / 3600);
            time -= hours * 3600;
            toreturn.append(hours + "h ");
        }
        if (time >= 60) {
            int minutes = (int) (time / 60);
            time -= minutes * 60;
            toreturn.append(minutes + "m ");
        }
        if (toreturn.equals("") || time > 0) {
            toreturn.append((time) + "s ");
        }
        return toreturn.toString().trim();
    }

    public static long timeToSec(String string) {
        if (MathMan.isInteger(string)) {
            return Long.parseLong(string);
        }
        string = string.toLowerCase().trim().toLowerCase();
        if (string.equalsIgnoreCase("false")) {
            return 0;
        }
        String[] split = string.split(" ");
        long time = 0;
        for (String value : split) {
            int nums = Integer.parseInt(value.replaceAll("[^\\d]", ""));
            String letters = value.replaceAll("[^a-z]", "");
            switch (letters) {
                case "week":
                case "weeks":
                case "wks":
                case "w":

                    time += 604800 * nums;
                case "days":
                case "day":
                case "d":
                    time += 86400 * nums;
                case "hour":
                case "hr":
                case "hrs":
                case "hours":
                case "h":
                    time += 3600 * nums;
                case "minutes":
                case "minute":
                case "mins":
                case "min":
                case "m":
                    time += 60 * nums;
                case "seconds":
                case "second":
                case "secs":
                case "sec":
                case "s":
                    time += nums;
            }
        }
        return time;
    }

    /**
     * Hashcode of a boolean array.<br>
     * - Used for traversing mega plots quickly.
     *
     * @param array
     * @return hashcode
     */
    public static int hash(boolean[] array) {
        if (array.length == 4) {
            if (!array[0] && !array[1] && !array[2] && !array[3]) {
                return 0;
            }
            return ((array[0] ? 1 : 0) << 3) + ((array[1] ? 1 : 0) << 2) + ((array[2] ? 1 : 0) << 1)
                + (array[3] ? 1 : 0);
        }
        int n = 0;
        for (boolean anArray : array) {
            n = (n << 1) + (anArray ? 1 : 0);
        }
        return n;
    }

    /**
     * Get a list of plot ids within a selection.
     *
     * @param pos1
     * @param pos2
     * @return
     */
    public static ArrayList<PlotId> getPlotSelectionIds(PlotId pos1, PlotId pos2) {
        ArrayList<PlotId> myPlots = new ArrayList<>();
        for (int x = pos1.x; x <= pos2.x; x++) {
            for (int y = pos1.y; y <= pos2.y; y++) {
                myPlots.add(new PlotId(x, y));
            }
        }
        return myPlots;
    }

    /**
     * Get the name from a UUID.
     *
     * @param owner
     * @return The player's name, None, Everyone or Unknown
     */
    @NotNull public static String getName(UUID owner) {
        if (owner == null) {
            return Captions.NONE.getTranslated();
        }
        if (owner.equals(DBFunc.EVERYONE)) {
            return Captions.EVERYONE.getTranslated();
        }
        if (owner.equals(DBFunc.SERVER)) {
            return Captions.SERVER.getTranslated();
        }
        String name = PlotSquared.get().getImpromptuUUIDPipeline().getSingle(owner, Settings.UUID.BLOCKING_TIMEOUT);
        if (name == null) {
            return Captions.UNKNOWN.getTranslated();
        }
        return name;
    }

    public static boolean isServerOwned(Plot plot) {
        return plot.getFlag(ServerPlotFlag.class);
    }

    @NotNull public static Location[] getCorners(@NotNull final String world, @NotNull final CuboidRegion region) {
        final BlockVector3 min = region.getMinimumPoint();
        final BlockVector3 max = region.getMaximumPoint();
        return new Location[] {Location.at(world, min), Location.at(world, max)};
    }

    /**
     * Get the corner locations for a list of regions.
     *
     * @param world
     * @param regions
     * @return
     * @see Plot#getCorners()
     */
    @NotNull public static Location[] getCorners(String world, Collection<CuboidRegion> regions) {
        Location min = null;
        Location max = null;
        for (CuboidRegion region : regions) {
            Location[] corners = getCorners(world, region);
            if (min == null) {
                min = corners[0];
                max = corners[1];
                continue;
            }
            Location pos1 = corners[0];
            Location pos2 = corners[1];
            if (pos2.getX() > max.getX()) {
                max = max.withX(pos2.getX());
            }
            if (pos1.getX() < min.getX()) {
                min = min.withX(pos1.getX());
            }
            if (pos2.getZ() > max.getZ()) {
                max = max.withZ(pos2.getZ());
            }
            if (pos1.getZ() < min.getZ()) {
                min = min.withZ(pos1.getZ());
            }
        }
        return new Location[] {min, max};
    }

    /**
     * Fuzzy plot search with spaces separating terms.
     * - Terms: type, alias, world, owner, trusted, member
     *
     * @param search
     * @return
     */
    public static List<Plot> getPlotsBySearch(String search) {
        String[] split = search.split(" ");
        int size = split.length * 2;

        List<UUID> uuids = new ArrayList<>();
        PlotId id = null;

        for (String term : split) {
            try {
                UUID uuid = PlotSquared.get().getImpromptuUUIDPipeline().getSingle(term, Settings.UUID.BLOCKING_TIMEOUT);
                if (uuid == null) {
                    uuid = UUID.fromString(term);
                }
                uuids.add(uuid);
            } catch (Exception ignored) {
                id = PlotId.fromString(term);
            }
        }

        ArrayList<ArrayList<Plot>> plotList =
            IntStream.range(0, size).mapToObj(i -> new ArrayList<Plot>())
                .collect(Collectors.toCollection(() -> new ArrayList<>(size)));

        PlotArea area = null;
        String alias = null;
        for (Plot plot : PlotQuery.newQuery().allPlots().asList()) {
            int count = 0;
            if (!uuids.isEmpty()) {
                for (UUID uuid : uuids) {
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

        List<Plot> plots = new ArrayList<>();
        for (int i = plotList.size() - 1; i >= 0; i--) {
            if (!plotList.get(i).isEmpty()) {
                plots.addAll(plotList.get(i));
            }
        }
        return plots;
    }

    /**
     * Get the plot from a string.
     *
     * @param player  Provides a context for what world to search in. Prefixing the term with 'world_name;' will override this context.
     * @param arg     The search term
     * @param message If a message should be sent to the player if a plot cannot be found
     * @return The plot if only 1 result is found, or null
     */
    @Nullable public static Plot getPlotFromString(PlotPlayer<?> player, String arg, boolean message) {
        if (arg == null) {
            if (player == null) {
                if (message) {
                    PlotSquared.log(Captions.NOT_VALID_PLOT_WORLD);
                }
                return null;
            }
            return player.getCurrentPlot();
        }
        PlotArea area;
        if (player != null) {
            area = PlotSquared.get().getPlotAreaManager().getPlotAreaByString(arg);
            if (area == null) {
                area = player.getApplicablePlotArea();
            }
        } else {
            area = ConsolePlayer.getConsole().getApplicablePlotArea();
        }
        String[] split = arg.split(";|,");
        PlotId id;
        if (split.length == 4) {
            area = PlotSquared.get().getPlotAreaManager().getPlotAreaByString(split[0] + ';' + split[1]);
            id = PlotId.fromString(split[2] + ';' + split[3]);
        } else if (split.length == 3) {
            area = PlotSquared.get().getPlotAreaManager().getPlotAreaByString(split[0]);
            id = PlotId.fromString(split[1] + ';' + split[2]);
        } else if (split.length == 2) {
            id = PlotId.fromString(arg);
        } else {
            Collection<Plot> plots;
            if (area == null) {
                plots = PlotQuery.newQuery().allPlots().asList();
            } else {
                plots = area.getPlots();
            }
            for (Plot p : plots) {
                String name = p.getAlias();
                if (!name.isEmpty() && name.equalsIgnoreCase(arg)) {
                    return p;
                }
            }
            if (message) {
                MainUtil.sendMessage(player, Captions.NOT_VALID_PLOT_ID);
            }
            return null;
        }
        if (area == null) {
            if (message) {
                MainUtil.sendMessage(player, Captions.NOT_VALID_PLOT_WORLD);
            }
            return null;
        }
        return area.getPlotAbs(id);
    }

    public static File getFile(File base, String path) {
        if (Paths.get(path).isAbsolute()) {
            return new File(path);
        }
        return new File(base, path);
    }

    /**
     * Synchronously set the biome in a selection.
     *
     * @param world
     * @param p1x
     * @param p1z
     * @param p2x
     * @param p2z
     * @param biome
     */
    public static void setBiome(String world, int p1x, int p1z, int p2x, int p2z, BiomeType biome) {
        BlockVector3 pos1 = BlockVector2.at(p1x, p1z).toBlockVector3();
        BlockVector3 pos2 = BlockVector2.at(p2x, p2z).toBlockVector3(Plot.MAX_HEIGHT - 1);
        CuboidRegion region = new CuboidRegion(pos1, pos2);
        WorldUtil.IMP.setBiomes(world, region, biome);
    }

    /**
     * Get the highest block at a location.
     */
    public static void getHighestBlock(String world, int x, int z, IntConsumer result) {
        WorldUtil.IMP.getHighestBlock(world, x, z, highest -> {
            if (highest == 0) {
                result.accept(63);
            } else {
                result.accept(highest);
            }
        });
    }

    /**
     * Send a message to the player.
     *
     * @param player  Player to receive message
     * @param message Message to send
     * @return true Can be used in things such as commands (return PlayerFunctions.sendMessage(...))
     */
    public static boolean sendMessage(PlotPlayer player, String message) {
        return sendMessage(player, message, true);
    }

    /**
     * Send a message to console.
     *
     * @param caption
     * @param args
     */
    public static void sendConsoleMessage(Captions caption, String... args) {
        sendMessage(null, caption, args);
    }

    /**
     * Send a message to a player.
     *
     * @param player Can be null to represent console, or use ConsolePlayer.getConsole()
     * @param msg
     * @param prefix If the message should be prefixed with the configured prefix
     * @return
     */
    public static boolean sendMessage(PlotPlayer player, @NotNull String msg, boolean prefix) {
        if (!msg.isEmpty()) {
            if (player == null) {
                String message = CaptionUtility
                    .format(null, (prefix ? Captions.PREFIX.getTranslated() : "") + msg);
                PlotSquared.log(message);
            } else {
                player.sendMessage(CaptionUtility.format(player,
                    (prefix ? Captions.PREFIX.getTranslated() : "") + Captions.color(msg)));
            }
        }
        return true;
    }

    /**
     * Send a message to the player.
     *
     * @param player  the recipient of the message
     * @param caption the message to send
     * @return boolean success
     */
    public static boolean sendMessage(PlotPlayer player, Caption caption, String... args) {
        return sendMessage(player, caption, (Object[]) args);
    }

    /**
     * Send a message to the player
     *
     * @param player  the recipient of the message
     * @param caption the message to send
     * @return boolean success
     */
    public static boolean sendMessage(final PlotPlayer player, final Caption caption,
        final Object... args) {
        if (caption.getTranslated().isEmpty()) {
            return true;
        }
        TaskManager.runTaskAsync(() -> {
            String m = CaptionUtility.format(player, caption, args);
            if (player == null) {
                PlotSquared.log(m);
            } else {
                player.sendMessage(m);
            }
        });
        return true;
    }

    /**
     * If rating categories are enabled, get the average rating by category.<br>
     * - The index corresponds to the index of the category in the config
     *
     * @param plot
     * @return
     */
    public static double[] getAverageRatings(Plot plot) {
        Map<UUID, Integer> rating;
        if (plot.getSettings().getRatings() != null) {
            rating = plot.getSettings().getRatings();
        } else if (Settings.Enabled_Components.RATING_CACHE) {
            rating = new HashMap<>();
        } else {
            rating = DBFunc.getRatings(plot);
        }
        int size = 1;
        if (!Settings.Ratings.CATEGORIES.isEmpty()) {
            size = Math.max(1, Settings.Ratings.CATEGORIES.size());
        }
        double[] ratings = new double[size];
        if (rating == null || rating.isEmpty()) {
            return ratings;
        }
        for (Entry<UUID, Integer> entry : rating.entrySet()) {
            int current = entry.getValue();
            if (Settings.Ratings.CATEGORIES.isEmpty()) {
                ratings[0] += current;
            } else {
                for (int i = 0; i < Settings.Ratings.CATEGORIES.size(); i++) {
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

    public static void getUUIDsFromString(final String list, final BiConsumer<Collection<UUID>, Throwable> consumer) {
        String[] split = list.split(",");

        final Set<UUID> result = new HashSet<>();
        final List<String> request = new LinkedList<>();

        for (final String name : split) {
            if (name.isEmpty()) {
                consumer.accept(Collections.emptySet(), null);
                return;
            } else if ("*".equals(name)) {
                result.add(DBFunc.EVERYONE);
            } else if (name.length() > 16) {
                try {
                    result.add(UUID.fromString(name));
                } catch (IllegalArgumentException ignored) {
                    consumer.accept(Collections.emptySet(), null);
                    return;
                }
            } else {
                request.add(name);
            }
        }

        if (request.isEmpty()) {
            consumer.accept(result, null);
        } else {
            PlotSquared.get().getImpromptuUUIDPipeline().getUUIDs(request, Settings.UUID.NON_BLOCKING_TIMEOUT)
                .whenComplete((uuids, throwable) -> {
                if (throwable != null) {
                    consumer.accept(null, throwable);
                } else {
                    for (final UUIDMapping uuid : uuids) {
                        result.add(uuid.getUuid());
                    }
                    consumer.accept(result, null);
                }
            });
        }
    }

    /**
     * Format a string with plot information.
     *
     * @param iInfo
     * @param plot
     * @param player
     * @param full
     * @param whenDone
     */
    public static void format(final String iInfo, final Plot plot, PlotPlayer player,
        final boolean full, final RunnableVal<String> whenDone) {
        int num = plot.getConnectedPlots().size();
        String alias = !plot.getAlias().isEmpty() ? plot.getAlias() : Captions.NONE.getTranslated();
        Location bot = plot.getCorners()[0];
        WorldUtil.IMP.getBiome(plot.getWorldName(), bot.getX(), bot.getZ(), biome -> {
            String info = iInfo;
            String trusted = getPlayerList(plot.getTrusted());
            String members = getPlayerList(plot.getMembers());
            String denied = getPlayerList(plot.getDenied());
            String seen;
            if (Settings.Enabled_Components.PLOT_EXPIRY && ExpireManager.IMP != null) {
                if (plot.isOnline()) {
                    seen = Captions.NOW.getTranslated();
                } else {
                    int time = (int) (ExpireManager.IMP.getAge(plot) / 1000);
                    if (time != 0) {
                        seen = MainUtil.secToTime(time);
                    } else {
                        seen = Captions.UNKNOWN.getTranslated();
                    }
                }
            } else {
                seen = Captions.NEVER.getTranslated();
            }

            String description = plot.getFlag(DescriptionFlag.class);
            if (description.isEmpty()) {
                description = Captions.PLOT_NO_DESCRIPTION.getTranslated();
            }

            StringBuilder flags = new StringBuilder();
            Collection<PlotFlag<?, ?>> flagCollection = plot.getApplicableFlags(true);
            if (flagCollection.isEmpty()) {
                flags.append(Captions.NONE.getTranslated());
            } else {
                String prefix = " ";
                for (final PlotFlag<?, ?> flag : flagCollection) {
                    Object value;
                    if (flag instanceof DoubleFlag && !Settings.General.SCIENTIFIC) {
                        value = FLAG_DECIMAL_FORMAT.format(flag.getValue());
                    } else {
                        value = flag.toString();
                    }
                    flags.append(prefix).append(CaptionUtility
                        .format(player, Captions.PLOT_FLAG_LIST.getTranslated(), flag.getName(),
                            CaptionUtility.formatRaw(player, value.toString(), "")));
                    prefix = ", ";
                }
            }
            boolean build = plot.isAdded(player.getUUID());
            String owner = plot.getOwners().isEmpty() ? "unowned" : getPlayerList(plot.getOwners());
            if (plot.getArea() != null) {
                info = info.replace("%area%",
                    plot.getArea().getWorldName() + (plot.getArea().getId() == null ?
                        "" :
                        "(" + plot.getArea().getId() + ")"));
            } else {
                info = info.replace("%area%", Captions.NONE.getTranslated());
            }
            info = info.replace("%id%", plot.getId().toString());
            info = info.replace("%alias%", alias);
            info = info.replace("%num%", String.valueOf(num));
            info = info.replace("%desc%", description);
            info = info.replace("%biome%", biome.toString().toLowerCase());
            info = info.replace("%owner%", owner);
            info = info.replace("%members%", members);
            info = info.replace("%player%", player.getName());
            info = info.replace("%trusted%", trusted);
            info = info.replace("%helpers%", members);
            info = info.replace("%denied%", denied);
            info = info.replace("%seen%", seen);
            info = info.replace("%flags%", flags);
            info = info.replace("%build%", String.valueOf(build));
            if (info.contains("%rating%")) {
                final String newInfo = info;
                TaskManager.runTaskAsync(() -> {
                    String info1;
                    if (Settings.Ratings.USE_LIKES) {
                        info1 = newInfo.replaceAll("%rating%",
                            String.format("%.0f%%", Like.getLikesPercentage(plot) * 100D));
                    } else {
                        int max = 10;
                        if (Settings.Ratings.CATEGORIES != null && !Settings.Ratings.CATEGORIES
                            .isEmpty()) {
                            max = 8;
                        }
                        if (full && Settings.Ratings.CATEGORIES != null
                            && Settings.Ratings.CATEGORIES.size() > 1) {
                            double[] ratings = MainUtil.getAverageRatings(plot);
                            String rating = "";
                            String prefix = "";
                            for (int i = 0; i < ratings.length; i++) {
                                rating += prefix + Settings.Ratings.CATEGORIES.get(i) + '=' + String
                                    .format("%.1f", ratings[i]);
                                prefix = ",";
                            }
                            info1 = newInfo.replaceAll("%rating%", rating);
                        } else {
                            info1 = newInfo.replaceAll("%rating%",
                                String.format("%.1f", plot.getAverageRating()) + '/' + max);
                        }
                    }
                    whenDone.run(info1);
                });
                return;
            }
            whenDone.run(info);
        });
    }

    public static boolean deleteDirectory(File directory) {
        if (directory.exists()) {
            File[] files = directory.listFiles();
            if (null != files) {
                for (File file : files) {
                    if (file.isDirectory()) {
                        deleteDirectory(file);
                    } else {
                        PlotSquared.debug("Deleting file: " + file + " | " + file.delete());
                    }
                }
            }
        }
        return (directory.delete());
    }

    /*
    @NotNull public static String getName(UUID owner) {
        if (owner == null) {
            return Captions.NONE.getTranslated();
        }
        if (owner.equals(DBFunc.EVERYONE)) {
            return Captions.EVERYONE.getTranslated();
        }
        if (owner.equals(DBFunc.SERVER)) {
            return Captions.SERVER.getTranslated();
        }
        String name = PlotSquared.get().getImpromptuUUIDPipeline().getSingle(owner, Settings.UUID.BLOCKING_TIMEOUT);
        if (name == null) {
            return Captions.UNKNOWN.getTranslated();
        }
        return name;
    }
     */

    /**
     * Get a list of names given a list of UUIDs.
     * - Uses the format {@link Captions#PLOT_USER_LIST} for the returned string
     */
    public static String getPlayerList(final Collection<UUID> uuids) {
        if (uuids.size() < 1) {
            return Captions.NONE.getTranslated();
        }

        final List<UUID> players = new LinkedList<>();
        final List<String> users = new LinkedList<>();
        for (final UUID uuid : uuids) {
            if (uuid == null) {
                users.add(Captions.NONE.getTranslated());
            } else if (DBFunc.EVERYONE.equals(uuid)) {
                users.add(Captions.EVERYONE.getTranslated());
            } else if (DBFunc.SERVER.equals(uuid)) {
                users.add(Captions.SERVER.getTranslated());
            } else {
                players.add(uuid);
            }
        }

        try {
            for (final UUIDMapping mapping : PlotSquared.get().getImpromptuUUIDPipeline().getNames(players).get(Settings.UUID.BLOCKING_TIMEOUT,
                TimeUnit.MILLISECONDS)) {
                users.add(mapping.getUsername());
            }
        } catch (final Exception e) {
            e.printStackTrace();
        }

        String c = Captions.PLOT_USER_LIST.getTranslated();
        StringBuilder list = new StringBuilder();
        for (int x = 0; x < users.size(); x++) {
            if (x + 1 == uuids.size()) {
                list.append(c.replace("%user%", users.get(x)).replace(",", ""));
            } else {
                list.append(c.replace("%user%", users.get(x)));
            }
        }
        return list.toString();
    }

    public static void getPersistentMeta(UUID uuid, final String key,
        final RunnableVal<byte[]> result) {
        PlotPlayer player = PlotSquared.platform().getPlayerManager().getPlayerIfExists(uuid);
        if (player != null) {
            result.run(player.getPersistentMeta(key));
        } else {
            DBFunc.getPersistentMeta(uuid, new RunnableVal<Map<String, byte[]>>() {
                @Override public void run(Map<String, byte[]> value) {
                    result.run(value.get(key));
                }
            });
        }
    }

    private static <T> T getValueFromConfig(ConfigurationSection config, String path,
        IntFunction<Optional<T>> intParser, Function<String, Optional<T>> textualParser,
        Supplier<T> defaultValue) {
        String value = config.getString(path);
        if (value == null) {
            return defaultValue.get();
        }
        if (MathMan.isInteger(value)) {
            return intParser.apply(Integer.parseInt(value)).orElseGet(defaultValue);
        }
        return textualParser.apply(value).orElseGet(defaultValue);
    }

    public static PlotAreaType getType(ConfigurationSection config) {
        return getValueFromConfig(config, "generator.type", PlotAreaType::fromLegacyInt,
            PlotAreaType::fromString, () -> PlotAreaType.NORMAL);
    }

    public static PlotAreaTerrainType getTerrain(ConfigurationSection config) {
        return getValueFromConfig(config, "generator.terrain", PlotAreaTerrainType::fromLegacyInt,
            PlotAreaTerrainType::fromString, () -> PlotAreaTerrainType.NONE);
    }
}
