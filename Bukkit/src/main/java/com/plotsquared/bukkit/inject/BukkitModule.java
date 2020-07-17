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
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.plotsquared.bukkit.inject;

import com.google.inject.AbstractModule;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import com.google.inject.util.Providers;
import com.plotsquared.bukkit.BukkitPlatform;
import com.plotsquared.bukkit.player.BukkitPlayerManager;
import com.plotsquared.bukkit.queue.BukkitQueueCoordinator;
import com.plotsquared.bukkit.schematic.BukkitSchematicHandler;
import com.plotsquared.bukkit.util.BukkitChunkManager;
import com.plotsquared.bukkit.util.BukkitEconHandler;
import com.plotsquared.bukkit.util.BukkitInventoryUtil;
import com.plotsquared.bukkit.util.BukkitPermHandler;
import com.plotsquared.bukkit.util.BukkitRegionManager;
import com.plotsquared.bukkit.util.BukkitSetupUtils;
import com.plotsquared.bukkit.util.BukkitUtil;
import com.plotsquared.core.PlotPlatform;
import com.plotsquared.core.configuration.Settings;
import com.plotsquared.core.generator.HybridGen;
import com.plotsquared.core.generator.IndependentPlotGenerator;
import com.plotsquared.core.inject.annotations.ConsoleActor;
import com.plotsquared.core.inject.annotations.DefaultGenerator;
import com.plotsquared.core.inject.factory.HybridPlotWorldFactory;
import com.plotsquared.core.plot.world.DefaultPlotAreaManager;
import com.plotsquared.core.plot.world.PlotAreaManager;
import com.plotsquared.core.plot.world.SinglePlotAreaManager;
import com.plotsquared.core.queue.GlobalBlockQueue;
import com.plotsquared.core.queue.QueueProvider;
import com.plotsquared.core.util.ChunkManager;
import com.plotsquared.core.util.EconHandler;
import com.plotsquared.core.util.InventoryUtil;
import com.plotsquared.core.util.PermHandler;
import com.plotsquared.core.util.PlayerManager;
import com.plotsquared.core.util.RegionManager;
import com.plotsquared.core.util.SchematicHandler;
import com.plotsquared.core.util.SetupUtils;
import com.plotsquared.core.util.WorldUtil;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldedit.extension.platform.Actor;
import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.plugin.java.JavaPlugin;

import javax.annotation.Nonnull;

@RequiredArgsConstructor
public class BukkitModule extends AbstractModule {

    private final BukkitPlatform bukkitPlatform;

    @Override protected void configure() {
        bind(PlayerManager.class).to(BukkitPlayerManager.class);
        bind(JavaPlugin.class).toInstance(bukkitPlatform);
        bind(PlotPlatform.class).toInstance(bukkitPlatform);
        bind(IndependentPlotGenerator.class).annotatedWith(DefaultGenerator.class)
            .to(HybridGen.class);
        // Console actor
        @Nonnull ConsoleCommandSender console = Bukkit.getServer().getConsoleSender();
        WorldEditPlugin wePlugin =
            ((WorldEditPlugin) Bukkit.getPluginManager().getPlugin("WorldEdit"));
        bind(Actor.class).annotatedWith(ConsoleActor.class)
            .toInstance(wePlugin.wrapCommandSender(console));
        bind(InventoryUtil.class).to(BukkitInventoryUtil.class);
        bind(SetupUtils.class).to(BukkitSetupUtils.class);
        bind(WorldUtil.class).to(BukkitUtil.class);
        bind(GlobalBlockQueue.class).toInstance(new GlobalBlockQueue(
            QueueProvider.of(BukkitQueueCoordinator.class, BukkitQueueCoordinator.class)));
        bind(ChunkManager.class).to(BukkitChunkManager.class);
        bind(RegionManager.class).to(BukkitRegionManager.class);
        bind(SchematicHandler.class).to(BukkitSchematicHandler.class);
        this.setupVault();
        if (Settings.Enabled_Components.WORLDS) {
            bind(PlotAreaManager.class).to(SinglePlotAreaManager.class);
        } else {
            bind(PlotAreaManager.class).to(DefaultPlotAreaManager.class);
        }
        install(new FactoryModuleBuilder().build(HybridPlotWorldFactory.class));
    }

    private void setupVault() {
        if (Bukkit.getPluginManager().isPluginEnabled("Vault")) {
            BukkitPermHandler bukkitPermHandler = null;
            try {
                bukkitPermHandler = new BukkitPermHandler();
                bind(PermHandler.class).toInstance(bukkitPermHandler);
            } catch (final Exception ignored) {
                bind(PermHandler.class).toProvider(Providers.of(null));
            }
            try {
                final BukkitEconHandler bukkitEconHandler =
                    new BukkitEconHandler(bukkitPermHandler);
                bind(EconHandler.class).toInstance(bukkitEconHandler);
            } catch (final Exception ignored) {
                bind(EconHandler.class).toProvider(Providers.of(null));
            }
        } else {
            bind(PermHandler.class).toProvider(Providers.of(null));
            bind(EconHandler.class).toProvider(Providers.of(null));
        }
    }

}
