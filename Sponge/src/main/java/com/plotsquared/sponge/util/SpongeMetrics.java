package com.plotsquared.sponge.util;

/*
 * Copyright 2011-2013 Tyler Blair. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are
 * permitted provided that the following conditions are met:
 *
 *    1. Redistributions of source code must retain the above copyright notice, this list of
 *       conditions and the following disclaimer.
 *
 *    2. Redistributions in binary form must reproduce the above copyright notice, this list
 *       of conditions and the following disclaimer in the documentation and/or other materials
 *       provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ''AS IS'' AND ANY EXPRESS OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE AUTHOR OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * The views and conclusions contained in the software and documentation are those of the
 * authors and contributors and should not be interpreted as representing official policies,
 * either expressed or implied, of anybody else.
 */

import com.google.inject.Inject;
import com.intellectualcrafters.plot.PS;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.Proxy;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.zip.GZIPOutputStream;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import org.spongepowered.api.Game;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.scheduler.Task;

public class SpongeMetrics {

    /**
     * The current revision number
     */
    private final static int REVISION = 7;

    /**
     * The base url of the metrics domain
     */
    private static final String BASE_URL = "http://report.mcstats.org";

    /**
     * The url used to report a server's status
     */
    private static final String REPORT_URL = "/plugin/%s";

    /**
     * Interval of time to ping (in minutes)
     */
    private static final int PING_INTERVAL = 15;

    /**
     * The game data is being sent for
     */
    private final Game game;

    /**
     * The plugin this metrics submits for
     */
    private final PluginContainer plugin;
    /**
     * Lock for synchronization
     */
    private final Object optOutLock = new Object();
    /**
     * The plugin configuration file
     */
    private CommentedConfigurationNode config;
    /**
     * The configuration loader
     */
    private ConfigurationLoader<CommentedConfigurationNode> configurationLoader;
    /**
     * The plugin configuration file
     */
    private File configurationFile;
    /**
     * Unique server id
     */
    private String guid;
    /**
     * Debug mode
     */
    private boolean debug;
    /**
     * The scheduled task
     */
    private volatile Task task = null;

    @Inject
    public SpongeMetrics(final Game game, final PluginContainer plugin) throws IOException {
        if (plugin == null) {
            throw new IllegalArgumentException("Plugin cannot be null");
        }

        this.game = game;
        this.plugin = plugin;

        loadConfiguration();
    }

