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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import com.intellectualcrafters.configuration.ConfigurationSection;
import com.intellectualcrafters.configuration.file.YamlConfiguration;
import com.intellectualcrafters.plot.PS;
import com.intellectualcrafters.plot.config.C;
import com.intellectualcrafters.plot.config.ConfigurationNode;
import com.intellectualcrafters.plot.object.FileBytes;
import com.intellectualcrafters.plot.object.PlotManager;
import com.intellectualcrafters.plot.object.PlotPlayer;
import com.intellectualcrafters.plot.object.PlotWorld;
import com.intellectualcrafters.plot.object.SetupObject;
import com.intellectualcrafters.plot.util.BlockManager;
import com.intellectualcrafters.plot.util.MainUtil;
import com.intellectualcrafters.plot.util.SetBlockQueue;
import com.intellectualcrafters.plot.util.SetupUtils;
import com.intellectualcrafters.plot.util.TaskManager;
import com.plotsquared.general.commands.CommandDeclaration;

@CommandDeclaration(
command = "template",
permission = "plots.admin",
description = "Create or use a world template",
usage = "/plot template [import|export] <world> <template>",
category = CommandCategory.DEBUG)
public class Template extends SubCommand {
    
    public static boolean extractAllFiles(final String world, final String template) {
        final byte[] buffer = new byte[2048];
        try {
            final File folder = new File(PS.get().IMP.getDirectory() + File.separator + "templates");
            if (!folder.exists()) {
                return false;
            }
            final File input = new File(folder + File.separator + template + ".template");
            final File output = PS.get().IMP.getDirectory();
            if (!output.exists()) {
                output.mkdirs();
            }
            final ZipInputStream zis = new ZipInputStream(new FileInputStream(input));
            ZipEntry ze = zis.getNextEntry();
            while (ze != null) {
                final String name = ze.getName();
                final File newFile = new File((output + File.separator + name).replaceAll("__TEMP_DIR__", world));
                new File(newFile.getParent()).mkdirs();
                final FileOutputStream fos = new FileOutputStream(newFile);
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
        } catch (final Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    public static byte[] getBytes(final PlotWorld plotworld) {
        final ConfigurationSection section = PS.get().config.getConfigurationSection("worlds." + plotworld.worldname);
        final YamlConfiguration config = new YamlConfiguration();
        final String generator = SetupUtils.manager.getGenerator(plotworld);
        if (generator != null) {
            config.set("generator.plugin", generator);
        }
        for (final String key : section.getKeys(true)) {
            config.set(key, section.get(key));
        }
        return config.saveToString().getBytes();
    }
    
    public static void zipAll(final String world, final Set<FileBytes> files) throws IOException {
        final File output = new File(PS.get().IMP.getDirectory() + File.separator + "templates");
        output.mkdirs();
        final FileOutputStream fos = new FileOutputStream(output + File.separator + world + ".template");
        final ZipOutputStream zos = new ZipOutputStream(fos);
        
        for (final FileBytes file : files) {
            final ZipEntry ze = new ZipEntry(file.path);
            zos.putNextEntry(ze);
            zos.write(file.data);
        }
        zos.closeEntry();
        zos.close();
    }
    
    @Override
    public boolean onCommand(final PlotPlayer plr, final String[] args) {
        if ((args.length != 2) && (args.length != 3)) {
            if (args.length == 1) {
                if (args[0].equalsIgnoreCase("export")) {
                    MainUtil.sendMessage(plr, C.COMMAND_SYNTAX, "/plot template export <world>");
                    return true;
                } else if (args[0].equalsIgnoreCase("import")) {
                    MainUtil.sendMessage(plr, C.COMMAND_SYNTAX, "/plot template import <world> <template>");
                    return true;
                }
            }
            MainUtil.sendMessage(plr, C.COMMAND_SYNTAX, "/plot template <import|explort> <world> [template]");
            return true;
        }
        final String world = args[1];
        switch (args[0].toLowerCase()) {
            case "import": {
                if (args.length != 3) {
                    MainUtil.sendMessage(plr, C.COMMAND_SYNTAX, "/plot template import <world> <template>");
                    return false;
                }
                if (PS.get().isPlotWorld(world)) {
                    MainUtil.sendMessage(plr, C.SETUP_WORLD_TAKEN, world);
                    return false;
                }
                final boolean result = extractAllFiles(world, args[2]);
                if (!result) {
                    MainUtil.sendMessage(plr, "&cInvalid template file: " + args[2] + ".template");
                    return false;
                }
                final File worldFile = new File(PS.get().IMP.getDirectory() + File.separator + "templates" + File.separator + "tmp-data.yml");
                final YamlConfiguration worldConfig = YamlConfiguration.loadConfiguration(worldFile);
                PS.get().config.set("worlds." + world, worldConfig.get(""));
                try {
                    PS.get().config.save(PS.get().configFile);
                    PS.get().config.load(PS.get().configFile);
                } catch (final Exception e) {
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
                
                final int type = worldConfig.getInt("generator.type");
                final int terrain = worldConfig.getInt("generator.terrain");
                
                final SetupObject setup = new SetupObject();
                setup.plotManager = manager;
                setup.setupGenerator = generator;
                setup.type = type;
                setup.terrain = terrain;
                setup.step = new ConfigurationNode[0];
                setup.world = world;
                SetupUtils.manager.setupWorld(setup);
                SetBlockQueue.addNotify(new Runnable() {
                    @Override
                    public void run() {
                        MainUtil.sendMessage(plr, "Done!");
                        plr.teleport(BlockManager.manager.getSpawn(world));
                    }
                });
                return true;
            }
            case "export": {
                if (args.length != 2) {
                    MainUtil.sendMessage(plr, C.COMMAND_SYNTAX, "/plot template export <world>");
                    return false;
                }
                final PlotWorld plotworld = PS.get().getPlotWorld(world);
                if (!BlockManager.manager.isWorld(world) || (plotworld == null)) {
                    MainUtil.sendMessage(plr, C.NOT_VALID_PLOT_WORLD);
                    return false;
                }
                final PlotManager manager = PS.get().getPlotManager(world);
                final PlotPlayer finalPlr = plr;
                TaskManager.runTaskAsync(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            manager.exportTemplate(plotworld);
                        } catch (final Exception e) {
                            e.printStackTrace();
                            MainUtil.sendMessage(finalPlr, "Failed: " + e.getMessage());
                            return;
                        }
                        MainUtil.sendMessage(finalPlr, "Done!");
                    }
                });
                return true;
            }
            default: {
                C.COMMAND_SYNTAX.send(plr, getUsage());
            }
        }
        return false;
    }
}
