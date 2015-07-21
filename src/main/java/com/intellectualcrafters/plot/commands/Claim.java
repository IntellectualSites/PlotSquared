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

import com.intellectualcrafters.plot.PS;
import com.intellectualcrafters.plot.config.C;
import com.intellectualcrafters.plot.config.Settings;
import com.intellectualcrafters.plot.object.Location;
import com.intellectualcrafters.plot.object.Plot;
import com.intellectualcrafters.plot.object.PlotPlayer;
import com.intellectualcrafters.plot.object.PlotWorld;
import com.intellectualcrafters.plot.util.EconHandler;
import com.intellectualcrafters.plot.util.EventUtil;
import com.intellectualcrafters.plot.util.MainUtil;
import com.intellectualcrafters.plot.util.Permissions;
import com.intellectualcrafters.plot.util.SchematicHandler;
import com.intellectualcrafters.plot.util.SchematicHandler.Schematic;

/**
 * @author Citymonstret
 */
public class Claim extends SubCommand {
    public Claim() {
        super(Command.CLAIM, "Claim the current plot you're standing on.", "claim", CommandCategory.CLAIMING, true);
    }

    public static boolean claimPlot(final PlotPlayer player, final Plot plot, final boolean teleport, final boolean auto) {
        return claimPlot(player, plot, teleport, "", auto);
    }

    public static boolean claimPlot(final PlotPlayer player, final Plot plot, final boolean teleport, final String schematic, final boolean auto) {
        if (plot.hasOwner() || plot.getSettings().isMerged()) {
            return false;
        }
        final boolean result = EventUtil.manager.callClaim(player, plot, false);
        if (result) {
            MainUtil.createPlot(player.getUUID(), plot);
            MainUtil.setSign(player.getName(), plot);
            MainUtil.sendMessage(player, C.CLAIMED);
            final Location loc = player.getLocation();
            if (teleport) {
                MainUtil.teleportPlayer(player, loc, plot);
            }
            final String world = plot.world;
            final PlotWorld plotworld = PS.get().getPlotWorld(world);
            final Plot plot2 = PS.get().getPlots(world).get(plot.id);
            if (plotworld.SCHEMATIC_ON_CLAIM) {
                Schematic sch;
                if (schematic.equals("")) {
                    sch = SchematicHandler.manager.getSchematic(plotworld.SCHEMATIC_FILE);
                } else {
                    sch = SchematicHandler.manager.getSchematic(schematic);
                    if (sch == null) {
                        sch = SchematicHandler.manager.getSchematic(plotworld.SCHEMATIC_FILE);
                    }
                }
                SchematicHandler.manager.paste(sch, plot2, 0, 0);
            }
            PS.get().getPlotManager(world).claimPlot(plotworld, plot);
        }
        return result;
    }

    @Override
    public boolean execute(final PlotPlayer plr, final String... args) {
        String schematic = "";
        if (args.length >= 1) {
            schematic = args[0];
        }
        final Location loc = plr.getLocation();
        final Plot plot = MainUtil.getPlot(loc);
        if (plot == null) {
            return sendMessage(plr, C.NOT_IN_PLOT);
        }
        final int currentPlots = Settings.GLOBAL_LIMIT ? MainUtil.getPlayerPlotCount(plr) : MainUtil.getPlayerPlotCount(loc.getWorld(), plr);
        if (currentPlots >= MainUtil.getAllowedPlots(plr)) {
            return sendMessage(plr, C.CANT_CLAIM_MORE_PLOTS);
        }
        if (!MainUtil.canClaim(plr, plot)) {
            return sendMessage(plr, C.PLOT_IS_CLAIMED);
        }
        final PlotWorld world = PS.get().getPlotWorld(plot.world);
        if ((EconHandler.manager != null) && world.USE_ECONOMY) {
            final double cost = world.PLOT_PRICE;
            if (cost > 0d) {
                if (EconHandler.manager.getMoney(plr) < cost) {
                    return sendMessage(plr, C.CANNOT_AFFORD_PLOT, "" + cost);
                }
                EconHandler.manager.withdrawMoney(plr, cost);
                sendMessage(plr, C.REMOVED_BALANCE, cost + "");
            }
        }
        if (!schematic.equals("")) {
            if (world.SCHEMATIC_CLAIM_SPECIFY) {
                if (!world.SCHEMATICS.contains(schematic.toLowerCase())) {
                    return sendMessage(plr, C.SCHEMATIC_INVALID, "non-existent: " + schematic);
                }
                if (!Permissions.hasPermission(plr, "plots.claim." + schematic) && !plr.hasPermission("plots.admin.command.schematic")) {
                    return sendMessage(plr, C.NO_SCHEMATIC_PERMISSION, schematic);
                }
            }
        }
        return claimPlot(plr, plot, false, schematic, false) || sendMessage(plr, C.PLOT_NOT_CLAIMED);
    }
}
