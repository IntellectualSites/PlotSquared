package com.github.intellectualsites.plotsquared.commands;

import com.github.intellectualsites.plotsquared.configuration.ConfigurationSection;
import com.github.intellectualsites.plotsquared.configuration.InvalidConfigurationException;
import com.github.intellectualsites.plotsquared.configuration.file.YamlConfiguration;
import com.github.intellectualsites.plotsquared.PlotSquared;
import com.github.intellectualsites.plotsquared.plot.config.Captions;
import com.github.intellectualsites.plotsquared.plot.config.ConfigurationNode;
import com.github.intellectualsites.plotsquared.plot.config.Settings;
import com.github.intellectualsites.plotsquared.util.FileBytes;
import com.github.intellectualsites.plotsquared.plot.object.PlotArea;
import com.github.intellectualsites.plotsquared.plot.object.PlotManager;
import com.github.intellectualsites.plotsquared.player.PlotPlayer;
import com.github.intellectualsites.plotsquared.plot.object.SetupObject;
import com.github.intellectualsites.plotsquared.plot.object.TeleportCause;
import com.github.intellectualsites.plotsquared.util.MainUtil;
import com.github.intellectualsites.plotsquared.util.SetupUtils;
import com.github.intellectualsites.plotsquared.util.tasks.TaskManager;
import com.github.intellectualsites.plotsquared.util.WorldUtil;
import com.github.intellectualsites.plotsquared.queue.GlobalBlockQueue;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

@CommandDeclaration(command = "template",
    permission = "plots.admin",
    description = "Create or use a world template",
    usage = "/plot template [import|export] <world> <template>",
    category = CommandCategory.ADMINISTRATION)
public class Template extends SubCommand {

