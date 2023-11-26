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

import com.plotsquared.bukkit.util.BukkitUtil;
import com.plotsquared.core.location.Location;
import com.plotsquared.core.plot.Plot;
import com.plotsquared.core.plot.PlotArea;
import com.plotsquared.core.plot.flag.implementations.EditSignFlag;
import com.plotsquared.core.util.PlotFlagUtil;
import org.bukkit.block.Sign;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerSignOpenEvent;

/**
 * For events since 1.20.1
 * @since TODO
 */
public class PlayerEventListener1201 implements Listener {

    @EventHandler(ignoreCancelled = true)
    @SuppressWarnings({"removal", "UnstableApiUsage"}) // thanks Paper, thanks Spigot
    public void onPlayerSignOpenEvent(PlayerSignOpenEvent event) {
        Sign sign = event.getSign();
        Location location = BukkitUtil.adapt(sign.getLocation());
        PlotArea area = location.getPlotArea();
        if (area == null) {
            return;
        }
        Plot plot = location.getOwnedPlot();
        if (plot == null) {
            if (PlotFlagUtil.isAreaRoadFlagsAndFlagEquals(area, EditSignFlag.class, false)) {
                event.setCancelled(true);
            }
            return;
        }
        if (plot.isAdded(event.getPlayer().getUniqueId())) {
            return; // allow for added players
        }
        if (!plot.getFlag(EditSignFlag.class)) {
            plot.debug(event.getPlayer().getName() + " could not edit the sign because of edit-sign = false");
            event.setCancelled(true);
        }
    }

}
