package com.plotsquared.sponge;

import java.io.IOException;
import java.util.Map.Entry;

import org.spongepowered.api.world.World;
import org.spongepowered.api.world.gen.WorldGenerator;

import com.intellectualcrafters.plot.PS;
import com.intellectualcrafters.plot.config.ConfigurationNode;
import com.intellectualcrafters.plot.generator.PlotGenerator;
import com.intellectualcrafters.plot.object.PlotCluster;
import com.intellectualcrafters.plot.object.PlotWorld;
import com.intellectualcrafters.plot.object.SetupObject;
import com.intellectualcrafters.plot.util.SetupUtils;
import com.plotsquared.sponge.generator.AugmentedPopulator;
import com.plotsquared.sponge.generator.SpongeBasicGen;
import com.plotsquared.sponge.generator.SpongeGeneratorWrapper;
import com.plotsquared.sponge.generator.SpongePlotGenerator;
import com.plotsquared.sponge.util.SpongeUtil;

public class SpongeSetupUtils extends SetupUtils
{

    @Override
    public void updateGenerators()
    {
        if (SetupUtils.generators.size() > 0) { return; }
        // TODO get external world generators
        final String testWorld = "CheckingPlotSquaredGenerator";
        SetupUtils.generators.put("PlotSquared", new SpongeGeneratorWrapper(testWorld, new SpongeBasicGen(testWorld)));
    }

    @Override
    public String getGenerator(final PlotWorld plotworld)
    {
        if (SetupUtils.generators.size() == 0)
        {
            updateGenerators();
        }
        final World world = SpongeUtil.getWorld(plotworld.worldname);
        if (world == null) { return null; }
        final WorldGenerator generator = world.getWorldGenerator();
        if (!(generator instanceof SpongePlotGenerator)) { return null; }
        for (final Entry<String, PlotGenerator<?>> entry : generators.entrySet())
        {
            if (entry.getValue().generator.getClass().getName().equals(generator.getClass().getName())) { return entry.getKey(); }
        }
        return null;
    }

    @Override
    public String setupWorld(final SetupObject object)
    {
        SetupUtils.manager.updateGenerators();
        final ConfigurationNode[] steps = object.step;
        final String world = object.world;
        for (final ConfigurationNode step : steps)
        {
            PS.get().config.set("worlds." + world + "." + step.getConstant(), step.getValue());
        }
        if (object.type != 0)
        {
            PS.get().config.set("worlds." + world + "." + "generator.type", object.type);
            PS.get().config.set("worlds." + world + "." + "generator.terrain", object.terrain);
            PS.get().config.set("worlds." + world + "." + "generator.plugin", object.plotManager);
            if ((object.setupGenerator != null) && !object.setupGenerator.equals(object.plotManager))
            {
                PS.get().config.set("worlds." + world + "." + "generator.init", object.setupGenerator);
            }
            final PlotGenerator<WorldGenerator> gen = (PlotGenerator<WorldGenerator>) generators.get(object.setupGenerator);
            if ((gen != null) && (gen.generator instanceof SpongePlotGenerator))
            {
                object.setupGenerator = null;
            }
        }
        try
        {
            PS.get().config.save(PS.get().configFile);
        }
        catch (final IOException e)
        {
            e.printStackTrace();
        }
        SpongeMain.THIS.createWorldFromConfig(world);
        return object.world;
    }

    @Override
    public void removePopulator(final String world, final PlotCluster cluster)
    {
        AugmentedPopulator.removePopulator(world, cluster);
    }
}
