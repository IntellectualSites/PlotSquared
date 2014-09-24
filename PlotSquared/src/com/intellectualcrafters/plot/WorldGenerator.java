package com.intellectualcrafters.plot;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.generator.BlockPopulator;
import org.bukkit.generator.ChunkGenerator;

import java.io.IOException;
import java.util.*;
import java.util.Map.Entry;

import static com.intellectualcrafters.plot.PlotWorld.*;


/**
 * TODO finish recoding this class
 * @auther Empire92
 * @author Citymonstret
 *
 */
public class WorldGenerator extends ChunkGenerator {
    private long state = 100;
    public final long nextLong() {
        long a=state;
        state = xorShift64(a);
        return a;
    }

    public final long xorShift64(long a) {
        a ^= (a << 21);
        a ^= (a >>> 35);
        a ^= (a << 4);
        return a;
    }

    public final int random(int n) {
        long r = ((nextLong()>>>32)*n)>>32;
        return (int) r;
    }
    PlotWorld plotworld;
    short[][] result;
    int plotsize;
    int pathsize;
    short bottom;
    short wall;
    short wallfilling;
    short floor1;
    short floor2;
    int size;
    Biome biome;
    int roadheight;
    int wallheight;
    int plotheight;
    
    short[] plotfloors;
    short[] filling;
    
    public Short getBlock(String block) {
        if (block.contains(":")) {
            String[] split = block.split(":");
            return Short.parseShort(split[0]);
        }
        return Short.parseShort(block);
    }
    
    public WorldGenerator(String world) {
        YamlConfiguration config = PlotMain.config;
        plotworld = new PlotWorld();
        Map<String, Object> options = new HashMap<String, Object>();
        
        options.put("worlds."+world+".plot_height", PLOT_HEIGHT_DEFAULT);
        options.put("worlds."+world+".plot_size", PLOT_WIDTH_DEFAULT);
        options.put("worlds."+world+".plot_biome", PLOT_BIOME_DEFAULT);
        options.put("worlds."+world+".plot_filling", Arrays.asList(MAIN_BLOCK_DEFAULT));
        options.put("worlds."+world+".top_floor", Arrays.asList(TOP_BLOCK_DEFAULT));
        options.put("worlds."+world+".wall.block", WALL_BLOCK_DEFAULT);
        options.put("worlds."+world+".road.width", ROAD_WIDTH_DEFAULT);
        options.put("worlds."+world+".road.height", ROAD_HEIGHT_DEFAULT);
        options.put("worlds."+world+".road.block", ROAD_BLOCK_DEFAULT);
        options.put("worlds."+world+".road.stripes", ROAD_STRIPES_DEFAULT);
        options.put("worlds."+world+".road.enable_stripes", ROAD_STRIPES_ENABLED_DEFAULT);
        options.put("worlds."+world+".wall.filling", WALL_FILLING_DEFAULT);
        options.put("worlds."+world+".wall.height", WALL_HEIGHT_DEFAULT);
        options.put("worlds."+world+".schematic.on_claim", SCHEMATIC_ON_CLAIM_DEFAULT);
        options.put("worlds."+world+".schematic.file", SCHEMATIC_FILE_DEFAULT);
        options.put("worlds."+world+".default_flags", DEFAULT_FLAGS_DEFAULT);
        
        for (Entry<String, Object> node : options.entrySet()) {
            if (!config.contains(node.getKey())) {
                config.set(node.getKey(), node.getValue());
            }
        }
        try {
            config.save(PlotMain.configFile);
        } catch (IOException e) {
            PlotMain.sendConsoleSenderMessage("&c[Warning] PlotSquared failed to save the configuration&7 (settings.yml may differ from the one in memory)\n - To force a save from console use /plots save");
        }
        plotworld.PLOT_HEIGHT = config.getInt("worlds."+world+".plot_height");
        plotworld.PLOT_WIDTH = config.getInt("worlds."+world+".plot_size");
        plotworld.PLOT_BIOME = config.getString("worlds."+world+".plot_biome");
        plotworld.MAIN_BLOCK = config.getStringList("worlds."+world+".plot_filling").toArray(new String[0]);
        plotworld.TOP_BLOCK = config.getStringList("worlds."+world+".top_floor").toArray(new String[0]);
        plotworld.WALL_BLOCK = config.getString("worlds."+world+".wall.block");
        plotworld.ROAD_WIDTH = config.getInt("worlds."+world+".road.width");
        plotworld.ROAD_HEIGHT = config.getInt("worlds."+world+".road.height");
        plotworld.ROAD_STRIPES_ENABLED = config.getBoolean("worlds."+world+".road.enable_stripes");
        plotworld.ROAD_BLOCK = config.getString("worlds."+world+".road.block");
        plotworld.ROAD_STRIPES = config.getString("worlds."+world+".road.stripes");
        plotworld.WALL_FILLING = config.getString("worlds."+world+".wall.filling");
        plotworld.WALL_HEIGHT = config.getInt("worlds."+world+".wall.height");
        plotworld.PLOT_CHAT = config.getBoolean("worlds."+world+".plot_chat");
        plotworld.SCHEMATIC_ON_CLAIM = config.getBoolean("worlds."+world+".schematic.on_claim");
        plotworld.SCHEMATIC_FILE = config.getString("worlds."+world+".schematic.file");
        
        String[] default_flags_string = config.getStringList("worlds."+world+".default_flags").toArray(new String[0]);
        Flag[] default_flags = new Flag[default_flags_string.length];
        for (int i = 0; i < default_flags.length; i++) {
            String current = default_flags_string[i];
            if (current.contains(","))
                default_flags[i] = new Flag(FlagManager.getFlag(current.split(",")[0], true), current.split(",")[1]);
            else
                default_flags[i] = new Flag(FlagManager.getFlag(current, true), "");
        }
        plotworld.DEFAULT_FLAGS = default_flags;
        
        PlotMain.addPlotWorld(world, plotworld);
        
        plotsize = plotworld.PLOT_WIDTH;
        pathsize = plotworld.ROAD_WIDTH;
        bottom = (short) Material.BEDROCK.getId();
        
        floor1 = getBlock(plotworld.ROAD_BLOCK);
        floor2 = getBlock(plotworld.ROAD_STRIPES);
        wallfilling = getBlock(plotworld.WALL_FILLING);
        size = pathsize + plotsize;
        wall = getBlock(plotworld.WALL_BLOCK);
        
        plotfloors = new short[plotworld.TOP_BLOCK.length];
        filling = new short[plotworld.MAIN_BLOCK.length];
        
        for (int i = 0; i < plotworld.TOP_BLOCK.length; i++) {
            plotfloors[i] = getBlock(plotworld.TOP_BLOCK[i]); 
        }
        for (int i = 0; i < plotworld.MAIN_BLOCK.length; i++) {
            filling[i] = getBlock(plotworld.MAIN_BLOCK[i]); 
        }
        
        wallheight = plotworld.WALL_HEIGHT;
        roadheight = plotworld.ROAD_HEIGHT;
        plotheight = plotworld.PLOT_HEIGHT;
        
        biome = Biome.FOREST;
        for (Biome myBiome:Biome.values()) {
            if (myBiome.name().equalsIgnoreCase(plotworld.PLOT_BIOME)) {
                biome = myBiome;
                break;
            }
        }
    }
    
