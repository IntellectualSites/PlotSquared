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

import com.intellectualcrafters.plot.*;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Visit extends SubCommand {
    public Visit() {
        super("visit", "plots.visit", "Visit someones plot", "visit {player} [#]", "v", CommandCategory.TELEPORT, true);
    }

    public List<Plot> getPlots(final UUID uuid) {
        final List<Plot> plots = new ArrayList<>();
        for (final Plot p : PlotMain.getPlots()) {
            if (p.hasOwner() && p.owner.equals(uuid)) {
                plots.add(p);
            }
        }
        return plots;
    }

    @Override
    public boolean execute(final Player plr, final String... args) {
        if (args.length < 1) {
            PlayerFunctions.sendMessage(plr, C.NEED_USER);
            return true;
        }
        final String username = args[0];

        final UUID uuid = UUIDHandler.getUUID(username);
        List<Plot> plots = null;
        if (uuid != null) {
            plots = getPlots(uuid);
        }
        if ((uuid == null) || plots.isEmpty()) {
            PlayerFunctions.sendMessage(plr, C.FOUND_NO_PLOTS);
            return true;
        }
        if (args.length < 2) {
            final Plot plot = plots.get(0);
            PlotMain.teleportPlayer(plr, plr.getLocation(), plot);
            return true;
        }
        int i;
        try {
            i = Integer.parseInt(args[1]);
        } catch (final Exception e) {
            PlayerFunctions.sendMessage(plr, C.NOT_VALID_NUMBER);
            return true;
        }
        if ((i < 0) || (i >= plots.size())) {
            PlayerFunctions.sendMessage(plr, C.NOT_VALID_NUMBER);
            return true;
        }
        final Plot plot = plots.get(i);
        PlotMain.teleportPlayer(plr, plr.getLocation(), plot);
        return true;
    }
}
