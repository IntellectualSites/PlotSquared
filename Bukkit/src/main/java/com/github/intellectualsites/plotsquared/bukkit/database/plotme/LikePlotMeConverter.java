package com.github.intellectualsites.plotsquared.bukkit.database.plotme;

import com.github.intellectualsites.plotsquared.bukkit.generator.BukkitPlotGenerator;
import com.github.intellectualsites.plotsquared.configuration.ConfigurationSection;
import com.github.intellectualsites.plotsquared.configuration.MemorySection;
import com.github.intellectualsites.plotsquared.configuration.file.FileConfiguration;
import com.github.intellectualsites.plotsquared.configuration.file.YamlConfiguration;
import com.github.intellectualsites.plotsquared.plot.PlotSquared;
import com.github.intellectualsites.plotsquared.plot.config.Settings;
import com.github.intellectualsites.plotsquared.plot.database.DBFunc;
import com.github.intellectualsites.plotsquared.plot.object.Plot;
import com.github.intellectualsites.plotsquared.plot.object.PlotArea;
import com.github.intellectualsites.plotsquared.plot.object.PlotId;
import com.github.intellectualsites.plotsquared.plot.util.TaskManager;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.command.CommandException;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicBoolean;

public class LikePlotMeConverter {

    private final String plugin;

    public LikePlotMeConverter(String plugin) {
        this.plugin = plugin;
    }

    public static String getWorld(String world) {
        for (World newWorld : Bukkit.getWorlds()) {
            if (newWorld.getName().equalsIgnoreCase(world)) {
                return newWorld.getName();
            }
        }
        return world;
    }

    private void sendMessage(String message) {
        PlotSquared.debug("&3PlotMe&8->&3" + PlotSquared.imp().getPluginName() + "&8: &7" + message);
    }

    public String getPlotMePath() {
        return new File(".").getAbsolutePath() + File.separator + "plugins" + File.separator
            + plugin + File.separator;
    }

    public FileConfiguration getPlotMeConfig(String dataFolder) {
        File plotMeFile = new File(dataFolder + "config.yml");
        if (!plotMeFile.exists()) {
            return null;
        }
        return YamlConfiguration.loadConfiguration(plotMeFile);
    }

    public Set<String> getPlotMeWorlds(FileConfiguration plotConfig) {
        return plotConfig.getConfigurationSection("worlds").getKeys(false);
    }

