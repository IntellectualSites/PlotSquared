package com.github.intellectualsites.plotsquared.plot.commands;

import com.github.intellectualsites.plotsquared.commands.CommandDeclaration;
import com.github.intellectualsites.plotsquared.configuration.ConfigurationSection;
import com.github.intellectualsites.plotsquared.configuration.InvalidConfigurationException;
import com.github.intellectualsites.plotsquared.configuration.file.YamlConfiguration;
import com.github.intellectualsites.plotsquared.plot.PS;
import com.github.intellectualsites.plotsquared.plot.config.C;
import com.github.intellectualsites.plotsquared.plot.config.ConfigurationNode;
import com.github.intellectualsites.plotsquared.plot.config.Settings;
import com.github.intellectualsites.plotsquared.plot.object.*;
import com.github.intellectualsites.plotsquared.plot.util.MainUtil;
import com.github.intellectualsites.plotsquared.plot.util.SetupUtils;
import com.github.intellectualsites.plotsquared.plot.util.TaskManager;
import com.github.intellectualsites.plotsquared.plot.util.WorldUtil;
import com.github.intellectualsites.plotsquared.plot.util.block.GlobalBlockQueue;

import java.io.*;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

@CommandDeclaration(command = "template", permission = "plots.admin", description = "Create or use a world template", usage = "/plot template [import|export] <world> <template>", category = CommandCategory.ADMINISTRATION)
public class Template extends SubCommand {

    public static boolean extractAllFiles(String world, String template) {
        try {
            File folder = MainUtil.getFile(PS.get().IMP.getDirectory(), Settings.Paths.TEMPLATES);
            if (!folder.exists()) {
                return false;
            }
            File input = new File(folder + File.separator + template + ".template");
            File output = PS.get().IMP.getDirectory();
            if (!output.exists()) {
                output.mkdirs();
            }
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
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return false;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static byte[] getBytes(PlotArea plotArea) {
        ConfigurationSection section =
            PS.get().worlds.getConfigurationSection("worlds." + plotArea.worldname);
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
        File output = MainUtil.getFile(PS.get().IMP.getDirectory(), Settings.Paths.TEMPLATES);
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
                    MainUtil.sendMessage(player, C.COMMAND_SYNTAX, "/plot template export <world>");
                    return true;
                } else if (args[0].equalsIgnoreCase("import")) {
                    MainUtil.sendMessage(player, C.COMMAND_SYNTAX,
                        "/plot template import <world> <template>");
                    return true;
                }
            }
            MainUtil.sendMessage(player, C.COMMAND_SYNTAX,
                "/plot template <import|export> <world> [template]");
            return true;
        }
        final String world = args[1];
        switch (args[0].toLowerCase()) {
            case "import": {
                if (args.length != 3) {
                    MainUtil.sendMessage(player, C.COMMAND_SYNTAX,
                        "/plot template import <world> <template>");
                    return false;
                }
                if (PS.get().hasPlotArea(world)) {
                    MainUtil.sendMessage(player, C.SETUP_WORLD_TAKEN, world);
                    return false;
                }
                boolean result = extractAllFiles(world, args[2]);
                if (!result) {
                    MainUtil
                        .sendMessage(player, "&cInvalid template file: " + args[2] + ".template");
                    return false;
                }
                File worldFile = MainUtil.getFile(PS.get().IMP.getDirectory(),
                    Settings.Paths.TEMPLATES + File.separator + "tmp-data.yml");
                YamlConfiguration worldConfig = YamlConfiguration.loadConfiguration(worldFile);
                PS.get().worlds.set("worlds." + world, worldConfig.get(""));
                try {
                    PS.get().worlds.save(PS.get().worldsFile);
                    PS.get().worlds.load(PS.get().worldsFile);
                } catch (InvalidConfigurationException | IOException e) {
                    e.printStackTrace();
                }
                String manager =
                    worldConfig.getString("generator.plugin", PS.imp().getPluginName());
                String generator = worldConfig.getString("generator.init", manager);
                int type = worldConfig.getInt("generator.type");
                int terrain = worldConfig.getInt("generator.terrain");

                SetupObject setup = new SetupObject();
                setup.plotManager = manager;
                setup.setupGenerator = generator;
                setup.type = type;
                setup.terrain = terrain;
                setup.step = new ConfigurationNode[0];
                setup.world = world;
                SetupUtils.manager.setupWorld(setup);
                GlobalBlockQueue.IMP.addTask(new Runnable() {
                    @Override public void run() {
                        MainUtil.sendMessage(player, "Done!");
                        player.teleport(WorldUtil.IMP.getSpawn(world));
                    }
                });
                return true;
            }
            case "export":
                if (args.length != 2) {
                    MainUtil.sendMessage(player, C.COMMAND_SYNTAX, "/plot template export <world>");
                    return false;
                }
                final PlotArea area = PS.get().getPlotAreaByString(world);
                if (area == null) {
                    MainUtil.sendMessage(player, C.NOT_VALID_PLOT_WORLD);
                    return false;
                }
                final PlotManager manager = area.getPlotManager();
                TaskManager.runTaskAsync(new Runnable() {
                    @Override public void run() {
                        try {
                            manager.exportTemplate(area);
                        } catch (Exception e) { // Must recover from any exception thrown a third party template manager
                            e.printStackTrace();
                            MainUtil.sendMessage(player, "Failed: " + e.getMessage());
                            return;
                        }
                        MainUtil.sendMessage(player, "Done!");
                    }
                });
                return true;
            default:
                C.COMMAND_SYNTAX.send(player, getUsage());
        }
        return false;
    }
}
