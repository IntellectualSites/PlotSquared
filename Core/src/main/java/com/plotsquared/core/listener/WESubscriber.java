/*
 *       _____  _       _    _____                                _
 *      |  __ \| |     | |  / ____|                              | |
 *      | |__) | | ___ | |_| (___   __ _ _   _  __ _ _ __ ___  __| |
 *      |  ___/| |/ _ \| __|\___ \ / _` | | | |/ _` | '__/ _ \/ _` |
 *      | |    | | (_) | |_ ____) | (_| | |_| | (_| | | |  __/ (_| |
 *      |_|    |_|\___/ \__|_____/ \__, |\__,_|\__,_|_|  \___|\__,_|
 *                                    | |
 *                                    |_|
 *            PlotSquared plot management system for Minecraft
 *                  Copyright (C) 2020 IntellectualSites
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.plotsquared.core.listener;

import com.google.inject.Inject;
import com.plotsquared.core.PlotSquared;
import com.plotsquared.core.configuration.Settings;
import com.plotsquared.core.configuration.caption.TranslatableCaption;
import com.plotsquared.core.player.PlotPlayer;
import com.plotsquared.core.plot.Plot;
import com.plotsquared.core.plot.world.PlotAreaManager;
import com.plotsquared.core.util.Permissions;
import com.plotsquared.core.util.WEManager;
import com.plotsquared.core.util.WorldUtil;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.entity.Player;
import com.sk89q.worldedit.event.extent.EditSessionEvent;
import com.sk89q.worldedit.extension.platform.Actor;
import com.sk89q.worldedit.extent.NullExtent;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.util.Location;
import com.sk89q.worldedit.util.eventbus.EventHandler.Priority;
import com.sk89q.worldedit.util.eventbus.Subscribe;
import com.sk89q.worldedit.world.World;
import net.kyori.adventure.text.minimessage.Template;

import javax.annotation.Nonnull;
import java.util.Set;

public class WESubscriber {
    
    private final PlotAreaManager plotAreaManager;
    private final WorldUtil worldUtil;

    @Inject public WESubscriber(@Nonnull final PlotAreaManager plotAreaManager, @Nonnull final WorldUtil worldUtil) {
        this.plotAreaManager = plotAreaManager;
        this.worldUtil = worldUtil;
    }

    @Subscribe(priority = Priority.VERY_EARLY) public void onEditSession(EditSessionEvent event) {
        if (!Settings.Enabled_Components.WORLDEDIT_RESTRICTIONS) {
            WorldEdit.getInstance().getEventBus().unregister(this);
            return;
        }
        World worldObj = event.getWorld();
        if (worldObj == null) {
            return;
        }
        String world = worldObj.getName();
        Actor actor = event.getActor();
        if (actor != null && actor.isPlayer()) {
            String name = actor.getName();
            final PlotPlayer<?> plotPlayer = PlotSquared.platform().getPlayerManager().getPlayerIfExists(name);
            Set<CuboidRegion> mask;
            if (plotPlayer == null) {
                Player player = (Player) actor;
                Location location = player.getLocation();
                com.plotsquared.core.location.Location pLoc = com.plotsquared.core.location.Location.at(player.getWorld().getName(),
                    location.toVector().toBlockPoint());
                Plot plot = pLoc.getPlot();
                if (plot == null) {
                    event.setExtent(new NullExtent());
                    return;
                }
                mask = plot.getRegions();
            } else if (plotPlayer.getAttribute("worldedit")) {
                return;
            } else {
                mask = WEManager.getMask(plotPlayer);
                if (mask.isEmpty()) {
                    if (Permissions.hasPermission(plotPlayer, "plots.worldedit.bypass")) {
                        plotPlayer.sendMessage(TranslatableCaption.of("worldedit.worldedit_bypass"),
                                Template.of("command", "/plot wea"));
                    }
                    if (this.plotAreaManager.hasPlotArea(world)) {
                        event.setExtent(new NullExtent());
                    }
                    return;
                }
            }
            if (Settings.Enabled_Components.CHUNK_PROCESSOR) {
                if (this.plotAreaManager.hasPlotArea(world)) {
                    event.setExtent(
                        new ProcessedWEExtent(world, mask, event.getMaxBlocks(), event.getExtent(),
                            event.getExtent(), this.worldUtil));
                }
            } else if (this.plotAreaManager.hasPlotArea(world)) {
                event.setExtent(new WEExtent(mask, event.getExtent()));
            }
        }
    }
}
