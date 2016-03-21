package com.intellectualcrafters.plot.object;

import com.intellectualcrafters.plot.PS;
import com.intellectualcrafters.plot.config.Settings;
import com.intellectualcrafters.plot.util.MainUtil;

import java.io.File;

public class BO3 {
    private final ChunkLoc chunk;
    private final String world;
    private final StringBuilder blocks;
    private final StringBuilder children;
    private final String name;
    
    public BO3(final String name, final String world, final ChunkLoc loc) {
        this.world = world;
        this.name = name;
        chunk = loc;
        blocks = new StringBuilder();
        children = new StringBuilder();
    }
    
    public void addChild(final BO3 child) {
        final ChunkLoc childloc = child.getLoc();
        children.append("Branch(" + (childloc.x - chunk.x) + ",0," + (childloc.z - chunk.z) + "," + name + "_" + childloc.x + "_" + childloc.z + ")\n");
    }
    
    public ChunkLoc getLoc() {
        return chunk;
    }

    public String getWorld() {
        return world;
    }
    
    public String getName() {
        return name;
    }
    
    public void addBlock(final int x, final int y, final int z, final PlotBlock block) {
        if (block.data == 0) {
            // Block(-3,1,-2,AIR)
            blocks.append("Block(" + x + "," + y + "," + z + "," + block.id + ")\n");
        } else {
            blocks.append("Block(" + x + "," + y + "," + z + "," + block.id + ":" + block.data + ")\n");
        }
    }
    
    public String getBlocks() {
        return blocks.toString();
    }
    
    public String getChildren() {
        return children.toString();
    }

    public File getFile() {
        return MainUtil.getFile(PS.get().IMP.getDirectory(), Settings.BO3_SAVE_PATH + File.separator + getWorld() + File.separator + getFilename());
    }

    public String getFilename() {
        return name + (chunk.x == 0 && chunk.z == 0 ? "" : "_" + chunk.x + "_" + chunk.z) + ".bo3";
    }
}
