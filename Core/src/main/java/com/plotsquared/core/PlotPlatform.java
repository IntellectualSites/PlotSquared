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
package com.plotsquared.core;

import cloud.commandframework.services.ServicePipeline;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.TypeLiteral;
import com.intellectualsites.annotations.DoNotUse;
import com.plotsquared.core.backup.BackupManager;
import com.plotsquared.core.configuration.caption.LocaleHolder;
import com.plotsquared.core.generator.GeneratorWrapper;
import com.plotsquared.core.generator.HybridUtils;
import com.plotsquared.core.generator.IndependentPlotGenerator;
import com.plotsquared.core.inject.annotations.DefaultGenerator;
import com.plotsquared.core.location.World;
import com.plotsquared.core.permissions.PermissionHandler;
import com.plotsquared.core.player.PlotPlayer;
import com.plotsquared.core.plot.expiration.ExpireManager;
import com.plotsquared.core.plot.world.PlotAreaManager;
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
import net.kyori.adventure.text.Component;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

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
    @NonNull File getDirectory();

    /**
     * Gets the folder where all world data is stored.
     *
     * @return the world folder
     */
    @NonNull File worldContainer();

    /**
     * Completely shuts down the plugin.
     */
    void shutdown();

    /**
     * Completely shuts down the server.
     */
    void shutdownServer();

    /**
     * Get the name of the plugin
     *
     * @return Plugin name
     */
    default @NonNull String pluginName() {
        return "PlotSquared";
    }

    /**
     * Gets the version of Minecraft that is running
     *
     * @return server version as array of numbers
     */
    int[] serverVersion();

    /**
     * Gets the default minimum world height for the version of Minecraft that the server is running.
     *
     * @return minimum world height
     * @since 6.6.0
     */
    int versionMinHeight();

    /**
     * Gets the default maximum world height for the version of Minecraft that the server is running.
     *
     * @return maximum world height (inclusive)
     * @since 6.6.0
     */
    int versionMaxHeight();

    /**
     * Gets the server implementation name and version
     *
     * @return server implementation and version as string
     */
    @NonNull String serverImplementation();

    /**
     * Gets the server brand name
     *
     * @return server brand
     * @since 7.5.3
     */
    @NonNull String serverBrand();

    /**
     * Gets the native server code package prefix.
     *
     * @return The package prefix
     */
    @NonNull String serverNativePackage();

    /**
     * Start Metrics.
     */
    void startMetrics();

    /**
     * If a world is already loaded, set the generator (use NMS if required).
     *
     * @param world The world to set the generator
     */
    void setGenerator(@NonNull String world);

    /**
     * Unregisters a {@link PlotPlayer} from cache e.g. if they have logged off.
     *
     * @param player the player to remove
     */
    void unregister(@NonNull PlotPlayer<?> player);

    /**
     * Gets the generator wrapper for a world (world) and generator (name).
     *
     * @param world The world to get the generator from
     * @param name  The name of the generator
     * @return The generator being used for the provided world
     */
    @Nullable GeneratorWrapper<?> getGenerator(
            @NonNull String world,
            @Nullable String name
    );

    /**
     * Create a platform generator from a plot generator
     *
     * @param world     World name
     * @param generator Plot generator
     * @return Platform generator wrapper
     */
    @NonNull GeneratorWrapper<?> wrapPlotGenerator(
            @NonNull String world,
            @NonNull IndependentPlotGenerator generator
    );

    /**
     * Usually HybridGen
     *
     * @return Default implementation generator
     */
    default @NonNull IndependentPlotGenerator defaultGenerator() {
        return injector().getInstance(Key.get(IndependentPlotGenerator.class, DefaultGenerator.class));
    }

    /**
     * Get the backup manager instance
     *
     * @return Backup manager
     */
    default @NonNull BackupManager backupManager() {
        return injector().getInstance(BackupManager.class);
    }

    /**
     * Get the platform specific world manager
     *
     * @return World manager
     */
    default @NonNull PlatformWorldManager<?> worldManager() {
        return injector().getInstance(PlatformWorldManager.class);
    }

    /**
     * Get the player manager implementation for the platform
     *
     * @return Player manager
     */
    default @NonNull PlayerManager<? extends PlotPlayer<P>, ? extends P> playerManager() {
        return injector().getInstance(Key.get(new TypeLiteral<PlayerManager<? extends PlotPlayer<P>, ? extends P>>() {
        }));
    }

    /**
     * Get a platform world wrapper from a world name
     *
     * @param worldName World name
     * @return Platform world wrapper
     */
    @Nullable World<?> getPlatformWorld(@NonNull String worldName);

    /**
     * Get the {@link com.google.inject.Injector} instance used by PlotSquared
     *
     * @return Injector instance
     */
    @NonNull Injector injector();

    /**
     * Get the world utility implementation
     *
     * @return World utility
     */
    default @NonNull WorldUtil worldUtil() {
        return injector().getInstance(WorldUtil.class);
    }

    /**
     * Get the global block queue implementation
     *
     * @return Global block queue implementation
     */
    default @NonNull GlobalBlockQueue globalBlockQueue() {
        return injector().getInstance(GlobalBlockQueue.class);
    }

    /**
     * Get the {@link HybridUtils} implementation for the platform
     *
     * @return Hybrid utils
     */
    default @NonNull HybridUtils hybridUtils() {
        return injector().getInstance(HybridUtils.class);
    }

    /**
     * Get the {@link SetupUtils} implementation for the platform
     *
     * @return Setup utils
     */
    default @NonNull SetupUtils setupUtils() {
        return injector().getInstance(SetupUtils.class);
    }

    /**
     * Get the {@link EconHandler} implementation for the platform
     *
     * @return Econ handler
     */
    default @NonNull EconHandler econHandler() {
        return injector().getInstance(EconHandler.class);
    }

    /**
     * Get the {@link RegionManager} implementation for the platform
     *
     * @return Region manager
     */
    default @NonNull RegionManager regionManager() {
        return injector().getInstance(RegionManager.class);
    }

    /**
     * Get the {@link ChunkManager} implementation for the platform
     *
     * @return Region manager
     */
    default @NonNull ChunkManager chunkManager() {
        return injector().getInstance(ChunkManager.class);
    }

    /**
     * Get the {@link ExpireManager} implementation for the platform
     *
     * @return Expire manager
     * @since 6.10.2
     */
    default @NonNull ExpireManager expireManager() {
        return injector().getInstance(ExpireManager.class);
    }

    /**
     * Get the {@link PlotAreaManager} implementation.
     *
     * @return the PlotAreaManager
     * @since 6.1.4
     */
    @NonNull PlotAreaManager plotAreaManager();

    /**
     * Get the platform specific console {@link Audience}
     *
     * @return Console audience
     */
    @NonNull Audience consoleAudience();

    /**
     * Get a formatted string containing all plugins on the server together
     * with plugin metadata. Mainly for use in debug pastes
     *
     * @return Formatted string
     */
    @NonNull String pluginsFormatted();

    /**
     * Get the kind of WorldEdit implementation
     *
     * @return worldedit implementations
     * @since 6.3.0
     */
    @DoNotUse
    @NonNull String worldEditImplementations();

    /**
     * Load the caption maps
     */
    void copyCaptionMaps();

    /**
     * Get the {@link PermissionHandler} implementation for the platform
     *
     * @return Permission handler
     */
    default @NonNull PermissionHandler permissionHandler() {
        return injector().getInstance(PermissionHandler.class);
    }

    /**
     * Get the {@link ServicePipeline} implementation
     *
     * @return Service pipeline
     */
    default @NonNull ServicePipeline servicePipeline() {
        return injector().getInstance(ServicePipeline.class);
    }

    /**
     * Get the {@link PlaceholderRegistry} implementation
     *
     * @return Placeholder registry
     */
    default @NonNull PlaceholderRegistry placeholderRegistry() {
        return injector().getInstance(PlaceholderRegistry.class);
    }

    /**
     * Convert a component to a legacy string
     *
     * @param component Component to convert
     * @return Converted string
     */
    @NonNull String toLegacyPlatformString(@NonNull Component component);

    /**
     * Returns if the FastAsyncWorldEdit-PlotSquared hook is active/enabled
     *
     * @return status of FastAsyncWorldEdit-PlotSquared hook
     */
    default boolean isFaweHooking() {
        return false;
    }

}
