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
import com.intellectualcrafters.configuration.MemorySection;
import com.intellectualcrafters.configuration.file.YamlConfiguration;
import com.intellectualcrafters.plot.PS;
import com.intellectualcrafters.plot.config.C;
import com.intellectualcrafters.plot.object.PlotArea;
import com.intellectualcrafters.plot.object.PlotPlayer;
import com.intellectualcrafters.plot.object.RunnableVal;
import com.intellectualcrafters.plot.util.MainUtil;
import com.plotsquared.general.commands.CommandDeclaration;

import java.util.Objects;

@CommandDeclaration(command = "reload",
        permission = "plots.admin.command.reload",
        description = "Reload configurations",
        usage = "/plot reload",
        category = CommandCategory.ADMINISTRATION)
public class Reload extends SubCommand {

    @Override
    public boolean onCommand(PlotPlayer plr, String[] args) {
        try {
            // The following won't affect world generation, as that has to be
            // loaded during startup unfortunately.
            PS.get().style.load(PS.get().styleFile);
            PS.get().config.load(PS.get().configFile);
            PS.get().setupConfig();
            C.load(PS.get().translationFile);
            PS.get().foreachPlotArea(new RunnableVal<PlotArea>() {
                @Override
                public void run(PlotArea area) {
                    ConfigurationSection worldSection = PS.get().config.getConfigurationSection("worlds." + area.worldname);
                    if (area.TYPE != 2 || !worldSection.contains("areas")) {
                        area.saveConfiguration(worldSection);
                        area.loadDefaultConfiguration(worldSection);
                    } else {
                        ConfigurationSection areaSection =
                                worldSection.getConfigurationSection("areas." + area.id + "-" + area.getMin() + "-" + area.getMax());
                        YamlConfiguration clone = new YamlConfiguration();
                        for (String key : areaSection.getKeys(true)) {
                            if (areaSection.get(key) instanceof MemorySection) {
                                continue;
                            }
                            if (!clone.contains(key)) {
                                clone.set(key, areaSection.get(key));
                            }
                        }
                        for (String key : worldSection.getKeys(true)) {
                            if (worldSection.get(key) instanceof MemorySection) {
                                continue;
                            }
                            if (!key.startsWith("areas") && !clone.contains(key)) {
                                clone.set(key, worldSection.get(key));
                            }
                        }
                        area.saveConfiguration(clone);
                        // netSections is the combination of
                        for (String key : clone.getKeys(true)) {
                            if (clone.get(key) instanceof MemorySection) {
                                continue;
                            }
                            if (!worldSection.contains(key)) {
                                worldSection.set(key, clone.get(key));
                            } else {
                                Object value = worldSection.get(key);
                                if (Objects.equals(value, clone.get(key))) {
                                    areaSection.set(key, clone.get(key));
                                }
                            }
                        }
                        area.loadDefaultConfiguration(clone);
                    }
                }
            });
            PS.get().config.save(PS.get().configFile);
            MainUtil.sendMessage(plr, C.RELOADED_CONFIGS);
        } catch (Exception e) {
            e.printStackTrace();
            MainUtil.sendMessage(plr, C.RELOAD_FAILED);
        }
        return true;
    }
}