    public void mergeWorldYml(FileConfiguration plotConfig) {
        try {
            File genConfig = new File(
                "plugins" + File.separator + plugin + File.separator + "PlotMe-DefaultGenerator"
                    + File.separator + "config.yml");
            if (genConfig.exists()) {
                YamlConfiguration yml = YamlConfiguration.loadConfiguration(genConfig);
                for (String key : yml.getKeys(true)) {
                    if (!plotConfig.contains(key)) {
                        Object value = yml.get(key);
                        if (!(value instanceof MemorySection)) {
                            plotConfig.set(key, value);
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void updateWorldYml(String location) {
        try {
            Path path = Paths.get(location);
            File file = new File(location);
            if (!file.exists()) {
                return;
            }
            String content = new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
            String pluginName = PlotSquared.imp().getPluginName();
            content = content.replace("PlotMe-DefaultGenerator", pluginName);
            content = content.replace("PlotMe", pluginName);
            content = content.replace("AthionPlots", pluginName);
            content = content.replace("PlotZWorld", pluginName);
            Files.write(path, content.getBytes(StandardCharsets.UTF_8));
        } catch (IOException ignored) {
        }
    }

    private void copyConfig(ConfigurationSection plotmeDgYml, String world) throws IOException {
        String actualWorldName = getWorld(world);
        String plotMeWorldName = world.toLowerCase();
        Integer pathWidth = plotmeDgYml.getInt("worlds." + plotMeWorldName + ".PathWidth"); //
        PlotSquared.get().worlds.set("worlds." + world + ".road.width", pathWidth);
        int height = plotmeDgYml.getInt("worlds." + plotMeWorldName + ".RoadHeight",
            plotmeDgYml.getInt("worlds." + plotMeWorldName + ".GroundHeight", 64)); //
        PlotSquared.get().worlds.set("worlds." + world + ".road.height", height);
        PlotSquared.get().worlds.set("worlds." + world + ".wall.height", height);
        PlotSquared.get().worlds.set("worlds." + world + ".plot.height", height);
        int plotSize = plotmeDgYml.getInt("worlds." + plotMeWorldName + ".PlotSize", 32); //
        PlotSquared.get().worlds.set("worlds." + world + ".plot.size", plotSize);
        String wallblock = plotmeDgYml.getString("worlds." + plotMeWorldName + ".UnclaimedBorder",
            plotmeDgYml.getString("worlds." + plotMeWorldName + ".WallBlock", "44")); //
        PlotSquared.get().worlds.set("worlds." + world + ".wall.block", wallblock);
        String claimed =
            plotmeDgYml.getString("worlds." + plotMeWorldName + ".ProtectedWallBlock", "44:1"); //
        PlotSquared.get().worlds.set("worlds." + world + ".wall.block_claimed", claimed);
        String floor =
            plotmeDgYml.getString("worlds." + plotMeWorldName + ".PlotFloorBlock", "2"); //
        PlotSquared.get().worlds.set("worlds." + world + ".plot.floor", Collections.singletonList(floor));
        String filling = plotmeDgYml.getString("worlds." + plotMeWorldName + ".FillBlock", "3"); //
        PlotSquared.get().worlds
            .set("worlds." + world + ".plot.filling", Collections.singletonList(filling));
        String road = plotmeDgYml.getString("worlds." + plotMeWorldName + ".RoadMainBlock", "5");
        PlotSquared.get().worlds.set("worlds." + world + ".road.block", road);
        PlotSquared.get().worlds.set("worlds." + actualWorldName + ".road.height", height);
        PlotSquared.get().worlds.set("worlds." + actualWorldName + ".plot.height", height);
        PlotSquared.get().worlds.set("worlds." + actualWorldName + ".wall.height", height);
        PlotSquared.get().worlds.save(PlotSquared.get().worldsFile);
    }

    public boolean run(APlotMeConnector connector) {
        try {
            String dataFolder = getPlotMePath();
            FileConfiguration plotConfig = getPlotMeConfig(dataFolder);
            if (plotConfig == null) {
                return false;
            }

            String version = plotConfig.getString("Version");
            if (version == null) {
                version = plotConfig.getString("version");
            }
            if (!connector.accepts(version)) {
                return false;
            }

            PlotSquared.debug("&3Using connector: " + connector.getClass().getCanonicalName());

            Connection connection = connector.getPlotMeConnection(plugin, plotConfig, dataFolder);

            if (!connector.isValidConnection(connection)) {
                sendMessage("Cannot connect to PlotMe DB. Conversion process will not continue");
                return false;
            }

            sendMessage(
                "PlotMe conversion has started. To disable this, please set 'enabled-components -> plotme-converter' to false in the 'settings.yml'");

            mergeWorldYml(plotConfig);

            sendMessage("Connecting to PlotMe DB");

            ArrayList<Plot> createdPlots = new ArrayList<>();

            sendMessage("Collecting plot data");

            String dbPrefix = "PlotMe".toLowerCase();
            sendMessage(" - " + dbPrefix + "Plots");
            final Set<String> worlds = getPlotMeWorlds(plotConfig);

            if (Settings.Enabled_Components.PLOTME_CONVERTER) {
                sendMessage("Updating bukkit.yml");
                updateWorldYml("bukkit.yml");
                updateWorldYml("plugins/Multiverse-Core/worlds.yml");
                for (String world : plotConfig.getConfigurationSection("worlds").getKeys(false)) {
                    sendMessage("Copying config for: " + world);
                    try {
                        String actualWorldName = getWorld(world);
                        connector.copyConfig(plotConfig, world, actualWorldName);
                        PlotSquared.get().worlds.save(PlotSquared.get().worldsFile);
                    } catch (IOException e) {
                        e.printStackTrace();
                        sendMessage("&c-- &lFailed to save configuration for world '" + world
                            + "'\nThis will need to be done using the setup command, or manually");
                    }
                }
            }
            HashMap<String, HashMap<PlotId, Plot>> plots = connector.getPlotMePlots(connection);
            int plotCount = 0;
            for (Entry<String, HashMap<PlotId, Plot>> entry : plots.entrySet()) {
                plotCount += entry.getValue().size();
            }
            if (!Settings.Enabled_Components.PLOTME_CONVERTER) {
                return false;
            }

            sendMessage(" - " + dbPrefix + "Allowed");

            sendMessage("Collected " + plotCount + " plots from PlotMe");
            File plotmeDgFile = new File(
                dataFolder + File.separator + "PlotMe-DefaultGenerator" + File.separator
                    + "config.yml");
            if (plotmeDgFile.exists()) {
                YamlConfiguration plotmeDgYml = YamlConfiguration.loadConfiguration(plotmeDgFile);
                try {
                    HashSet<String> allWorlds = new HashSet<>(plots.keySet());
                    allWorlds.addAll(worlds);
                    for (String world : allWorlds) {
                        copyConfig(plotmeDgYml, world);
                    }
                } catch (IOException ignored) {
                    ignored.printStackTrace();
                }
            }
            for (Entry<String, HashMap<PlotId, Plot>> entry : plots.entrySet()) {
                String world = entry.getKey();
                PlotArea area = PlotSquared.get().getPlotArea(world, null);
                int duplicate = 0;
                if (area != null) {
                    for (Entry<PlotId, Plot> entry2 : entry.getValue().entrySet()) {
                        if (area.getOwnedPlotAbs(entry2.getKey()) != null) {
                            duplicate++;
                        } else {
                            createdPlots.add(entry2.getValue());
                        }
                    }
                    if (duplicate > 0) {
                        PlotSquared.debug("&c[WARNING] Found " + duplicate
                            + " duplicate plots already in DB for world: '" + world
                            + "'. Have you run the converter already?");
                    }
                } else {
                    if (PlotSquared.get().plots_tmp != null) {
                        HashMap<PlotId, Plot> map = PlotSquared.get().plots_tmp.get(world);
                        if (map != null) {
                            for (Entry<PlotId, Plot> entry2 : entry.getValue().entrySet()) {
                                if (map.containsKey(entry2.getKey())) {
                                    duplicate++;
                                } else {
                                    createdPlots.add(entry2.getValue());
                                }
                            }
                            if (duplicate > 0) {
                                PlotSquared.debug("&c[WARNING] Found " + duplicate
                                    + " duplicate plots already in DB for world: '" + world
                                    + "'. Have you run the converter already?");
                            }
                            continue;
                        }
                    }
                    createdPlots.addAll(entry.getValue().values());
                }
            }
            sendMessage("Creating plot DB");
            Thread.sleep(1000);
            final AtomicBoolean done = new AtomicBoolean(false);
            DBFunc.createPlotsAndData(createdPlots, new Runnable() {
                @Override public void run() {
                    if (done.get()) {
                        done();
                        sendMessage("&aDatabase conversion is now complete!");
                        PlotSquared.debug("&c - Stop the server");
                        PlotSquared.debug(
                            "&c - Disable 'plotme-converter' and 'plotme-convert.cache-uuids' in the settings.yml");
                        PlotSquared.debug(
                            "&c - Correct any generator settings that haven't copied to 'settings.yml' properly");
                        PlotSquared.debug("&c - Start the server");
                        PlotSquared.get().setPlots(DBFunc.getPlots());
                    } else {
                        sendMessage(
                            "&cPlease wait until database conversion is complete. You will be notified with instructions when this happens!");
                        done.set(true);
                    }
                }
            });
            sendMessage("Saving configuration...");
            try {
                PlotSquared.get().worlds.save(PlotSquared.get().worldsFile);
            } catch (IOException ignored) {
                sendMessage(" - &cFailed to save configuration.");
            }
            TaskManager.runTask(new Runnable() {
                @Override public void run() {
                    try {
                        boolean mv = false;
                        boolean mw = false;
                        if ((Bukkit.getPluginManager().getPlugin("Multiverse-Core") != null)
                            && Bukkit.getPluginManager().getPlugin("Multiverse-Core").isEnabled()) {
                            mv = true;
                        } else if ((Bukkit.getPluginManager().getPlugin("MultiWorld") != null)
                            && Bukkit.getPluginManager().getPlugin("MultiWorld").isEnabled()) {
                            mw = true;
                        }
                        for (String worldName : worlds) {
                            World world = Bukkit.getWorld(getWorld(worldName));
                            if (world == null) {
                                sendMessage(
                                    "&cInvalid world in PlotMe configuration: " + worldName);
                                continue;
                            }
                            String actualWorldName = world.getName();
                            sendMessage(
                                "Reloading generator for world: '" + actualWorldName + "'...");
                            if (!Bukkit.getWorlds().isEmpty() && Bukkit.getWorlds().get(0).getName()
                                .equals(worldName)) {
                                sendMessage(
                                    "&cYou need to stop the server to reload this world properly");
                            } else {
                                PlotSquared.get().removePlotAreas(actualWorldName);
                                if (mv) {
                                    // unload world with MV
                                    Bukkit.getServer()
                                        .dispatchCommand(Bukkit.getServer().getConsoleSender(),
                                            "mv unload " + actualWorldName);
                                    try {
                                        Thread.sleep(1000);
                                    } catch (InterruptedException ignored) {
                                        Thread.currentThread().interrupt();
                                    }
                                    // load world with MV
                                    Bukkit.getServer()
                                        .dispatchCommand(Bukkit.getServer().getConsoleSender(),
                                            "mv import " + actualWorldName + " normal -g " + PlotSquared
                                                .imp().getPluginName());
                                } else if (mw) {
                                    // unload world with MW
                                    Bukkit.getServer()
                                        .dispatchCommand(Bukkit.getServer().getConsoleSender(),
                                            "mw unload " + actualWorldName);
                                    try {
                                        Thread.sleep(1000);
                                    } catch (InterruptedException ignored) {
                                        Thread.currentThread().interrupt();
                                    }
                                    // load world with MW
                                    Bukkit.getServer()
                                        .dispatchCommand(Bukkit.getServer().getConsoleSender(),
                                            "mw create " + actualWorldName + " plugin:" + PlotSquared
                                                .imp()
                                                .getPluginName());
                                } else {
                                    // Load using Bukkit API
                                    // - User must set generator manually
                                    Bukkit.getServer().unloadWorld(world, true);
                                    World myWorld = WorldCreator.name(actualWorldName).generator(
                                        new BukkitPlotGenerator(PlotSquared.get().IMP.getDefaultGenerator()))
                                        .createWorld();
                                    myWorld.save();
                                }
                            }
                        }
                    } catch (CommandException e) {
                        e.printStackTrace();
                    }
                    if (done.get()) {
                        done();
                        sendMessage("&aDatabase conversion is now complete!");
                        PlotSquared.debug("&c - Stop the server");
                        PlotSquared.debug(
                            "&c - Disable 'plotme-converter' and 'plotme-convert.cache-uuids' in the settings.yml");
                        PlotSquared.debug(
                            "&c - Correct any generator settings that haven't copied to 'settings.yml' properly");
                        PlotSquared.debug("&c - Start the server");
                    } else {
                        sendMessage(
                            "&cPlease wait until database conversion is complete. You will be notified with instructions when this happens!");
                        done.set(true);
                    }
                }
            });
        } catch (InterruptedException | SQLException e) {
            e.printStackTrace();
            PlotSquared.debug("&/end/");
        }
        return true;
    }

    public void done() {
        PlotSquared.get().setPlots(DBFunc.getPlots());
    }
}
