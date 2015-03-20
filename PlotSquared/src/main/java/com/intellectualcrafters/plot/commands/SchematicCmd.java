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

import java.util.Collection;
import java.util.HashMap;

import com.intellectualcrafters.jnbt.CompoundTag;
import com.intellectualcrafters.plot.PlotSquared;
import com.intellectualcrafters.plot.config.C;
import com.intellectualcrafters.plot.config.Settings;
import com.intellectualcrafters.plot.object.Location;
import com.intellectualcrafters.plot.object.Plot;
import com.intellectualcrafters.plot.object.PlotId;
import com.intellectualcrafters.plot.object.PlotPlayer;
import com.intellectualcrafters.plot.util.BlockManager;
import com.intellectualcrafters.plot.util.MainUtil;
import com.intellectualcrafters.plot.util.Permissions;
import com.intellectualcrafters.plot.util.SchematicHandler;
import com.intellectualcrafters.plot.util.SchematicHandler.DataCollection;
import com.intellectualcrafters.plot.util.SchematicHandler.Dimension;
import com.intellectualcrafters.plot.util.SchematicHandler.Schematic;
import com.intellectualcrafters.plot.util.TaskManager;
import com.intellectualcrafters.plot.util.bukkit.BukkitUtil;
import com.intellectualcrafters.plot.util.bukkit.UUIDHandler;

public class SchematicCmd extends SubCommand {
    private int counter = 0;
    private boolean running = false;
    private Plot[] plots;
    private int task;

    public SchematicCmd() {
        super("schematic", "plots.schematic", "Schematic Command", "schematic {arg}", "sch", CommandCategory.ACTIONS, false);
        // TODO command to fetch schematic from worldedit directory
    }

