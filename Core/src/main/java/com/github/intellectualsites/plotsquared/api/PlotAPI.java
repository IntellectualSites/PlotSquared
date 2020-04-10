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
package com.github.intellectualsites.plotsquared.api;

import com.github.intellectualsites.plotsquared.configuration.file.YamlConfiguration;
import com.github.intellectualsites.plotsquared.plot.PlotSquared;
import com.github.intellectualsites.plotsquared.plot.config.Caption;
import com.github.intellectualsites.plotsquared.plot.config.Captions;
import com.github.intellectualsites.plotsquared.plot.object.Plot;
import com.github.intellectualsites.plotsquared.plot.object.PlotArea;
import com.github.intellectualsites.plotsquared.plot.object.PlotPlayer;
import com.github.intellectualsites.plotsquared.plot.util.*;
import com.github.intellectualsites.plotsquared.plot.util.block.GlobalBlockQueue;
import com.github.intellectualsites.plotsquared.plot.uuid.UUIDWrapper;
import lombok.NoArgsConstructor;

import java.util.Collections;
import java.util.Set;
import java.util.UUID;

/**
 * PlotSquared API.
 *
 * <p>Useful classes:
 * <ul>
 * <li>{@link PlotPlayer}</li>
 * <li>{@link Plot}</li>
 * <li>{@link com.github.intellectualsites.plotsquared.plot.object.Location}</li>
 * <li>{@link PlotArea}</li>
 * <li>{@link PlotSquared}</li>
 * </ul>
 *
 * @version 3.3.3
 */
@SuppressWarnings({"unused", "WeakerAccess"}) @NoArgsConstructor public class PlotAPI {

    /**
     * Gets all plots.
     *
     * @return all plots
     * @see PlotSquared#getPlots()
     */
    public Set<Plot> getAllPlots() {
        return PlotSquared.get().getPlots();
    }

    /**
     * Gets all plots for a player.
     *
     * @param player Player, whose plots to search for
     * @return all plots that a player owns
     */
    public Set<Plot> getPlayerPlots(PlotPlayer player) {
        return PlotSquared.get().getPlots(player);
    }

    /**
     * Adds a plot world.
     *
     * @param plotArea Plot World Object
     * @see PlotSquared#addPlotArea(PlotArea)
     */
    public void addPlotArea(PlotArea plotArea) {
        PlotSquared.get().addPlotArea(plotArea);
    }

    /**
     * Gets the configuration file for this plugin.
     *
     * @return the configuration file for PlotSquared
     * =
     */
    public YamlConfiguration getConfig() {
        return PlotSquared.get().getConfig();
    }

    /**
     * Gets the PlotSquared storage file.
     *
     * @return storage configuration
     * @see PlotSquared#storage
     */
    public YamlConfiguration getStorage() {
        return PlotSquared.get().storage;
    }

    /**
     * ChunkManager class contains several useful methods.
     * <ul>
     * <li>Chunk deletion</li>
     * <li>Moving or copying regions</li>
     * <li>Plot swapping</li>
     * <li>Entity Tracking</li>
     * <li>Region Regeneration</li>
     * </ul>
     *
     * @return ChunkManager
     * @see ChunkManager
     */
    public ChunkManager getChunkManager() {
        return ChunkManager.manager;
    }

    /**
     * Gets the block/biome set queue
     *
     * @return GlobalBlockQueue.IMP
     */
    public GlobalBlockQueue getBlockQueue() {
        return GlobalBlockQueue.IMP;
    }

    /**
     * UUIDWrapper class has basic methods for getting UUIDS. It's recommended
     * to use the UUIDHandler class instead.
     *
     * @return UUIDWrapper
     * @see UUIDWrapper
     */
    public UUIDWrapper getUUIDWrapper() {
        return UUIDHandler.getUUIDWrapper();
    }

    /**
     * SchematicHandler class contains methods related to pasting, reading
     * and writing schematics.
     *
     * @return SchematicHandler
     * @see SchematicHandler
     */
    public SchematicHandler getSchematicHandler() {
        return SchematicHandler.manager;
    }

    /**
     * Gets a list of PlotAreas in the world.
     *
     * @param world The world to check for plot areas
     * @return A set of PlotAreas
     */
    public Set<PlotArea> getPlotAreas(String world) {
        if (world == null) {
            return Collections.emptySet();
        }
        return PlotSquared.get().getPlotAreas(world);
    }

    /**
     * Send a message to the console. The message supports color codes.
     *
     * @param message the message
     * @see MainUtil#sendConsoleMessage(Captions, String...)
     */
    public void sendConsoleMessage(String message) {
        PlotSquared.log(message);
    }

    /**
     * Sends a message to the console.
     *
     * @param caption the message
     * @see #sendConsoleMessage(String)
     * @see Captions
     */
    public void sendConsoleMessage(Caption caption) {
        sendConsoleMessage(caption.getTranslated());
    }

    /**
     * Gets the PlotSquared class.
     *
     * @return PlotSquared Class
     * @see PlotSquared
     */
    public PlotSquared getPlotSquared() {
        return PlotSquared.get();
    }

    /**
     * Gets the PlotPlayer for a UUID.
     *
     * <p><i>Please note that PlotSquared can be configured to provide
     * different UUIDs than bukkit</i>
     *
     * @param uuid the uuid of the player to wrap
     * @return a {@code PlotPlayer}
     * @see PlotPlayer#wrap(Object)
     */
    public PlotPlayer wrapPlayer(UUID uuid) {
        return PlotPlayer.wrap(uuid);
    }

    /**
     * Gets the PlotPlayer for a username.
     *
     * @param player the player to wrap
     * @return a {@code PlotPlayer}
     * @see PlotPlayer#wrap(Object)
     */
    public PlotPlayer wrapPlayer(String player) {
        return PlotPlayer.wrap(player);
    }

    /**
     * Registers a listener for PlotSquared Events
     *
     * @param listener the listener class to register
     * @see EventDispatcher#registerListener(Object)
     */
    public void registerListener(Object listener) {
        PlotSquared.get().getEventDispatcher().registerListener(listener);
    }
}
