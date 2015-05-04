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

import com.intellectualcrafters.plot.config.C;
import com.intellectualcrafters.plot.object.Location;
import com.intellectualcrafters.plot.object.Plot;
import com.intellectualcrafters.plot.object.PlotBlock;
import com.intellectualcrafters.plot.object.PlotPlayer;
import com.intellectualcrafters.plot.util.MainUtil;
import com.intellectualcrafters.plot.util.Permissions;
import com.intellectualcrafters.plot.util.SetBlockQueue;
import com.intellectualcrafters.plot.util.TaskManager;

public class DebugFill extends SubCommand {
    public DebugFill() {
        super("fill", "plots.fill", "Fill or surround a plot in bedrock", "fill", "debugfill", CommandCategory.DEBUG, true);
    }

    @Override
    public boolean execute(final PlotPlayer player, final String... args) {
        if (args.length != 1 || (!args[0].equalsIgnoreCase("outline") && !args[0].equalsIgnoreCase("all"))) {
            MainUtil.sendMessage(player, C.COMMAND_SYNTAX, "/plot fill <outline|all>");
            return true;
        }
        final Location loc = player.getLocation();
        final Plot plot = MainUtil.getPlot(loc);
        if (plot == null) {
            return !sendMessage(player, C.NOT_IN_PLOT);
        }
        if ((plot == null) || !plot.hasOwner()) {
            MainUtil.sendMessage(player, C.PLOT_UNOWNED);
            return false;
        }
        if (!plot.isOwner(player.getUUID()) && !Permissions.hasPermission(player, "plots.admin.command.fill")) {
            MainUtil.sendMessage(player, C.NO_PLOT_PERMS);
            return true;
        }
        final Location bottom = MainUtil.getPlotBottomLoc(plot.world, plot.id).add(1, 0, 1);
        final Location top = MainUtil.getPlotTopLoc(plot.world, plot.id);
        MainUtil.sendMessage(player, "&cPreparing task");
        SetBlockQueue.addNotify(new Runnable() {
            @Override
            public void run() {
                TaskManager.runTaskAsync(new Runnable() {
                    @Override
                    public void run() {
                        MainUtil.sendMessage(player, "&7 - Starting");
                        if (args[0].equalsIgnoreCase("all")) {
                            for (int x = bottom.getX(); x <= top.getX(); x++) {
                                for (int y = 0; y <= 255; y++) {
                                    for (int z = bottom.getZ(); z <= top.getZ(); z++) {
                                        SetBlockQueue.setBlock(plot.world, x, y, z, 7);
                                    }
                                }
                            }
                            SetBlockQueue.addNotify(new Runnable() {
                                @Override
                                public void run() {
                                    MainUtil.sendMessage(player, "&aFill task complete!");
                                }
                            });
                        }
                        else if (args[0].equals("outline")) {
                            int x, z;
                            z = bottom.getZ();
                            for (x = bottom.getX(); x <= (top.getX() - 1); x++) {
                                for (int y = 1; y <= 255; y++) {
                                    SetBlockQueue.setBlock(plot.world, x, y, z, 7);
                                }
                            }
                            x = top.getX();
                            for (z = bottom.getZ(); z <= (top.getZ() - 1); z++) {
                                for (int y = 1; y <= 255; y++) {
                                    SetBlockQueue.setBlock(plot.world, x, y, z, 7);
                                }
                            }
                            z = top.getZ();
                            for (x = top.getX(); x >= (bottom.getX() + 1); x--) {
                                for (int y = 1; y <= 255; y++) {
                                    SetBlockQueue.setBlock(plot.world, x, y, z, 7);
                                }
                            }
                            x = bottom.getX();
                            for (z = top.getZ(); z >= (bottom.getZ() + 1); z--) {
                                for (int y = 1; y <= 255; y++) {
                                    SetBlockQueue.setBlock(plot.world, x, y, z, 7);
                                }
                            }
                            SetBlockQueue.addNotify(new Runnable() {
                                @Override
                                public void run() {
                                    MainUtil.sendMessage(player, "&aWalls complete! The ceiling will take a while :(");
                                    bottom.setY(255);
                                    top.add(1,0,1);
                                    SetBlockQueue.setSlow(true);
                                    MainUtil.setSimpleCuboidAsync(plot.world, bottom, top, new PlotBlock((short) 7, (byte) 0));
                                    SetBlockQueue.addNotify(new Runnable() {
                                        @Override
                                        public void run() {
                                            MainUtil.sendMessage(player, "&aFill task complete!");
                                            SetBlockQueue.setSlow(false);
                                        }
                                    });
                                }
                            });
                        }
                    }
                });
            }
        });
        return true;
    }
}
