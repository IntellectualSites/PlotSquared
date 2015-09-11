package com.plotsquared.listener;

import java.util.HashSet;
import java.util.UUID;

import com.intellectualcrafters.plot.PS;
import com.intellectualcrafters.plot.config.Settings;
import com.intellectualcrafters.plot.flag.FlagManager;
import com.intellectualcrafters.plot.object.Location;
import com.intellectualcrafters.plot.object.Plot;
import com.intellectualcrafters.plot.object.PlotPlayer;
import com.intellectualcrafters.plot.object.RegionWrapper;
import com.intellectualcrafters.plot.util.MainUtil;

public class WEManager
{
    //    public static HashSet<String> bypass = new HashSet<>();

    public static boolean maskContains(final HashSet<RegionWrapper> mask, final int x, final int y, final int z)
    {
        for (final RegionWrapper region : mask)
        {
            if (region.isIn(x, y, z)) { return true; }
        }
        return false;
    }

    public static boolean maskContains(final HashSet<RegionWrapper> mask, final int x, final int z)
    {
        for (final RegionWrapper region : mask)
        {
            if (region.isIn(x, z)) { return true; }
        }
        return false;
    }

    public static HashSet<RegionWrapper> getMask(final PlotPlayer player)
    {
        final HashSet<RegionWrapper> regions = new HashSet<>();
        final UUID uuid = player.getUUID();
        final Location location = player.getLocation();
        final String world = location.getWorld();
        if (!PS.get().isPlotWorld(world))
        {
            regions.add(new RegionWrapper(Integer.MIN_VALUE, Integer.MAX_VALUE, Integer.MIN_VALUE, Integer.MAX_VALUE));
            return regions;
        }
        for (final Plot plot : PS.get().getPlotsInWorld(player.getLocation().getWorld()))
        {
            if (!plot.isBasePlot() || (Settings.DONE_RESTRICTS_BUILDING && (FlagManager.getPlotFlag(plot, "done") != null)))
            {
                continue;
            }
            if (Settings.WE_ALLOW_HELPER ? plot.isAdded(uuid) : (plot.isOwner(uuid) || plot.getTrusted().contains(uuid)))
            {
                final Location pos1 = MainUtil.getPlotBottomLoc(plot.world, plot.id).add(1, 0, 1);
                final Location pos2 = MainUtil.getPlotTopLoc(plot.world, plot.id);
                regions.add(new RegionWrapper(pos1.getX(), pos2.getX(), pos1.getY(), pos2.getY(), pos1.getZ(), pos2.getZ()));
            }
        }
        return regions;
    }

    public static boolean intersects(final RegionWrapper region1, final RegionWrapper region2)
    {
        return (region1.minX <= region2.maxX) && (region1.maxX >= region2.minX) && (region1.minZ <= region2.maxZ) && (region1.maxZ >= region2.minZ);
    }

    public static boolean regionContains(final RegionWrapper selection, final HashSet<RegionWrapper> mask)
    {
        for (final RegionWrapper region : mask)
        {
            if (intersects(region, selection)) { return true; }
        }
        return false;
    }
}