    public static boolean extractAllFiles(String world, String template) {
        try {
            File folder =
                MainUtil.getFile(PlotSquared.get().IMP.getDirectory(), Settings.Paths.TEMPLATES);
            if (!folder.exists()) {
                return false;
            }
            File output = PlotSquared.get().IMP.getDirectory();
            if (!output.exists()) {
                output.mkdirs();
            }
            File input = new File(folder + File.separator + template + ".template");
            try (ZipInputStream zis = new ZipInputStream(new FileInputStream(input))) {
                ZipEntry ze = zis.getNextEntry();
                byte[] buffer = new byte[2048];
                while (ze != null) {
                    if (!ze.isDirectory()) {
                        String name = ze.getName().replace('\\', File.separatorChar)
                            .replace('/', File.separatorChar);
                        File newFile = new File(
                            (output + File.separator + name).replaceAll("__TEMP_DIR__", world));
                        File parent = newFile.getParentFile();
                        if (parent != null) {
                            parent.mkdirs();
                        }
                        try (FileOutputStream fos = new FileOutputStream(newFile)) {
                            int len;
                            while ((len = zis.read(buffer)) > 0) {
                                fos.write(buffer, 0, len);
                            }
                        }
                    }
                    ze = zis.getNextEntry();
                }
                zis.closeEntry();
            }
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static byte[] getBytes(PlotArea plotArea) {
        ConfigurationSection section =
            PlotSquared.get().worlds.getConfigurationSection("worlds." + plotArea.getWorldName());
        YamlConfiguration config = new YamlConfiguration();
        String generator = SetupUtils.manager.getGenerator(plotArea);
        if (generator != null) {
            config.set("generator.plugin", generator);
        }
        for (String key : section.getKeys(true)) {
            config.set(key, section.get(key));
        }
        return config.saveToString().getBytes();
    }

    public static void zipAll(String world, Set<FileBytes> files) throws IOException {
        File output =
            MainUtil.getFile(PlotSquared.get().IMP.getDirectory(), Settings.Paths.TEMPLATES);
        output.mkdirs();
        try (FileOutputStream fos = new FileOutputStream(
            output + File.separator + world + ".template");
            ZipOutputStream zos = new ZipOutputStream(fos)) {

            for (FileBytes file : files) {
                ZipEntry ze = new ZipEntry(file.path);
                zos.putNextEntry(ze);
                zos.write(file.data);
            }
            zos.closeEntry();
        }
    }

    @Override public boolean onCommand(final PlotPlayer player, String[] args) {
        if (args.length != 2 && args.length != 3) {
            if (args.length == 1) {
                if (args[0].equalsIgnoreCase("export")) {
                    MainUtil.sendMessage(player, Captions.COMMAND_SYNTAX,
                        "/plot template export <world>");
                    return true;
                } else if (args[0].equalsIgnoreCase("import")) {
                    MainUtil.sendMessage(player, Captions.COMMAND_SYNTAX,
                        "/plot template import <world> <template>");
                    return true;
                }
            }
            MainUtil.sendMessage(player, Captions.COMMAND_SYNTAX, getUsage());
            return true;
        }
        final String world = args[1];
        switch (args[0].toLowerCase()) {
            case "import": {
                if (args.length != 3) {
                    MainUtil.sendMessage(player, Captions.COMMAND_SYNTAX,
                        "/plot template import <world> <template>");
                    return false;
                }
                if (PlotSquared.get().hasPlotArea(world)) {
                    MainUtil.sendMessage(player, Captions.SETUP_WORLD_TAKEN, world);
                    return false;
                }
                boolean result = extractAllFiles(world, args[2]);
                if (!result) {
                    MainUtil
                        .sendMessage(player, "&cInvalid template file: " + args[2] + ".template");
                    return false;
                }
                File worldFile = MainUtil.getFile(PlotSquared.get().IMP.getDirectory(),
                    Settings.Paths.TEMPLATES + File.separator + "tmp-data.yml");
                YamlConfiguration worldConfig = YamlConfiguration.loadConfiguration(worldFile);
                PlotSquared.get().worlds.set("worlds." + world, worldConfig.get(""));
                try {
                    PlotSquared.get().worlds.save(PlotSquared.get().worldsFile);
                    PlotSquared.get().worlds.load(PlotSquared.get().worldsFile);
                } catch (InvalidConfigurationException | IOException e) {
                    e.printStackTrace();
                }
                String manager =
                    worldConfig.getString("generator.plugin", PlotSquared.imp().getPluginName());
                String generator = worldConfig.getString("generator.init", manager);
                SetupObject setup = new SetupObject();
                setup.type = MainUtil.getType(worldConfig);
                setup.terrain = MainUtil.getTerrain(worldConfig);

                setup.plotManager = manager;
                setup.setupGenerator = generator;
                setup.step = new ConfigurationNode[0];
                setup.world = world;
                SetupUtils.manager.setupWorld(setup);
                GlobalBlockQueue.IMP.addEmptyTask(() -> {
                    MainUtil.sendMessage(player, "Done!");
                    player.teleport(WorldUtil.IMP.getSpawn(world), TeleportCause.COMMAND);
                });
                return true;
            }
            case "export":
                if (args.length != 2) {
                    MainUtil.sendMessage(player, Captions.COMMAND_SYNTAX,
                        "/plot template export <world>");
                    return false;
                }
                final PlotArea area = PlotSquared.get().getPlotAreaByString(world);
                if (area == null) {
                    MainUtil.sendMessage(player, Captions.NOT_VALID_PLOT_WORLD);
                    return false;
                }
                final PlotManager manager = area.getPlotManager();
                TaskManager.runTaskAsync(() -> {
                    try {
                        manager.exportTemplate();
                    } catch (Exception e) { // Must recover from any exception thrown a third party template manager
                        e.printStackTrace();
                        MainUtil.sendMessage(player, "Failed: " + e.getMessage());
                        return;
                    }
                    MainUtil.sendMessage(player, "Done!");
                });
                return true;
            default:
                Captions.COMMAND_SYNTAX.send(player, getUsage());
        }
        return false;
    }
}
