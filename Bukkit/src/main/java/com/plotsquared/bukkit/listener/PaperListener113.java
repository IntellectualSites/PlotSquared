package com.plotsquared.bukkit.listener;

import com.plotsquared.bukkit.util.BukkitUtil;
import com.plotsquared.core.configuration.Captions;
import com.plotsquared.core.configuration.Settings;
import com.plotsquared.core.location.Location;
import com.plotsquared.core.player.PlotPlayer;
import com.plotsquared.core.plot.PlotArea;
import org.bukkit.block.Banner;
import org.bukkit.block.Beacon;
import org.bukkit.block.Bed;
import org.bukkit.block.BlockState;
import org.bukkit.block.CommandBlock;
import org.bukkit.block.Comparator;
import org.bukkit.block.Conduit;
import org.bukkit.block.Container;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.block.DaylightDetector;
import org.bukkit.block.EnchantingTable;
import org.bukkit.block.EndGateway;
import org.bukkit.block.EnderChest;
import org.bukkit.block.Jukebox;
import org.bukkit.block.Sign;
import org.bukkit.block.Skull;
import org.bukkit.block.Structure;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockPlaceEvent;

public class PaperListener113 extends PaperListener {

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        if (!Settings.Paper_Components.TILE_ENTITY_CHECK || !Settings.Enabled_Components.CHUNK_PROCESSOR) {
            return;
        }
        BlockState state = event.getBlock().getState(false);
        if (!(state instanceof Banner || state instanceof Beacon || state instanceof Bed
                || state instanceof CommandBlock || state instanceof Comparator || state instanceof Conduit
                || state instanceof Container || state instanceof CreatureSpawner || state instanceof DaylightDetector
                || state instanceof EnchantingTable || state instanceof EnderChest || state instanceof EndGateway
                || state instanceof Jukebox || state instanceof Sign || state instanceof Skull
                || state instanceof Structure)) {
            return;
        }
        final Location location = BukkitUtil.getLocation(event.getBlock().getLocation());
        final PlotArea plotArea = location.getPlotArea();
        if (plotArea == null) {
            return;
        }
        final int tileEntityCount = event.getBlock().getChunk().getTileEntities(false).length;
        if (tileEntityCount >= Settings.Chunk_Processor.MAX_TILES) {
            final PlotPlayer<?> plotPlayer = BukkitUtil.getPlayer(event.getPlayer());
            Captions.TILE_ENTITY_CAP_REACHED.send(plotPlayer, Settings.Chunk_Processor.MAX_TILES);
            event.setCancelled(true);
            event.setBuild(false);
        }
    }
}
