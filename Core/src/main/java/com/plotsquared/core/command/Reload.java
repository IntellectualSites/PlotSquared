/*
 *
 *       _____  _       _    _____                                _
 *      |  __ \| |     | |  / ____|                              | |
 *      | |__) | | ___ | |_| (___   __ _ _   _  __ _ _ __ ___  __| |
 *      |  ___/| |/ _ \| __|\___ \ / _` | | | |/ _` | '__/ _ \/ _` |
 *      | |    | | (_) | |_ ____) | (_| | |_| | (_| | | |  __/ (_| |
 *      |_|    |_|\___/ \__|_____/ \__, |\__,_|\__,_|_|  \___|\__,_|
 *                                    | |
 *                                    |_|
 *            PlotSquared plot management system for Minecraft
 *                  Copyright (C) 2020 IntellectualSites
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.plotsquared.core.command;

import com.plotsquared.core.configuration.ConfigurationSection;
import com.plotsquared.core.configuration.MemorySection;
import com.plotsquared.core.configuration.file.YamlConfiguration;
import com.plotsquared.core.PlotSquared;
import com.plotsquared.core.config.Captions;
import com.plotsquared.core.plot.PlotAreaType;
import com.plotsquared.core.player.PlotPlayer;
import com.plotsquared.core.util.MainUtil;

import java.io.IOException;
import java.util.Objects;

@CommandDeclaration(command = "reload",
    aliases = "rl",
    permission = "plots.admin.command.reload",
    description = "Reload translations and world settings",
    usage = "/plot reload",
    category = CommandCategory.ADMINISTRATION)
public class Reload extends SubCommand {

    @Override public boolean onCommand(PlotPlayer player, String[] args) {
        try {
            // The following won't affect world generation, as that has to be
            // loaded during startup unfortunately.
            PlotSquared.get().setupConfigs();
            Captions.load(PlotSquared.get().translationFile);
            PlotSquared.get().forEachPlotArea(area -> {
                ConfigurationSection worldSection =
                    PlotSquared.get().worlds.getConfigurationSection("worlds." + area.getWorldName());
                if (worldSection == null) {
                    return;
                }
                if (area.getType() != PlotAreaType.PARTIAL || !worldSection.contains("areas")) {
                    area.saveConfiguration(worldSection);
                    area.loadDefaultConfiguration(worldSection);
                } else {
                    ConfigurationSection areaSection = worldSection.getConfigurationSection(
                        "areas." + area.getId() + "-" + area.getMin() + "-" + area.getMax());
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
            });
            PlotSquared.get().worlds.save(PlotSquared.get().worldsFile);
            MainUtil.sendMessage(player, Captions.RELOADED_CONFIGS);
        } catch (IOException e) {
            e.printStackTrace();
            MainUtil.sendMessage(player, Captions.RELOAD_FAILED);
        }
        return true;
    }
}
