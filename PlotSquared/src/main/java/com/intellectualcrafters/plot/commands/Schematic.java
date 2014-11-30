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

import com.intellectualcrafters.jnbt.CompoundTag;
import com.intellectualcrafters.plot.PlotMain;
import com.intellectualcrafters.plot.config.C;
import com.intellectualcrafters.plot.config.Settings;
import com.intellectualcrafters.plot.object.Plot;
import com.intellectualcrafters.plot.object.PlotId;
import com.intellectualcrafters.plot.util.*;
import com.intellectualcrafters.plot.util.SchematicHandler.DataCollection;
import com.intellectualcrafters.plot.util.SchematicHandler.Dimension;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.Collection;
import java.util.HashMap;

public class Schematic extends SubCommand {

    private int counter = 0;
    private boolean running = false;
    private Plot[] plots;
    private int task;

    public Schematic() {
        super("schematic", "plots.schematic", "Schematic Command", "schematic {arg}", "sch", CommandCategory.ACTIONS, false);
        // TODO command to fetch schematic from worldedit directory
    }

    @Override
    public boolean execute(final Player plr, final String... args) {
        if (args.length < 1) {
            sendMessage(plr, C.SCHEMATIC_MISSING_ARG);
            return true;
        }
        final String arg = args[0].toLowerCase();
        final String file;
        final SchematicHandler.Schematic schematic;
        switch (arg) {
            case "paste":
                if (plr == null) {
                    PlotMain.sendConsoleSenderMessage(C.IS_CONSOLE);
                    return false;
                }
                if (!PlotMain.hasPermission(plr, "plots.schematic.paste")) {
                    PlayerFunctions.sendMessage(plr, C.NO_PERMISSION, "plots.schematic.paste");
                    return false;
                }
                if (args.length < 2) {
                    sendMessage(plr, C.SCHEMATIC_MISSING_ARG);
                    break;
                }
                if (!PlayerFunctions.isInPlot(plr)) {
                    sendMessage(plr, C.NOT_IN_PLOT);
                    break;
                }
                if (this.running) {
                    PlayerFunctions.sendMessage(plr, "&cTask is already running.");
                    return false;
                }
                final String file2 = args[1];
                this.running = true;
                this.counter = 0;
                Bukkit.getScheduler().runTaskAsynchronously(Bukkit.getServer().getPluginManager().getPlugin("PlotSquared"), new Runnable() {
                    @Override
                    public void run() {
                        final SchematicHandler.Schematic schematic = SchematicHandler.getSchematic(file2);
                        if (schematic == null) {
                            sendMessage(plr, C.SCHEMATIC_INVALID, "non-existent or not in gzip format");
                            Schematic.this.running = false;
                            return;
                        }

                        final int x;
                        final int z;

                        final Plot plot2 = PlayerFunctions.getCurrentPlot(plr);

                        final Dimension dem = schematic.getSchematicDimension();
                        final Location bot = PlotHelper.getPlotBottomLoc(plr.getWorld(), plot2.id).add(1, 0, 1);
                        final int length2 = PlotHelper.getPlotWidth(plr.getWorld(), plot2.id);

                        if ((dem.getX() > length2) || (dem.getZ() > length2)) {
                            sendMessage(plr, C.SCHEMATIC_INVALID, String.format("Wrong size (x: %s, z: %d) vs %d ", dem.getX(), dem.getZ(), length2));
                            Schematic.this.running = false;
                            return;
                        }

                        if ((dem.getX() != length2) || (dem.getZ() != length2)) {
                            final Location loc = plr.getLocation();
                            x = Math.min(length2 - dem.getX(), loc.getBlockX() - bot.getBlockX());
                            z = Math.min(length2 - dem.getZ(), loc.getBlockZ() - bot.getBlockZ());
                        } else {
                            x = 0;
                            z = 0;
                        }

                        final World w = plot2.getWorld();
                        final DataCollection[] b = schematic.getBlockCollection();
                        final int sy = w.getHighestBlockYAt(bot.getBlockX(), bot.getBlockZ());
                        final Location l1 = bot.add(0, sy - 1, 0);
                        final int WIDTH = schematic.getSchematicDimension().getX();
                        final int LENGTH = schematic.getSchematicDimension().getZ();
                        final int blen = b.length - 1;

                        final Plugin plugin = Bukkit.getServer().getPluginManager().getPlugin("PlotSquared");
                        Schematic.this.task = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable() {
                            @Override
                            public void run() {
                                boolean result = false;
                                while (!result) {
                                    final int start = Schematic.this.counter * 5000;
                                    if (start > blen) {
                                        sendMessage(plr, C.SCHEMATIC_PASTE_SUCCESS);
                                        if (PlotHelper.canSetFast) {
                                            SetBlockFast.update(plr);
                                        }
                                        Schematic.this.running = false;
                                        Bukkit.getScheduler().cancelTask(Schematic.this.task);
                                        return;
                                    }
                                    final int end = Math.min(start + 5000, blen);
                                    result = SchematicHandler.pastePart(w, b, l1, x, z, start, end, WIDTH, LENGTH);
                                    Schematic.this.counter++;
                                }
                            }
                        }, 1, 1);
                    }
                });
                break;
            case "test":
                if (plr == null) {
                    PlotMain.sendConsoleSenderMessage(C.IS_CONSOLE);
                    return false;
                }
                if (!PlotMain.hasPermission(plr, "plots.schematic.test")) {
                    PlayerFunctions.sendMessage(plr, C.NO_PERMISSION, "plots.schematic.test");
                    return false;
                }
                if (args.length < 2) {
                    sendMessage(plr, C.SCHEMATIC_MISSING_ARG);
                    break;
                }
                file = args[1];
                schematic = SchematicHandler.getSchematic(file);
                if (schematic == null) {
                    sendMessage(plr, C.SCHEMATIC_INVALID, "non-existent");
                    break;
                }

                final int l1 = schematic.getSchematicDimension().getX();
                final int l2 = schematic.getSchematicDimension().getZ();

                final Plot plot = PlayerFunctions.getCurrentPlot(plr);
                final int length = PlotHelper.getPlotWidth(plr.getWorld(), plot.id);

                if ((l1 < length) || (l2 < length)) {
                    sendMessage(plr, C.SCHEMATIC_INVALID, String.format("Wrong size (x: %s, z: %d) vs %d ", l1, l2, length));
                    break;
                }
                sendMessage(plr, C.SCHEMATIC_VALID);
                break;
            case "saveall":
            case "exportall":
                if (plr != null) {
                    PlayerFunctions.sendMessage(plr, C.NOT_CONSOLE);
                    return false;
                }
                if (args.length != 2) {
                    PlayerFunctions.sendMessage(null, "&cNeed world arg. Use &7/plots sch exportall <world>");
                    return false;
                }
                final HashMap<PlotId, Plot> plotmap = PlotMain.getPlots(args[1]);
                if ((plotmap == null) || (plotmap.size() == 0)) {
                    PlayerFunctions.sendMessage(null, "&cInvalid world. Use &7/plots sch exportall <world>");
                    return false;
                }
                if (this.running) {
                    PlayerFunctions.sendMessage(null, "&cTask is already running.");
                    return false;
                }

                PlotMain.sendConsoleSenderMessage("&3PlotSquared&8->&3Schemaitc&8: &7Mass export has started. This may take a while.");
                PlotMain.sendConsoleSenderMessage("&3PlotSquared&8->&3Schemaitc&8: &7Found &c" + plotmap.size() + "&7 plots...");
                final World worldObj = Bukkit.getWorld(args[1]);
                final String worldname = Bukkit.getWorld(args[1]).getName();

                final Plugin plugin = Bukkit.getServer().getPluginManager().getPlugin("PlotSquared");

                Collection<Plot> values = plotmap.values();
                this.plots = values.toArray(new Plot[values.size()]);
                this.running = true;
                this.counter = 0;

                this.task = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable() {
                    @Override
                    public void run() {
                        if (Schematic.this.counter >= Schematic.this.plots.length) {
                            PlotMain.sendConsoleSenderMessage("&3PlotSquared&8->&3Schemaitc&8: &aFinished!");
                            Schematic.this.running = false;
                            Bukkit.getScheduler().cancelTask(Schematic.this.task);
                            return;
                        }
                        final Plot plot = Schematic.this.plots[Schematic.this.counter];
                        final CompoundTag sch = SchematicHandler.getCompoundTag(worldObj, plot.id);
                        final String o = UUIDHandler.getName(plot.owner);
                        final String owner = o == null ? "unknown" : o;
                        if (sch == null) {
                            PlayerFunctions.sendMessage(null, "&7 - Skipped plot &c" + plot.id);
                        } else {
                            Bukkit.getScheduler().runTaskAsynchronously(Bukkit.getServer().getPluginManager().getPlugin("PlotSquared"), new Runnable() {
                                @Override
                                public void run() {
                                    PlayerFunctions.sendMessage(null, "&6ID: " + plot.id);
                                    final boolean result = SchematicHandler.save(sch, Settings.SCHEMATIC_SAVE_PATH + "/" + plot.id.x + ";" + plot.id.y + "," + worldname + "," + owner + ".schematic");
                                    if (!result) {
                                        PlayerFunctions.sendMessage(null, "&7 - Failed to save &c" + plot.id);
                                    } else {
                                        PlayerFunctions.sendMessage(null, "&7 - &aExport success: " + plot.id);
                                    }
                                }
                            });
                        }
                        Schematic.this.counter++;
                    }
                }, 20, 20);
                break;
            case "export":
            case "save":
                if (!PlotMain.hasPermission(plr, "plots.schematic.save")) {
                    PlayerFunctions.sendMessage(plr, C.NO_PERMISSION, "plots.schematic.save");
                    return false;
                }
                if (this.running) {
                    PlayerFunctions.sendMessage(plr, "&cTask is already running.");
                    return false;
                }
                final String world;
                final Plot p2;
                if (plr != null) {
                    if (!PlayerFunctions.isInPlot(plr)) {
                        sendMessage(plr, C.NOT_IN_PLOT);
                        return false;
                    }
                    final Plot myplot = PlayerFunctions.getCurrentPlot(plr);
                    if (!myplot.hasRights(plr)) {
                        sendMessage(plr, C.NO_PLOT_PERMS);
                        return false;
                    }
                    p2 = myplot;
                    world = plr.getWorld().getName();
                } else {
                    if (args.length == 3) {
                        try {
                            world = args[0];
                            final String[] split = args[2].split(";");
                            final PlotId i = new PlotId(Integer.parseInt(split[0]), Integer.parseInt(split[1]));
                            if ((PlotMain.getPlots(world) == null) || (PlotMain.getPlots(world).get(i) == null)) {
                                PlayerFunctions.sendMessage(null, "&cInvalid world or id. Use &7/plots sch save <world> <id>");
                                return false;
                            }
                            p2 = PlotMain.getPlots(world).get(i);
                        } catch (final Exception e) {
                            PlayerFunctions.sendMessage(null, "&cInvalid world or id. Use &7/plots sch save <world> <id>");
                            return false;
                        }
                    } else {
                        PlayerFunctions.sendMessage(null, "&cInvalid world or id. Use &7/plots sch save <world> <id>");
                        return false;
                    }
                }

