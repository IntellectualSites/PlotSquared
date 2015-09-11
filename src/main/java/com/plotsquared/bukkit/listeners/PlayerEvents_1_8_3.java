package com.plotsquared.bukkit.listeners;

import java.util.Iterator;

import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockExplodeEvent;

import com.intellectualcrafters.plot.PS;
import com.intellectualcrafters.plot.flag.FlagManager;
import com.intellectualcrafters.plot.object.Location;
import com.intellectualcrafters.plot.object.Plot;
import com.intellectualcrafters.plot.util.MainUtil;
import com.plotsquared.bukkit.util.BukkitUtil;

public class PlayerEvents_1_8_3 implements Listener
{
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBigBoom(final BlockExplodeEvent event)
    {
        final Block block = event.getBlock();
        final Location loc = BukkitUtil.getLocation(block.getLocation());
        final String world = loc.getWorld();
        if (!PS.get().isPlotWorld(world)) { return; }
        final Plot plot = MainUtil.getPlot(loc);
        if ((plot != null) && plot.hasOwner())
        {
            if (FlagManager.isPlotFlagTrue(plot, "explosion"))
            {
                final Iterator<Block> iter = event.blockList().iterator();
                while (iter.hasNext())
                {
                    final Block b = iter.next();
                    if (!plot.equals(MainUtil.getPlot(BukkitUtil.getLocation(b.getLocation()))))
                    {
                        iter.remove();
                    }
                }
                return;
            }
        }
        if (MainUtil.isPlotArea(loc))
        {
            event.setCancelled(true);
        }
        else
        {
            final Iterator<Block> iter = event.blockList().iterator();
            while (iter.hasNext())
            {
                iter.next();
                if (MainUtil.isPlotArea(loc))
                {
                    iter.remove();
                }
            }
        }
    }
}
