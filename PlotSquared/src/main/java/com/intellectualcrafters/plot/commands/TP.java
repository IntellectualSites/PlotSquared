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

import org.apache.commons.lang.StringUtils;

import com.intellectualcrafters.plot.PlotSquared;
import com.intellectualcrafters.plot.config.C;
import com.intellectualcrafters.plot.object.Plot;
import com.intellectualcrafters.plot.object.PlotId;
import com.intellectualcrafters.plot.object.PlotPlayer;
import com.intellectualcrafters.plot.util.MainUtil;

/**
 * @author Citymonstret
 */
public class TP extends SubCommand {
    public TP() {
        super(Command.TP, "Teleport to a plot", "tp {alias|id}", CommandCategory.TELEPORT, true);
    }
    
    @Override
    public boolean execute(final PlotPlayer plr, final String... args) {
        if (args.length < 1) {
            MainUtil.sendMessage(plr, C.NEED_PLOT_ID);
            return false;
        }
        final String id = args[0];
        PlotId plotid;
        World world = plr.getWorld();
        if (args.length == 2) {
            if (Bukkit.getWorld(args[1]) != null) {
                world = Bukkit.getWorld(args[1]);
            }
        }
        if (!PlotSquared.isPlotWorld(world)) {
            MainUtil.sendMessage(plr, C.NOT_IN_PLOT_WORLD);
            return false;
        }
        Plot temp;
        if ((temp = isAlias(world, id)) != null) {
            MainUtil.teleportPlayer(plr, plr.getLocation(), temp);
            return true;
        }
        try {
            plotid = new PlotId(Integer.parseInt(id.split(";")[0]), Integer.parseInt(id.split(";")[1]));
            MainUtil.teleportPlayer(plr, plr.getLocation(), MainUtil.getPlot(world, plotid));
            return true;
        } catch (final Exception e) {
            MainUtil.sendMessage(plr, C.NOT_VALID_PLOT_ID);
        }
        return false;
    }
    
    private Plot isAlias(final World world, String a) {
        int index = 0;
        if (a.contains(";")) {
            final String[] split = a.split(";");
            if ((split[1].length() > 0) && StringUtils.isNumeric(split[1])) {
                index = Integer.parseInt(split[1]);
            }
            a = split[0];
        }
        @SuppressWarnings("deprecation")
        final Player player = Bukkit.getPlayer(a);
        if (player != null) {
            final java.util.Set<Plot> plotMainPlots = PlotSquared.getPlots(world, player);
            final Plot[] plots = plotMainPlots.toArray(new Plot[plotMainPlots.size()]);
            if (plots.length > index) {
                return plots[index];
            }
            return null;
        }
        for (final Plot p : PlotSquared.getPlots(world).values()) {
            if ((p.settings.getAlias().length() > 0) && p.settings.getAlias().equalsIgnoreCase(a)) {
                return p;
            }
        }
        return null;
    }
}
