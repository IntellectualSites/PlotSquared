package com.intellectualcrafters.plot.listeners.worldedit;

import java.util.HashSet;
import java.util.UUID;

import com.intellectualcrafters.plot.PS;
import com.intellectualcrafters.plot.config.Settings;
import com.intellectualcrafters.plot.object.Location;
import com.intellectualcrafters.plot.object.Plot;
import com.intellectualcrafters.plot.object.PlotPlayer;
import com.intellectualcrafters.plot.object.RegionWrapper;
import com.intellectualcrafters.plot.util.MainUtil;

public class WEManager {
    public static HashSet<String> bypass = new HashSet<>();
    
    public static boolean maskContains(HashSet<RegionWrapper> mask, int x, int z) {
        for (RegionWrapper region : mask) {
            if ((x >= region.minX) && (x <= region.maxX) && (z >= region.minZ) && (z <= region.maxZ)) {
                return true;
            }
        }
        return false;
    }
    
    public static HashSet<RegionWrapper> getMask(PlotPlayer player) {
        HashSet<RegionWrapper> regions = new HashSet<>();
        UUID uuid = player.getUUID();
        for (Plot plot : PS.get().getPlots(player.getLocation().getWorld()).values()) {
            if (!plot.settings.getMerged(0) && !plot.settings.getMerged(3)) {
                if (Settings.WE_ALLOW_HELPER ? plot.isAdded(uuid) : (plot.isOwner(uuid) || plot.trusted.contains(uuid))) {
                    Location pos1 = MainUtil.getPlotBottomLoc(plot.world, plot.id).add(1, 0, 1);
                    Location pos2 = MainUtil.getPlotTopLoc(plot.world, plot.id);
                    regions.add(new RegionWrapper(pos1.getX(), pos2.getX(), pos1.getZ(), pos2.getZ()));
                }
            }
        }
        return regions;
    }
    
    public static boolean intersects(RegionWrapper region1, RegionWrapper region2) {
        if ((region1.minX <= region2.maxX) && (region1.maxX >= region2.minX) && (region1.minZ <= region2.maxZ) && (region1.maxZ >= region2.minZ)) {
            return true;
        }
        return false;
    }
    
    public static boolean regionContains(RegionWrapper selection, HashSet<RegionWrapper> mask) {
        for (RegionWrapper region : mask) {
            if (intersects(region, selection)) {
                return true;
            }
        }
        return false;
    }
}
