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
 *  
 * @auther Empire92
 * @author Citymonstret
 * 
 */
@SuppressWarnings("deprecation")
public class WorldGenerator extends ChunkGenerator {
    private long state;

    public final long nextLong() {
        long a = this.state;
        this.state = xorShift64(a);
        return a;
    }

    public final long xorShift64(long a) {
        a ^= (a << 21);
        a ^= (a >>> 35);
        a ^= (a << 4);
        return a;
    }

    public final int random(int n) {
        long r = ((nextLong() >>> 32) * n) >> 32;
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
        this.plotworld = new PlotWorld();
        Map<String, Object> options = new HashMap<String, Object>();

        options.put("worlds." + world + ".plot_height", PLOT_HEIGHT_DEFAULT);
        options.put("worlds." + world + ".plot_size", PLOT_WIDTH_DEFAULT);
        options.put("worlds." + world + ".plot_biome", PLOT_BIOME_DEFAULT);
        options.put("worlds." + world + ".plot_filling", Arrays.asList(MAIN_BLOCK_DEFAULT));
        options.put("worlds." + world + ".top_floor", Arrays.asList(TOP_BLOCK_DEFAULT));
        options.put("worlds." + world + ".wall.block", WALL_BLOCK_DEFAULT);
        options.put("worlds." + world + ".road.width", ROAD_WIDTH_DEFAULT);
        options.put("worlds." + world + ".road.height", ROAD_HEIGHT_DEFAULT);
        options.put("worlds." + world + ".road.block", ROAD_BLOCK_DEFAULT);
        options.put("worlds." + world + ".road.stripes", ROAD_STRIPES_DEFAULT);
        options.put("worlds." + world + ".road.enable_stripes", ROAD_STRIPES_ENABLED_DEFAULT);
        options.put("worlds." + world + ".wall.filling", WALL_FILLING_DEFAULT);
        options.put("worlds." + world + ".wall.height", WALL_HEIGHT_DEFAULT);
        options.put("worlds." + world + ".schematic.on_claim", SCHEMATIC_ON_CLAIM_DEFAULT);
        options.put("worlds." + world + ".schematic.file", SCHEMATIC_FILE_DEFAULT);
        options.put("worlds." + world + ".default_flags", DEFAULT_FLAGS_DEFAULT);

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
        this.plotworld.PLOT_HEIGHT = config.getInt("worlds." + world + ".plot_height");
        this.plotworld.PLOT_WIDTH = config.getInt("worlds." + world + ".plot_size");
        this.plotworld.PLOT_BIOME = config.getString("worlds." + world + ".plot_biome");
        this.plotworld.MAIN_BLOCK = config.getStringList("worlds." + world + ".plot_filling").toArray(new String[0]);
        this.plotworld.TOP_BLOCK = config.getStringList("worlds." + world + ".top_floor").toArray(new String[0]);
        this.plotworld.WALL_BLOCK = config.getString("worlds." + world + ".wall.block");
        this.plotworld.ROAD_WIDTH = config.getInt("worlds." + world + ".road.width");
        this.plotworld.ROAD_HEIGHT = config.getInt("worlds." + world + ".road.height");
        this.plotworld.ROAD_STRIPES_ENABLED = config.getBoolean("worlds." + world + ".road.enable_stripes");
        this.plotworld.ROAD_BLOCK = config.getString("worlds." + world + ".road.block");
        this.plotworld.ROAD_STRIPES = config.getString("worlds." + world + ".road.stripes");
        this.plotworld.WALL_FILLING = config.getString("worlds." + world + ".wall.filling");
        this.plotworld.WALL_HEIGHT = config.getInt("worlds." + world + ".wall.height");
        this.plotworld.PLOT_CHAT = config.getBoolean("worlds." + world + ".plot_chat");
        this.plotworld.SCHEMATIC_ON_CLAIM = config.getBoolean("worlds." + world + ".schematic.on_claim");
        this.plotworld.SCHEMATIC_FILE = config.getString("worlds." + world + ".schematic.file");

        String[] default_flags_string = config.getStringList("worlds." + world + ".default_flags").toArray(new String[0]);
        Flag[] default_flags = new Flag[default_flags_string.length];
        for (int i = 0; i < default_flags.length; i++) {
            String current = default_flags_string[i];
            if (current.contains(",")) {
                default_flags[i] = new Flag(FlagManager.getFlag(current.split(",")[0], true), current.split(",")[1]);
            } else {
                default_flags[i] = new Flag(FlagManager.getFlag(current, true), "");
            }
        }
        this.plotworld.DEFAULT_FLAGS = default_flags;

        PlotMain.addPlotWorld(world, this.plotworld);

        this.plotsize = this.plotworld.PLOT_WIDTH;
        this.pathsize = this.plotworld.ROAD_WIDTH;
        this.bottom = (short) Material.BEDROCK.getId();

        this.floor1 = getBlock(this.plotworld.ROAD_BLOCK);
        this.floor2 = getBlock(this.plotworld.ROAD_STRIPES);
        this.wallfilling = getBlock(this.plotworld.WALL_FILLING);
        this.size = this.pathsize + this.plotsize;
        this.wall = getBlock(this.plotworld.WALL_BLOCK);

        this.plotfloors = new short[this.plotworld.TOP_BLOCK.length];
        this.filling = new short[this.plotworld.MAIN_BLOCK.length];

        for (int i = 0; i < this.plotworld.TOP_BLOCK.length; i++) {
            this.plotfloors[i] = getBlock(this.plotworld.TOP_BLOCK[i]);
        }
        for (int i = 0; i < this.plotworld.MAIN_BLOCK.length; i++) {
            this.filling[i] = getBlock(this.plotworld.MAIN_BLOCK[i]);
        }

        this.wallheight = this.plotworld.WALL_HEIGHT;
        this.roadheight = this.plotworld.ROAD_HEIGHT;
        this.plotheight = this.plotworld.PLOT_HEIGHT;

        this.biome = Biome.FOREST;
        for (Biome myBiome : Biome.values()) {
            if (myBiome.name().equalsIgnoreCase(this.plotworld.PLOT_BIOME)) {
                this.biome = myBiome;
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

    public void setCuboidRegion(int x1, int x2, int y1, int y2, int z1, int z2, short id) {
        for (int x = x1; x < x2; x++) {
            for (int z = z1; z < z2; z++) {
                for (int y = y1; y < y2; y++) {
                    setBlock(this.result, x, y, z, id);
                }
            }
        }
    }

    private void setCuboidRegion(int x1, int x2, int y1, int y2, int z1, int z2, short[] id) {
        if (id.length == 1) {
            setCuboidRegion(x1, x2, y1, y2, z1, z2, id[0]);
        } else {
            for (int x = x1; x < x2; x++) {
                for (int z = z1; z < z2; z++) {
                    for (int y = y1; y < y2; y++) {
                        int i = random(id.length);
                        setBlock(this.result, x, y, z, id[i]);
                    }
                }
            }
        }

    }

    @SuppressWarnings("deprecation")
    @Override
    public short[][] generateExtBlockSections(World world, Random random, int cx, int cz, BiomeGrid biomes) {
        int maxY = world.getMaxHeight();

        this.result = new short[maxY / 16][];
        double pathWidthLower;
        if ((pathsize % 2) == 0) {
            pathWidthLower = Math.floor(this.pathsize / 2)-1;
        }
        else {
            pathWidthLower = Math.floor(this.pathsize / 2);
        }
        final int prime = 31;
        int h = 1;
        h = (prime * h) + cx;
        h = (prime * h) + cz;
        this.state = h;
        cx = (cx % this.size) + (8 * this.size);
        cz = (cz % this.size) + (8 * this.size);
        int absX = (int) ((((cx * 16) + 16) - pathWidthLower - 1) + (8 * this.size));
        int absZ = (int) ((((cz * 16) + 16) - pathWidthLower - 1) + (8 * this.size));
        int plotMinX = (((absX) % this.size));
        int plotMinZ = (((absZ) % this.size));
        int roadStartX = (plotMinX + this.pathsize);
        int roadStartZ = (plotMinZ + this.pathsize);
        if (roadStartX >= this.size) {
            roadStartX -= this.size;
        }
        if (roadStartZ >= this.size) {
            roadStartZ -= this.size;
        }

        // BOTTOM (1/1 cuboids)
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                setBlock(this.result, x, 0, z, this.bottom);
                biomes.setBiome(x, z, this.biome);
            }
        }
        // ROAD (0/24) The following is an inefficient placeholder as it is too
        // much work to finish it

        if (((plotMinZ + 1) <= 16) || ((roadStartZ <= 16) && (roadStartZ > 0))) {
            int start = Math.max((16 - plotMinZ - this.pathsize) + 1, (16 - roadStartZ) + 1);
            int end = Math.min(16 - plotMinZ - 1, (16 - roadStartZ) + this.pathsize);
            if ((start >= 0) && (start <= 16) && (end < 0)) {
                end = 16;
            }
            setCuboidRegion(0, 16, 1, this.roadheight + 1, Math.max(start, 0), Math.min(16, end), this.floor1);
        }
        if (((plotMinX + 1) <= 16) || ((roadStartX <= 16) && (roadStartX > 0))) {
            int start = Math.max((16 - plotMinX - this.pathsize) + 1, (16 - roadStartX) + 1);
            int end = Math.min(16 - plotMinX - 1, (16 - roadStartX) + this.pathsize);
            if ((start >= 0) && (start <= 16) && (end < 0)) {
                end = 16;
            }
            setCuboidRegion(Math.max(start, 0), Math.min(16, end), 1, this.roadheight + 1, 0, 16, this.floor1);
        }

        // ROAD STRIPES
        if ((this.pathsize > 4) && this.plotworld.ROAD_STRIPES_ENABLED) {
            if ((plotMinZ + 2) <= 16) {
                int value = (plotMinZ + 2);
                int start, end;
                if ((plotMinX + 2) <= 16) {
                    start = 16 - plotMinX - 1;
                } else {
                    start = 16;
                }
                if ((roadStartX - 1) <= 16) {
                    end = (16 - roadStartX) + 1;
                } else {
                    end = 0;
                }
                if (!(((plotMinX + 2) <= 16) || ((roadStartX - 1) <= 16))) {
                    start = 0;
                }
                setCuboidRegion(0, end, this.wallheight, this.wallheight + 1, 16 - value, (16 - value) + 1, this.floor2); //
                setCuboidRegion(start, 16, this.wallheight, this.wallheight + 1, 16 - value, (16 - value) + 1, this.floor2); //
            }
            if ((plotMinX + 2) <= 16) {
                int value = (plotMinX + 2);
                int start, end;
                if ((plotMinZ + 2) <= 16) {
                    start = 16 - plotMinZ - 1;
                } else {
                    start = 16;
                }
                if ((roadStartZ - 1) <= 16) {
                    end = (16 - roadStartZ) + 1;
                } else {
                    end = 0;
                }
                if (!(((plotMinZ + 2) <= 16) || ((roadStartZ - 1) <= 16))) {
                    start = 0;
                }
                setCuboidRegion(16 - value, (16 - value) + 1, this.wallheight, this.wallheight + 1, 0, end, this.floor2); //
                setCuboidRegion(16 - value, (16 - value) + 1, this.wallheight, this.wallheight + 1, start, 16, this.floor2); //
            }
            if ((roadStartZ <= 16) && (roadStartZ > 1)) {
                int val = roadStartZ;
                int start, end;
                if ((plotMinX + 2) <= 16) {
                    start = 16 - plotMinX - 1;
                } else {
                    start = 16;
                }
                if ((roadStartX - 1) <= 16) {
                    end = (16 - roadStartX) + 1;
                } else {
                    end = 0;
                }
                if (!(((plotMinX + 2) <= 16) || ((roadStartX - 1) <= 16))) {
                    start = 0;
                }
                setCuboidRegion(0, end, this.wallheight, this.wallheight + 1, (16 - val) + 1, (16 - val) + 2, this.floor2);
                setCuboidRegion(start, 16, this.wallheight, this.wallheight + 1, (16 - val) + 1, (16 - val) + 2, this.floor2);
            }
            if ((roadStartX <= 16) && (roadStartX > 1)) {
                int val = roadStartX;
                int start, end;
                if ((plotMinZ + 2) <= 16) {
                    start = 16 - plotMinZ - 1;
                } else {
                    start = 16;
                }
                if ((roadStartZ - 1) <= 16) {
                    end = (16 - roadStartZ) + 1;
                } else {
                    end = 0;
                }
                if (!(((plotMinZ + 2) <= 16) || ((roadStartZ - 1) <= 16))) {
                    start = 0;
                }
                setCuboidRegion((16 - val) + 1, (16 - val) + 2, this.wallheight, this.wallheight + 1, 0, end, this.floor2); //
                setCuboidRegion((16 - val) + 1, (16 - val) + 2, this.wallheight, this.wallheight + 1, start, 16, this.floor2); //
            }
        }

        // Plot filling (28/28 cuboids) (10x2 + 4x2)
        if (this.plotsize > 16) {
            if (roadStartX <= 16) {
                if (roadStartZ <= 16) {
                    setCuboidRegion(0, 16 - roadStartX, 1, this.plotheight, 0, 16 - roadStartZ, this.filling);
                    setCuboidRegion(0, 16 - roadStartX, this.plotheight, this.plotheight + 1, 0, 16 - roadStartZ, this.plotfloors);
                }
                if (plotMinZ <= 16) {
                    setCuboidRegion(0, 16 - roadStartX, 1, this.plotheight, 16 - plotMinZ, 16, this.filling);
                    setCuboidRegion(0, 16 - roadStartX, this.plotheight, this.plotheight + 1, 16 - plotMinZ, 16, this.plotfloors);
                }
            } else {
                if (roadStartZ <= 16) {
                    if (plotMinX > 16) {
                        setCuboidRegion(0, 16, 1, this.plotheight, 0, 16 - roadStartZ, this.filling);
                        setCuboidRegion(0, 16, this.plotheight, this.plotheight + 1, 0, 16 - roadStartZ, this.plotfloors);
                    }
                }
            }
            if (plotMinX <= 16) {
                if (plotMinZ <= 16) {
                    setCuboidRegion(16 - plotMinX, 16, 1, this.plotheight, 16 - plotMinZ, 16, this.filling);
                    setCuboidRegion(16 - plotMinX, 16, this.plotheight, this.plotheight + 1, 16 - plotMinZ, 16, this.plotfloors);
                } else {
                    int z = 16 - roadStartZ;
                    if (z < 0) {
                        z = 16;
                    }
                    setCuboidRegion(16 - plotMinX, 16, 1, this.plotheight, 0, z, this.filling);
                    setCuboidRegion(16 - plotMinX, 16, this.plotheight, this.plotheight + 1, 0, z, this.plotfloors);
                }
                if (roadStartZ <= 16) {
                    setCuboidRegion(16 - plotMinX, 16, 1, this.plotheight, 0, 16 - roadStartZ, this.filling);
                    setCuboidRegion(16 - plotMinX, 16, this.plotheight, this.plotheight + 1, 0, 16 - roadStartZ, this.plotfloors);
                } else {
                    if (roadStartX <= 16) {
                        if (plotMinZ > 16) {
                            int x = 16 - roadStartX;
                            if (x < 0) {
                                x = 16;
                            }
                            setCuboidRegion(0, x, 1, this.plotheight, 0, 16, this.filling);
                            setCuboidRegion(0, x, this.plotheight, this.plotheight + 1, 0, 16, this.plotfloors);
                        }
                    }
                }
            } else {
                if (plotMinZ <= 16) {
                    if (roadStartX > 16) {
                        int x = 16 - roadStartX;
                        if (x < 0) {
                            x = 16;
                        }
                        setCuboidRegion(0, x, 1, this.plotheight, 16 - plotMinZ, 16, this.filling);
                        setCuboidRegion(0, x, this.plotheight, this.plotheight + 1, 16 - plotMinZ, 16, this.plotfloors);
                    }
                } else {
                    if (roadStartZ > 16) {
                        int x = 16 - roadStartX;
                        if (x < 0) {
                            x = 16;
                        }
                        int z = 16 - roadStartZ;
                        if (z < 0) {
                            z = 16;
                        }
                        if (roadStartX > 16) {
                            setCuboidRegion(0, x, 1, this.plotheight, 0, z, this.filling);
                            setCuboidRegion(0, x, this.plotheight, this.plotheight + 1, 0, z, this.plotfloors);
                        } else {
                            setCuboidRegion(0, x, 1, this.plotheight, 0, z, this.filling);
                            setCuboidRegion(0, x, this.plotheight, this.plotheight + 1, 0, z, this.plotfloors);
                        }
                    }
                }
            }
        } else {
            if (roadStartX <= 16) {
                if (roadStartZ <= 16) {
                    setCuboidRegion(0, 16 - roadStartX, 1, this.plotheight, 0, 16 - roadStartZ, this.filling);
                    setCuboidRegion(0, 16 - roadStartX, this.plotheight, this.plotheight + 1, 0, 16 - roadStartZ, this.plotfloors);
                }
                if (plotMinZ <= 16) {
                    setCuboidRegion(0, 16 - roadStartX, 1, this.plotheight, 16 - plotMinZ, 16, this.filling);
                    setCuboidRegion(0, 16 - roadStartX, this.plotheight, this.plotheight + 1, 16 - plotMinZ, 16, this.plotfloors);
                }
            }
            if (plotMinX <= 16) {
                if (plotMinZ <= 16) {
                    setCuboidRegion(16 - plotMinX, 16, 1, this.plotheight, 16 - plotMinZ, 16, this.filling);
                    setCuboidRegion(16 - plotMinX, 16, this.plotheight, this.plotheight + 1, 16 - plotMinZ, 16, this.plotfloors);
                }
                if (roadStartZ <= 16) {
                    setCuboidRegion(16 - plotMinX, 16, 1, this.plotheight, 0, 16 - roadStartZ, this.filling);
                    setCuboidRegion(16 - plotMinX, 16, this.plotheight, this.plotheight + 1, 0, 16 - roadStartZ, this.plotfloors);
                }
            }
        }

        // WALLS (16/16 cuboids)
        if (this.pathsize > 0) {
            if ((plotMinZ + 1) <= 16) {
                int start, end;
                if ((plotMinX + 2) <= 16) {
                    start = 16 - plotMinX - 1;
                } else {
                    start = 16;
                }
                if ((roadStartX - 1) <= 16) {
                    end = (16 - roadStartX) + 1;
                } else {
                    end = 0;
                }
                if (!(((plotMinX + 2) <= 16) || ((roadStartX - 1) <= 16))) {
                    start = 0;
                }
                setCuboidRegion(0, end, 1, this.wallheight + 1, 16 - plotMinZ - 1, 16 - plotMinZ, this.wallfilling);
                setCuboidRegion(0, end, this.wallheight + 1, this.wallheight + 2, 16 - plotMinZ - 1, 16 - plotMinZ, this.wall);
                setCuboidRegion(start, 16, 1, this.wallheight + 1, 16 - plotMinZ - 1, 16 - plotMinZ, this.wallfilling);
                setCuboidRegion(start, 16, this.wallheight + 1, this.wallheight + 2, 16 - plotMinZ - 1, 16 - plotMinZ, this.wall);
            }
            if ((plotMinX + 1) <= 16) {
                int start, end;
                if ((plotMinZ + 2) <= 16) {
                    start = 16 - plotMinZ - 1;
                } else {
                    start = 16;
                }
                if ((roadStartZ - 1) <= 16) {
                    end = (16 - roadStartZ) + 1;
                } else {
                    end = 0;
                }
                if (!(((plotMinZ + 2) <= 16) || ((roadStartZ - 1) <= 16))) {
                    start = 0;
                }
                setCuboidRegion(16 - plotMinX - 1, 16 - plotMinX, 1, this.wallheight + 1, 0, end, this.wallfilling);
                setCuboidRegion(16 - plotMinX - 1, 16 - plotMinX, this.wallheight + 1, this.wallheight + 2, 0, end, this.wall);
                setCuboidRegion(16 - plotMinX - 1, 16 - plotMinX, 1, this.wallheight + 1, start, 16, this.wallfilling);
                setCuboidRegion(16 - plotMinX - 1, 16 - plotMinX, this.wallheight + 1, this.wallheight + 2, start, 16, this.wall);
            }
            if ((roadStartZ <= 16) && (roadStartZ > 0)) {
                int start, end;
                if ((plotMinX + 1) <= 16) {
                    start = 16 - plotMinX;
                } else {
                    start = 16;
                }
                if ((roadStartX + 1) <= 16) {
                    end = (16 - roadStartX) + 1;
                } else {
                    end = 0;
                }
                if (!(((plotMinX + 1) <= 16) || (roadStartX <= 16))) {
                    start = 0;
                }
                setCuboidRegion(0, end, 1, this.wallheight + 1, 16 - roadStartZ, (16 - roadStartZ) + 1, this.wallfilling);
                setCuboidRegion(0, end, this.wallheight + 1, this.wallheight + 2, 16 - roadStartZ, (16 - roadStartZ) + 1, this.wall);
                setCuboidRegion(start, 16, 1, this.wallheight + 1, 16 - roadStartZ, (16 - roadStartZ) + 1, this.wallfilling);
                setCuboidRegion(start, 16, this.wallheight + 1, this.wallheight + 2, 16 - roadStartZ, (16 - roadStartZ) + 1, this.wall);
            }
            if ((roadStartX <= 16) && (roadStartX > 0)) {
                int start, end;
                if ((plotMinZ + 1) <= 16) {
                    start = 16 - plotMinZ;
                } else {
                    start = 16;
                }
                if ((roadStartZ + 1) <= 16) {
                    end = (16 - roadStartZ) + 1;
                } else {
                    end = 0;
                }
                if (!(((plotMinZ + 1) <= 16) || ((roadStartZ + 1) <= 16))) {
                    start = 0;
                }
                setCuboidRegion(16 - roadStartX, (16 - roadStartX) + 1, 1, this.wallheight + 1, 0, end, this.wallfilling);
                setCuboidRegion(16 - roadStartX, (16 - roadStartX) + 1, this.wallheight + 1, this.roadheight + 2, 0, end, this.wall);
                setCuboidRegion(16 - roadStartX, (16 - roadStartX) + 1, 1, this.wallheight + 1, start, 16, this.wallfilling);
                setCuboidRegion(16 - roadStartX, (16 - roadStartX) + 1, this.wallheight + 1, this.wallheight + 2, start, 16, this.wall);
            }
        }
        return this.result;
    }

    private void setBlock(short[][] result, int x, int y, int z, short blkid) {
        if (result[y >> 4] == null) {
            result[y >> 4] = new short[4096];
        }
        result[y >> 4][((y & 0xF) << 8) | (z << 4) | x] = blkid;
    }
}
