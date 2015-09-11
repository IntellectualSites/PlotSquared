package com.intellectualcrafters.plot.object;

public class BO3
{
    private final ChunkLoc chunk;
    private final StringBuilder blocks;
    private final StringBuilder children;
    private final String name;

    public BO3(final String name, final ChunkLoc loc)
    {
        this.name = name;
        chunk = loc;
        blocks = new StringBuilder();
        children = new StringBuilder();
    }

    public void addChild(final BO3 child)
    {
        final ChunkLoc childloc = child.getLoc();
        children.append("Branch(" + (childloc.x - chunk.x) + ",0," + (childloc.z - chunk.z) + "," + name + "_" + childloc.x + "_" + childloc.z + ")\n");
    }

    public ChunkLoc getLoc()
    {
        return chunk;
    }

    public String getName()
    {
        return name;
    }

    public void addBlock(final int x, final int y, final int z, final PlotBlock block)
    {
        if (block.data == 0)
        {
            // Block(-3,1,-2,AIR)
            blocks.append("Block(" + x + "," + y + "," + z + "," + block.id + ")\n");
        }
        else
        {
            blocks.append("Block(" + x + "," + y + "," + z + "," + block.id + ":" + block.data + ")\n");
        }
    }

    public String getBlocks()
    {
        return blocks.toString();
    }

    public String getChildren()
    {
        return children.toString();
    }
}
