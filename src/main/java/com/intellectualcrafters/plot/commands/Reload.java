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

import com.intellectualcrafters.configuration.ConfigurationSection;
import com.intellectualcrafters.plot.PS;
import com.intellectualcrafters.plot.config.C;
import com.intellectualcrafters.plot.object.PlotPlayer;
import com.intellectualcrafters.plot.object.PlotWorld;
import com.intellectualcrafters.plot.util.MainUtil;
import com.plotsquared.general.commands.CommandDeclaration;

@CommandDeclaration(
        command = "reload",
        permission = "plots.admin.command.reload",
        description = "Reload configurations",
        usage = "/plot reload",
        category = CommandCategory.INFO
)
public class Reload extends SubCommand {

    @Override
    public boolean onCommand(final PlotPlayer plr, final String[] args) {
        try {
            // The following won't affect world generation, as that has to be
            // loaded during startup unfortunately.
            PS.get().config.load(PS.get().configFile);
            PS.get().setupConfig();
            C.load(PS.get().translationFile);
            for (final String pw : PS.get().getPlotWorlds()) {
                final PlotWorld plotworld = PS.get().getPlotWorld(pw);
                ConfigurationSection section = PS.get().config.getConfigurationSection("worlds." + pw);
                plotworld.saveConfiguration(section);
                plotworld.loadDefaultConfiguration(section);
            }
            MainUtil.sendMessage(plr, C.RELOADED_CONFIGS);
        } catch (final Exception e) {
            e.printStackTrace();
            MainUtil.sendMessage(plr, C.RELOAD_FAILED);
        }
        return true;
    }
}
