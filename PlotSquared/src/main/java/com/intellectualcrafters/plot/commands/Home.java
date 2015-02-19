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

import org.bukkit.entity.Player;

import com.intellectualcrafters.plot.PlotSquared;
import com.intellectualcrafters.plot.config.C;
import com.intellectualcrafters.plot.object.Plot;
import com.intellectualcrafters.plot.util.PlayerFunctions;
import com.intellectualcrafters.plot.util.UUIDHandler;

/**
 * @author Citymonstret
 */
public class Home extends SubCommand {

    public Home() {
        super(Command.HOME, "Go to your plot", "home {id|alias}", CommandCategory.TELEPORT, true);
    }

    private Plot isAlias(final String a) {
        for (final Plot p : PlotSquared.getPlots()) {
            if ((p.settings.getAlias().length() > 0) && p.settings.getAlias().equalsIgnoreCase(a)) {
                return p;
            }
        }
        return null;
    }

    @Override
    public boolean execute(final Player plr, String... args) {
        final Plot[] plots = PlotSquared.getPlots(plr).toArray(new Plot[0]);
        if (plots.length == 1) {
            PlotSquared.teleportPlayer(plr, plr.getLocation(), plots[0]);
            return true;
        } else if (plots.length > 1) {
            if (args.length < 1) {
                args = new String[]{"1"};
            }
            int id = 0;
            try {
                id = Integer.parseInt(args[0]);
            } catch (final Exception e) {
                Plot temp;
                if ((temp = isAlias(args[0])) != null) {
                    if (temp.hasOwner()) {
                        if (temp.getOwner().equals(UUIDHandler.getUUID(plr))) {
                            teleportPlayer(plr, temp);
                            return true;
                        }
                    }
                    PlayerFunctions.sendMessage(plr, C.NOT_YOUR_PLOT);
                    return false;
                }
                PlayerFunctions.sendMessage(plr, C.NOT_VALID_NUMBER);
                return true;
            }
            if ((id > (plots.length)) || (id < 1)) {
                PlayerFunctions.sendMessage(plr, C.NOT_VALID_NUMBER);
                return false;
            }
            teleportPlayer(plr, plots[id - 1]);
            return true;
        } else {
            PlayerFunctions.sendMessage(plr, C.NO_PLOTS);
            return true;
        }
    }
    
    public void teleportPlayer(Player player, Plot plot) {
    	PlotSquared.teleportPlayer(player, player.getLocation(), plot);
    }
    
}
