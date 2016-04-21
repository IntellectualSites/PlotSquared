package com.plotsquared.sponge;

import com.intellectualcrafters.plot.PS;
import com.intellectualcrafters.plot.config.ConfigurationNode;
import com.intellectualcrafters.plot.generator.GeneratorWrapper;
import com.intellectualcrafters.plot.generator.HybridGen;
import com.intellectualcrafters.plot.generator.PlotGenerator;
import com.intellectualcrafters.plot.object.PlotArea;
import com.intellectualcrafters.plot.object.SetupObject;
import com.intellectualcrafters.plot.util.SetupUtils;
import com.plotsquared.sponge.generator.SpongePlotGenerator;
import com.plotsquared.sponge.util.SpongeUtil;
import org.spongepowered.api.world.World;
import org.spongepowered.api.world.gen.WorldGenerator;

import java.io.IOException;
import java.util.Map;

public class SpongeSetupUtils extends SetupUtils {
    
    @Override
    public void updateGenerators() {
        if (!SetupUtils.generators.isEmpty()) {
            return;
        }
        SetupUtils.generators.put("PlotSquared", new SpongePlotGenerator(new HybridGen()));
        throw new UnsupportedOperationException("TODO FETCH EXTERNAL WorldGenerationModifiers");
    }
    
    @Override
    public String getGenerator(final PlotArea plotworld) {
        if (SetupUtils.generators.isEmpty()) {
            updateGenerators();
        }
        final World world = SpongeUtil.getWorld(plotworld.worldname);
        if (world == null) {
            return null;
        }
        final WorldGenerator generator = world.getWorldGenerator();
        if (!(generator instanceof SpongePlotGenerator)) {
            return null;
        }
        for (final Map.Entry<String, GeneratorWrapper<?>> entry : generators.entrySet()) {
            GeneratorWrapper<?> current = entry.getValue();
            if (current.equals(generator)) {
                return entry.getKey();
            }
        }
        return null;
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
            PS.get().config.set("worlds." + world + ".generator.type", object.type);
            PS.get().config.set("worlds." + world + ".generator.terrain", object.terrain);
            PS.get().config.set("worlds." + world + ".generator.plugin", object.plotManager);
            if ((object.setupGenerator != null) && !object.setupGenerator.equals(object.plotManager)) {
                PS.get().config.set("worlds." + world + ".generator.init", object.setupGenerator);
            }
            final PlotGenerator<WorldGenerator> gen = (PlotGenerator<WorldGenerator>) generators.get(object.setupGenerator);
            if ((gen != null) && (gen.generator instanceof SpongePlotGenerator)) {
                object.setupGenerator = null;
            }
        }
        try {
            PS.get().config.save(PS.get().configFile);
        } catch (final IOException e) {
            e.printStackTrace();
        }
        // TODO FIXME
        throw new UnsupportedOperationException("NOT IMPLEMENTED YET: Create a new world here");
        //        return object.world;
    }
}
