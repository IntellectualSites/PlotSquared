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
import com.plotsquared.bukkit.BukkitPlatform;
import com.plotsquared.bukkit.util.BukkitUtil;
import com.plotsquared.core.location.Location;
import com.plotsquared.core.plot.Plot;
import com.plotsquared.core.plot.PlotArea;
import com.plotsquared.core.plot.flag.implementations.CopperGolemTransformFlag;
import com.plotsquared.core.plot.world.PlotAreaManager;
import com.plotsquared.core.util.EventDispatcher;
import com.plotsquared.core.util.PlotFlagUtil;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.EnumSet;
import java.util.Set;

@SuppressWarnings("unused")
public class EntityEventListener1219 implements Listener {

    private static final Set<Material> COPPER_GOLEM_STATUES = EnumSet.of(
            Material.COPPER_GOLEM_STATUE,
            Material.EXPOSED_COPPER_GOLEM_STATUE,
            Material.WEATHERED_COPPER_GOLEM_STATUE,
            Material.OXIDIZED_COPPER_GOLEM_STATUE,
            Material.WAXED_COPPER_GOLEM_STATUE,
            Material.WAXED_EXPOSED_COPPER_GOLEM_STATUE,
            Material.WAXED_WEATHERED_COPPER_GOLEM_STATUE,
            Material.WAXED_OXIDIZED_COPPER_GOLEM_STATUE
    );

    private final BukkitPlatform platform;
    private final PlotAreaManager plotAreaManager;
    private final EventDispatcher eventDispatcher;

    @Inject
    public EntityEventListener1219(
            final @NonNull BukkitPlatform platform,
            final @NonNull PlotAreaManager plotAreaManager,
            final @NonNull EventDispatcher eventDispatcher
    ) {
        this.platform = platform;
        this.plotAreaManager = plotAreaManager;
        this.eventDispatcher = eventDispatcher;
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onCopperGolemHarden(EntityChangeBlockEvent event) {

        if (event.getEntityType() != EntityType.COPPER_GOLEM) {
            return;
        }

        if (!COPPER_GOLEM_STATUES.contains(event.getTo())) {
            return;
        }

        Location location = BukkitUtil.adapt(event.getBlock().getLocation());
        PlotArea area = location.getPlotArea();
        if (area == null) {
            return;
        }
        Plot plot = location.getOwnedPlot();
        if (plot == null) {
            if (!PlotFlagUtil.isAreaRoadFlagsAndFlagEquals(area, CopperGolemTransformFlag.class, true)) {
                event.setCancelled(true);
            }
            return;
        }
        if (!plot.getFlag(CopperGolemTransformFlag.class)) {
            plot.debug(event.getEntityType() + " could not spawn because copper-golem-transform = false");
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onCopperGolemRevived(EntityChangeBlockEvent event) {

        if (event.getBlock().getType() != Material.COPPER_GOLEM_STATUE) {
            return;
        }

        if (event.getEntity().getType() != EntityType.PLAYER) {
            return;
        }

        Location location = BukkitUtil.adapt(event.getBlock().getLocation());
        PlotArea area = location.getPlotArea();
        if (area == null) {
            return;
        }
        Plot plot = location.getOwnedPlot();
        if (plot == null) {
            if (!PlotFlagUtil.isAreaRoadFlagsAndFlagEquals(area, CopperGolemTransformFlag.class, true)) {
                event.setCancelled(true);
            }
            return;
        }
        if (!plot.getFlag(CopperGolemTransformFlag.class)) {
            plot.debug(event.getEntityType() + " could not spawn because copper-golem-transform = false");
            event.setCancelled(true);
        }
    }

}
