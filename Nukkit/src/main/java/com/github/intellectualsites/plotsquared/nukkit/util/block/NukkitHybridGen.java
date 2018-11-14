package com.github.intellectualsites.plotsquared.nukkit.util.block;

import cn.nukkit.level.Level;
import com.github.intellectualsites.plotsquared.nukkit.NukkitMain;
import com.github.intellectualsites.plotsquared.nukkit.generator.NukkitPlotGenerator;
import com.github.intellectualsites.plotsquared.plot.PlotSquared;

import java.util.Map;

public class NukkitHybridGen extends NukkitPlotGenerator {
    public NukkitHybridGen(Map<String, Object> settings) {
        super(defaultSettings(settings));
    }

    private static Map<String, Object> defaultSettings(Map<String, Object> existing) {
        if (!existing.containsKey("world")) {
            Map<Integer, Level> levels =
                ((NukkitMain) PlotSquared.get().IMP).getServer().getLevels();
            int max = -1;
            for (Map.Entry<Integer, Level> entry : levels.entrySet()) {
                int id = entry.getKey();
                if (id > max) {
                    max = id;
                    existing.put("world", entry.getValue().getName());
                }
            }
        }
        existing.put("plot-generator", PlotSquared.get().IMP.getDefaultGenerator());
        return existing;
    }
}
