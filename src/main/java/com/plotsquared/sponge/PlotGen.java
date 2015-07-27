package com.plotsquared.sponge;

import com.flowpowered.math.vector.Vector3i;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.world.World;
import org.spongepowered.api.world.extent.ImmutableBiomeArea;
import org.spongepowered.api.world.extent.MutableBlockVolume;
import org.spongepowered.api.world.gen.GeneratorPopulator;

public class PlotGen implements GeneratorPopulator {

    public String worldname;
    public long seed;
    

    public final int PLOT_HEIGHT = 64; // Plot height of 64
    public final int PLOT_WIDTH = 42; // Plot width of 42
    public final int ROAD_WIDTH = 7; // Road width of 7
    
    public final BlockState ROAD_BLOCK = BlockTypes.QUARTZ_BLOCK.getDefaultState(); // Quartz
    public final BlockState MAIN_BLOCK = BlockTypes.STONE.getDefaultState(); // Stone
    public final BlockState WALL_BLOCK = BlockTypes.BEDROCK.getDefaultState(); // Bedrock
    public final BlockState BORDER_BLOCK = BlockTypes.STONE_SLAB.getDefaultState(); // Stone slab
    public final BlockState[] FLOOR_BLOCK = new BlockState[] {BlockTypes.GRASS.getDefaultState(), BlockTypes.SPONGE.getDefaultState()}; // Grass and sponge 
    
    public final int total_width;
    public final int road_width_lower;
    public final int road_width_upper;
    
    /**
     * I'm using my PseudoRandom class as it's more efficient and we don't need secure randomness
     */
    public final PseudoRandom RANDOM = new PseudoRandom();
    private SpongeMain main;

    public PlotGen(SpongeMain main, String worldname, long seed) {
        this.main = main;
        this.worldname = worldname;
        this.seed = seed;
        
        total_width = PLOT_WIDTH + ROAD_WIDTH;
        
        // Calculating the bottom and top road portions (this is for a PlotSquared compatible generator, but you can have any offset you want)
        if ((ROAD_WIDTH % 2) == 0) {
            road_width_lower = ROAD_WIDTH / 2 - 1;
        } else {
            road_width_lower = ROAD_WIDTH / 2;
        }
        road_width_upper = road_width_lower + PLOT_WIDTH + 1;
        main.log("LOADED GEN FOR: " + worldname);
    }
    
    /**
     * This simple pairing function is used for the seed for each chunk,
     *  - This is useful if you want generation to appear random, but be the same each time
     *  - You could also use a simple hash function like `return x + y * 31` - but this looks fancier
     * @param x
     * @param y
     * @return
     */
    public int pair(int x, int y) {
        long hash;
        if (x >= 0) {
            if (y >= 0) {
                hash = (x * x) + (3 * x) + (2 * x * y) + y + (y * y) + 2;
            } else {
                final int y1 = -y;
                hash = (x * x) + (3 * x) + (2 * x * y1) + y1 + (y1 * y1) + 1;
            }
        } else {
            final int x1 = -x;
            if (y >= 0) {
                hash = -((x1 * x1) + (3 * x1) + (2 * x1 * y) + y + (y * y));
            } else {
                final int y1 = -y;
                hash = -((x1 * x1) + (3 * x1) + (2 * x1 * y1) + y1 + (y1 * y1) + 1);
            }
        }
        return (int) (hash % Integer.MAX_VALUE);
    }

    @Override
    public void populate(World world, MutableBlockVolume buffer, ImmutableBiomeArea biomeBase) {
        try {
            Vector3i min = buffer.getBlockMin();
            int X = min.getX();
            int Z = min.getZ();
            int cx = X >> 4;
            int cz = Z >> 4;
            main.log("POPULATING " + worldname + " | " + cx + "," + cz);
            // If you have any random elements to your generation, you will want to set the state of the random class
            RANDOM.state = pair(cx, cz);

            // TODO set bedrock
            
            // We want all plots to be the same
            // To do this we will need to reduce the coordinates to the same base location
            // To get the world coord from a chunk coord we multiply by 16 `cx << 4`
            // Then we find the remainder of that `(cx << 4) % total_width`
            // We don't want negative numbers though so add the `total_width` if the remainder is less than 0
            // We have to do this as the plot size will not necessarily have to be a multiple of 16, and so it won't always align to the chunk
            // If the total width is a multiple of 16, you can in fact make some neat optimizations, see PlotSquaredMG source for more info
            int bx = (cx << 4) % total_width + (cx < 0 ? total_width : 0);
            int bz = (cz << 4) % total_width + (cz < 0 ? total_width : 0);
            
            // This is our main loop where we go over all the columns in the chunk and set the blocks
            for (int x = 0; x < 16; x++) {
                for (int z = 0; z < 16; z++) {
                    // Getting the reduced coordinate
                    int xx = (x + bx);
                    int zz = (z + bz);
                    // If it's greater than the total width, we need to reduce it
                    // Although we reduced the chunk coordinates before, that only means the base coordinate of the chunk is reduced
                    // The top coordinate could still be outside our desired range
                    if (xx >= total_width) xx -= total_width;
                    if (zz >= total_width) zz -= total_width;
                    
                    // ROAD
                    if (xx < road_width_lower || zz < road_width_lower || xx > road_width_upper || zz > road_width_upper) {
                        for (int y = 0; y < PLOT_HEIGHT; y++) setBlock(buffer, X + x, y, Z + z, ROAD_BLOCK); 
                    }
                    // WALL
                    else if (xx == road_width_lower || zz == road_width_lower || xx == road_width_upper || zz == road_width_upper) {
                        // Set the wall block
                        for (int y = 0; y < PLOT_HEIGHT; y++) setBlock(buffer, X + x, y, Z + z, WALL_BLOCK);
                        // Set the border block (on top of the wall)
                        setBlock(buffer, X + x, PLOT_HEIGHT, Z + z, BORDER_BLOCK);
                    }
                    // PLOT
                    else {
                        // Set the main plot block
                        for (int y = 0; y < PLOT_HEIGHT - 1; y++) setBlock(buffer, X + x, y, Z + z, MAIN_BLOCK);
                        // Set the plot floor
                        setBlock(buffer, X + x, PLOT_HEIGHT - 1, Z + z, FLOOR_BLOCK);
                    }
                }
            }
            main.log("SUCCESS " + worldname + " | " + cx + "," + cz);
        }
        catch (Exception e) {
            // Normally if something goes wrong in your code it will fail silently with world generators
            // Having this try/catch will help recover the exception
            e.printStackTrace();
        }
    }

    public void setBlock(MutableBlockVolume buffer, int x, int y, int z, BlockState...states) {
        if (states.length == 1) {
            setBlock(buffer, x, y, z, states[0]);
        }
        setBlock(buffer, x, y, z, states[RANDOM.random(states.length)]);
    }
    
    public void setBlock(MutableBlockVolume buffer, int x, int y, int z, BlockState state) {
        buffer.setBlock(x, y, z, state);
    }
}
