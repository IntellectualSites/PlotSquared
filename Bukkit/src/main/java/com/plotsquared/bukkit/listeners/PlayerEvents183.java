package com.plotsquared.bukkit.listeners;

import com.intellectualcrafters.plot.PS;
import com.intellectualcrafters.plot.flag.FlagManager;
import com.intellectualcrafters.plot.object.Location;
import com.intellectualcrafters.plot.object.Plot;
import com.intellectualcrafters.plot.object.PlotArea;
import com.plotsquared.bukkit.util.BukkitUtil;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockExplodeEvent;

import java.util.Iterator;

public class PlayerEvents183 implements Listener {
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBigBoom(BlockExplodeEvent event) {
        Block block = event.getBlock();
        Location loc = BukkitUtil.getLocation(block.getLocation());
        String world = loc.getWorld();
        if (!PS.get().hasPlotArea(world)) {
            return;
        }
        PlotArea area = loc.getPlotArea();
        if (area == null) {
            Iterator<Block> iter = event.blockList().iterator();
            while (iter.hasNext()) {
                loc = BukkitUtil.getLocation(iter.next().getLocation());
                if (loc.getPlotArea() != null) {
                    iter.remove();
                }
            }
            return;
        }
        Plot plot = area.getOwnedPlot(loc);
        if (plot == null || !FlagManager.isPlotFlagTrue(plot, "explosion")) {
            event.setCancelled(true);
        }
        Iterator<Block> iter = event.blockList().iterator();
        while (iter.hasNext()) {
            Block b = iter.next();
            if (!plot.equals(area.getOwnedPlot(BukkitUtil.getLocation(b.getLocation())))) {
                iter.remove();
            }
        }
    }
}
