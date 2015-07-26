package com.plotsquared.bukkit.util.bukkit.uuid;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

import java.util.concurrent.ConcurrentHashMap;

import com.google.common.collect.Maps;
import com.intellectualcrafters.json.JSONObject;
import com.plotsquared.bukkit.BukkitMain;
import com.intellectualcrafters.plot.database.SQLite;

import com.plotsquared.bukkit.util.bukkit.UUIDHandler;
import com.intellectualcrafters.plot.uuid.LowerOfflineUUIDWrapper;
import org.bukkit.Bukkit;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.io.Files;
import com.google.common.io.InputSupplier;
import com.intellectualcrafters.plot.PS;
import com.intellectualcrafters.plot.config.C;
import com.intellectualcrafters.plot.config.Settings;
import com.intellectualcrafters.plot.database.DBFunc;
import com.plotsquared.bukkit.object.BukkitOfflinePlayer;
import com.intellectualcrafters.plot.object.OfflinePlotPlayer;

import com.intellectualcrafters.plot.object.PlotPlayer;
import com.intellectualcrafters.plot.object.StringWrapper;
import com.intellectualcrafters.plot.util.ExpireManager;
import com.intellectualcrafters.plot.util.NbtFactory;
import com.intellectualcrafters.plot.util.NbtFactory.NbtCompound;
import com.intellectualcrafters.plot.util.NbtFactory.StreamOptions;
import com.intellectualcrafters.plot.util.TaskManager;
import com.intellectualcrafters.plot.uuid.OfflineUUIDWrapper;
import com.intellectualcrafters.plot.uuid.UUIDWrapper;

import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import org.bukkit.event.player.PlayerLoginEvent;

import org.bukkit.plugin.java.JavaPlugin;

public class SQLUUIDHandler implements Listener, UUIDHandlerImplementation {

    private class SQLUUIDHandlerException extends RuntimeException {
        SQLUUIDHandlerException(String s, Throwable c) {
            super("SQLUUIDHandler caused an exception: " + s, c);
        }
        @SuppressWarnings("unused")
        SQLUUIDHandlerException(String s) {
            super("SQLUUIDHandler caused an exception: " + s);
        }
    }

    private final SQLite _sqLite;
    private final BiMap<String, UUID> _uuidMap;
    private UUIDWrapper _uuidWrapper;
    private final Map<String, PlotPlayer> _players = new ConcurrentHashMap<>();

    private Connection getConnection() {
        synchronized (_sqLite) {
            return _sqLite.getConnection();
        }
    }

