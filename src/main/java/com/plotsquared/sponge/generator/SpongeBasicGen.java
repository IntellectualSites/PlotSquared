package com.plotsquared.sponge.generator;

import java.util.ArrayList;
import java.util.List;

import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.world.gen.BiomeGenerator;

import com.intellectualcrafters.plot.generator.HybridPlotManager;
import com.intellectualcrafters.plot.generator.HybridPlotWorld;
import com.intellectualcrafters.plot.object.PlotManager;
import com.intellectualcrafters.plot.object.PlotWorld;
import com.plotsquared.sponge.SpongeMain;

public class SpongeBasicGen extends SpongePlotGenerator {

    public final BlockState ROAD_BLOCK = BlockTypes.QUARTZ_BLOCK.getDefaultState(); // Quartz
    public final BlockState MAIN_BLOCK = BlockTypes.STONE.getDefaultState(); // Stone
    public final BlockState WALL_BLOCK = BlockTypes.BEDROCK.getDefaultState(); // Bedrock
    public final BlockState BORDER_BLOCK = BlockTypes.STONE_SLAB.getDefaultState(); // Stone slab
    public final BlockState[] FLOOR_BLOCK = new BlockState[] {BlockTypes.GRASS.getDefaultState(), BlockTypes.SPONGE.getDefaultState(), BlockTypes.PLANKS.getDefaultState() }; // Grass and sponge
    
    private static HybridPlotManager manager;
    public HybridPlotWorld plotworld;

    public SpongeBasicGen(String world) {
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
    public void init(PlotWorld plotworld) {
        if (plotworld != null) {
            this.plotworld = (HybridPlotWorld) plotworld;
        }
        this.plotsize = this.plotworld.PLOT_WIDTH;
        this.pathsize = this.plotworld.ROAD_WIDTH;
        this.size = this.pathsize + this.plotsize;
        this.wallheight = this.plotworld.WALL_HEIGHT;
        this.roadheight = this.plotworld.ROAD_HEIGHT;
        this.plotheight = this.plotworld.PLOT_HEIGHT;
        if (this.pathsize == 0) {
            this.pathWidthLower = (short) -1;
            this.pathWidthUpper = (short) (this.plotsize + 1);
        }
        else {
            if ((this.pathsize % 2) == 0) {
                this.pathWidthLower = (short) (Math.floor(this.pathsize / 2) - 1);
            } else {
                this.pathWidthLower = (short) (Math.floor(this.pathsize / 2));
            }
            this.pathWidthUpper = (short) (this.pathWidthLower + this.plotsize + 1);
        }
        
        this.roadblock = SpongeMain.THIS.getBlockState(this.plotworld.ROAD_BLOCK);
        this.wallfilling = SpongeMain.THIS.getBlockState(this.plotworld.WALL_FILLING);
        this.wall = SpongeMain.THIS.getBlockState(this.plotworld.WALL_BLOCK);
        this.plotfloors = new BlockState[this.plotworld.TOP_BLOCK.length];
        for (int i = 0; i < this.plotworld.TOP_BLOCK.length; i++) {
            this.plotfloors[i] = SpongeMain.THIS.getBlockState(this.plotworld.TOP_BLOCK[i]);
        }
        this.filling = new BlockState[this.plotworld.MAIN_BLOCK.length];
        for (int i = 0; i < this.plotworld.MAIN_BLOCK.length; i++) {
            this.filling[i] = SpongeMain.THIS.getBlockState(this.plotworld.MAIN_BLOCK[i]);
        }
        if ((this.filling.length > 1) || (this.plotfloors.length > 1)) {
            this.doState = true;
        }
    }

    @Override
    public PlotWorld getNewPlotWorld(String world) {
        if (this.plotworld == null) {
            this.plotworld = new HybridPlotWorld(world); 
        }
        return this.plotworld;
    }

    @Override
    public PlotManager getPlotManager() {
        if (SpongeBasicGen.manager == null) {
            SpongeBasicGen.manager = new HybridPlotManager();
        }
        return SpongeBasicGen.manager;
    }

    @Override
    public List<SpongePlotPopulator> getPlotPopulators() {
        // TODO Auto-generated method stub
        return new ArrayList<>();
    }

    private SpongeBasicPop generator;
    
    @Override
    public SpongePlotPopulator getGenerator() {
        if (generator == null) {
            generator = new SpongeBasicPop(this);
        }
        return generator;
    }
    
    private BiomeGenerator biome;

    @Override
    public BiomeGenerator getPlotBiomeProvider() {
        if (biome == null) {
            biome = new SpongeBasicBiomeProvider(plotworld);
        }
        return biome;
    }
    
}
