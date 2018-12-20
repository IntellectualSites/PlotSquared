package com.github.intellectualsites.plotsquared.plot.generator;

import com.github.intellectualsites.plotsquared.plot.PlotSquared;
import com.github.intellectualsites.plotsquared.plot.object.*;
import com.github.intellectualsites.plotsquared.plot.util.block.ScopedLocalBlockQueue;

/**
 * This class allows for implementation independent world generation.
 * - Sponge/Bukkit API
 * Use the specify method to get the generator for that platform.
 */
public abstract class IndependentPlotGenerator {

    /**
     * Get the name of this generator.
     */
    public abstract String getName();

    /**
     * Use the setBlock or setBiome method of the PlotChunk result parameter to make changes.
     * The PlotArea settings is the same one this was initialized with.
     * The PseudoRandom random is a fast random object.
     *
     * @param result
     * @param settings
     */
    public abstract void generateChunk(ScopedLocalBlockQueue result, PlotArea settings);

    public boolean populateChunk(ScopedLocalBlockQueue result, PlotArea setting) {
        return false;
    }

    /**
     * Return a new PlotArea object.
     *
     * @param world world name
     * @param id    (May be null) Area name
     * @param min   Min plot id (may be null)
     * @param max   Max plot id (may be null)
     * @return
     */
    public abstract PlotArea getNewPlotArea(String world, String id, PlotId min, PlotId max);

    /**
     * Return a new PlotManager object.
     *
     * @return
     */
    public abstract PlotManager getNewPlotManager();

    /**
     * If any additional setup options need to be changed before world creation.
     * - e.g. If setup doesn't support some standard options
     *
     * @param setup
     */
    public void processSetup(SetupObject setup) {
    }

    /**
     * It is preferred for the PlotArea object to do most of the initialization necessary.
     *
     * @param area
     */
    public abstract void initialize(PlotArea area);

    /**
     * Get the generator for your specific implementation (bukkit/sponge).<br>
     * - e.g. YourIndependentGenerator.&lt;ChunkGenerator&gt;specify() - Would return a ChunkGenerator object<br>
     *
     * @param <T>
     * @param <T>
     * @return
     */
    public <T> GeneratorWrapper<T> specify(String world) {
        return (GeneratorWrapper<T>) PlotSquared.get().IMP.wrapPlotGenerator(world, this);
    }

    @Override public String toString() {
        return getName();
    }
}