    public SQLUUIDHandler() {
        Bukkit.getPluginManager().registerEvents(this, JavaPlugin.getPlugin(BukkitMain.class));

        _sqLite = new SQLite("./plugins/PlotSquared/usercache.db");

        try {
            _sqLite.openConnection();
        } catch(final Exception e) {
            e.printStackTrace();
        }

        try {
            PreparedStatement stmt = getConnection().prepareStatement("CREATE TABLE IF NOT EXISTS `usercache` (cache_key INTEGER PRIMARY KEY, uuid VARCHAR(32) NOT NULL, username VARCHAR(32) NOT NULL)");
            stmt.execute();
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        _uuidMap = Maps.synchronizedBiMap(HashBiMap.create(new HashMap<String, UUID>()));

        _startCaching();
    }

    @Override
    public void handleShutdown() {
        try {
            getConnection().close();
        } catch (SQLException e) {
            throw new SQLUUIDHandlerException("Couldn't close database connection", e);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    @SuppressWarnings("unused")
    public void onPlayerJoin(final PlayerLoginEvent event) {
        TaskManager.runTaskAsync(new Runnable() {
            @Override
            public void run() {
                if (!nameExists(new StringWrapper(event.getPlayer().getName()))) {
                    add(new StringWrapper(event.getPlayer().getName()), event.getPlayer().getUniqueId());
                }
            }
        });
    }

    @Override
    public void startCaching() {
        // startCaching();
    }

    @Override
    public void setUUIDWrapper(UUIDWrapper wrapper) {
        this._uuidWrapper = wrapper;
    }

    @Override
    public UUIDWrapper getUUIDWrapper() {
        return _uuidWrapper;
    }

    @Override
    public Map<String, PlotPlayer> getPlayers() {
        return _players;
    }

    public void _startCaching() {
        TaskManager.runTaskAsync(new Runnable() {
            @Override
            public void run() {
                try {
                    PreparedStatement statement = getConnection().prepareStatement("SELECT `uuid`, `username` FROM `usercache`");
                    ResultSet resultSet = statement.executeQuery();
                    StringWrapper username;
                    UUID uuid;
                    boolean found = false;
                    while (resultSet.next()) {
                        found = true;
                        username = new StringWrapper(resultSet.getString("username"));
                        uuid = UUID.fromString(resultSet.getString("uuid"));
                        _uuidMap.put(username.value, uuid);
                    }
                    _uuidMap.put("*", DBFunc.everyone);
                    statement.close();
                    if (!found) {
                        PS.log(C.PREFIX.s() + "&cUsing player data files, couldn't find any cached UUIDs");
                        for (World world : Bukkit.getWorlds()) {
                            _cacheAll(world.getName());
                        }
                        PS.log(C.PREFIX.s() + "&cWill fetch the uuids for all plots!");

                        List<UUID> toFetch = new ArrayList<>();
                        for (UUID u : UUIDHandler.getAllUUIDS()) {
                            if (!uuidExists(u)) {
                                toFetch.add(u);
                            }
                        }
                        PS.log(C.PREFIX.s() + "&cFetching &6" + toFetch.size() + "&c uuids!");
                        if (Settings.OFFLINE_MODE) {
                            if (!(_uuidWrapper instanceof OfflineUUIDWrapper)) {
                                PS.log(C.PREFIX.s() + "Offline mode is on, but the uuid wrapper isn't set for offline mode. Activating appropriate uuid wrapper");
                                if (Settings.UUID_LOWERCASE) {
                                    _uuidWrapper = new LowerOfflineUUIDWrapper();
                                } else {
                                    _uuidWrapper = new OfflineUUIDWrapper();
                                }
                            }
                        }
                        List<UUID> fetched = new ArrayList<>();
                        for (UUID u : toFetch) {
                            OfflinePlayer plr = Bukkit.getOfflinePlayer(u);
                            if (plr != null) {
                                if  (plr.getName() != null) {
                                    add(new StringWrapper(plr.getName()), u);
                                    fetched.add(u);
                                }
                            }
                        }
                        PS.log(C.PREFIX.s() + "&cFetched &6" + fetched.size() + "&c from player files!");
                        toFetch.removeAll(fetched);
                        if (!Settings.OFFLINE_MODE) {
                            if (toFetch.isEmpty()) {
                                return;
                            }
                            PS.log(C.PREFIX.s() + "&cWill fetch &6" + toFetch.size() + "&c from mojang!");
                            int i = 0;
                            Iterator<UUID> iterator = toFetch.iterator();
                            while(iterator.hasNext()) {
                                StringBuilder url = new StringBuilder("http://api.intellectualsites.com/uuid/?user=");
                                List<UUID> currentIteration = new ArrayList<>();
                                while (i++ <= 15 && iterator.hasNext()) {
                                    UUID _uuid = iterator.next();
                                    url.append(_uuid.toString());
                                    if (iterator.hasNext()) {
                                        url.append(",");
                                    }
                                    currentIteration.add(_uuid);
                                }
                                PS.log(C.PREFIX.s() + "&cWill attempt to fetch &6" + currentIteration.size() + "&c uuids from: &6" + url.toString());
                                try {
                                    HttpURLConnection connection = (HttpURLConnection) new URL(url.toString()).openConnection();
                                    connection.setRequestProperty("User-Agent", "Mozilla/5.0");
                                    BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                                    String line;
                                    StringBuilder rawJSON = new StringBuilder();
                                    while ((line = reader.readLine()) != null) {
                                        rawJSON.append(line);
                                    }
                                    reader.close();
                                    JSONObject object = new JSONObject(rawJSON.toString());
                                    for (UUID _u : currentIteration) {
                                        Object o = object.getJSONObject(_u.toString().replace("-", "")).get("username");
                                        if (o == null || !(o instanceof String)) {
                                            continue;
                                        }
                                        add(new StringWrapper(o.toString()), _u);
                                    }
                                } catch(final Exception e) {
                                    e.printStackTrace();
                                }
                                i = 0;
                            }
                        }
                    }
                } catch (SQLException e) {
                    throw new SQLUUIDHandlerException("Couldn't select :s", e);
                }
            }
        });
    }

    @Override
    public void add(final StringWrapper name, final UUID uuid) {
        if ((uuid == null) || (name == null)) {
            PS.log(C.PREFIX.s() + "&cSQL Caching Failed: name/uuid was null??");
            return;
        }
        if (name.value == null) {
            PS.log(C.PREFIX.s() + "&cname.value == null for: " + uuid);
            return;
        }
        if (_uuidMap.containsKey(name.value)) {
            _uuidMap.remove(name.value);
        }
        if (_uuidMap.containsValue(uuid)) {
            _uuidMap.inverse().remove(uuid);
        }
        _uuidMap.put(name.value, uuid);
        TaskManager.runTaskAsync(new Runnable() {
            @Override
            public void run() {
                try {
                    PreparedStatement statement = getConnection().prepareStatement("INSERT INTO usercache (`uuid`, `username`) VALUES(?, ?)");
                    statement.setString(1, uuid.toString());
                    statement.setString(2, name.toString());
                    statement.execute();
                    PS.log(C.PREFIX.s() + "&cAdded '&6" + uuid + "&c' - '&6" + name + "&c'");
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public boolean uuidExists(final UUID uuid) {
        return _uuidMap.containsValue(uuid);
    }

    @Override
    public BiMap<StringWrapper, UUID> getUUIDMap() {
        BiMap<StringWrapper, UUID> map = HashBiMap.create();
        for (Map.Entry<String, UUID> entry : _uuidMap.entrySet()) {
            map.put(new StringWrapper(entry.getKey()), entry.getValue());
        }
        return map;
    }

    @Override
    public boolean nameExists(final StringWrapper name) {
        return _uuidMap.containsKey(name.value);
    }

    @Override
    public void cacheWorld(String world) {}

    @SuppressWarnings("deprecation")
    private void _cacheAll(final String world) {
        final File container = Bukkit.getWorldContainer();
        PS.log(C.PREFIX.s() + "&6Starting player data caching for: " + world);
        final HashMap<StringWrapper, UUID> toAdd = new HashMap<>();
        if (Settings.TWIN_MODE_UUID) {
            Set<UUID> all = getUUIDMap().values();
            PS.log("&aFast mode UUID caching enabled!");
            final File playerdataFolder = new File(container, world + File.separator + "playerdata");
            String[] dat = playerdataFolder.list(new FilenameFilter() {
                @Override
                public boolean accept(final File f, final String s) {
                    return s.endsWith(".dat");
                }
            });
            boolean check = all.size() == 0;
            if (dat != null) {
                for (final String current : dat) {
                    final String s = current.replaceAll(".dat$", "");
                    try {
                        UUID uuid = UUID.fromString(s);
                        if (check || all.contains(uuid)) {
                            File file = new File(playerdataFolder + File.separator + current);
                            InputSupplier<FileInputStream> is = Files.newInputStreamSupplier(file);
                            NbtCompound compound = NbtFactory.fromStream(is, StreamOptions.GZIP_COMPRESSION);
                            NbtCompound bukkit = (NbtCompound) compound.get("bukkit");
                            String name = (String) bukkit.get("lastKnownName");
                            long last = (long) bukkit.get("lastPlayed");
                            ExpireManager.dates.put(uuid, last);
                            toAdd.put(new StringWrapper(name), uuid);
                        }
                    } catch (final Exception e) {
                        e.printStackTrace();
                        PS.log(C.PREFIX.s() + "Invalid playerdata: " + current);
                    }
                }
            }
            cache(toAdd);
            return;
        }
        final HashSet<String> worlds = new HashSet<>();
        worlds.add(world);
        worlds.add("world");
        final HashSet<UUID> uuids = new HashSet<>();
        final HashSet<String> names = new HashSet<>();
        File playerdataFolder = null;
        for (final String worldname : worlds) {
            // Getting UUIDs
            playerdataFolder = new File(container, worldname + File.separator + "playerdata");
            String[] dat = playerdataFolder.list(new FilenameFilter() {
                @Override
                public boolean accept(final File f, final String s) {
                    return s.endsWith(".dat");
                }
            });
            if (dat != null && dat.length != 0) {
                for (final String current : dat) {
                    final String s = current.replaceAll(".dat$", "");
                    try {
                        final UUID uuid = UUID.fromString(s);
                        uuids.add(uuid);
                    } catch (final Exception e) {
                        PS.log(C.PREFIX.s() + "Invalid playerdata: " + current);
                    }
                }
                break;
            }
            // Getting names
            final File playersFolder = new File(worldname + File.separator + "players");
            dat = playersFolder.list(new FilenameFilter() {
                @Override
                public boolean accept(final File f, final String s) {
                    return s.endsWith(".dat");
                }
            });
            if (dat != null && dat.length != 0) {
                for (final String current : dat) {
                    names.add(current.replaceAll(".dat$", ""));
                }
                break;
            }
        }
        for (UUID uuid : uuids) {
            try {
                File file = new File(playerdataFolder + File.separator + uuid.toString() + ".dat");
                InputSupplier<FileInputStream> is = Files.newInputStreamSupplier(file);
                NbtCompound compound = NbtFactory.fromStream(is, StreamOptions.GZIP_COMPRESSION);
                NbtCompound bukkit = (NbtCompound) compound.get("bukkit");
                String name = (String) bukkit.get("lastKnownName");
                long last = (long) bukkit.get("lastPlayed");
                if (Settings.OFFLINE_MODE) {
                    if (Settings.UUID_LOWERCASE && !name.toLowerCase().equals(name)) {
                        uuid = _uuidWrapper.getUUID(name);
                    } else {
                        long most = (long) compound.get("UUIDMost");
                        long least = (long) compound.get("UUIDLeast");
                        uuid = new UUID(most, least);
                    }
                }
                ExpireManager.dates.put(uuid, last);
                toAdd.put(new StringWrapper(name), uuid);
            } catch (final Throwable e) {
                PS.log(C.PREFIX.s() + "&6Invalid playerdata: " + uuid.toString() + ".dat");
            }
        }
        for (final String name : names) {
            final UUID uuid = _uuidWrapper.getUUID(name);
            final StringWrapper nameWrap = new StringWrapper(name);
            toAdd.put(nameWrap, uuid);
        }

        if (_uuidMap.size() == 0) {
            for (OfflinePlotPlayer op : _uuidWrapper.getOfflinePlayers()) {
                if (op.getLastPlayed() != 0) {
                    String name = op.getName();
                    StringWrapper wrap = new StringWrapper(name);
                    UUID uuid = _uuidWrapper.getUUID(op);
                    toAdd.put(wrap, uuid);
                }
            }
        }
        cache(toAdd);
    }

    @Override
    public void cache(final Map<StringWrapper, UUID> toAdd) {
        for (Map.Entry<StringWrapper, UUID> entry : toAdd.entrySet()) {
            add(entry.getKey(), entry.getValue());
        }
    }

    @Override
    public String getName(final UUID uuid) {
        if (uuid == null) {
            return null;
        }
        final PlotPlayer player = getPlayer(uuid);
        if (player != null) {
            return player.getName();
        }
        if (_uuidMap.containsValue(uuid)) {
            return _uuidMap.inverse().get(uuid);
        }
        if (Settings.OFFLINE_MODE) {
            if (!(_uuidWrapper instanceof OfflineUUIDWrapper)) {
                PS.log(C.PREFIX.s() + "Offline mode is on, but the uuid wrapper isn't set for offline mode. Activating appropriate uuid wrapper");
                if (Settings.UUID_LOWERCASE) {
                    _uuidWrapper = new LowerOfflineUUIDWrapper();
                } else {
                    _uuidWrapper = new OfflineUUIDWrapper();
                }
            }
        }
        OfflinePlayer plr = Bukkit.getOfflinePlayer(uuid);
        if (plr != null) {
            add(new StringWrapper(plr.getName()), plr.getUniqueId());
            return plr.getName();
        }
        if (!Settings.OFFLINE_MODE) {
            PS.log(C.PREFIX.s() + "Name for '" + uuid + "' was null. We'll cache this from the mojang servers!");
            TaskManager.runTaskAsync(new Runnable() {
                @Override
                public void run() {
                    String url = "http://api.intellectualsites.com/uuid/?user=" + uuid;
                    try {
                        HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
                        connection.setRequestProperty("User-Agent", "Mozilla/5.0");
                        BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                        String line;
                        StringBuilder rawJSON = new StringBuilder();
                        while ((line = reader.readLine()) != null) {
                            rawJSON.append(line);
                        }
                        reader.close();
                        JSONObject object = new JSONObject(rawJSON.toString());
                        String username = object.getJSONObject(uuid.toString().replace("-", "")).getString("username");
                        add(new StringWrapper(username), uuid);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
        return null;
    }

    @Override
    public UUID getUUID(final PlotPlayer player) {
        return _uuidWrapper.getUUID(player);
    }

    @Override
    public UUID getUUID(final BukkitOfflinePlayer player) {
        return _uuidWrapper.getUUID(player);
    }

    @Override
    public PlotPlayer getPlayer(final UUID uuid) {
        for (final PlotPlayer player : _players.values()) {
            if (player.getUUID().equals(uuid)) {
                return player;
            }
        }
        return null;
    }

    @Override
    public PlotPlayer getPlayer(final String name) {
        return _players.get(name);
    }

    @Override
    public UUID getUUID(final String name) {
        if ((name == null) || (name.length() == 0)) {
            return null;
        }
        // check online
        final PlotPlayer player = getPlayer(name);
        if (player != null) {
            return player.getUUID();
        }
        // check cache
        UUID uuid = _uuidMap.get(name);
        if (uuid != null) {
            return uuid;
        }
        if (Settings.OFFLINE_MODE) {
            if (!(_uuidWrapper instanceof OfflineUUIDWrapper)) {
                PS.log(C.PREFIX.s() + "Offline mode is on, but the uuid wrapper isn't set for offline mode. Activating appropriate uuid wrapper");
                if (Settings.UUID_LOWERCASE) {
                    _uuidWrapper = new LowerOfflineUUIDWrapper();
                } else {
                    _uuidWrapper = new OfflineUUIDWrapper();
                }
            }
        }
        // Read from disk OR convert directly to offline UUID
        if (Settings.UUID_FROM_DISK || (_uuidWrapper instanceof OfflineUUIDWrapper)) {
            uuid = _uuidWrapper.getUUID(name);
            add(new StringWrapper(name), uuid);
            return uuid;
        }
        PS.log(C.PREFIX.s() + "UUID for '" + name + "' was null. We'll cache this from the mojang servers!");
        TaskManager.runTaskAsync(new Runnable() {
            @Override
            public void run() {
                String url = "http://api.intellectualsites.com/uuid/?user=" + name;
                try {
                    HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
                    connection.setRequestProperty("User-Agent", "Mozilla/5.0");
                    BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    String line;
                    StringBuilder rawJSON = new StringBuilder();
                    while ((line = reader.readLine()) != null) {
                        rawJSON.append(line);
                    }
                    reader.close();
                    JSONObject object = new JSONObject(rawJSON.toString());
                    UUID uuid = UUID.fromString(object.getJSONObject(name).getString("dashed"));
                    add(new StringWrapper(name), uuid);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        return null;
    }
}
