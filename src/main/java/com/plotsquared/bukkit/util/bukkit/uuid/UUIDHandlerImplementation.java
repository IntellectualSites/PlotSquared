package com.plotsquared.bukkit.util.bukkit.uuid;

import com.google.common.collect.BiMap;
import com.plotsquared.bukkit.object.BukkitOfflinePlayer;
import com.intellectualcrafters.plot.object.PlotPlayer;
import com.intellectualcrafters.plot.object.StringWrapper;
import com.intellectualcrafters.plot.uuid.UUIDWrapper;

import java.util.Map;
import java.util.UUID;

public interface UUIDHandlerImplementation {

    void add(final StringWrapper name, final UUID uuid);
    boolean uuidExists(final UUID uuid);
    BiMap<StringWrapper, UUID> getUUIDMap();
    boolean nameExists(final StringWrapper wrapper);
    void handleShutdown();
    void cacheWorld(String world);
    void cache(final Map<StringWrapper, UUID> toAdd);
    String getName(final UUID uuid);
    UUID getUUID(final PlotPlayer player);
    UUID getUUID(final BukkitOfflinePlayer player);
    PlotPlayer getPlayer(final UUID uuid);
    PlotPlayer getPlayer(String name);
    UUID getUUID(String name);
    void startCaching();
    void setUUIDWrapper(UUIDWrapper wrapper);
    UUIDWrapper getUUIDWrapper();
    Map<String, PlotPlayer> getPlayers();

}