    /**
     * GZip compress a string of bytes
     *
     * @param input
     * @return
     */
    public static byte[] gzip(final String input) {
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        GZIPOutputStream gzos = null;

        try {
            gzos = new GZIPOutputStream(baos);
            gzos.write(input.getBytes("UTF-8"));
        } catch (final IOException e) {
            e.printStackTrace();
        } finally {
            if (gzos != null) {
                try {
                    gzos.close();
                } catch (final IOException ignore) {
                }
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
     * @throws java.io.UnsupportedEncodingException
     */
    private static void appendJSONPair(final StringBuilder json, final String key, final String value) throws UnsupportedEncodingException {
        boolean isValueNumeric = false;

        try {
            if (value.equals("0") || !value.endsWith("0")) {
                Double.parseDouble(value);
                isValueNumeric = true;
            }
        } catch (final NumberFormatException e) {
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
     * @return
     */
    private static String escapeJSON(final String text) {
        final StringBuilder builder = new StringBuilder();

        builder.append('"');
        for (int index = 0; index < text.length(); index++) {
            final char chr = text.charAt(index);

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
                        final String t = "000" + Integer.toHexString(chr);
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
     * @return the encoded text, as UTF-8
     */
    private static String urlEncode(final String text) throws UnsupportedEncodingException {
        return URLEncoder.encode(text, "UTF-8");
    }

    /**
     * Loads the configuration
     */
    private void loadConfiguration() {
        configurationFile = getConfigFile();
        configurationLoader = HoconConfigurationLoader.builder().setFile(configurationFile).build();

        try {
            if (!configurationFile.exists()) {
                configurationFile.createNewFile();
                config = configurationLoader.load();

                config.setComment("This contains settings for MCStats: http://mcstats.org");
                config.getNode("mcstats.guid").setValue(UUID.randomUUID().toString());
                config.getNode("mcstats.opt-out").setValue(false);
                config.getNode("mcstats.debug").setValue(false);

                configurationLoader.save(config);
            } else {
                config = configurationLoader.load();
            }

            guid = config.getNode("mcstats.guid").getString();
            debug = config.getNode("mcstats.debug").getBoolean();
        } catch (final IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Start measuring statistics. This will immediately create an async repeating task as the plugin and send the
     * initial data to the metrics backend, and then after that it will post in increments of PING_INTERVAL * 1200
     * ticks.
     *
     * @return True if statistics measuring is running, otherwise false.
     */
    public boolean start() {
        synchronized (optOutLock) {
            // Did we opt out?
            if (isOptOut()) {
                return false;
            }

            // Is metrics already running?
            if (task != null) {
                return true;
            }

            // Begin hitting the server with glorious data
            final Task.Builder builder = game.getScheduler().createTaskBuilder();
            builder.async().interval(PING_INTERVAL, TimeUnit.MINUTES).execute(new Runnable() {

                private boolean firstPost = true;

                @Override
                public void run() {
                    try {
                        // This has to be synchronized or it can collide with the disable method.
                        synchronized (optOutLock) {
                            // Disable Task, if it is running and the server owner decided to opt-out
                            if (isOptOut() && (task != null)) {
                                task.cancel();
                                task = null;
                            }
                        }

                        // We use the inverse of firstPost because if it is the first time we are posting,
                        // it is not a interval ping, so it evaluates to FALSE
                        // Each time thereafter it will evaluate to TRUE, i.e PING!
                        postPlugin(!firstPost);

                        // After the first post we set firstPost to false
                        // Each post thereafter will be a ping
                        firstPost = false;
                    } catch (final IOException e) {
                        if (debug) {
                            PS.debug("[Metrics] " + e.getMessage());
                        }
                    }
                }
            });
            return true;
        }
    }

    /**
     * Has the server owner denied plugin metrics?
     *
     * @return true if metrics should be opted out of it
     */
    public boolean isOptOut() {
        synchronized (optOutLock) {
            loadConfiguration();

            return config.getNode("mcstats.opt-out").getBoolean();
        }
    }

    /**
     * Enables metrics for the server by setting "opt-out" to false in the config file and starting the metrics task.
     *
     * @throws java.io.IOException
     */
    public void enable() throws IOException {
        // This has to be synchronized or it can collide with the check in the task.
        synchronized (optOutLock) {
            // Check if the server owner has already set opt-out, if not, set it.
            if (isOptOut()) {
                config.getNode("mcstats.opt-out").setValue(false);
                configurationLoader.save(config);
            }

            // Enable Task, if it is not running
            if (task == null) {
                start();
            }
        }
    }

    /**
     * Disables metrics for the server by setting "opt-out" to true in the config file and canceling the metrics task.
     *
     * @throws java.io.IOException
     */
    public void disable() throws IOException {
        // This has to be synchronized or it can collide with the check in the task.
        synchronized (optOutLock) {
            // Check if the server owner has already set opt-out, if not, set it.
            if (!isOptOut()) {
                config.getNode("mcstats.opt-out").setValue(true);
                configurationLoader.save(config);
            }

            // Disable Task, if it is running
            if (task != null) {
                task.cancel();
                task = null;
            }
        }
    }

    /**
     * Gets the File object of the config file that should be used to store data such as the GUID and opt-out status
     *
     * @return the File object for the config file
     */
    public File getConfigFile() {
        // TODO configDir
        final File configFolder = new File("config");

        return new File(configFolder, "PluginMetrics.conf");
    }

    /**
     * Generic method that posts a plugin to the metrics website
     *
     */
    private void postPlugin(final boolean isPing) throws IOException {
        // Server software specific section
        final String pluginName = plugin.getName();
        final boolean onlineMode = game.getServer().getOnlineMode(); // TRUE if online mode is enabled
        final String pluginVersion = plugin.getVersion().get();
        // TODO no visible way to get MC version at the moment
        // TODO added by game.getPlatform().getMinecraftVersion() -- impl in 2.1
        final String serverVersion = String.format("%s %s", "Sponge", game.getPlatform().getMinecraftVersion());
        final int playersOnline = game.getServer().getOnlinePlayers().size();

        // END server software specific section -- all code below does not use any code outside of this class / Java

        // Construct the post data
        final StringBuilder json = new StringBuilder(1024);
        json.append('{');

        // The plugin's description file containg all of the plugin data such as name, version, author, etc
        appendJSONPair(json, "guid", guid);
        appendJSONPair(json, "plugin_version", pluginVersion);
        appendJSONPair(json, "server_version", serverVersion);
        appendJSONPair(json, "players_online", Integer.toString(playersOnline));

        // New data as of R6
        final String osname = System.getProperty("os.name");
        String osarch = System.getProperty("os.arch");
        final String osversion = System.getProperty("os.version");
        final String java_version = System.getProperty("java.version");
        final int coreCount = Runtime.getRuntime().availableProcessors();

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
        final URL url = new URL(BASE_URL + String.format(REPORT_URL, urlEncode(pluginName)));

        // Connect to the website
        URLConnection connection;

        // Mineshafter creates a socks proxy, so we can safely bypass it
        // It does not reroute POST requests so we need to go around it
        if (isMineshafterPresent()) {
            connection = url.openConnection(Proxy.NO_PROXY);
        } else {
            connection = url.openConnection();
        }

        final byte[] uncompressed = json.toString().getBytes();
        final byte[] compressed = gzip(json.toString());

        // Headers
        connection.addRequestProperty("User-Agent", "MCStats/" + REVISION);
        connection.addRequestProperty("Content-Type", "application/json");
        connection.addRequestProperty("Content-Encoding", "gzip");
        connection.addRequestProperty("Content-Length", Integer.toString(compressed.length));
        connection.addRequestProperty("Accept", "application/json");
        connection.addRequestProperty("Connection", "close");

        connection.setDoOutput(true);

        if (debug) {
            PS.debug("[Metrics] Prepared request for " + pluginName + " uncompressed=" + uncompressed.length + " compressed=" + compressed.length);
        }

        // Write the data
        final OutputStream os = connection.getOutputStream();
        os.write(compressed);
        os.flush();

        // Now read the response
        final BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        String response = reader.readLine();

        // close resources
        os.close();
        reader.close();

        if ((response == null) || response.startsWith("ERR") || response.startsWith("7")) {
            if (response == null) {
                response = "null";
            } else if (response.startsWith("7")) {
                response = response.substring(response.startsWith("7,") ? 2 : 1);
            }

            throw new IOException(response);
        }
    }

    /**
     * Check if mineshafter is present. If it is, we need to bypass it to send POST requests
     *
     * @return true if mineshafter is installed on the server
     */
    private boolean isMineshafterPresent() {
        try {
            Class.forName("mineshafter.MineServer");
            return true;
        } catch (final Exception e) {
            return false;
        }
    }

}