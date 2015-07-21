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

import java.util.ArrayList;

import com.intellectualcrafters.plot.PS;
import com.intellectualcrafters.plot.config.C;
import com.intellectualcrafters.plot.object.Plot;
import com.intellectualcrafters.plot.object.PlotPlayer;
import com.intellectualcrafters.plot.util.MainUtil;

/**
 * @author Citymonstret
 */
public class Home extends SubCommand {
    public Home() {
        super(Command.HOME, "Go to your plot", "home {id|alias}", CommandCategory.TELEPORT, true);
    }

    private Plot isAlias(final String a) {
        for (final Plot p : PS.get().getPlots()) {
            if ((p.getSettings().getAlias().length() > 0) && p.getSettings().getAlias().equalsIgnoreCase(a)) {
                return p;
            }
        }
        return null;
    }

    @Override
    public boolean execute(final PlotPlayer plr, String... args) {
        final ArrayList<Plot> plots = PS.get().sortPlotsByWorld(PS.get().getPlots(plr));
        if (plots.size() == 1) {
            MainUtil.teleportPlayer(plr, plr.getLocation(), plots.get(0));
            return true;
        } else if (plots.size() > 1) {
            if (args.length < 1) {
                args = new String[] { "1" };
            }
            int id = 0;
            try {
                id = Integer.parseInt(args[0]);
            } catch (final Exception e) {
                Plot temp;
                if ((temp = isAlias(args[0])) != null) {
                    if (temp.hasOwner()) {
                        if (temp.isOwner(plr.getUUID())) {
                            MainUtil.teleportPlayer(plr, plr.getLocation(), temp);
                            return true;
                        }
                    }
                    MainUtil.sendMessage(plr, C.NOT_YOUR_PLOT);
                    return false;
                }
                MainUtil.sendMessage(plr, C.NOT_VALID_NUMBER, "(1, " + plots.size() + ")");
                return true;
            }
            if ((id > (plots.size())) || (id < 1)) {
                MainUtil.sendMessage(plr, C.NOT_VALID_NUMBER, "(1, " + plots.size() + ")");
                return false;
            }
            MainUtil.teleportPlayer(plr, plr.getLocation(), plots.get(id - 1));
            return true;
        } else {
            MainUtil.sendMessage(plr, C.FOUND_NO_PLOTS);
            return true;
        }
    }

    public void teleportPlayer(final PlotPlayer player, final Plot plot) {
        MainUtil.teleportPlayer(player, player.getLocation(), plot);
    }
}
