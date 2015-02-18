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

import com.intellectualcrafters.plot.config.C;
import com.intellectualcrafters.plot.util.PlayerFunctions;

public class Template extends SubCommand {

    public Template() {
        super("template", "plots.admin", "Create or use a world template", "template", "", CommandCategory.DEBUG, true);
    }

    @Override
    public boolean execute(final Player plr, final String... args) {
        if (args.length != 2) {
            PlayerFunctions.sendMessage(plr, C.COMMAND_SYNTAX, "/plot template <import|export> <world>");
            return false;
        }
        
        // TODO allow world settings (including schematics to be packed into a single file)
        
        // TODO allow world created based on these packaged files
        
        return true;
    }
}
