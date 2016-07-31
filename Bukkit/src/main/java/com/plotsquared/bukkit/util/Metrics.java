package com.plotsquared.bukkit.util;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.scheduler.BukkitTask;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.Collection;
import java.util.UUID;
import java.util.zip.GZIPOutputStream;

public class Metrics {

    /** The current revision number. */
    private static final int REVISION = 7;
    /** The base url of the metrics domain.*/
    private static final String BASE_URL = "http://report.mcstats.org";
    /** The url used to report a server's status. */
    private static final String REPORT_URL = "/plugin/%s";
    /** Interval of time to ping (in minutes). */
    private static final int PING_INTERVAL = 15;
    /** The plugin this metrics submits for. */
    private final Plugin plugin;
    /** Unique server id. */
    private final String guid;
    /** The scheduled task. */
    private volatile BukkitTask task = null;

    public Metrics(Plugin plugin) {
        this.plugin = plugin;
        this.guid = UUID.randomUUID().toString();
    }

    /**
     * GZip compress a string of bytes.
     *
     * @param input
     *
     * @return byte[] the file as a byte array
     */
    public static byte[] gzip(String input) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        GZIPOutputStream gzos = null;
        try {
            gzos = new GZIPOutputStream(baos);
            gzos.write(input.getBytes("UTF-8"));
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (gzos != null) {
                try {
                    gzos.close();
                } catch (IOException ignore) {}
            }
        }
        return baos.toByteArray();
    }

    /**
     * Appends a json encoded key/value pair to the given string builder.
     *
     * @param json
     * @param key
     * @param value
     *
     */
    private static void appendJSONPair(StringBuilder json, String key, String value) {
        boolean isValueNumeric = false;
        try {
            if (value.equals("0") || !value.endsWith("0")) {
                Double.parseDouble(value);
                isValueNumeric = true;
            }
        } catch (NumberFormatException ignored) {
            isValueNumeric = false;
        }
        if (json.charAt(json.length() - 1) != '{') {
            json.append(',');
        }
        json.append(escapeJSON(key));
        json.append(':');
        if (isValueNumeric) {
            json.append(value);
        } else {
            json.append(escapeJSON(value));
        }
    }

    /**
     * Escape a string to create a valid JSON string
     *
     * @param text
     *
     * @return String
     */
    private static String escapeJSON(String text) {
        StringBuilder builder = new StringBuilder();
        builder.append('"');
        for (int index = 0; index < text.length(); index++) {
            char chr = text.charAt(index);
            switch (chr) {
                case '"':
                case '\\':
                    builder.append('\\');
                    builder.append(chr);
                    break;
                case '\b':
                    builder.append("\\b");
                    break;
                case '\t':
                    builder.append("\\t");
                    break;
                case '\n':
                    builder.append("\\n");
                    break;
                case '\r':
                    builder.append("\\r");
                    break;
                default:
                    if (chr < ' ') {
                        String t = "000" + Integer.toHexString(chr);
                        builder.append("\\u" + t.substring(t.length() - 4));
                    } else {
                        builder.append(chr);
                    }
                    break;
            }
        }
        builder.append('"');
        return builder.toString();
    }

    /**
     * Encode text as UTF-8
     *
     * @param text the text to encode
     *
     * @return the encoded text, as UTF-8
     */
    private static String urlEncode(String text) throws UnsupportedEncodingException {
        return URLEncoder.encode(text, "UTF-8");
    }

    /**
     * Start measuring statistics. This will immediately create an async repeating task as the plugin and send the
     * initial data to the metrics backend, and then after that it will post in increments of PING_INTERVAL * 1200
     * ticks.
     *
     * @return True if statistics measuring is running, otherwise false.
     */
    public boolean start() {
        // Is metrics already running?
        if (this.task != null) {
            return true;
        }
        // Begin hitting the server with glorious data
        this.task = this.plugin.getServer().getScheduler().runTaskTimerAsynchronously(this.plugin, new Runnable() {
            private boolean firstPost = true;

            @Override
            public void run() {
                try {
                    postPlugin(!this.firstPost);
                    // After the first post we set firstPost to
                    // false
                    // Each post thereafter will be a ping
                    this.firstPost = false;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }, 0, PING_INTERVAL * 1200);
        return true;
    }

    /**
     * Generic method that posts a plugin to the metrics website.
     */
    private void postPlugin(boolean isPing) throws IOException {
        // Server software specific section
        PluginDescriptionFile description = this.plugin.getDescription();
        String pluginName = description.getName();
        boolean onlineMode = Bukkit.getServer().getOnlineMode(); // TRUE if online mode is enabled
        String pluginVersion = description.getVersion();
        String serverVersion = Bukkit.getVersion();
        int playersOnline = 0;
        try {
            if (Bukkit.class.getMethod("getOnlinePlayers", new Class<?>[0]).getReturnType() == Collection.class) {
                playersOnline = ((Collection<?>) Bukkit.class.getMethod("getOnlinePlayers", new Class<?>[0]).invoke(null)).size();
            } else {
                playersOnline = ((Player[]) Bukkit.class.getMethod("getOnlinePlayers", new Class<?>[0]).invoke(null)).length;
            }
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException ex) {
            ex.printStackTrace();
        }
        // END server software specific section -- all code below does not use
        // any code outside of this class / Java
        // Construct the post data
        StringBuilder json = new StringBuilder(1024);
        json.append('{');
        // The plugin's description file containing all of the plugin data such as name, version, author, etc
        appendJSONPair(json, "guid", this.guid);
        appendJSONPair(json, "plugin_version", pluginVersion);
        appendJSONPair(json, "server_version", serverVersion);
        appendJSONPair(json, "players_online", Integer.toString(playersOnline));
        // New data as of R6
        String osname = System.getProperty("os.name");
        String osarch = System.getProperty("os.arch");
        String osversion = System.getProperty("os.version");
        String java_version = System.getProperty("java.version");
        int coreCount = Runtime.getRuntime().availableProcessors();
        // normalize os arch .. amd64 -> x86_64
        if (osarch.equals("amd64")) {
            osarch = "x86_64";
        }
        appendJSONPair(json, "osname", osname);
        appendJSONPair(json, "osarch", osarch);
        appendJSONPair(json, "osversion", osversion);
        appendJSONPair(json, "cores", Integer.toString(coreCount));
        appendJSONPair(json, "auth_mode", onlineMode ? "1" : "0");
        appendJSONPair(json, "java_version", java_version);
        // If we're pinging, append it
        if (isPing) {
            appendJSONPair(json, "ping", "1");
        }
        // close json
        json.append('}');
        // Create the url
        URL url = new URL(BASE_URL + String.format(REPORT_URL, urlEncode(pluginName)));
        // Connect to the website
        URLConnection connection = url.openConnection();
        byte[] uncompressed = json.toString().getBytes();
        byte[] compressed = gzip(json.toString());
        // Headers
        connection.addRequestProperty("User-Agent", "MCStats/" + REVISION);
        connection.addRequestProperty("Content-Type", "application/json");
        connection.addRequestProperty("Content-Encoding", "gzip");
        connection.addRequestProperty("Content-Length", Integer.toString(compressed.length));
        connection.addRequestProperty("Accept", "application/json");
        connection.addRequestProperty("Connection", "close");
        connection.setDoOutput(true);
        try {
            try (OutputStream os = connection.getOutputStream()) {
                os.write(compressed);
                os.flush();
            }
            String response;
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                response = reader.readLine();
            }
            if (response == null || response.startsWith("ERR") || response.startsWith("7")) {
                if (response == null) {
                    response = "null";
                } else if (response.startsWith("7")) {
                    response = response.substring(response.startsWith("7,") ? 2 : 1);
                }
                throw new IOException(response);
            }
        } catch (IOException ignored) {}
    }
}