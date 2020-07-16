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
package com.plotsquared.core;

import com.plotsquared.core.backup.BackupManager;
import com.plotsquared.core.generator.GeneratorWrapper;
import com.plotsquared.core.generator.HybridUtils;
import com.plotsquared.core.generator.IndependentPlotGenerator;
import com.plotsquared.core.player.PlotPlayer;
import com.plotsquared.core.queue.QueueProvider;
import com.plotsquared.core.util.ChatManager;
import com.plotsquared.core.util.ChunkManager;
import com.plotsquared.core.util.EconHandler;
import com.plotsquared.core.util.InventoryUtil;
import com.plotsquared.core.util.PermHandler;
import com.plotsquared.core.util.PlatformWorldManager;
import com.plotsquared.core.util.PlayerManager;
import com.plotsquared.core.util.RegionManager;
import com.plotsquared.core.util.SchematicHandler;
import com.plotsquared.core.util.SetupUtils;
import com.plotsquared.core.util.WorldUtil;
import com.plotsquared.core.util.logger.ILogger;
import com.plotsquared.core.util.task.TaskManager;
import com.sk89q.worldedit.extension.platform.Actor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.List;
import java.util.Map;

/**
 * PlotSquared main utility class
 *
 * @param <P> Player type
 */
public interface IPlotMain<P> extends ILogger {

    /**
     * Logs a message to console.
     *
     * @param message the message to log
     */
    void log(String message);

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
     * Wraps a player into a PlotPlayer object.
     *
     * @param player The player to convert to a PlotPlayer
     * @return A PlotPlayer
     */
    @Nullable PlotPlayer<P> wrapPlayer(Object player);

    /**
     * Completely shuts down the plugin.
     */
    void shutdown();

    /**
     * Gets the version of the PlotSquared being used.
     *
     * @return the plugin version
     */
    int[] getPluginVersion();

    /**
     * Gets the version of the PlotSquared being used as a string.
     *
     * @return the plugin version as a string
     */
    String getPluginVersionString();

    default String getPluginName() {
        return "PlotSquared";
    }

    /**
     * Gets the version of Minecraft that is running.
     */
    int[] getServerVersion();

    /**
     * Gets the server implementation name and version
     */
    String getServerImplementation();

    /**
     * Gets the NMS package prefix.
     *
     * @return The NMS package prefix
     */
    String getNMSPackage();

    /**
     * Gets the schematic handler.
     *
     * @return The {@link SchematicHandler}
     */
    SchematicHandler initSchematicHandler();

    /**
     * Starts the {@link ChatManager}.
     *
     * @return the ChatManager
     */
    ChatManager initChatManager();

    /**
     * The task manager will run and manage Minecraft tasks.
     *
     * @return the PlotSquared task manager
     */
    TaskManager getTaskManager();

    /**
     * Run the task that will kill road mobs.
     */
    void runEntityTask();

    /**
     * Registerss the implementation specific commands.
     */
    void registerCommands();

    /**
     * Register the protection system.
     */
    void registerEvents();

    /**
     * Register force field events.
     */
    void registerForceFieldEvents();

    /**
     * Registers the WorldEdit hook.
     */
    boolean initWorldEdit();

    /**
     * Gets the economy provider, if there is one
     *
     * @return the PlotSquared economy manager
     */
    @Nullable EconHandler getEconomyHandler();

    /**
     * Gets the permission provider, if there is one
     *
     * @return the PlotSquared permission manager
     */
    @Nullable PermHandler getPermissionHandler();

    /**
     * Gets the {@link QueueProvider} class.
     */
    QueueProvider initBlockQueue();

    /**
     * Gets the {@link WorldUtil} class.
     */
    WorldUtil initWorldUtil();

    /**
     * Gets the chunk manager.
     *
     * @return the PlotSquared chunk manager
     */
    ChunkManager initChunkManager();

    /**
     * Gets the region manager.
     *
     * @return the PlotSquared region manager
     */
    RegionManager initRegionManager();

    /**
     * Gets the {@link SetupUtils} class.
     */
    SetupUtils initSetupUtils();

    /**
     * Gets {@link HybridUtils} class.
     */
    HybridUtils initHybridUtils();

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
     * Gets the {@link InventoryUtil} class (used for implementation specific
     * inventory guis).
     */
    InventoryUtil initInventoryUtil();

    /**
     * Unregisters a {@link PlotPlayer} from cache e.g. if they have logged off.
     *
     * @param player the player to remove
     */
    void unregister(PlotPlayer player);

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
     * Register the chunk processor which will clean out chunks that have too
     * many block states or entities.
     */
    void registerChunkProcessor();

    /**
     * Register the world initialization events (used to keep track of worlds
     * being generated).
     */
    void registerWorldEvents();

    /**
     * Register events related to the server
     */
    void registerServerEvents();

    /**
     * Usually HybridGen
     *
     * @return Default implementation generator
     */
    @NotNull IndependentPlotGenerator getDefaultGenerator();

    List<Map.Entry<Map.Entry<String, String>, Boolean>> getPluginIds();

    Actor getConsole();

    /**
     * Get the backup manager instance
     *
     * @return Backup manager
     */
    @NotNull BackupManager getBackupManager();

    /**
     * Get the platform specific world manager
     *
     * @return World manager
     */
    @NotNull PlatformWorldManager<?> getWorldManager();

    /**
     * Get the player manager implementation for the platform
     *
     * @return Player manager
     */
    @NotNull PlayerManager<? extends PlotPlayer<P>, ? extends P> getPlayerManager();

}
