package com.plotsquared.bukkit.listeners;

import com.intellectualcrafters.plot.PS;
import com.intellectualcrafters.plot.flag.Flags;
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
        Location location = BukkitUtil.getLocation(block.getLocation());
        String world = location.getWorld();
        if (!PS.get().hasPlotArea(world)) {
            return;
        }
        PlotArea area = location.getPlotArea();
        if (area == null) {
            Iterator<Block> iterator = event.blockList().iterator();
            while (iterator.hasNext()) {
                location = BukkitUtil.getLocation(iterator.next().getLocation());
                if (location.getPlotArea() != null) {
                    iterator.remove();
                }
            }
            return;
        }
        Plot plot = area.getOwnedPlot(location);
        if (plot == null) {
            event.setCancelled(true);
        } else if (!plot.getFlag(Flags.EXPLOSION).or(false)) {
            event.setCancelled(true);
        }
        event.blockList().removeIf(
            b -> !plot.equals(area.getOwnedPlot(BukkitUtil.getLocation(b.getLocation()))));
    }
}
