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
import com.intellectualsites.services.ServicePipeline;
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
import com.plotsquared.core.util.placeholders.PlaceholderRegistry;
import net.kyori.adventure.audience.Audience;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;

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
    @Nonnull File getDirectory();

    /**
     * Gets the folder where all world data is stored.
     *
     * @return the world folder
     */
    @Nonnull File worldContainer();

    /**
     * Completely shuts down the plugin.
     */
    void shutdown();

    /**
     * Get the name of the plugin
     *
     * @return Plugin name
     */
    @Nonnull default String pluginName() {
        return "PlotSquared";
    }

    /**
     * Gets the version of Minecraft that is running
     *
     * @return server version as array of numbers
     */
    @Nonnull int[] serverVersion();

    /**
     * Gets the server implementation name and version
     *
     * @return server implementation and version as string
     */
    @Nonnull String serverImplementation();

    /**
     * Gets the native server code package prefix.
     *
     * @return The package prefix
     */
    @Nonnull String serverNativePackage();

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
     * @param world The world to get the generator from
     * @param name  The name of the generator
     * @return The generator being used for the provided world
     */
    @Nullable GeneratorWrapper<?> getGenerator(@Nonnull String world, @Nullable String name);

    /**
     * Create a platform generator from a plot generator
     *
     * @param world     World name
     * @param generator Plot generator
     * @return Platform generator wrapper
     */
    @Nonnull GeneratorWrapper<?> wrapPlotGenerator(@Nonnull String world, @Nonnull IndependentPlotGenerator generator);

    /**
     * Usually HybridGen
     *
     * @return Default implementation generator
     */
    @Nonnull default IndependentPlotGenerator defaultGenerator() {
        return injector().getInstance(Key.get(IndependentPlotGenerator.class, DefaultGenerator.class));
    }

    /**
     * Get the backup manager instance
     *
     * @return Backup manager
     */
    @Nonnull default BackupManager backupManager() {
        return injector().getInstance(BackupManager.class);
    }

    /**
     * Get the platform specific world manager
     *
     * @return World manager
     */
    @Nonnull default PlatformWorldManager<?> worldManager() {
        return injector().getInstance(PlatformWorldManager.class);
    }

    /**
     * Get the player manager implementation for the platform
     *
     * @return Player manager
     */
    @Nonnull default PlayerManager<? extends PlotPlayer<P>, ? extends P> playerManager() {
        return injector().getInstance(Key.get(new TypeLiteral<PlayerManager<? extends PlotPlayer<P>, ? extends P>>() {}));
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
    @Nonnull Injector injector();

    /**
     * Get the world utility implementation
     *
     * @return World utility
     */
    @Nonnull default WorldUtil worldUtil() {
        return injector().getInstance(WorldUtil.class);
    }

    /**
     * Get the global block queue implementation
     *
     * @return Global block queue implementation
     */
    @Nonnull default GlobalBlockQueue globalBlockQueue() {
        return injector().getInstance(GlobalBlockQueue.class);
    }

    /**
     * Get the {@link HybridUtils} implementation for the platform
     *
     * @return Hybrid utils
     */
    @Nonnull default HybridUtils hybridUtils() {
        return injector().getInstance(HybridUtils.class);
    }

    /**
     * Get the {@link SetupUtils} implementation for the platform
     *
     * @return Setup utils
     */
    @Nonnull default SetupUtils setupUtils() {
        return injector().getInstance(SetupUtils.class);
    }

    /**
     * Get the {@link EconHandler} implementation for the platform
     *      *
     * @return Econ handler
     */
    @Nonnull default EconHandler econHandler() {
        return injector().getInstance(EconHandler.class);
    }

    /**
     * Get the {@link RegionManager} implementation for the platform
     *
     * @return Region manager
     */
    @Nonnull default RegionManager regionManager() {
        return injector().getInstance(RegionManager.class);
    }

    /**
     * Get the {@link ChunkManager} implementation for the platform
     *
     * @return Region manager
     */
    @Nonnull default ChunkManager chunkManager() {
        return injector().getInstance(ChunkManager.class);
    }

    /**
     * Get the platform specific console {@link Audience}
     *
     * @return Console audience
     */
    @Nonnull Audience consoleAudience();

    /**
     * Get a formatted string containing all plugins on the server together
     * with plugin metadata. Mainly for use in debug pastes
     *
     * @return Formatted string
     */
    @Nonnull String pluginsFormatted();

    /**
     * Load the caption maps
     */
    void copyCaptionMaps();

    /**
     * Get the {@link PermissionHandler} implementation for the platform
     *
     * @return Permission handler
     */
    @Nonnull default PermissionHandler permissionHandler() {
        return injector().getInstance(PermissionHandler.class);
    }

    /**
     * Get the {@link ServicePipeline} implementation
     *
     * @return Service pipeline
     */
    @Nonnull default ServicePipeline servicePipeline() {
        return injector().getInstance(ServicePipeline.class);
    }

    /**
     * Get the {@link PlaceholderRegistry} implementation
     *
     * @return Placeholder registry
     */
    @Nonnull default PlaceholderRegistry placeholderRegistry() {
        return injector().getInstance(PlaceholderRegistry.class);
    }

}
