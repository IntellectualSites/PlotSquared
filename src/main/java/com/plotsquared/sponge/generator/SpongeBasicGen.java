package com.plotsquared.sponge.generator;

import java.util.ArrayList;
import java.util.List;

import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.world.gen.BiomeGenerator;

import com.intellectualcrafters.plot.PS;
import com.intellectualcrafters.plot.generator.HybridPlotManager;
import com.intellectualcrafters.plot.generator.HybridPlotWorld;
import com.intellectualcrafters.plot.object.PlotManager;
import com.intellectualcrafters.plot.object.PlotWorld;
import com.plotsquared.sponge.SpongeMain;

public class SpongeBasicGen extends SpongePlotGenerator
{

    public final BlockState AIR = BlockTypes.AIR.getDefaultState();

    private static HybridPlotManager manager;
    public HybridPlotWorld plotworld;

    public SpongeBasicGen(final String world)
    {
        super(world);
    }

    /**
     * Some generator specific variables (implementation dependent)
     *
     * TODO USE THESE
     *
     */
    public int plotsize;
    public int pathsize;
    public int size;
    public int roadheight;
    public int wallheight;
    public int plotheight;
    public short pathWidthLower;
    public short pathWidthUpper;
    public boolean doState = false;

    BlockState wall;
    BlockState wallfilling;
    BlockState roadblock;
    BlockState[] plotfloors;
    BlockState[] filling;

    @Override
    public void init(final PlotWorld plotworld)
    {
        if (plotworld != null)
        {
            this.plotworld = (HybridPlotWorld) plotworld;
        }
        plotsize = this.plotworld.PLOT_WIDTH;
        pathsize = this.plotworld.ROAD_WIDTH;
        size = pathsize + plotsize;
        wallheight = this.plotworld.WALL_HEIGHT;
        roadheight = this.plotworld.ROAD_HEIGHT;
        plotheight = this.plotworld.PLOT_HEIGHT;
        if (pathsize == 0)
        {
            pathWidthLower = (short) -1;
            pathWidthUpper = (short) (plotsize + 1);
        }
        else
        {
            if ((pathsize % 2) == 0)
            {
                pathWidthLower = (short) (Math.floor(pathsize / 2) - 1);
            }
            else
            {
                pathWidthLower = (short) (Math.floor(pathsize / 2));
            }
            pathWidthUpper = (short) (pathWidthLower + plotsize + 1);
        }

        roadblock = SpongeMain.THIS.getBlockState(this.plotworld.ROAD_BLOCK);
        wallfilling = SpongeMain.THIS.getBlockState(this.plotworld.WALL_FILLING);
        wall = SpongeMain.THIS.getBlockState(this.plotworld.WALL_BLOCK);
        plotfloors = new BlockState[this.plotworld.TOP_BLOCK.length];
        for (int i = 0; i < this.plotworld.TOP_BLOCK.length; i++)
        {
            plotfloors[i] = SpongeMain.THIS.getBlockState(this.plotworld.TOP_BLOCK[i]);
        }
        filling = new BlockState[this.plotworld.MAIN_BLOCK.length];
        for (int i = 0; i < this.plotworld.MAIN_BLOCK.length; i++)
        {
            filling[i] = SpongeMain.THIS.getBlockState(this.plotworld.MAIN_BLOCK[i]);
        }
        if ((filling.length > 1) || (plotfloors.length > 1))
        {
            doState = true;
        }
    }

    @Override
    public PlotWorld getNewPlotWorld(final String world)
    {
        if (plotworld == null)
        {
            plotworld = (HybridPlotWorld) PS.get().getPlotWorld(world);
            if (plotworld == null)
            {
                plotworld = new HybridPlotWorld(world);
            }
        }
        return plotworld;
    }

    @Override
    public PlotManager getPlotManager()
    {
        if (SpongeBasicGen.manager == null)
        {
            SpongeBasicGen.manager = new HybridPlotManager();
        }
        return SpongeBasicGen.manager;
    }

    @Override
    public List<SpongePlotPopulator> getPlotPopulators()
    {
        // TODO Auto-generated method stub
        return new ArrayList<>();
    }

    private SpongeBasicPop generator;

    @Override
    public SpongePlotPopulator getGenerator()
    {
        if (generator == null)
        {
            generator = new SpongeBasicPop(this);
        }
        return generator;
    }

    private BiomeGenerator biome;

    @Override
    public BiomeGenerator getPlotBiomeProvider()
    {
        if (biome == null)
        {
            biome = new SpongeBasicBiomeProvider(plotworld);
        }
        return biome;
    }

}
