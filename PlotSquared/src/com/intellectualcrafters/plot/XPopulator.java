package com.intellectualcrafters.plot;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.generator.BlockPopulator;

import java.util.Random;

/**
 * TODO recode this class
 * Fuck you PlotMe!
 * @author Citymonstret
 *
 */
public class XPopulator extends BlockPopulator {
    private SetBlockFast setBlockClass = null;
    public XPopulator() {
        try {
            setBlockClass = new SetBlockFast();
        }
        catch (NoClassDefFoundError e) {
            PlotMain.sendConsoleSenderMessage(C.PREFIX.s() + "&cFast plot clearing is currently not enabled.");
            PlotMain.sendConsoleSenderMessage(C.PREFIX.s() + "&c - Please get PlotSquared for "+Bukkit.getVersion()+" for improved performance");
        }
    }
    
    public void setCuboidRegion(int x1,int x2, int y1, int y2, int z1, int z2, short id, byte data, World w) {
        if (data!=0) {
            for (int x = x1; x < x2; x++) {
                for (int z = z1; z < z2; z++) {
                    for (int y = y1; y < y2; y++) {
                        if (w.getBlockTypeIdAt(x, y, z)==id)
                            setBlock(w, x, y, z, id, data);
                    }
                }
            }
        }
    }
    
    public short[] getBlock(String block) {
        if (block.contains(":")) {
            String[] split = block.split(":");
            return new short[] {Short.parseShort(split[0]),Short.parseShort(split[1])};
        }
        return new short[] {Short.parseShort(block),0};
    }
    
