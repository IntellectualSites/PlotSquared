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

package com.plotsquared.bukkit.listener;

import com.plotsquared.bukkit.util.BukkitEntityUtil;
import com.plotsquared.bukkit.util.BukkitUtil;
import com.plotsquared.core.location.Location;
import com.plotsquared.core.permissions.Permission;
import com.plotsquared.core.player.PlotPlayer;
import com.plotsquared.core.plot.Plot;
import com.plotsquared.core.plot.PlotArea;
import com.plotsquared.core.plot.PlotHandler;
import com.plotsquared.core.plot.world.PlotAreaManager;
import com.plotsquared.core.util.Permissions;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.ThrownPotion;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.LingeringPotionSplashEvent;
import org.bukkit.event.entity.PotionSplashEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.projectiles.BlockProjectileSource;
import org.bukkit.projectiles.ProjectileSource;

import javax.annotation.Nonnull;
import javax.inject.Inject;

@SuppressWarnings("unused")
public class ProjectileEventListener implements Listener {

    private final PlotAreaManager plotAreaManager;

    @Inject public ProjectileEventListener(@Nonnull final PlotAreaManager plotAreaManager) {
        this.plotAreaManager = plotAreaManager;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true) public void onPotionSplash(LingeringPotionSplashEvent event) {
        Projectile entity = event.getEntity();
        Location location = BukkitUtil.adapt(entity.getLocation());
        if (!this.plotAreaManager.hasPlotArea(location.getWorldName())) {
            return;
        }
        if (!this.onProjectileHit(event)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true) public void onPotionSplash(PotionSplashEvent event) {
        ThrownPotion damager = event.getPotion();
        Location location = BukkitUtil.adapt(damager.getLocation());
        if (!this.plotAreaManager.hasPlotArea(location.getWorldName())) {
            return;
        }
        int count = 0;
        for (LivingEntity victim : event.getAffectedEntities()) {
            if (!BukkitEntityUtil.entityDamage(damager, victim)) {
                event.setIntensity(victim, 0);
                count++;
            }
        }
        if ((count > 0 && count == event.getAffectedEntities().size()) || !onProjectileHit(event)) {
            event.setCancelled(true);
        }
    }

    @EventHandler public void onProjectileLaunch(ProjectileLaunchEvent event) {
        Projectile entity = event.getEntity();
        if (!(entity instanceof ThrownPotion)) {
            return;
        }
        ProjectileSource shooter = entity.getShooter();
        if (!(shooter instanceof Player)) {
            return;
        }
        Location location = BukkitUtil.adapt(entity.getLocation());
        if (!this.plotAreaManager.hasPlotArea(location.getWorldName())) {
            return;
        }
        PlotPlayer<Player> pp = BukkitUtil.adapt((Player) shooter);
        Plot plot = location.getOwnedPlot();
        if (plot != null && !plot.isAdded(pp.getUUID())) {
            entity.remove();
            event.setCancelled(true);
        }
    }

    @SuppressWarnings({"BooleanMethodIsAlwaysInverted", "cos it's not... dum IntelliJ"}) @EventHandler
    public boolean onProjectileHit(ProjectileHitEvent event) {
        Projectile entity = event.getEntity();
        Location location = BukkitUtil.adapt(entity.getLocation());
        if (!this.plotAreaManager.hasPlotArea(location.getWorldName())) {
            return true;
        }
        PlotArea area = location.getPlotArea();
        if (area == null) {
            return true;
        }
        Plot plot = area.getPlot(location);
        ProjectileSource shooter = entity.getShooter();
        if (shooter instanceof Player) {
            PlotPlayer<?> pp = BukkitUtil.adapt((Player) shooter);
            if (plot == null) {
                if (!Permissions.hasPermission(pp, Permission.PERMISSION_PROJECTILE_UNOWNED)) {
                    entity.remove();
                    return false;
                }
                return true;
            }
            if (plot.isAdded(pp.getUUID()) || Permissions
                .hasPermission(pp, Permission.PERMISSION_PROJECTILE_OTHER)) {
                return true;
            }
            entity.remove();
            return false;
        }
        if (!(shooter instanceof Entity) && shooter != null) {
            if (plot == null) {
                entity.remove();
                return false;
            }
            Location sLoc =
                BukkitUtil.adapt(((BlockProjectileSource) shooter).getBlock().getLocation());
            if (!area.contains(sLoc.getX(), sLoc.getZ())) {
                entity.remove();
                return false;
            }
            Plot sPlot = area.getOwnedPlotAbs(sLoc);
            if (sPlot == null || !PlotHandler.sameOwners(plot, sPlot)) {
                entity.remove();
                return false;
            }
        }
        return true;
    }
}
