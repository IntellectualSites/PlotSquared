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
package com.intellectualcrafters.plot.commands;

import com.intellectualcrafters.plot.PlotSquared;
import com.intellectualcrafters.plot.config.C;
import com.intellectualcrafters.plot.config.ConfigurationNode;
import com.intellectualcrafters.plot.object.*;
import com.intellectualcrafters.plot.util.BlockManager;
import com.intellectualcrafters.plot.util.MainUtil;
import com.intellectualcrafters.plot.util.SetupUtils;
import com.intellectualcrafters.plot.util.TaskManager;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class Template extends SubCommand {
    public Template() {
        super("template", "plots.admin", "Create or use a world template", "template", "", CommandCategory.DEBUG, false);
    }

    public static boolean extractAllFiles(String world, String template) {
        byte[] buffer = new byte[2048];
        try {
            File folder = new File(PlotSquared.IMP.getDirectory() + File.separator + "templates");
            if (!folder.exists()) {
                return false;
            }
            File input = new File(folder + File.separator + template + ".template");
            File output = PlotSquared.IMP.getDirectory();
            if (!output.exists()) {
                output.mkdirs();
            }
            ZipInputStream zis = new ZipInputStream(new FileInputStream(input));
            ZipEntry ze = zis.getNextEntry();
            while (ze != null) {
                String name = ze.getName();
                File newFile = new File((output + File.separator + name).replaceAll("__TEMP_DIR__", world));
                new File(newFile.getParent()).mkdirs();
                FileOutputStream fos = new FileOutputStream(newFile);
                int len;
                while ((len = zis.read(buffer)) > 0) {
                    fos.write(buffer, 0, len);
                }
                fos.close();
                ze = zis.getNextEntry();
            }
            zis.closeEntry();
            zis.close();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static byte[] getBytes(PlotWorld plotworld) {
        ConfigurationSection section = PlotSquared.config.getConfigurationSection("worlds." + plotworld.worldname);
        YamlConfiguration config = new YamlConfiguration();
        String generator = SetupUtils.manager.getGenerator(plotworld);
        if (generator != null) {
            config.set("generator.plugin", generator);
        }
        for (String key : section.getKeys(true)) {
            config.set(key, section.get(key));
        }
        return config.saveToString().getBytes();
    }

    public static void zipAll(final String world, Set<FileBytes> files) throws IOException {
        File output = new File(PlotSquared.IMP.getDirectory() + File.separator + "templates");
        output.mkdirs();
        FileOutputStream fos = new FileOutputStream(output + File.separator + world + ".template");
        ZipOutputStream zos = new ZipOutputStream(fos);

        for (FileBytes file : files) {
            ZipEntry ze = new ZipEntry(file.path);
            zos.putNextEntry(ze);
            zos.write(file.data);
        }
        zos.closeEntry();
        zos.close();
    }
    
    @Override
    public boolean execute(final PlotPlayer plr, final String... args) {
        if (args.length != 2 && args.length != 3) {
            if (args.length == 1) {
                if (args[0].equalsIgnoreCase("export")) {
                    MainUtil.sendMessage(plr, C.COMMAND_SYNTAX, "/plot template export <world>");
                    return false;
                }
                else if (args[0].equalsIgnoreCase("import")) {
                    MainUtil.sendMessage(plr, C.COMMAND_SYNTAX, "/plot template import <world> <template>");
                    return false;
                }
            }
            MainUtil.sendMessage(plr, C.COMMAND_SYNTAX, "/plot template <import|export> <world> [template]");
            return false;
        }
        final String world = args[1];
        switch (args[0].toLowerCase()) {
            case "import": {
                if (args.length != 3) {
                    MainUtil.sendMessage(plr, C.COMMAND_SYNTAX, "/plot template import <world> <template>");
                    return false;
                }
                if (PlotSquared.getInstance().isPlotWorld(world)) {
                    MainUtil.sendMessage(plr, C.SETUP_WORLD_TAKEN, world);
                    return false;
                }
                boolean result = extractAllFiles(world, args[2]);
                if (!result) {
                    MainUtil.sendMessage(plr, "&cInvalid template file: " + args[2] +".template");
                    return false;
                }
                File worldFile = new File(PlotSquared.IMP.getDirectory() + File.separator + "templates" + File.separator + "tmp-data.yml");
                YamlConfiguration worldConfig = YamlConfiguration.loadConfiguration(worldFile);
                PlotSquared.config.set("worlds." + world, worldConfig.get(""));
                try {
                    PlotSquared.config.save(PlotSquared.configFile);
                    PlotSquared.config.load(PlotSquared.configFile);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                String manager = worldConfig.getString("generator.plugin");
                if (manager == null) {
                    manager = "PlotSquared";
                }
                String generator = worldConfig.getString("generator.init");
                if (generator == null) {
                    generator = manager;
                }

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
                MainUtil.sendMessage(plr, "Done!");
                if (plr != null) {
                    plr.teleport(BlockManager.manager.getSpawn(world));
                }
                return true;
            }
            case "export": {
                if (args.length != 2) {
                    MainUtil.sendMessage(plr, C.COMMAND_SYNTAX, "/plot template export <world>");
                    return false;
                }
                final PlotWorld plotworld = PlotSquared.getInstance().getPlotWorld(world);
                if (!BlockManager.manager.isWorld(world) || (plotworld == null)) {
                    MainUtil.sendMessage(plr, C.NOT_VALID_PLOT_WORLD);
                    return false;
                }
                final PlotManager manager = PlotSquared.getInstance().getPlotManager(world);
                TaskManager.runTaskAsync(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            manager.exportTemplate(plotworld);
                        }
                        catch (Exception e) {
                            e.printStackTrace();
                            MainUtil.sendMessage(plr, "Failed: " + e.getMessage());
                            return;
                        }
                        MainUtil.sendMessage(plr, "Done!");
                    }
                });
                return true;
            }
        }
        return true;
    }
}
