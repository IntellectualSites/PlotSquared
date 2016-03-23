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

    public BO3(String name, String world, ChunkLoc loc) {
        this.world = world;
        this.name = name;
        this.chunk = loc;
        this.blocks = new StringBuilder();
        this.children = new StringBuilder();
    }

    public void addChild(BO3 child) {
        ChunkLoc childloc = child.getLoc();
        this.children.append("Branch(").append(childloc.x - this.chunk.x).append(",0,").append(childloc.z - this.chunk.z).append(",")
                .append(this.name).append("_").append(childloc.x).append("_").append(childloc.z).append(")\n");
    }

    public ChunkLoc getLoc() {
        return this.chunk;
    }

    public String getWorld() {
        return this.world;
    }

    public String getName() {
        return this.name;
    }

    public void addBlock(int x, int y, int z, PlotBlock block) {
        if (block.data == 0) {
            // Block(-3,1,-2,AIR)
            this.blocks.append("Block(").append(x).append(",").append(y).append(",").append(z).append(",").append(block.id).append(")\n");
        } else {
            this.blocks.append("Block(").append(x).append(",").append(y).append(",").append(z).append(",").append(block.id).append(":")
                    .append(block.data).append(")\n");
        }
    }

    public String getBlocks() {
        return this.blocks.toString();
    }

    public String getChildren() {
        return this.children.toString();
    }

    public File getFile() {
        return MainUtil.getFile(PS.get().IMP.getDirectory(), Settings.BO3_SAVE_PATH + File.separator + getWorld() + File.separator + getFilename());
    }

    public String getFilename() {
        if (this.chunk.x == 0 && this.chunk.z == 0) {
            return this.name + "" + ".bo3";
        } else {
            return this.name + ("_" + this.chunk.x + "_" + this.chunk.z) + ".bo3";
        }
    }
}
