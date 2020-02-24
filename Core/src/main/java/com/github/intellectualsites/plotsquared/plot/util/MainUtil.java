package com.github.intellectualsites.plotsquared.plot.util;

import com.github.intellectualsites.plotsquared.plot.PlotSquared;
import com.github.intellectualsites.plotsquared.plot.commands.Like;
import com.github.intellectualsites.plotsquared.plot.config.Caption;
import com.github.intellectualsites.plotsquared.plot.config.CaptionUtility;
import com.github.intellectualsites.plotsquared.plot.config.Captions;
import com.github.intellectualsites.plotsquared.plot.config.Settings;
import com.github.intellectualsites.plotsquared.plot.database.DBFunc;
import com.github.intellectualsites.plotsquared.plot.flags.PlotFlag;
import com.github.intellectualsites.plotsquared.plot.flags.implementations.DescriptionFlag;
import com.github.intellectualsites.plotsquared.plot.flags.implementations.ServerPlotFlag;
import com.github.intellectualsites.plotsquared.plot.flags.types.DoubleFlag;
import com.github.intellectualsites.plotsquared.plot.object.ConsolePlayer;
import com.github.intellectualsites.plotsquared.plot.object.Location;
import com.github.intellectualsites.plotsquared.plot.object.Plot;
import com.github.intellectualsites.plotsquared.plot.object.PlotArea;
import com.github.intellectualsites.plotsquared.plot.object.PlotId;
import com.github.intellectualsites.plotsquared.plot.object.PlotPlayer;
import com.github.intellectualsites.plotsquared.plot.object.RunnableVal;
import com.github.intellectualsites.plotsquared.plot.object.stream.AbstractDelegateOutputStream;
import com.github.intellectualsites.plotsquared.plot.util.expiry.ExpireManager;
import com.sk89q.worldedit.math.BlockVector2;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.world.biome.BiomeType;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Reader;
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
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
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
     * If the NMS code for sending chunk updates is functional<br>
     * - E.g. If using an older version of Bukkit, or before the plugin is updated to 1.5<br>
     * - Slower fallback code will be used if not.<br>
     */
    public static boolean canSendChunk = false;
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

    public static void sendAdmin(final String s) {
        for (final PlotPlayer player : UUIDHandler.getPlayers().values()) {
            if (player.hasPermission(Captions.PERMISSION_ADMIN.getTranslated())) {
                player.sendMessage(Captions.color(s));
            }
        }
        PlotSquared.debug(s);
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
                try (Reader response = new InputStreamReader(con.getInputStream(),
                    StandardCharsets.UTF_8)) {
                    final char[] buffer = new char[256];
                    final StringBuilder result = new StringBuilder();
                    while (true) {
                        final int r = response.read(buffer);
                        if (r < 0) {
                            break;
                        }
                        result.append(buffer, 0, r);
                    }
                    if (!result.toString().startsWith("Success")) {
                        PlotSquared.debug(result);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
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
        BiomeType biome = area.PLOT_BIOME;
        if (!Objects.equals(WorldUtil.IMP.getBiome(area.worldname, (pos1.getX() + pos2.getX()) / 2,
            (pos1.getZ() + pos2.getZ()) / 2), biome)) {
            MainUtil.setBiome(area.worldname, pos1.getX(), pos1.getZ(), pos2.getX(), pos2.getZ(),
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
        String name = UUIDHandler.getName(owner);
        if (name == null) {
            return Captions.UNKNOWN.getTranslated();
        }
        return name;
    }

    public static boolean isServerOwned(Plot plot) {
        return plot.getFlag(ServerPlotFlag.class);
    }

    @NotNull public static Location[] getCorners(String world, CuboidRegion region) {
        BlockVector3 min = region.getMinimumPoint();
        BlockVector3 max = region.getMaximumPoint();
        Location pos1 = new Location(world, min.getX(), min.getY(), min.getZ());
        Location pos2 = new Location(world, max.getX(), max.getY(), max.getZ());
        return new Location[] {pos1, pos2};
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
                UUID uuid = UUIDHandler.getUUID(term, null);
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
        for (Plot plot : PlotSquared.get().getPlots()) {
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
    public static Plot getPlotFromString(PlotPlayer player, String arg, boolean message) {
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
            area = PlotSquared.get().getPlotAreaByString(arg);
            if (area == null) {
                area = player.getApplicablePlotArea();
            }
        } else {
            area = ConsolePlayer.getConsole().getApplicablePlotArea();
        }
        String[] split = arg.split(";|,");
        PlotId id;
        if (split.length == 4) {
            area = PlotSquared.get().getPlotAreaByString(split[0] + ';' + split[1]);
            id = PlotId.fromString(split[2] + ';' + split[3]);
        } else if (split.length == 3) {
            area = PlotSquared.get().getPlotAreaByString(split[0]);
            id = PlotId.fromString(split[1] + ';' + split[2]);
        } else if (split.length == 2) {
            id = PlotId.fromString(arg);
        } else {
            Collection<Plot> plots;
            if (area == null) {
                plots = PlotSquared.get().getPlots();
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
     *
     * @param world
     * @param x
     * @param z
     * @return
     */
    public static int getHeighestBlock(String world, int x, int z) {
        int result = WorldUtil.IMP.getHighestBlock(world, x, z);
        if (result == 0) {
            return 63;
        }
        return result;
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
                String message = CaptionUtility.format(null, (prefix ? Captions.PREFIX.getTranslated() : "") + msg);
                PlotSquared.log(message);
            } else {
                player.sendMessage(
                    CaptionUtility.format(player, (prefix ? Captions.PREFIX.getTranslated() : "") + Captions.color(msg)));
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

    public static Set<UUID> getUUIDsFromString(String list) {
        String[] split = list.split(",");
        HashSet<UUID> result = new HashSet<>();
        for (String name : split) {
            if (name.isEmpty()) {
                // Invalid
                return Collections.emptySet();
            }
            if ("*".equals(name)) {
                result.add(DBFunc.EVERYONE);
                continue;
            }
            if (name.length() > 16) {
                try {
                    result.add(UUID.fromString(name));
                    continue;
                } catch (IllegalArgumentException ignored) {
                    return Collections.emptySet();
                }
            }
            UUID uuid = UUIDHandler.getUUID(name, null);
            if (uuid == null) {
                return Collections.emptySet();
            }
            result.add(uuid);
        }
        return result;
    }

    /**
     * Format a string with plot information.
     *
     * @param info
     * @param plot
     * @param player
     * @param full
     * @param whenDone
     */
    public static void format(String info, final Plot plot, PlotPlayer player, final boolean full,
        final RunnableVal<String> whenDone) {
        int num = plot.getConnectedPlots().size();
        String alias = !plot.getAlias().isEmpty() ? plot.getAlias() : Captions.NONE.getTranslated();
        Location bot = plot.getCorners()[0];
        BiomeType biome = WorldUtil.IMP.getBiome(plot.getWorldName(), bot.getX(), bot.getZ());
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
                flags.append(prefix).append(CaptionUtility.format(player, Captions.PLOT_FLAG_LIST.getTranslated(),
                    flag.getName(), CaptionUtility.formatRaw(player, value.toString(), "")));
                prefix = ", ";
            }
        }
        boolean build = plot.isAdded(player.getUUID());
        String owner = plot.getOwners().isEmpty() ? "unowned" : getPlayerList(plot.getOwners());
        if (plot.getArea() != null) {
            info = info.replace("%area%", plot.getArea().worldname +
                (plot.getArea().id == null ? "" : "(" + plot.getArea().id + ")"));
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

    /**
     * Get a list of names given a list of uuids.<br>
     * - Uses the format {@link Captions#PLOT_USER_LIST} for the returned string
     *
     * @param uuids
     * @return
     */
    public static String getPlayerList(Collection<UUID> uuids) {
        ArrayList<UUID> l = new ArrayList<>(uuids);
        if (l.size() < 1) {
            return Captions.NONE.getTranslated();
        }
        List<String> users =
            l.stream().map(MainUtil::getName).sorted().collect(Collectors.toList());
        String c = Captions.PLOT_USER_LIST.getTranslated();
        StringBuilder list = new StringBuilder();
        for (int x = 0; x < users.size(); x++) {
            if (x + 1 == l.size()) {
                list.append(c.replace("%user%", users.get(x)).replace(",", ""));
            } else {
                list.append(c.replace("%user%", users.get(x)));
            }
        }
        return list.toString();
    }

    public static void getPersistentMeta(UUID uuid, final String key,
        final RunnableVal<byte[]> result) {
        PlotPlayer player = UUIDHandler.getPlayer(uuid);
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
}