	@Override
	public void populate(World w, Random r, Chunk c) {
	    PlotWorld plotworld = PlotMain.getWorldSettings(w);
	    
	    int plotsize = plotworld.PLOT_WIDTH;
	    int pathsize = plotworld.ROAD_WIDTH;
	    int wallheight = plotworld.WALL_HEIGHT;
	    int roadheight = plotworld.ROAD_HEIGHT;
	    int plotheight = plotworld.PLOT_HEIGHT;
	    int size = pathsize + plotsize;
	    byte w_v, f1_v, wf_v, f2_v;
	    
	    short w_id, f1_id, wf_id, f2_id;
	    
	    // WALL
	    short[] result_w = getBlock(plotworld.WALL_BLOCK); 
	    w_id = result_w[0];
	    w_v = (byte) result_w[1];
	     
	    // WALL FILLING
	    short[] result_wf = getBlock(plotworld.WALL_FILLING);
	    wf_id = result_wf[0];
        wf_v = (byte) result_wf[1];
	    
	    // ROAD
        short[] result_f1 = getBlock(plotworld.ROAD_BLOCK);
        f1_id = result_f1[0];
        f1_v = (byte) result_f1[1];
	    //
	    
	    // Floor 2
        short[] result_f2 = getBlock(plotworld.ROAD_STRIPES);
        f2_id = result_f1[0];
        f2_v = (byte) result_f1[1];
	    //

      int cx = c.getX(), cz = c.getZ();

        double pathWidthLower;
        pathWidthLower = Math.floor(pathsize/2);
        if (cx<0)
            cx+=((-cx)*(size));
        if (cz<0)
            cz+=((-cz)*(size));
        double absX = (cx*16+16-pathWidthLower-1+8*size);
        double absZ = (cz*16+16-pathWidthLower-1+8*size);
        int plotMinX = (int) (((absX)%size));
        int plotMinZ = (int) (((absZ)%size));
        int roadStartX = (plotMinX + pathsize);
        int roadStartZ = (plotMinZ + pathsize);
        if (roadStartX>=size)
            roadStartX-=size;
        if (roadStartZ>=size)
            roadStartZ-=size;
        
        // ROADS
        
        if (plotMinZ+1<=16||roadStartZ<=16&&roadStartZ>0) {
            int start = (int) Math.max(16-plotMinZ-pathsize+1,16-roadStartZ+1);
            int end = (int) Math.min(16-plotMinZ-1,16-roadStartZ+pathsize);
            if (start>=0 && start<=16 && end <0)
                end = 16;
            setCuboidRegion(0, 16, 1, roadheight+1, Math.max(start,0),Math.min(16,end), f1_id, f1_v, w);
        }
        if (plotMinX+1<=16||roadStartX<=16&&roadStartX>0) {
            int start = (int) Math.max(16-plotMinX-pathsize+1,16-roadStartX+1);
            int end = (int) Math.min(16-plotMinX-1,16-roadStartX+pathsize);
            if (start>=0 && start<=16 && end <0)
                end = 16;
            setCuboidRegion(Math.max(start,0), Math.min(16,end), 1, roadheight+1, 0, 16, f1_id, f1_v, w);
        }
        
        if (pathsize>4) {
            if ((plotMinZ+2)%size<=16) {
                int value = (plotMinZ+2)%size;
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
                setCuboidRegion(0, end, wallheight, wallheight+1, 16-value, 16-value+1, f2_id, f2_v, w); //
                setCuboidRegion(start, 16, wallheight, wallheight+1, 16-value, 16-value+1, f2_id, f2_v, w); //
            }
            if ((plotMinX+2)%size<=16) {
                int value = (plotMinX+2)%size;
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
                setCuboidRegion( 16-value, 16-value+1,wallheight, wallheight+1, 0, end, f2_id, f2_v, w); //
                setCuboidRegion( 16-value, 16-value+1, wallheight, wallheight+1,start, 16, f2_id, f2_v, w); //
            }
            if (roadStartZ<=16&&roadStartZ>0) {
                int val = roadStartZ;
                if (val==0)
                    val+=16-pathsize+2;
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
                setCuboidRegion(0, end, wallheight, wallheight+1, 16-val+1, 16-val+2, f2_id, f2_v, w); //
                setCuboidRegion(start, 16, wallheight, wallheight+1, 16-val+1, 16-val+2, f2_id, f2_v, w); //
            }
            if (roadStartX<=16&&roadStartX>0) {
                int val = roadStartX;
                if (val==0)
                    val+=16-pathsize+2;
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
                setCuboidRegion(16-val+1, 16-val+2, wallheight, wallheight+1, 0, end, f2_id, f2_v, w); //
                setCuboidRegion(16-val+1, 16-val+2, wallheight, wallheight+1, start, 16, f2_id, f2_v, w); //
            }
        }
        
        // WALLS
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
                setCuboidRegion(0, end, 1, wallheight+1, 16-plotMinZ-1, 16-plotMinZ, wf_id, wf_v, w);
                setCuboidRegion(0, end, wallheight+1, wallheight+2, 16-plotMinZ-1, 16-plotMinZ, w_id, w_v, w);
                setCuboidRegion(start, 16, 1, wallheight+1, 16-plotMinZ-1, 16-plotMinZ, wf_id, wf_v, w);
                setCuboidRegion(start, 16, wallheight+1, wallheight+2, 16-plotMinZ-1, 16-plotMinZ, w_id, w_v, w);
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
                setCuboidRegion( 16-plotMinX-1, 16-plotMinX, 1, wallheight+1,0, end, wf_id, wf_v, w);
                setCuboidRegion( 16-plotMinX-1, 16-plotMinX,wallheight+1, wallheight+2, 0, end, w_id, w_v, w);
                setCuboidRegion(16-plotMinX-1, 16-plotMinX, 1, wallheight+1, start, 16, wf_id, wf_v, w);
                setCuboidRegion( 16-plotMinX-1, 16-plotMinX, wallheight+1, wallheight+2,start, 16, w_id, w_v, w);
            }
            if (roadStartZ<=16&&roadStartZ>0) {
                int start,end;
                if (plotMinX+1<=16)
                    start = 16-plotMinX;
                else
                    start = 16;
                if (roadStartX<=16)
                    end = 16-roadStartX;
                else
                    end = 0;
                if (!(plotMinX+1<=16||roadStartX<=16)) {
                    start = 0;
                }
                setCuboidRegion(0, end, 1, wallheight+1, 16-roadStartZ, 16-roadStartZ+1, wf_id, wf_v, w);
                setCuboidRegion(0, end, wallheight+1, wallheight+2, 16-roadStartZ, 16-roadStartZ+1, w_id, w_v, w);
                setCuboidRegion(start, 16, 1, wallheight+1, 16-roadStartZ, 16-roadStartZ+1, wf_id, wf_v, w);
                setCuboidRegion(start, 16, wallheight+1, wallheight+2, 16-roadStartZ, 16-roadStartZ+1, w_id, w_v, w);
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
                setCuboidRegion( 16-roadStartX, 16-roadStartX+1, 1, wallheight+1,0, end, wf_id, wf_v, w);
                setCuboidRegion( 16-roadStartX, 16-roadStartX+1,wallheight+1, roadheight+2,0, end,  w_id, w_v, w);
                setCuboidRegion( 16-roadStartX, 16-roadStartX+1, 1, wallheight+1, start, 16,wf_id, wf_v, w);
                setCuboidRegion( 16-roadStartX, 16-roadStartX+1,wallheight+1, wallheight+2, start, 16, w_id, w_v, w);
            }
        }
        
        // TODO PLOT MAIN - How are we going to do the IDs for the actual plot if we have randomized blocks...
        // - possibly on plot claiming, we could regenerate using the xpopulator
	}

	@SuppressWarnings("deprecation")
	private void setBlock(World w, int x, int y, int z, short id, byte val) {
	    if (setBlockClass!=null) {
	        setBlockClass.set(w, x, y, z, id, val);
	    }
	    else {
	        if (val != 0) {
	            w.getBlockAt(x, y, z).setTypeIdAndData(id, val, false);
	        } else {
	            w.getBlockAt(x, y, z).setTypeId(id);
	        } 
	    }
	}

}
