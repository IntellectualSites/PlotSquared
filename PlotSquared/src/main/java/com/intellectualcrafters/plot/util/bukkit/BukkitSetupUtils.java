package com.intellectualcrafters.plot.util.bukkit;

import java.io.IOException;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.WorldCreator;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.plugin.Plugin;

import com.intellectualcrafters.plot.PlotSquared;
import com.intellectualcrafters.plot.config.ConfigurationNode;
import com.intellectualcrafters.plot.generator.SquarePlotManager;
import com.intellectualcrafters.plot.object.PlotGenerator;
import com.intellectualcrafters.plot.object.SetupObject;
import com.intellectualcrafters.plot.util.SetupUtils;

public class BukkitSetupUtils extends SetupUtils {
    
    @Override
    public void updateGenerators() {
        if (SetupUtils.generators.size() > 0) {
            return;
        }
        final String testWorld = "CheckingPlotSquaredGenerator";
        for (final Plugin plugin : Bukkit.getPluginManager().getPlugins()) {
            if (plugin.isEnabled()) {
                final ChunkGenerator generator = plugin.getDefaultWorldGenerator(testWorld, "");
                if (generator != null) {
                    PlotSquared.removePlotWorld(testWorld);
                    final String name = plugin.getDescription().getName();
                    if (generator instanceof PlotGenerator) {
                        final PlotGenerator pgen = (PlotGenerator) generator;
                        if (pgen.getPlotManager() instanceof SquarePlotManager) {
                            SetupUtils.generators.put(name, pgen);
                        }
                    }
                }
            }
        }
    }
    
    @Override
    public String setupWorld(final SetupObject object) {
        final ConfigurationNode[] steps = object.step;
        final String world = object.world;
        for (final ConfigurationNode step : steps) {
            PlotSquared.config.set("worlds." + world + "." + step.getConstant(), step.getValue());
        }
        if (object.type != 0) {
            PlotSquared.config.set("worlds." + world + "." + "generator.type", object.type);
            PlotSquared.config.set("worlds." + world + "." + "generator.terrain", object.terrain);
            PlotSquared.config.set("worlds." + world + "." + "generator.plugin", object.generator);
        }
        try {
            PlotSquared.config.save(PlotSquared.configFile);
        } catch (final IOException e) {
            e.printStackTrace();
        }
        if (object.type == 0) {
            if ((Bukkit.getPluginManager().getPlugin("Multiverse-Core") != null) && Bukkit.getPluginManager().getPlugin("Multiverse-Core").isEnabled()) {
                Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(), "mv create " + world + " normal -g " + object.generator);
            } else {
                if ((Bukkit.getPluginManager().getPlugin("MultiWorld") != null) && Bukkit.getPluginManager().getPlugin("MultiWorld").isEnabled()) {
                    Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(), "mw create " + world + " plugin:" + object.generator);
                } else {
                    final WorldCreator wc = new WorldCreator(object.world);
                    wc.generator(object.generator);
                    wc.environment(Environment.NORMAL);
                    Bukkit.createWorld(wc);
                }
            }
        } else {
            if ((Bukkit.getPluginManager().getPlugin("Multiverse-Core") != null) && Bukkit.getPluginManager().getPlugin("Multiverse-Core").isEnabled()) {
                Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(), "mv create " + world + " normal");
            } else {
                if ((Bukkit.getPluginManager().getPlugin("MultiWorld") != null) && Bukkit.getPluginManager().getPlugin("MultiWorld").isEnabled()) {
                    Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(), "mw create " + world);
                } else {
                    Bukkit.createWorld(new WorldCreator(object.world).environment(World.Environment.NORMAL));
                }
            }
        }
        return object.world;
    }

}
