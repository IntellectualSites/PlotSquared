package com.github.intellectualsites.plotsquared.api;

import com.github.intellectualsites.plotsquared.configuration.file.YamlConfiguration;
import com.github.intellectualsites.plotsquared.plot.PlotSquared;
import com.github.intellectualsites.plotsquared.plot.config.Captions;
import com.github.intellectualsites.plotsquared.plot.flag.Flag;
import com.github.intellectualsites.plotsquared.plot.flag.Flags;
import com.github.intellectualsites.plotsquared.plot.object.Plot;
import com.github.intellectualsites.plotsquared.plot.object.PlotArea;
import com.github.intellectualsites.plotsquared.plot.object.PlotPlayer;
import com.github.intellectualsites.plotsquared.plot.util.ChunkManager;
import com.github.intellectualsites.plotsquared.plot.util.MainUtil;
import com.github.intellectualsites.plotsquared.plot.util.SchematicHandler;
import com.github.intellectualsites.plotsquared.plot.util.UUIDHandler;
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
    public void sendConsoleMessage(Captions caption) {
        sendConsoleMessage(caption.s());
    }

    /**
     * Registers a flag for use in plots.
     *
     * @param flag the flag to register
     */
    public void addFlag(Flag<?> flag) {
        Flags.registerFlag(flag);
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
}
