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
import com.intellectualcrafters.plot.config.Settings;
import com.intellectualcrafters.plot.object.Location;
import com.intellectualcrafters.plot.object.Plot;
import com.intellectualcrafters.plot.object.PlotArea;
import com.intellectualcrafters.plot.object.PlotPlayer;
import com.intellectualcrafters.plot.util.ByteArrayUtilities;
import com.intellectualcrafters.plot.util.EconHandler;
import com.intellectualcrafters.plot.util.Permissions;
import com.plotsquared.general.commands.CommandDeclaration;

@CommandDeclaration(command = "claim", aliases = "c", description = "Claim the current plot you're standing on", category = CommandCategory.CLAIMING,
        requiredType = RequiredType.NONE, permission = "plots.claim", usage = "/plot claim")
public class Claim extends SubCommand {
    @Override
    public boolean onCommand(final PlotPlayer plr, final String... args) {
        String schematic = "";
        if (args.length >= 1) {
            schematic = args[0];
        }
        final Location loc = plr.getLocation();
        final Plot plot = loc.getPlotAbs();
        if (plot == null) {
            return sendMessage(plr, C.NOT_IN_PLOT);
        }
        final int currentPlots = Settings.GLOBAL_LIMIT ? plr.getPlotCount() : plr.getPlotCount(loc.getWorld());
        int grants = 0;
        if (currentPlots >= plr.getAllowedPlots()) {
            if (plr.hasPersistentMeta("grantedPlots")) {
                grants = ByteArrayUtilities.bytesToInteger(plr.getPersistentMeta("grantedPlots"));
                if (grants <= 0) {
                    plr.removePersistentMeta("grantedPlots");
                    return sendMessage(plr, C.CANT_CLAIM_MORE_PLOTS);
                }
            } else {
                return sendMessage(plr, C.CANT_CLAIM_MORE_PLOTS);
            }
        }
        if (!plot.canClaim(plr)) {
            return sendMessage(plr, C.PLOT_IS_CLAIMED);
        }
        final PlotArea world = plot.getArea();
        if ((EconHandler.manager != null) && world.USE_ECONOMY) {
            final double cost = world.PRICES.get("claim");
            if (cost > 0d) {
                if (EconHandler.manager.getMoney(plr) < cost) {
                    return sendMessage(plr, C.CANNOT_AFFORD_PLOT, "" + cost);
                }
                EconHandler.manager.withdrawMoney(plr, cost);
                sendMessage(plr, C.REMOVED_BALANCE, cost + "");
            }
        }
        if (grants > 0) {
            if (grants == 1) {
                plr.removePersistentMeta("grantedPlots");
            } else {
                plr.setPersistentMeta("grantedPlots", ByteArrayUtilities.integerToBytes(grants - 1));
            }
            sendMessage(plr, C.REMOVED_GRANTED_PLOT, "1", "" + (grants - 1));
        }
        if (!schematic.isEmpty()) {
            if (world.SCHEMATIC_CLAIM_SPECIFY) {
                if (!world.SCHEMATICS.contains(schematic.toLowerCase())) {
                    return sendMessage(plr, C.SCHEMATIC_INVALID, "non-existent: " + schematic);
                }
                if (!Permissions.hasPermission(plr, "plots.claim." + schematic) && !Permissions.hasPermission(plr, "plots.admin.command.schematic")) {
                    return sendMessage(plr, C.NO_SCHEMATIC_PERMISSION, schematic);
                }
            }
        }
        return plot.claim(plr, false, schematic) || sendMessage(plr, C.PLOT_NOT_CLAIMED);
    }
}
