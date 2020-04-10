package com.github.intellectualsites.plotsquared.plot.object;

import com.github.intellectualsites.plotsquared.plot.config.ConfigurationNode;
import com.github.intellectualsites.plotsquared.util.SetupUtils;

public class SetupObject {

    /**
     * Specify a SetupUtils object here to override the existing
     */
    public SetupUtils setupManager;

    /**
     * The current state
     */
    public int current = 0;

    /**
     * The index in generator specific settings
     */
    public int setup_index = 0;

    /**
     * The name of the world
     */
    public String world = null;

    /**
     * The name of the plot manager
     */
    public String plotManager = null;

    /**
     * The name of the generator to use for world creation
     */
    public String setupGenerator = null;

    /**
     * The management type (normal, augmented, partial)
     */
    public PlotAreaType type;

    /**
     * The terrain type
     */
    public PlotAreaTerrainType terrain;

    /**
     * Area ID (may be null)
     */
    public String id;

    /**
     * Minimum plot id (may be null)
     */
    public PlotId min;

    /**
     * Max plot id (may be null)
     */
    public PlotId max;

    /**
     * Generator specific configuration steps
     */
    public ConfigurationNode[] step = null;
}
