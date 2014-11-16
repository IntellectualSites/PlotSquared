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

import com.intellectualcrafters.plot.PlotMain;
import com.intellectualcrafters.plot.config.C;
import com.intellectualcrafters.plot.object.PlotWorld;
import com.intellectualcrafters.plot.util.PlayerFunctions;
import org.bukkit.entity.Player;

public class Reload extends SubCommand {

    public Reload() {
        super("reload", "plots.admin", "Reload configurations", "", "reload", CommandCategory.INFO, false);
    }

    @Override
    public boolean execute(final Player plr, final String... args) {
        try {
            PlotMain.reloadTranslations();

            // The following won't affect world generation, as that has to be
            // loaded during startup unfortunately.
            PlotMain.config.load(PlotMain.configFile);
            for (final String pw : PlotMain.getPlotWorlds()) {
                final PlotWorld plotworld = PlotMain.getWorldSettings(pw);
                plotworld.loadDefaultConfiguration(PlotMain.config.getConfigurationSection("worlds." + pw));
            }
            PlotMain.BroadcastWithPerms(C.RELOADED_CONFIGS);
        } catch (final Exception e) {
            PlayerFunctions.sendMessage(plr, C.RELOAD_FAILED);
        }
        return true;
    }

}