    @Override
    public List<BlockPopulator> getDefaultPopulators(World world) {
        return Arrays.asList((BlockPopulator) new XPopulator(PlotMain.getWorldSettings(world)));
    }
    
    @Override
    public Location getFixedSpawnLocation(World world, Random random) {
        return new Location(world, 0, PlotMain.getWorldSettings(world).ROAD_HEIGHT + 2, 0);
    }
    
    public void setCuboidRegion(int x1,int x2, int y1, int y2, int z1, int z2, short id) {
        for (int x = x1; x < x2; x++) {
            for (int z = z1; z < z2; z++) {
                for (int y = y1; y < y2; y++) {
                    setBlock(result, x, y, z, id);
                }
            }
        }
    }

    
    
    
    private void setCuboidRegion(int x1, int x2, int y1, int y2, int z1, int z2, short[] id) {
        if (id.length==1) {
            setCuboidRegion(x1,x2,y1,y2,z1,z2,id[0]);
        }
        else {
            for (int x = x1; x < x2; x++) {
                for (int z = z1; z < z2; z++) {
                    for (int y = y1; y < y2; y++) {
                        int i = random(id.length);
                        setBlock(result, x, y, z, id[i]);
                    }
                }
            }
        }
        
    }
    @SuppressWarnings("deprecation")
    @Override
    public short[][] generateExtBlockSections(World world, Random random,
            int cx, int cz, BiomeGrid biomes) {
        int maxY = world.getMaxHeight();
        
        result = new short[maxY / 16][];
        
        double pathWidthLower;
        pathWidthLower = Math.floor(pathsize/2);
        final int prime = 31;
        int h = 1;
        h = prime * h + cx;
        h = prime * h + cz;
        state = h;
        cx=cx%size+8*size;
        cz=cz%size+8*size;
        int absX = (int) (cx*16+16-pathWidthLower-1+8*size);
        int absZ = (int) (cz*16+16-pathWidthLower-1+8*size);
        int plotMinX = (((absX)%size));
        int plotMinZ = (((absZ)%size));
        int roadStartX = (plotMinX + pathsize);
        int roadStartZ = (plotMinZ + pathsize);
        if (roadStartX>=size)
            roadStartX-=size;
        if (roadStartZ>=size)
            roadStartZ-=size;
        
        // BOTTOM (1/1 cuboids)
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                setBlock(result, x, 0, z, bottom);
                biomes.setBiome(x, z, biome);
            }
        }
        // ROAD (0/24) The following is an inefficient placeholder as it is too much work to finish it
        
        if (plotMinZ+1<=16||roadStartZ<=16&&roadStartZ>0) {
            int start = (int) Math.max(16-plotMinZ-pathsize+1,16-roadStartZ+1);
            int end = (int) Math.min(16-plotMinZ-1,16-roadStartZ+pathsize);
            if (start>=0 && start<=16 && end <0)
                end = 16;
            setCuboidRegion(0, 16, 1, roadheight+1, Math.max(start,0),Math.min(16,end), floor1);
        }
        if (plotMinX+1<=16||roadStartX<=16&&roadStartX>0) {
            int start = (int) Math.max(16-plotMinX-pathsize+1,16-roadStartX+1);
            int end = (int) Math.min(16-plotMinX-1,16-roadStartX+pathsize);
            if (start>=0 && start<=16 && end <0)
                end = 16;
            setCuboidRegion(Math.max(start,0), Math.min(16,end), 1, roadheight+1, 0, 16, floor1);
        }
        
        // ROAD STRIPES
        if (pathsize>4&&plotworld.ROAD_STRIPES_ENABLED) {
            if ((plotMinZ+2)<=16) {
                int value = (plotMinZ+2);
                int start,end;
                if (plotMinX+2<=16)
                    start = 16-plotMinX-1;
                else
                    start = 16;
                if (roadStartX-1<=16)
                    end = 16-roadStartX+1;
                else
                    end = 0;
                if (!(plotMinX+2<=16||roadStartX-1<=16)) {
                    start = 0;
                }
                setCuboidRegion(0, end, wallheight, wallheight+1, 16-value, 16-value+1, floor2); //
                setCuboidRegion(start, 16, wallheight, wallheight+1, 16-value, 16-value+1, floor2); //
            }
            if ((plotMinX+2)<=16) {
                int value = (plotMinX+2);
                int start,end;
                if (plotMinZ+2<=16)
                    start = 16-plotMinZ-1;
                else
                    start = 16;
                if (roadStartZ-1<=16)
                    end = 16-roadStartZ+1;
                else
                    end = 0;
                if (!(plotMinZ+2<=16||roadStartZ-1<=16)) {
                    start = 0;
                }
                setCuboidRegion( 16-value, 16-value+1,wallheight, wallheight+1, 0, end, floor2); //
                setCuboidRegion( 16-value, 16-value+1, wallheight, wallheight+1,start, 16, floor2); //
            }
            if (roadStartZ<=16&&roadStartZ>1) {
                int val = roadStartZ;
                int start,end;
                if (plotMinX+2<=16)
                    start = 16-plotMinX-1;
                else
                    start = 16;
                if (roadStartX-1<=16)
                    end = 16-roadStartX+1;
                else
                    end = 0;
                if (!(plotMinX+2<=16||roadStartX-1<=16)) {
                    start = 0;
                }
                setCuboidRegion(0, end, wallheight, wallheight+1, 16-val+1, 16-val+2, floor2);
                setCuboidRegion(start, 16, wallheight, wallheight+1, 16-val+1, 16-val+2, floor2);
            }
            if (roadStartX<=16&&roadStartX>1) {
                int val = roadStartX;
                int start,end;
                if (plotMinZ+2<=16)
                    start = 16-plotMinZ-1;
                else
                    start = 16;
                if (roadStartZ-1<=16)
                    end = 16-roadStartZ+1;
                else
                    end = 0;
                if (!(plotMinZ+2<=16||roadStartZ-1<=16)) {
                    start = 0;
                }
                setCuboidRegion(16-val+1, 16-val+2, wallheight, wallheight+1, 0, end, floor2); //
                setCuboidRegion(16-val+1, 16-val+2, wallheight, wallheight+1, start, 16, floor2); //
            }
        }
        
