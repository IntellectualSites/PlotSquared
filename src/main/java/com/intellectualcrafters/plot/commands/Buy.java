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

import java.util.Set;

import com.intellectualcrafters.plot.PS;
import com.intellectualcrafters.plot.config.C;
import com.intellectualcrafters.plot.flag.Flag;
import com.intellectualcrafters.plot.flag.FlagManager;
import com.intellectualcrafters.plot.object.Location;
import com.intellectualcrafters.plot.object.Plot;
import com.intellectualcrafters.plot.object.PlotId;
import com.intellectualcrafters.plot.object.PlotPlayer;
import com.intellectualcrafters.plot.util.EconHandler;
import com.intellectualcrafters.plot.util.MainUtil;
import com.intellectualcrafters.plot.util.UUIDHandler;
import com.plotsquared.general.commands.CommandDeclaration;

@CommandDeclaration(
command = "buy",
aliases = { "b" },
description = "Buy the plot you are standing on",
usage = "/plot buy",
permission = "plots.buy",
category = CommandCategory.CLAIMING,
requiredType = RequiredType.NONE)
public class Buy extends SubCommand {
    
    @Override
    public boolean onCommand(final PlotPlayer plr, final String... args) {
        if (EconHandler.manager == null) {
            return sendMessage(plr, C.ECON_DISABLED);
        }
        final Location loc = plr.getLocation();
        final String world = loc.getWorld();
        if (!PS.get().isPlotWorld(world)) {
            return sendMessage(plr, C.NOT_IN_PLOT_WORLD);
        }
        Set<Plot> plots;
        Plot plot;
        if (args.length > 0) {
            try {
                final String[] split = args[0].split(";");
                final PlotId id = new PlotId(Integer.parseInt(split[0]), Integer.parseInt(split[1]));
                plot = MainUtil.getPlotAbs(world, id);
                plots = MainUtil.getConnectedPlots(plot);
            } catch (final Exception e) {
                return sendMessage(plr, C.NOT_VALID_PLOT_ID);
            }
        } else {
            plot = MainUtil.getPlotAbs(loc);
            plots = MainUtil.getConnectedPlots(plot);
        }
        if (plots == null) {
            return sendMessage(plr, C.NOT_IN_PLOT);
        }
        if (!plot.hasOwner()) {
            return sendMessage(plr, C.PLOT_UNOWNED);
        }
        final int currentPlots = MainUtil.getPlayerPlotCount(plr) + plots.size();
        if (currentPlots > MainUtil.getAllowedPlots(plr)) {
            return sendMessage(plr, C.CANT_CLAIM_MORE_PLOTS);
        }
        final Flag flag = FlagManager.getPlotFlagRaw(plot, "price");
        if (flag == null) {
            return sendMessage(plr, C.NOT_FOR_SALE);
        }
        if (plot.isOwner(plr.getUUID())) {
            return sendMessage(plr, C.CANNOT_BUY_OWN);
        }
        double price = (double) flag.getValue();
        if ((EconHandler.manager != null) && (price > 0d)) {
            if (EconHandler.manager.getMoney(plr) < price) {
                return sendMessage(plr, C.CANNOT_AFFORD_PLOT, "" + price);
            }
            EconHandler.manager.withdrawMoney(plr, price);
            sendMessage(plr, C.REMOVED_BALANCE, price + "");
            EconHandler.manager.depositMoney(UUIDHandler.getUUIDWrapper().getOfflinePlayer(plot.owner), price);
            final PlotPlayer owner = UUIDHandler.getPlayer(plot.owner);
            if (owner != null) {
                sendMessage(plr, C.PLOT_SOLD, plot.id + "", plr.getName(), price + "");
            }
            FlagManager.removePlotFlag(plot, "price");
        }
        for (final Plot current : plots) {
            plot.setOwner(plr.getUUID());
        }
        MainUtil.sendMessage(plr, C.CLAIMED);
        return true;
    }
}
