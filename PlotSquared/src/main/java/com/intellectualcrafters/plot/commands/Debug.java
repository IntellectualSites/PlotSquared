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

import org.bukkit.Bukkit;
import org.bukkit.World;

import com.intellectualcrafters.plot.PlotSquared;
import com.intellectualcrafters.plot.config.C;
import com.intellectualcrafters.plot.object.PlotPlayer;
import com.intellectualcrafters.plot.util.Lag;
import com.intellectualcrafters.plot.util.MainUtil;
import com.intellectualcrafters.plot.util.RUtils;
import com.intellectualcrafters.plot.util.bukkit.BukkitUtil;

public class Debug extends SubCommand {
    public Debug() {
        super(Command.DEBUG, "Show debug information", "debug [msg]", CommandCategory.DEBUG, false);
    }
    
    @Override
    public boolean execute(final PlotPlayer plr, final String... args) {
        if ((args.length > 0) && args[0].equalsIgnoreCase("msg")) {
            final StringBuilder msg = new StringBuilder();
            for (final C c : C.values()) {
                msg.append(c.s()).append("\n");
            }
            MainUtil.sendMessage(plr, msg.toString());
            return true;
        }
        StringBuilder information;
        String header, line, section;
        {
            information = new StringBuilder();
            header = C.DEUBG_HEADER.s();
            line = C.DEBUG_LINE.s();
            section = C.DEBUG_SECTION.s();
        }
        {
            final StringBuilder worlds = new StringBuilder("");
            for (final String world : PlotSquared.getPlotWorlds()) {
                worlds.append(world).append(" ");
            }
            
            // FIXME not sure if we actually need any of this debug info as we should just do a timings report which is more detailed anyway
            
            information.append(header);
            information.append(getSection(section, "Lag / TPS"));
            information.append(getLine(line, "Ticks Per Second", Lag.getTPS()));
            information.append(getLine(line, "Lag Percentage", (int) Lag.getPercentage() + "%"));
            information.append(getLine(line, "TPS Percentage", (int) Lag.getFullPercentage() + "%"));
            information.append(getSection(section, "PlotWorld"));
            information.append(getLine(line, "Plot Worlds", worlds));
            information.append(getLine(line, "Owned Plots", PlotSquared.getPlots().size()));
            information.append(getSection(section, "RAM"));
            information.append(getLine(line, "Free Ram", RUtils.getFreeRam() + "MB"));
            information.append(getLine(line, "Total Ram", RUtils.getTotalRam() + "MB"));
            information.append(getSection(section, "Messages"));
            information.append(getLine(line, "Total Messages", C.values().length));
            information.append(getLine(line, "View all captions", "/plot debug msg"));
        }
        {
            MainUtil.sendMessage(plr, information.toString());
        }
        return true;
    }
    
    private String getSection(final String line, final String val) {
        return line.replaceAll("%val%", val) + "\n";
    }
    
    private String getLine(final String line, final String var, final Object val) {
        return line.replaceAll("%var%", var).replaceAll("%val%", "" + val) + "\n";
    }
}
