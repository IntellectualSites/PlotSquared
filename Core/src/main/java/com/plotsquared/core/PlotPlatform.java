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
package com.plotsquared.core;

import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.TypeLiteral;
import com.plotsquared.core.backup.BackupManager;
import com.plotsquared.core.configuration.caption.LocaleHolder;
import com.plotsquared.core.generator.GeneratorWrapper;
import com.plotsquared.core.generator.HybridUtils;
import com.plotsquared.core.generator.IndependentPlotGenerator;
import com.plotsquared.core.inject.annotations.DefaultGenerator;
import com.plotsquared.core.location.World;
import com.plotsquared.core.permissions.PermissionHandler;
import com.plotsquared.core.player.PlotPlayer;
import com.plotsquared.core.queue.GlobalBlockQueue;
import com.plotsquared.core.util.ChunkManager;
import com.plotsquared.core.util.EconHandler;
import com.plotsquared.core.util.PlatformWorldManager;
import com.plotsquared.core.util.PlayerManager;
import com.plotsquared.core.util.RegionManager;
import com.plotsquared.core.util.SetupUtils;
import com.plotsquared.core.util.WorldUtil;
import net.kyori.adventure.audience.Audience;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.util.List;
import java.util.Map;

/**
 * PlotSquared main utility class
 *
 * @param <P> Player type
 */
public interface PlotPlatform<P> extends LocaleHolder {

    /**
     * Gets the directory which contains PlotSquared files. The directory may not exist.
     *
     * @return the PlotSquared directory
     */
    File getDirectory();

    /**
     * Gets the folder where all world data is stored.
     *
     * @return the world folder
     */
    File getWorldContainer();

    /**
     * Completely shuts down the plugin.
     */
    void shutdown();

    default String getPluginName() {
        return "PlotSquared";
    }

    /**
     * Gets the version of Minecraft that is running.
     * @return server version as array of numbers
     */
    int[] getServerVersion();

    /**
     * Gets the server implementation name and version
     * @return server implementationa and version as string
     */
    String getServerImplementation();

    /**
     * Gets the NMS package prefix.
     *
     * @return The NMS package prefix
     */
    String getNMSPackage();

    /**
     * Start Metrics.
     */
    void startMetrics();

    /**
     * If a world is already loaded, set the generator (use NMS if required).
     *
     * @param world The world to set the generator
     */
    void setGenerator(String world);

    /**
     * Unregisters a {@link PlotPlayer} from cache e.g. if they have logged off.
     *
     * @param player the player to remove
     */
    void unregister(PlotPlayer<?> player);

    /**
     * Gets the generator wrapper for a world (world) and generator (name).
     *
     * @param world the world to get the generator from
     * @param name  the name of the generator
     * @return the generator being used for the provided world
     */
    GeneratorWrapper<?> getGenerator(String world, String name);

    GeneratorWrapper<?> wrapPlotGenerator(String world, IndependentPlotGenerator generator);

    /**
     * Usually HybridGen
     *
     * @return Default implementation generator
     */
    @Nonnull default IndependentPlotGenerator getDefaultGenerator() {
        return getInjector().getInstance(Key.get(IndependentPlotGenerator.class, DefaultGenerator.class));
    }

    List<Map.Entry<Map.Entry<String, String>, Boolean>> getPluginIds();

    /**
     * Get the backup manager instance
     *
     * @return Backup manager
     */
    @Nonnull default BackupManager getBackupManager() {
        return getInjector().getInstance(BackupManager.class);
    }

    /**
     * Get the platform specific world manager
     *
     * @return World manager
     */
    @Nonnull default PlatformWorldManager<?> getWorldManager() {
        return getInjector().getInstance(PlatformWorldManager.class);
    }

    /**
     * Get the player manager implementation for the platform
     *
     * @return Player manager
     */
    @Nonnull default PlayerManager<? extends PlotPlayer<P>, ? extends P> getPlayerManager() {
        return getInjector().getInstance(Key.get(new TypeLiteral<PlayerManager<? extends PlotPlayer<P>, ? extends P>>() {}));
    }

    /**
     * Get a platform world wrapper from a world name
     *
     * @param worldName World name
     * @return Platform world wrapper
     */
    @Nullable World<?> getPlatformWorld(@Nonnull final String worldName);

    /**
     * Get the {@link com.google.inject.Injector} instance used by PlotSquared
     *
     * @return Injector instance
     */
    @Nonnull Injector getInjector();

    /**
     * Get the world utility implementation
     *
     * @return World utility
     */
    @Nonnull default WorldUtil getWorldUtil() {
        return getInjector().getInstance(WorldUtil.class);
    }

    /**
     * Get the global block queue implementation
     *
     * @return Global block queue implementation
     */
    @Nonnull default GlobalBlockQueue getGlobalBlockQueue() {
        return getInjector().getInstance(GlobalBlockQueue.class);
    }

    /**
     * Get the {@link HybridUtils} implementation for the platform
     *
     * @return Hybrid utils
     */
    @Nonnull default HybridUtils getHybridUtils() {
        return getInjector().getInstance(HybridUtils.class);
    }

    /**
     * Get the {@link SetupUtils} implementation for the platform
     *
     * @return Setup utils
     */
    @Nonnull default SetupUtils getSetupUtils() {
        return getInjector().getInstance(SetupUtils.class);
    }

    /**
     * Get the {@link EconHandler} implementation for the platform
     *      *
     * @return Econ handler
     */
    @Nullable default EconHandler getEconHandler() {
        return getInjector().getInstance(EconHandler.class);
    }

    /**
     * Get the {@link RegionManager} implementation for the platform
     *
     * @return Region manager
     */
    @Nonnull default RegionManager getRegionManager() {
        return getInjector().getInstance(RegionManager.class);
    }

    /**
     * Get the {@link ChunkManager} implementation for the platform
     *
     * @return Region manager
     */
    @Nonnull default ChunkManager getChunkManager() {
        return getInjector().getInstance(ChunkManager.class);
    }

    /**
     * Get the platform specific console {@link Audience}
     *
     * @return Console audience
     */
    @Nonnull Audience getConsoleAudience();

    /**
     * Load the caption maps
     */
    void copyCaptionMaps();

    /**
     * Get the {@link PermissionHandler} implementation for the platform
     *
     * @return Permission handler
     */
    @Nonnull default PermissionHandler getPermissionHandler() {
        return getInjector().getInstance(PermissionHandler.class);
    }

}