    @Override
    public boolean execute(final PlotPlayer plr, final String... args) {
        if (args.length < 1) {
            sendMessage(plr, C.SCHEMATIC_MISSING_ARG);
            return true;
        }
        final String arg = args[0].toLowerCase();
        final String file;
        final Schematic schematic;
        switch (arg) {
            case "paste": {
                if (plr == null) {
                    PlotSquared.log(C.IS_CONSOLE.s());
                    return false;
                }
                if (!Permissions.hasPermission(plr, "plots.schematic.paste")) {
                    MainUtil.sendMessage(plr, C.NO_PERMISSION, "plots.schematic.paste");
                    return false;
                }
                if (args.length < 2) {
                    sendMessage(plr, C.SCHEMATIC_MISSING_ARG);
                    break;
                }
                final Location loc = plr.getLocation();
                final Plot plot = MainUtil.getPlot(loc);
                if (plot == null) {
                    sendMessage(plr, C.NOT_IN_PLOT);
                    break;
                }
                if (this.running) {
                    MainUtil.sendMessage(plr, "&cTask is already running.");
                    return false;
                }
                final String file2 = args[1];
                this.running = true;
                this.counter = 0;
                TaskManager.runTaskAsync(new Runnable() {
                    @Override
                    public void run() {
                        final Schematic schematic = SchematicHandler.manager.getSchematic(file2);
                        if (schematic == null) {
                            sendMessage(plr, C.SCHEMATIC_INVALID, "non-existent or not in gzip format");
                            SchematicCmd.this.running = false;
                            return;
                        }
                        final int x;
                        final int z;
                        final Plot plot2 = MainUtil.getPlot(loc);
                        final Dimension dem = schematic.getSchematicDimension();
                        final Location bot = MainUtil.getPlotBottomLoc(loc.getWorld(), plot2.id).add(1, 0, 1);
                        final int length2 = MainUtil.getPlotWidth(loc.getWorld(), plot2.id);
                        if ((dem.getX() > length2) || (dem.getZ() > length2)) {
                            sendMessage(plr, C.SCHEMATIC_INVALID, String.format("Wrong size (x: %s, z: %d) vs %d ", dem.getX(), dem.getZ(), length2));
                            SchematicCmd.this.running = false;
                            return;
                        }
                        if ((dem.getX() != length2) || (dem.getZ() != length2)) {
                            final Location loc = plr.getLocation();
                            x = Math.min(length2 - dem.getX(), loc.getX() - bot.getX());
                            z = Math.min(length2 - dem.getZ(), loc.getZ() - bot.getZ());
                        } else {
                            x = 0;
                            z = 0;
                        }
                        final DataCollection[] b = schematic.getBlockCollection();
                        final int sy = BlockManager.manager.getHeighestBlock(bot);
                        final int WIDTH = schematic.getSchematicDimension().getX();
                        final int LENGTH = schematic.getSchematicDimension().getZ();
                        final Location l1;
                        if (!(schematic.getSchematicDimension().getY() == BukkitUtil.getMaxHeight(loc.getWorld()))) {
                             l1 = bot.add(0, sy - 1, 0);
                        }
                         else {
                             l1 = bot;
                         }
                        
                        final int blen = b.length - 1;
                        SchematicCmd.this.task = TaskManager.runTaskRepeat(new Runnable() {
                            @Override
                            public void run() {
                                boolean result = false;
                                while (!result) {
                                    final int start = SchematicCmd.this.counter * 5000;
                                    if (start > blen) {
                                        SchematicHandler.manager.pasteStates(schematic, plot, 0, 0);
                                        sendMessage(plr, C.SCHEMATIC_PASTE_SUCCESS);
                                        MainUtil.update(plr.getLocation());
                                        SchematicCmd.this.running = false;
                                        PlotSquared.TASK.cancelTask(SchematicCmd.this.task);
                                        return;
                                    }
                                    final int end = Math.min(start + 5000, blen);
                                    result = SchematicHandler.manager.pastePart(loc.getWorld(), b, l1, x, z, start, end, WIDTH, LENGTH);
                                    SchematicCmd.this.counter++;
                                }
                            }
                        }, 1);
                    }
                });
                break;
            }
            case "test": {
                if (plr == null) {
                    PlotSquared.log(C.IS_CONSOLE.s());
                    return false;
                }
                if (!Permissions.hasPermission(plr, "plots.schematic.test")) {
                    MainUtil.sendMessage(plr, C.NO_PERMISSION, "plots.schematic.test");
                    return false;
                }
                if (args.length < 2) {
                    sendMessage(plr, C.SCHEMATIC_MISSING_ARG);
                    break;
                }
                file = args[1];
                schematic = SchematicHandler.manager.getSchematic(file);
                if (schematic == null) {
                    sendMessage(plr, C.SCHEMATIC_INVALID, "non-existent");
                    break;
                }
                final Location loc = plr.getLocation();
                final int l1 = schematic.getSchematicDimension().getX();
                final int l2 = schematic.getSchematicDimension().getZ();
                final Plot plot = MainUtil.getPlot(loc);
                final int length = MainUtil.getPlotWidth(loc.getWorld(), plot.id);
                if ((l1 < length) || (l2 < length)) {
                    sendMessage(plr, C.SCHEMATIC_INVALID, String.format("Wrong size (x: %s, z: %d) vs %d ", l1, l2, length));
                    break;
                }
                sendMessage(plr, C.SCHEMATIC_VALID);
                break;
            }
            case "saveall":
            case "exportall": {
                if (plr != null) {
                    MainUtil.sendMessage(plr, C.NOT_CONSOLE);
                    return false;
                }
                if (args.length != 2) {
                    MainUtil.sendMessage(null, "&cNeed world arg. Use &7/plots sch exportall <world>");
                    return false;
                }
                final HashMap<PlotId, Plot> plotmap = PlotSquared.getPlots(args[1]);
                if ((plotmap == null) || (plotmap.size() == 0)) {
                    MainUtil.sendMessage(null, "&cInvalid world. Use &7/plots sch exportall <world>");
                    return false;
                }
                if (this.running) {
                    MainUtil.sendMessage(null, "&cTask is already running.");
                    return false;
                }
                PlotSquared.log("&3PlotSquared&8->&3Schemaitc&8: &7Mass export has started. This may take a while.");
                PlotSquared.log("&3PlotSquared&8->&3Schemaitc&8: &7Found &c" + plotmap.size() + "&7 plots...");
                final String worldname = args[1];
                final Collection<Plot> values = plotmap.values();
                this.plots = values.toArray(new Plot[values.size()]);
                this.running = true;
                this.counter = 0;
                this.task = TaskManager.runTaskRepeat(new Runnable() {
                    @Override
                    public void run() {
                        if (SchematicCmd.this.counter >= SchematicCmd.this.plots.length) {
                            PlotSquared.log("&3PlotSquared&8->&3Schemaitc&8: &aFinished!");
                            SchematicCmd.this.running = false;
                            PlotSquared.TASK.cancelTask(SchematicCmd.this.task);
                            return;
                        }
                        final Plot plot = SchematicCmd.this.plots[SchematicCmd.this.counter];
                        final CompoundTag sch = SchematicHandler.manager.getCompoundTag(worldname, plot.id);
                        final String o = UUIDHandler.getName(plot.owner_);
                        final String owner = o == null ? "unknown" : o;
                        if (sch == null) {
                            MainUtil.sendMessage(null, "&7 - Skipped plot &c" + plot.id);
                        } else {
                            TaskManager.runTaskAsync(new Runnable() {
                                @Override
                                public void run() {
                                    MainUtil.sendMessage(null, "&6ID: " + plot.id);
                                    final boolean result = SchematicHandler.manager.save(sch, Settings.SCHEMATIC_SAVE_PATH + "/" + plot.id.x + ";" + plot.id.y + "," + worldname + "," + owner + ".schematic");
                                    if (!result) {
                                        MainUtil.sendMessage(null, "&7 - Failed to save &c" + plot.id);
                                    } else {
                                        MainUtil.sendMessage(null, "&7 - &a  success: " + plot.id);
                                    }
                                }
                            });
                        }
                        SchematicCmd.this.counter++;
                    }
                }, 20);
                break;
            }
            case "export":
            case "save": {
                if (!Permissions.hasPermission(plr, "plots.schematic.save")) {
                    MainUtil.sendMessage(plr, C.NO_PERMISSION, "plots.schematic.save");
                    return false;
                }
                if (this.running) {
                    MainUtil.sendMessage(plr, "&cTask is already running.");
                    return false;
                }
                final String world;
                final Plot p2;
                if (plr != null) {
                    final Location loc = plr.getLocation();
                    final Plot plot = MainUtil.getPlot(loc);
                    if (plot == null) {
                        return !sendMessage(plr, C.NOT_IN_PLOT);
                    }
                    if (!plot.isAdded(plr.getUUID())) {
                        sendMessage(plr, C.NO_PLOT_PERMS);
                        return false;
                    }
                    p2 = plot;
                    world = loc.getWorld();
                } else {
                    if (args.length == 3) {
                        try {
                            world = args[0];
                            final String[] split = args[2].split(";");
                            final PlotId i = new PlotId(Integer.parseInt(split[0]), Integer.parseInt(split[1]));
                            if ((PlotSquared.getPlots(world) == null) || (PlotSquared.getPlots(world).get(i) == null)) {
                                MainUtil.sendMessage(null, "&cInvalid world or id. Use &7/plots sch save <world> <id>");
                                return false;
                            }
                            p2 = PlotSquared.getPlots(world).get(i);
                        } catch (final Exception e) {
                            MainUtil.sendMessage(null, "&cInvalid world or id. Use &7/plots sch save <world> <id>");
                            return false;
                        }
                    } else {
                        MainUtil.sendMessage(null, "&cInvalid world or id. Use &7/plots sch save <world> <id>");
                        return false;
                    }
                }
                this.plots = new Plot[] { p2 };
                this.running = true;
                this.counter = 0;
                this.task = TaskManager.runTaskRepeat(new Runnable() {
                    @Override
                    public void run() {
                        if (SchematicCmd.this.counter >= SchematicCmd.this.plots.length) {
                            PlotSquared.log("&3PlotSquared&8->&3Schemaitc&8: &aFinished!");
                            SchematicCmd.this.running = false;
                            PlotSquared.TASK.cancelTask(SchematicCmd.this.task);
                            return;
                        }
                        final Plot plot = SchematicCmd.this.plots[SchematicCmd.this.counter];
                        final CompoundTag sch = SchematicHandler.manager.getCompoundTag(world, plot.id);
                        final String o = UUIDHandler.getName(plot.owner_);
                        final String owner = o == null ? "unknown" : o;
                        if (sch == null) {
                            MainUtil.sendMessage(plr, "&7 - Skipped plot &c" + plot.id);
                        } else {
                            TaskManager.runTaskAsync(new Runnable() {
                                @Override
                                public void run() {
                                    MainUtil.sendMessage(plr, "&6ID: " + plot.id);
                                    final boolean result = SchematicHandler.manager.save(sch, Settings.SCHEMATIC_SAVE_PATH + "/" + plot.id.x + ";" + plot.id.y + "," + world + "," + owner.trim() + ".schematic");
                                    if (!result) {
                                        MainUtil.sendMessage(plr, "&7 - Failed to save &c" + plot.id);
                                    } else {
                                        MainUtil.sendMessage(plr, "&7 - &aExport success: " + plot.id);
                                    }
                                }
                            });
                        }
                        SchematicCmd.this.counter++;
                    }
                }, 60);
                break;
            }
            default: {
                sendMessage(plr, C.SCHEMATIC_MISSING_ARG);
                break;
            }
        }
        return true;
    }
}
