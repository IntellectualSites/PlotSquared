package com.plotsquared.nukkit.util.block;

import cn.nukkit.level.Level;
import com.intellectualcrafters.plot.PS;
import com.plotsquared.nukkit.NukkitMain;
import com.plotsquared.nukkit.generator.NukkitPlotGenerator;

import java.util.Map;

public class NukkitHybridGen extends NukkitPlotGenerator {
    public NukkitHybridGen(Map<String, Object> settings) {
        super(defaultSettings(settings));
    }

    private static Map<String, Object> defaultSettings(Map<String, Object> existing) {
        if (!existing.containsKey("world")) {
            Map<Integer, Level> levels = ((NukkitMain) PS.get().IMP).getServer().getLevels();
            int max = -1;
            for (Map.Entry<Integer, Level> entry : levels.entrySet()) {
                int id = entry.getKey();
                if (id > max) {
                    max = id;
                    existing.put("world", entry.getValue().getName());
                }
            }
        }
        existing.put("plot-generator", PS.get().IMP.getDefaultGenerator());
        return existing;
    }
}
