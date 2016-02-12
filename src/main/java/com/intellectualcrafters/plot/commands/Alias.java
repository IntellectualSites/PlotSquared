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
import com.intellectualcrafters.plot.object.Plot;
import com.intellectualcrafters.plot.object.PlotPlayer;
import com.intellectualcrafters.plot.object.StringWrapper;
import com.intellectualcrafters.plot.util.MainUtil;
import com.intellectualcrafters.plot.util.StringMan;
import com.intellectualcrafters.plot.util.UUIDHandler;
import com.plotsquared.general.commands.CommandDeclaration;

@CommandDeclaration(
command = "setalias",
permission = "plots.set.alias",
 description = "Set the plot name",
usage = "/plot alias <alias>",
aliases = { "alias", "sa", "name", "rename", "setname", "seta" },
 category = CommandCategory.SETTINGS,
requiredType = RequiredType.NONE)
public class Alias extends SetCommand {
    
    @Override
    public boolean set(final PlotPlayer plr, final Plot plot, final String alias) {
        if (alias.length() == 0) {
            C.COMMAND_SYNTAX.send(plr, getUsage());
            return false;
        }
        if (alias.length() >= 50) {
            MainUtil.sendMessage(plr, C.ALIAS_TOO_LONG);
            return false;
        }
        if (alias.contains(" ") || !StringMan.isAsciiPrintable(alias)) {
            C.NOT_VALID_VALUE.send(plr);
            return false;
        }
        for (final Plot p : PS.get().getPlots(plot.area)) {
            if (p.getAlias().equalsIgnoreCase(alias)) {
                MainUtil.sendMessage(plr, C.ALIAS_IS_TAKEN);
                return false;
            }
        }
        if (UUIDHandler.nameExists(new StringWrapper(alias)) || PS.get().hasPlotArea(alias)) {
            MainUtil.sendMessage(plr, C.ALIAS_IS_TAKEN);
            return false;
        }
        plot.setAlias(alias);
        MainUtil.sendMessage(plr, C.ALIAS_SET_TO.s().replaceAll("%alias%", alias));
        return true;
    }
}
