package com.intellectualcrafters.plot.util;

import com.google.common.base.Charsets;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.intellectualcrafters.plot.PS;
import com.intellectualcrafters.plot.config.C;
import com.intellectualcrafters.plot.config.Settings;
import com.intellectualcrafters.plot.database.DBFunc;
import com.intellectualcrafters.plot.object.*;
import com.intellectualcrafters.plot.uuid.UUIDWrapper;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public abstract class UUIDHandlerImplementation {

    public final ConcurrentHashMap<String, PlotPlayer> players;
    public final HashSet<UUID> unknown = new HashSet<>();
    public UUIDWrapper uuidWrapper;
    private boolean cached = false;
    private BiMap<StringWrapper, UUID> uuidMap =
        HashBiMap.create(new HashMap<StringWrapper, UUID>());
    //    private BiMap<UUID, StringWrapper> nameMap = uuidMap.inverse();

    public UUIDHandlerImplementation(UUIDWrapper wrapper) {
        this.uuidWrapper = wrapper;
        this.players = new ConcurrentHashMap<>();
    }

    /**
     * If the UUID is not found, some commands can request to fetch the UUID when possible.
     *
     * @param name
     * @param ifFetch
     */
    public abstract void fetchUUID(String name, RunnableVal<UUID> ifFetch);

    /**
     * Start UUID caching (this should be an async task)
     * Recommended to override this is you want to cache offline players
     */
    public boolean startCaching(Runnable whenDone) {
        if (this.cached) {
            return false;
        }
        return this.cached = true;
    }

    public UUIDWrapper getUUIDWrapper() {
        return this.uuidWrapper;
    }

    public void setUUIDWrapper(UUIDWrapper wrapper) {
        this.uuidWrapper = wrapper;
    }

    public void rename(UUID uuid, StringWrapper name) {
        this.uuidMap.inverse().remove(uuid);
        this.uuidMap.put(name, uuid);
    }

    public void add(BiMap<StringWrapper, UUID> toAdd) {
        if (this.uuidMap.isEmpty()) {
            this.uuidMap = toAdd;
        }
        for (Map.Entry<StringWrapper, UUID> entry : toAdd.entrySet()) {
            UUID uuid = entry.getValue();
            StringWrapper name = entry.getKey();
            if (uuid == null || name == null) {
                continue;
            }
            StringWrapper oldName = this.uuidMap.inverse().get(uuid);
            if (oldName != null) {
                if (this.uuidMap.containsKey(name)) {
                    continue;
                }
                if (getPlayer(uuid) == null) {
                    rename(uuid, name);
                }
                continue;
            }
            this.uuidMap.put(name, uuid);
        }
        PS.debug(C.PREFIX + "&6Cached a total of: " + this.uuidMap.size() + " UUIDs");
    }

    public boolean add(final StringWrapper name, final UUID uuid) {
        if (uuid == null) {
            PS.debug("UUID cannot be null!");
            return false;
        }
        if (name == null) {
            try {
                this.unknown.add(uuid);
            } catch (Exception e) {
                PS.log("&c(minor) Invalid UUID mapping: " + uuid);
                e.printStackTrace();
            }
            return false;
        }

        /*
         * lazy UUID conversion:
         *  - Useful if the person misconfigured the database, or settings before
          *   PlotMe conversion
         */
        if (!Settings.UUID.OFFLINE && !this.unknown.isEmpty()) {
            TaskManager.runTaskAsync(new Runnable() {
                @Override public void run() {
                    UUID offline = UUID.nameUUIDFromBytes(
                        ("OfflinePlayer:" + name.value).getBytes(Charsets.UTF_8));
                    if (!UUIDHandlerImplementation.this.unknown.contains(offline) && !name.value
                        .equals(name.value.toLowerCase())) {
                        offline = UUID.nameUUIDFromBytes(
                            ("OfflinePlayer:" + name.value.toLowerCase()).getBytes(Charsets.UTF_8));
                        if (!UUIDHandlerImplementation.this.unknown.contains(offline)) {
                            offline = null;
                        }
                    }
                    if (offline != null && !offline.equals(uuid)) {
                        UUIDHandlerImplementation.this.unknown.remove(offline);
                        Set<Plot> plots = PS.get().getPlotsAbs(offline);
                        if (!plots.isEmpty()) {
                            for (Plot plot : plots) {
                                plot.owner = uuid;
                            }
                            DBFunc.replaceUUID(offline, uuid);
                            PS.debug("&cDetected invalid UUID stored for: " + name.value);
                            PS.debug(
                                "&7 - Did you recently switch to online-mode storage without running `uuidconvert`?");
                            PS.debug("&6" + PS.imp().getPluginName()
                                + " will update incorrect entries when the user logs in, or you can reconstruct your database.");
                        }
                    }
                }
            });
        } else if (Settings.UUID.FORCE_LOWERCASE && !this.unknown.isEmpty() && !name.value
            .equals(name.value.toLowerCase())) {
            TaskManager.runTaskAsync(new Runnable() {
                @Override public void run() {
                    UUID offlineUpper = UUID.nameUUIDFromBytes(
                        ("OfflinePlayer:" + name.value).getBytes(Charsets.UTF_8));
                    if (UUIDHandlerImplementation.this.unknown.contains(offlineUpper)
                        && offlineUpper != null && !offlineUpper.equals(uuid)) {
                        UUIDHandlerImplementation.this.unknown.remove(offlineUpper);
                        Set<Plot> plots = PS.get().getPlotsAbs(offlineUpper);
                        if (!plots.isEmpty()) {
                            for (Plot plot : plots) {
                                plot.owner = uuid;
                            }
                            replace(offlineUpper, uuid, name.value);
                        }
                    }
                }
            });
        }
        try {
            UUID offline = this.uuidMap.put(name, uuid);
            if (offline != null) {
                if (!offline.equals(uuid)) {
                    Set<Plot> plots = PS.get().getPlots(offline);
                    if (!plots.isEmpty()) {
                        for (Plot plot : plots) {
                            plot.owner = uuid;
                        }
                        replace(offline, uuid, name.value);
                    }
                    return true;
                } else {
                    StringWrapper oName = this.uuidMap.inverse().get(offline);
                    if (!oName.equals(name)) {
                        this.uuidMap.remove(name);
                        this.uuidMap.put(name, uuid);
                    }
                }
                return false;
            }
        } catch (Exception ignored) {
            BiMap<UUID, StringWrapper> inverse = this.uuidMap.inverse();
            StringWrapper oldName = inverse.get(uuid);
            if (oldName != null) {
                if (this.uuidMap.containsKey(name)) {
                    return false;
                }
                PlotPlayer player = getPlayer(uuid);
                if (player == null || player.getName().equalsIgnoreCase(name.value)) {
                    rename(uuid, name);
                }
                return false;
            }
            this.uuidMap.put(name, uuid);
        }
        return true;
    }

    private void replace(UUID from, UUID to, String name) {
        DBFunc.replaceUUID(from, to);
        PS.debug("&cDetected invalid UUID stored for: " + name);
        PS.debug(
            "&7 - Did you recently switch to online-mode storage without running `uuidconvert`?");
        PS.debug("&6" + PS.imp().getPluginName()
            + " will update incorrect entries when the user logs in, or you can reconstruct your database.");
    }

    public boolean uuidExists(UUID uuid) {
        return this.uuidMap.containsValue(uuid);
    }

    public BiMap<StringWrapper, UUID> getUUIDMap() {
        return this.uuidMap;
    }

    public boolean nameExists(StringWrapper wrapper) {
        return this.uuidMap.containsKey(wrapper);
    }

    public void handleShutdown() {
        this.players.clear();
        this.uuidMap.clear();
    }

    public String getName(UUID uuid) {
        if (uuid == null) {
            return null;
        }
        StringWrapper name = this.uuidMap.inverse().get(uuid);
        if (name != null) {
            return name.value;
        }
        return null;
    }

    public UUID getUUID(String name, RunnableVal<UUID> ifFetch) {
        if (name.isEmpty()) {
            return null;
        }
        // check online
        PlotPlayer player = getPlayer(name);
        if (player != null) {
            return player.getUUID();
        }
        // check cache
        StringWrapper wrap = new StringWrapper(name);
        UUID uuid = this.uuidMap.get(wrap);
        if (uuid != null) {
            return uuid;
        }
        // Read from disk OR convert directly to offline UUID
        if (Settings.UUID.OFFLINE && !StringMan.contains(name, ';')) {
            uuid = this.uuidWrapper.getUUID(name);
            add(new StringWrapper(name), uuid);
            return uuid;
        }
        if ((ifFetch != null)) {
            fetchUUID(name, ifFetch);
            return null;
        }
        return null;
    }

    public UUID getUUID(PlotPlayer player) {
        return this.uuidWrapper.getUUID(player);
    }

    public UUID getUUID(OfflinePlotPlayer player) {
        return this.uuidWrapper.getUUID(player);
    }

    public PlotPlayer getPlayer(UUID uuid) {
        String name = getName(uuid);
        if (name != null) {
            return getPlayer(name);
        }
        return null;
    }

    public PlotPlayer getPlayer(String name) {
        return this.players.get(name);
    }

    public Map<String, PlotPlayer> getPlayers() {
        return this.players;
    }

}