                final Plugin plugin2 = Bukkit.getServer().getPluginManager().getPlugin("PlotSquared");

                this.plots = new Plot[]{p2};
                this.running = true;
                this.counter = 0;

                this.task = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin2, new Runnable() {
                    @Override
                    public void run() {
                        if (Schematic.this.counter >= Schematic.this.plots.length) {
                            PlotMain.sendConsoleSenderMessage("&3PlotSquared&8->&3Schemaitc&8: &aFinished!");
                            Schematic.this.running = false;
                            Bukkit.getScheduler().cancelTask(Schematic.this.task);
                            return;
                        }
                        final Plot plot = Schematic.this.plots[Schematic.this.counter];
                        final CompoundTag sch = SchematicHandler.getCompoundTag(Bukkit.getWorld(world), plot.id);
                        final String o = UUIDHandler.getName(plot.owner);
                        final String owner = o == null ? "unknown" : o;
                        if (sch == null) {
                            PlayerFunctions.sendMessage(plr, "&7 - Skipped plot &c" + plot.id);
                        } else {
                            Bukkit.getScheduler().runTaskAsynchronously(Bukkit.getServer().getPluginManager().getPlugin("PlotSquared"), new Runnable() {
                                @Override
                                public void run() {
                                    PlayerFunctions.sendMessage(plr, "&6ID: " + plot.id);
                                    final boolean result = SchematicHandler.save(sch, Settings.SCHEMATIC_SAVE_PATH + "/" + plot.id.x + ";" + plot.id.y + "," + world + "," + owner.trim() + ".schematic");
                                    if (!result) {
                                        PlayerFunctions.sendMessage(plr, "&7 - Failed to save &c" + plot.id);
                                    } else {
                                        PlayerFunctions.sendMessage(plr, "&7 - &aExport success: " + plot.id);
                                    }
                                }
                            });
                        }
                        Schematic.this.counter++;
                    }
                }, 20, 60);
                break;
            default:
                sendMessage(plr, C.SCHEMATIC_MISSING_ARG);
                break;
        }
        return true;
    }
}
