package com.intellectualcrafters.plot.util.bukkit;

import com.intellectualcrafters.plot.PlotSquared;
import com.intellectualcrafters.plot.config.ConfigurationNode;
import com.intellectualcrafters.plot.object.PlotGenerator;
import com.intellectualcrafters.plot.object.PlotWorld;
import com.intellectualcrafters.plot.object.SetupObject;
import com.intellectualcrafters.plot.util.SetupUtils;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.WorldCreator;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.plugin.Plugin;

import java.io.IOException;
import java.util.Map.Entry;

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
                    PlotSquared.getInstance().removePlotWorld(testWorld);
                    final String name = plugin.getDescription().getName();
//                        final PlotGenerator pgen = (PlotGenerator) generator;
//                        if (pgen.getPlotManager() instanceof SquarePlotManager) {
                            SetupUtils.generators.put(name, generator);
//                        }
//                    }
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
            PlotSquared.getInstance().config.set("worlds." + world + "." + step.getConstant(), step.getValue());
        }
        if (object.type != 0) {
            PlotSquared.getInstance().config.set("worlds." + world + "." + "generator.type", object.type);
            PlotSquared.getInstance().config.set("worlds." + world + "." + "generator.terrain", object.terrain);
            PlotSquared.getInstance().config.set("worlds." + world + "." + "generator.plugin", object.plotManager);
            if (object.setupGenerator != null && !object.setupGenerator.equals(object.plotManager)) {
                PlotSquared.getInstance().config.set("worlds." + world + "." + "generator.init", object.setupGenerator);
            }
            ChunkGenerator gen = generators.get(object.setupGenerator);
            if (gen instanceof PlotGenerator) {
                object.setupGenerator = null;
            }
        }
        try {
            PlotSquared.getInstance().config.save(PlotSquared.getInstance().configFile);
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
        if (!(generator instanceof PlotGenerator)) {
            return null;
        }
        for (Entry<String, ChunkGenerator> entry : generators.entrySet()) {
            if (entry.getValue().getClass().getName().equals(generator.getClass().getName())) {
                return entry.getKey();
            }
        }
        return null;
    }
}
