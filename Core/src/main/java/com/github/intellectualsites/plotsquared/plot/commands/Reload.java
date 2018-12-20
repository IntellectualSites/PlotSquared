package com.github.intellectualsites.plotsquared.plot.commands;

import com.github.intellectualsites.plotsquared.commands.CommandDeclaration;
import com.github.intellectualsites.plotsquared.configuration.ConfigurationSection;
import com.github.intellectualsites.plotsquared.configuration.MemorySection;
import com.github.intellectualsites.plotsquared.configuration.file.YamlConfiguration;
import com.github.intellectualsites.plotsquared.plot.PlotSquared;
import com.github.intellectualsites.plotsquared.plot.config.C;
import com.github.intellectualsites.plotsquared.plot.object.PlotArea;
import com.github.intellectualsites.plotsquared.plot.object.PlotPlayer;
import com.github.intellectualsites.plotsquared.plot.object.RunnableVal;
import com.github.intellectualsites.plotsquared.plot.util.MainUtil;

import java.io.IOException;
import java.util.Objects;

@CommandDeclaration(command = "reload", aliases = "rl", permission = "plots.admin.command.reload", description = "Reload translations and world settings", usage = "/plot reload", category = CommandCategory.ADMINISTRATION)
public class Reload extends SubCommand {

    @Override public boolean onCommand(PlotPlayer player, String[] args) {
        try {
            // The following won't affect world generation, as that has to be
            // loaded during startup unfortunately.
            PlotSquared.get().setupConfigs();
            C.load(PlotSquared.get().translationFile);
            PlotSquared.get().foreachPlotArea(new RunnableVal<PlotArea>() {
                @Override public void run(PlotArea area) {
                    ConfigurationSection worldSection = PlotSquared.get().worlds
                        .getConfigurationSection("worlds." + area.worldname);
                    if (worldSection == null) {
                        return;
                    }
                    if (area.TYPE != 2 || !worldSection.contains("areas")) {
                        area.saveConfiguration(worldSection);
                        area.loadDefaultConfiguration(worldSection);
                    } else {
                        ConfigurationSection areaSection = worldSection.getConfigurationSection(
                            "areas." + area.id + "-" + area.getMin() + "-" + area.getMax());
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
            PlotSquared.get().worlds.save(PlotSquared.get().worldsFile);
            MainUtil.sendMessage(player, C.RELOADED_CONFIGS);
        } catch (IOException e) {
            e.printStackTrace();
            MainUtil.sendMessage(player, C.RELOAD_FAILED);
        }
        return true;
    }
}
