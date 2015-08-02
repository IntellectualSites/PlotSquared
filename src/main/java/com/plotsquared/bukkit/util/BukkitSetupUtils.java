package com.plotsquared.bukkit.util;

import java.io.IOException;
import java.util.Map.Entry;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.WorldCreator;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.plugin.Plugin;

import com.intellectualcrafters.plot.PS;
import com.intellectualcrafters.plot.config.ConfigurationNode;
import com.intellectualcrafters.plot.generator.PlotGenerator;
import com.intellectualcrafters.plot.object.PlotCluster;
import com.intellectualcrafters.plot.object.PlotWorld;
import com.intellectualcrafters.plot.object.SetupObject;
import com.intellectualcrafters.plot.util.SetupUtils;
import com.plotsquared.bukkit.generator.AugmentedPopulator;
import com.plotsquared.bukkit.generator.BukkitGeneratorWrapper;
import com.plotsquared.bukkit.generator.BukkitPlotGenerator;

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
                    PS.get().removePlotWorld(testWorld);
                    final String name = plugin.getDescription().getName();
                            SetupUtils.generators.put(name, new BukkitGeneratorWrapper("CheckingPlotSquaredGenerator", generator));
                }
            }
        }
    }
    
    @Override
    public String setupWorld(final SetupObject object) {
        SetupUtils.manager.updateGenerators();
        final ConfigurationNode[] steps = object.step;
        final String world = object.world;
        for (final ConfigurationNode step : steps) {
            PS.get().config.set("worlds." + world + "." + step.getConstant(), step.getValue());
        }
        if (object.type != 0) {
            PS.get().config.set("worlds." + world + "." + "generator.type", object.type);
            PS.get().config.set("worlds." + world + "." + "generator.terrain", object.terrain);
            PS.get().config.set("worlds." + world + "." + "generator.plugin", object.plotManager);
            if (object.setupGenerator != null && !object.setupGenerator.equals(object.plotManager)) {
                PS.get().config.set("worlds." + world + "." + "generator.init", object.setupGenerator);
            }
            PlotGenerator<ChunkGenerator> gen = (PlotGenerator<ChunkGenerator>) generators.get(object.setupGenerator);
            if (gen != null && gen.generator instanceof BukkitPlotGenerator) {
                object.setupGenerator = null;
            }
        }
        try {
            PS.get().config.save(PS.get().configFile);
        } catch (final IOException e) {
            e.printStackTrace();
        }
        if (object.setupGenerator != null) {
            if ((Bukkit.getPluginManager().getPlugin("Multiverse-Core") != null) && Bukkit.getPluginManager().getPlugin("Multiverse-Core").isEnabled()) {
                Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(), "mv create " + world + " normal -g " + object.setupGenerator);
            } else {
                if ((Bukkit.getPluginManager().getPlugin("MultiWorld") != null) && Bukkit.getPluginManager().getPlugin("MultiWorld").isEnabled()) {
                    Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(), "mw create " + world + " plugin:" + object.setupGenerator);
                } else {
                    final WorldCreator wc = new WorldCreator(object.world);
                    wc.generator(object.setupGenerator);
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

    @Override
    public String getGenerator(PlotWorld plotworld) {
        if (SetupUtils.generators.size() == 0) {
            updateGenerators();
        }
        World world = Bukkit.getWorld(plotworld.worldname);
        if (world == null) {
            return null;
        }
        ChunkGenerator generator = world.getGenerator();
        if (!(generator instanceof BukkitPlotGenerator)) {
            return null;
        }
        for (Entry<String, PlotGenerator<?>> entry : generators.entrySet()) {
            if (entry.getValue().generator.getClass().getName().equals(generator.getClass().getName())) {
                return entry.getKey();
            }
        }
        return null;
    }

    @Override
    public void removePopulator(String world, PlotCluster cluster) {
        AugmentedPopulator.removePopulator(world, cluster);
    }
}
