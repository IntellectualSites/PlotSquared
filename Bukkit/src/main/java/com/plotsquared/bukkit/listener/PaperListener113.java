/*
 * PlotSquared, a land and world management plugin for Minecraft.
 * Copyright (C) IntellectualSites <https://intellectualsites.com>
 * Copyright (C) IntellectualSites team and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.plotsquared.bukkit.listener;

import com.google.inject.Inject;
import com.plotsquared.bukkit.util.BukkitUtil;
import com.plotsquared.core.configuration.Settings;
import com.plotsquared.core.configuration.caption.TranslatableCaption;
import com.plotsquared.core.location.Location;
import com.plotsquared.core.player.PlotPlayer;
import com.plotsquared.core.plot.PlotArea;
import com.plotsquared.core.plot.world.PlotAreaManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.block.Banner;
import org.bukkit.block.Beacon;
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
import org.bukkit.block.data.type.Bed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockPlaceEvent;
import org.checkerframework.checker.nullness.qual.NonNull;

public class PaperListener113 extends PaperListener {

    @Inject
    public PaperListener113(@NonNull PlotAreaManager plotAreaManager) {
        super(plotAreaManager);
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        if (!Settings.Paper_Components.TILE_ENTITY_CHECK || !Settings.Enabled_Components.CHUNK_PROCESSOR) {
            return;
        }
        BlockState state = event.getBlock().getState(false);
        if (!(state instanceof Banner || state instanceof Beacon || state instanceof Bed || state instanceof CommandBlock
                || state instanceof Comparator || state instanceof Conduit || state instanceof Container || state instanceof CreatureSpawner
                || state instanceof DaylightDetector || state instanceof EnchantingTable || state instanceof EnderChest || state instanceof EndGateway
                || state instanceof Jukebox || state instanceof Sign || state instanceof Skull || state instanceof Structure)) {
            return;
        }
        final Location location = BukkitUtil.adapt(event.getBlock().getLocation());
        final PlotArea plotArea = location.getPlotArea();
        if (plotArea == null) {
            return;
        }
        final int tileEntityCount = event.getBlock().getChunk().getTileEntities(false).length;
        if (tileEntityCount >= Settings.Chunk_Processor.MAX_TILES) {
            final PlotPlayer<?> plotPlayer = BukkitUtil.adapt(event.getPlayer());
            plotPlayer.sendMessage(
                    TranslatableCaption.of("errors.tile_entity_cap_reached"),
                    TagResolver.resolver("amount", Tag.inserting(Component.text(Settings.Chunk_Processor.MAX_TILES)))
            );
            event.setCancelled(true);
            event.setBuild(false);
        }
    }

}
