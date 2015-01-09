package com.intellectualcrafters.plot.util;

import java.io.File;
import java.util.ArrayList;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;

import com.intellectualcrafters.plot.PlotMain;
import com.intellectualcrafters.plot.object.ChunkLoc;
import com.intellectualcrafters.plot.object.Plot;

public class ChunkManager {
    public static ArrayList<ChunkLoc> getChunkChunks(World world) {
        File[] regionFiles = new File(new File(".").getAbsolutePath() + File.separator + world.getName() + File.separator + "region").listFiles();
        ArrayList<ChunkLoc> chunks = new ArrayList<>();
        for (File file : regionFiles) {
            String name = file.getName();
            if (name.endsWith("mca")) {
                String[] split = name.split("\\.");
                try {
                    chunks.add(new ChunkLoc(Integer.parseInt(split[1]), Integer.parseInt(split[2])));
                } catch (Exception e) {  }
            }
        }
        return chunks;
    }
    
    public static void deleteRegionFile(final String world, final ChunkLoc loc) {
        TaskManager.runTask(new Runnable() {
            @Override
            public void run() {
                String directory = new File(".").getAbsolutePath() + File.separator + world + File.separator + "region" + File.separator + "r." + loc.x + "." + loc.z + ".mca";
                File file = new File(directory);
                PlotMain.sendConsoleSenderMessage("&6 - Deleted region "+file.getName()+" (max 256 chunks)");
                if (file.exists()) {
                    file.delete();
                }
            }
        });
        
    }
    
    public static boolean hasPlot(World world, Chunk chunk) {
        int x1 = chunk.getX() << 4;
        int z1 = chunk.getZ() << 4;
        int x2 = x1 + 15;
        int z2 = z1 + 15;
        
        Location bot = new Location(world, x1, 0, z1);
        Plot plot;
        plot = PlotHelper.getCurrentPlot(bot); 
        if (plot != null && plot.owner != null) {
            return true;
        }
        Location top = new Location(world, x2, 0, z2);
        plot = PlotHelper.getCurrentPlot(top); 
        if (plot != null && plot.owner != null) {
            return true;
        }
        return false;
    }
}