//      Plot filling (28/28 cuboids) (10x2 + 4x2)
        if (plotsize>16) {
            if (roadStartX<=16) {
                if (roadStartZ<=16) {
                    setCuboidRegion(0, 16-roadStartX, 1, plotheight, 0, 16-roadStartZ, filling);
                    setCuboidRegion(0, 16-roadStartX, plotheight, plotheight+1, 0, 16-roadStartZ, plotfloors);
                }
                if (plotMinZ<=16) {
                    setCuboidRegion(0, 16-roadStartX, 1, plotheight, 16-plotMinZ, 16, filling);
                    setCuboidRegion(0, 16-roadStartX, plotheight, plotheight+1, 16-plotMinZ, 16, plotfloors);
                }
            }
            else {
                if (roadStartZ<=16) {
                    if (plotMinX>16) {
                        setCuboidRegion(0, 16, 1, plotheight, 0, 16-roadStartZ, filling);
                        setCuboidRegion(0, 16, plotheight, plotheight+1, 0, 16-roadStartZ, plotfloors);
                    }
                }
            }
            if (plotMinX<=16) {
                if (plotMinZ<=16) {
                    setCuboidRegion(16-plotMinX, 16, 1, plotheight, 16-plotMinZ, 16, filling);
                    setCuboidRegion(16-plotMinX, 16, plotheight, plotheight+1, 16-plotMinZ, 16, plotfloors);
                }
                else {
                    int z = (int) (16-roadStartZ);
                    if (z<0)
                        z=16;
                    setCuboidRegion(16-plotMinX, 16, 1, plotheight, 0, z, filling);
                    setCuboidRegion(16-plotMinX, 16, plotheight, plotheight+1, 0, z, plotfloors);
                }
                if (roadStartZ<=16) {
                    setCuboidRegion(16-plotMinX, 16, 1, plotheight, 0, 16-roadStartZ, filling);
                    setCuboidRegion(16-plotMinX, 16, plotheight, plotheight+1, 0, 16-roadStartZ, plotfloors);
                }
                else {
                    if (roadStartX<=16) {
                        if (plotMinZ>16) {
                            int x = (int) (16-roadStartX);
                            if (x<0)
                                x=16;
                            setCuboidRegion(0, x, 1, plotheight, 0, 16, filling);
                            setCuboidRegion(0, x, plotheight,plotheight+1, 0, 16, plotfloors);
                        }
                    }
                }
            }
            else {
                if (plotMinZ<=16) {
                    if (roadStartX>16) {
                        int x = (int) (16-roadStartX);
                        if (x<0)
                            x=16;
                        setCuboidRegion(0, x, 1, plotheight, 16-plotMinZ, 16, filling);
                         setCuboidRegion(0, x, plotheight, plotheight+1, 16-plotMinZ, 16, plotfloors);
                    }
                }
                else {
                    if (roadStartZ>16) {
                        int x = (int) (16-roadStartX);
                        if (x<0)
                            x=16;
                        int z = (int) (16-roadStartZ);
                        if (z<0)
                            z=16;
                        if (roadStartX>16) {
                            setCuboidRegion(0, x, 1, plotheight, 0, z, filling);
                            setCuboidRegion(0, x, plotheight, plotheight+1, 0, z, plotfloors);
                        }
                        else {
                            setCuboidRegion(0, x, 1, plotheight, 0, z, filling);
                            setCuboidRegion(0, x, plotheight, plotheight+1, 0, z, plotfloors);
                        }
                    }
                }
            }
        }
        else {
            if (roadStartX<=16) {
                if (roadStartZ<=16) {
                    setCuboidRegion(0, 16-roadStartX, 1, plotheight, 0, 16-roadStartZ, filling);
                    setCuboidRegion(0, 16-roadStartX, plotheight, plotheight+1, 0, 16-roadStartZ, plotfloors);
                }
                if (plotMinZ<=16) {
                    setCuboidRegion(0, 16-roadStartX, 1, plotheight, 16-plotMinZ, 16, filling);
                    setCuboidRegion(0, 16-roadStartX, plotheight, plotheight+1, 16-plotMinZ, 16, plotfloors);
                }
            }
            if (plotMinX<=16) {
                if (plotMinZ<=16) {
                    setCuboidRegion(16-plotMinX, 16, 1, plotheight, 16-plotMinZ, 16, filling);
                    setCuboidRegion(16-plotMinX, 16, plotheight, plotheight+1, 16-plotMinZ, 16, plotfloors);
                }
                if (roadStartZ<=16) {
                    setCuboidRegion(16-plotMinX, 16, 1, plotheight, 0, 16-roadStartZ, filling);
                    setCuboidRegion(16-plotMinX, 16, plotheight, plotheight+1, 0, 16-roadStartZ, plotfloors);
                }
            }
        }
        
        // WALLS (16/16 cuboids)
        if (pathsize>0) {
            if (plotMinZ+1<=16) {
                int start,end;
                if (plotMinX+2<=16)
                    start = 16-plotMinX-1;
                else
                    start = 16;
                if (roadStartX-1<=16)
                    end = 16-roadStartX+1;
                else
                    end = 0;
                if (!(plotMinX+2<=16||roadStartX-1<=16)) {
                    start = 0;
                }
                setCuboidRegion(0, end, 1, wallheight+1, 16-plotMinZ-1, 16-plotMinZ, wallfilling);
                setCuboidRegion(0, end, wallheight+1, wallheight+2, 16-plotMinZ-1, 16-plotMinZ, wall);
                setCuboidRegion(start, 16, 1, wallheight+1, 16-plotMinZ-1, 16-plotMinZ, wallfilling);
                setCuboidRegion(start, 16, wallheight+1, wallheight+2, 16-plotMinZ-1, 16-plotMinZ, wall);
            }
            if (plotMinX+1<=16) {
                int start,end;
                if (plotMinZ+2<=16)
                    start = 16-plotMinZ-1;
                else
                    start = 16;
                if (roadStartZ-1<=16)
                    end = 16-roadStartZ+1;
                else
                    end = 0;
                if (!(plotMinZ+2<=16||roadStartZ-1<=16)) {
                    start = 0;
                }
                setCuboidRegion( 16-plotMinX-1, 16-plotMinX, 1, wallheight+1,0, end, wallfilling);
                setCuboidRegion( 16-plotMinX-1, 16-plotMinX,wallheight+1, wallheight+2, 0, end, wall);
                setCuboidRegion(16-plotMinX-1, 16-plotMinX, 1, wallheight+1, start, 16, wallfilling);
                setCuboidRegion( 16-plotMinX-1, 16-plotMinX, wallheight+1, wallheight+2,start, 16, wall);
            }
            if (roadStartZ<=16&&roadStartZ>0) {
                int start,end;
                if (plotMinX+1<=16)
                    start = 16-plotMinX;
                else
                    start = 16;
                if (roadStartX+1<=16)
                    end = 16-roadStartX+1;
                else
                    end = 0;
                if (!(plotMinX+1<=16||roadStartX<=16)) {
                    start = 0;
                }
                setCuboidRegion(0, end, 1, wallheight+1, 16-roadStartZ, 16-roadStartZ+1, wallfilling);
                setCuboidRegion(0, end, wallheight+1, wallheight+2, 16-roadStartZ, 16-roadStartZ+1, wall);
                setCuboidRegion(start, 16, 1, wallheight+1, 16-roadStartZ, 16-roadStartZ+1, wallfilling);
                setCuboidRegion(start, 16, wallheight+1, wallheight+2, 16-roadStartZ, 16-roadStartZ+1, wall);
            }
            if (roadStartX<=16&&roadStartX>0) {
                int start,end;
                if (plotMinZ+1<=16)
                    start = 16-plotMinZ;
                else
                    start = 16;
                if (roadStartZ+1<=16)
                    end = 16-roadStartZ+1;
                else
                    end = 0;
                if (!(plotMinZ+1<=16||roadStartZ+1<=16)) {
                    start = 0;
                }
                setCuboidRegion( 16-roadStartX, 16-roadStartX+1, 1, wallheight+1,0, end, wallfilling);
                setCuboidRegion( 16-roadStartX, 16-roadStartX+1,wallheight+1, roadheight+2,0, end,  wall);
                setCuboidRegion( 16-roadStartX, 16-roadStartX+1, 1, wallheight+1, start, 16,wallfilling);
                setCuboidRegion( 16-roadStartX, 16-roadStartX+1,wallheight+1, wallheight+2, start, 16, wall);
            }
        }
        return result;
    }
    
    @SuppressWarnings({ "deprecation", "unused" })
    private void setBlock(short[][] result, int x, int y, int z,
            Material material) {
        setBlock(result, x, y, z, (short) material.getId());

    }

    private void setBlock(short[][] result, int x, int y, int z, short blkid) {
        if (result[y >> 4] == null) {
            result[y >> 4] = new short[4096];
        }
        result[y >> 4][((y & 0xF) << 8) | (z << 4) | x] = blkid;
    }
}
