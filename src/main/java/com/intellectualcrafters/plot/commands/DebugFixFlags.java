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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import com.intellectualcrafters.plot.PS;
import com.intellectualcrafters.plot.config.C;
import com.intellectualcrafters.plot.database.DBFunc;
import com.intellectualcrafters.plot.flag.Flag;
import com.intellectualcrafters.plot.flag.FlagManager;
import com.intellectualcrafters.plot.object.Plot;
import com.intellectualcrafters.plot.object.PlotPlayer;
import com.intellectualcrafters.plot.util.BlockManager;
import com.intellectualcrafters.plot.util.MainUtil;
import com.plotsquared.general.commands.Argument;
import com.plotsquared.general.commands.CommandDeclaration;

@CommandDeclaration(
command = "debugfixflags",
usage = "/plot debugfixflags <world>",
permission = "plots.debugfixflags",
description = "Attempt to fix all flags for a world",
requiredType = RequiredType.CONSOLE,
category = CommandCategory.DEBUG)
public class DebugFixFlags extends SubCommand {
    
    public DebugFixFlags() {
        requiredArguments = new Argument[] { Argument.String };
    }
    
    @Override
    public boolean onCommand(final PlotPlayer plr, final String[] args) {
        final String world = args[0];
        if (!BlockManager.manager.isWorld(world) || !PS.get().isPlotWorld(world)) {
            MainUtil.sendMessage(plr, C.NOT_VALID_PLOT_WORLD, args[0]);
            return false;
        }
        MainUtil.sendMessage(plr, "&8--- &6Starting task &8 ---");
        for (final Plot plot : PS.get().getPlotsInWorld(world)) {
            final HashMap<String, Flag> flags = plot.getSettings().flags;
            final Iterator<Entry<String, Flag>> i = flags.entrySet().iterator();
            boolean changed = false;
            while (i.hasNext()) {
                if (FlagManager.getFlag(i.next().getKey()) == null) {
                    changed = true;
                    i.remove();
                }
            }
            if (changed) {
                DBFunc.setFlags(plot, plot.getSettings().flags.values());
            }
        }
        MainUtil.sendMessage(plr, "&aDone!");
        return true;
    }
}
