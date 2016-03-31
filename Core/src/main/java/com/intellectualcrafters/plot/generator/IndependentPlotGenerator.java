package com.intellectualcrafters.plot.generator;

import com.intellectualcrafters.plot.PS;
import com.intellectualcrafters.plot.object.PlotArea;
import com.intellectualcrafters.plot.object.PlotId;
import com.intellectualcrafters.plot.object.PlotManager;
import com.intellectualcrafters.plot.object.PseudoRandom;
import com.intellectualcrafters.plot.object.SetupObject;
import com.intellectualcrafters.plot.util.PlotChunk;

/**
 * This class allows for implementation independent world generation<br>
 *     - Sponge/Bukkit API<br><br>
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
     * @param result
     * @param settings
     * @param random
     */
    public abstract void generateChunk(PlotChunk<?> result, PlotArea settings, PseudoRandom random);

    public boolean populateChunk(PlotChunk<?> result, PlotArea settings, PseudoRandom random) {
        return false;
    }

    /**
     * Return a new PlotArea object.
     * @param world world name
     * @param id (May be null) Area name
     * @param min Min plot id (may be null)
     * @param max Max plot id (may be null)
     * @return
     */
    public abstract PlotArea getNewPlotArea(String world, String id, PlotId min, PlotId max);

    /**
     * Return a new PlotManager object.
     * @return
     */
    public abstract PlotManager getNewPlotManager();

    /**
     * If any additional setup options need to be changed before world creation.
     *  - e.g. If setup doesn't support some standard options
     * @param setup
     */
    public void processSetup(SetupObject setup) {
    }

    /**
     * It is preferred for the PlotArea object to do most of the initialization necessary.
     * @param area
     */
    public abstract void initialize(PlotArea area);

    /**
     * Get the generator for your specific implementation (bukkit/sponge).<br>
     *  - e.g. YourIndependentGenerator.&lt;ChunkGenerator&gt;specify() - Would return a ChunkGenerator object<br>
     * @param <T>
     * @param <T>
     * @return
     */
    public <T> GeneratorWrapper<T> specify() {
        return (GeneratorWrapper<T>) PS.get().IMP.wrapPlotGenerator(this);
    }

    @Override
    public String toString() {
        return getName();
    }
}
