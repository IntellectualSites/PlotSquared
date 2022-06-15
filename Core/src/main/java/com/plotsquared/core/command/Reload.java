/*
 * PlotSquared, a land and world management plugin for Minecraft.
 * Copyright (C) IntellectualSites <https://intellectualsites.com>
 * Copyright (C) IntellectualSites team and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.plotsquared.core.command;

import com.google.inject.Inject;
import com.plotsquared.core.PlotSquared;
import com.plotsquared.core.configuration.ConfigurationSection;
import com.plotsquared.core.configuration.MemorySection;
import com.plotsquared.core.configuration.caption.TranslatableCaption;
import com.plotsquared.core.configuration.file.YamlConfiguration;
import com.plotsquared.core.inject.annotations.WorldConfig;
import com.plotsquared.core.inject.annotations.WorldFile;
import com.plotsquared.core.player.PlotPlayer;
import com.plotsquared.core.plot.PlotAreaType;
import com.plotsquared.core.plot.world.PlotAreaManager;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.io.File;
import java.util.Objects;

@CommandDeclaration(command = "reload",
        aliases = "rl",
        permission = "plots.admin.command.reload",
        usage = "/plot reload",
        category = CommandCategory.ADMINISTRATION)
public class Reload extends SubCommand {

    private final PlotAreaManager plotAreaManager;
    private YamlConfiguration worldConfiguration;
    private File worldFile;

    @Inject
    public Reload(
            final @NonNull PlotAreaManager plotAreaManager,
            @WorldConfig final @NonNull YamlConfiguration worldConfiguration,
            @WorldFile final @NonNull File worldFile
    ) {
        this.plotAreaManager = plotAreaManager;
        this.worldConfiguration = worldConfiguration;
        this.worldFile = worldFile;
    }

    @Override
    public boolean onCommand(PlotPlayer<?> player, String[] args) {
        try {
            // The following won't affect world generation, as that has to be
            // loaded during startup unfortunately.
            PlotSquared.get().setupConfigs();
            this.worldConfiguration = PlotSquared.get().getWorldConfiguration();
            this.worldFile = PlotSquared.get().getWorldsFile();
            PlotSquared.get().loadCaptionMap();
            this.plotAreaManager.forEachPlotArea(area -> {
                ConfigurationSection worldSection = this.worldConfiguration
                        .getConfigurationSection("worlds." + area.getWorldName());
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
            this.worldConfiguration.save(this.worldFile);
            player.sendMessage(TranslatableCaption.of("reload.reloaded_configs"));
        } catch (Exception e) {
            e.printStackTrace();
            player.sendMessage(TranslatableCaption.of("reload.reload_failed"));
        }
        return true;
    }

}
