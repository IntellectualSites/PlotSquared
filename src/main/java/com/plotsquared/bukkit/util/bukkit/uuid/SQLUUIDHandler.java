package com.plotsquared.bukkit.util.bukkit.uuid;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import com.google.common.collect.HashBiMap;
import com.intellectualcrafters.json.JSONObject;
import com.intellectualcrafters.plot.PS;
import com.intellectualcrafters.plot.config.C;
import com.intellectualcrafters.plot.config.Settings;
import com.intellectualcrafters.plot.database.DBFunc;
import com.intellectualcrafters.plot.database.SQLite;
import com.intellectualcrafters.plot.object.RunnableVal;
import com.intellectualcrafters.plot.object.StringWrapper;
import com.intellectualcrafters.plot.util.TaskManager;
import com.intellectualcrafters.plot.util.UUIDHandler;
import com.intellectualcrafters.plot.util.UUIDHandlerImplementation;
import com.intellectualcrafters.plot.uuid.UUIDWrapper;

public class SQLUUIDHandler extends UUIDHandlerImplementation {
    
    public SQLUUIDHandler(UUIDWrapper wrapper) {
        super(wrapper);
        _sqLite = new SQLite("./plugins/PlotSquared/usercache.db");
        try {
            _sqLite.openConnection();
        } catch (final Exception e) {
            e.printStackTrace();
        }
        
        try {
            PreparedStatement stmt = getConnection().prepareStatement("CREATE TABLE IF NOT EXISTS `usercache` (uuid VARCHAR(32) NOT NULL, username VARCHAR(32) NOT NULL, PRIMARY KEY (uuid, username))");
            stmt.execute();
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        startCaching(null);
    }
    
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
    
    private Connection getConnection() {
        synchronized (_sqLite) {
            return _sqLite.getConnection();
        }
    }
    
    @Override
    public boolean startCaching(final Runnable whenDone) {
        if (!super.startCaching(whenDone)) {
            return false;
        }
        TaskManager.runTaskAsync(new Runnable() {
            @Override
            public void run() {
                try {
                    final HashBiMap<StringWrapper, UUID> toAdd = HashBiMap.create(new HashMap<StringWrapper, UUID>());
                    PreparedStatement statement = getConnection().prepareStatement("SELECT `uuid`, `username` FROM `usercache`");
                    ResultSet resultSet = statement.executeQuery();
                    StringWrapper username;
                    UUID uuid;
                    while (resultSet.next()) {
                        username = new StringWrapper(resultSet.getString("username"));
                        uuid = UUID.fromString(resultSet.getString("uuid"));
                        toAdd.put(new StringWrapper(username.value), uuid);
                    }
                    statement.close();
                    add(toAdd);
                    add(new StringWrapper("*"), DBFunc.everyone);
                    
                    // This should be called as long as there are some unknown plots
                    final List<UUID> toFetch = new ArrayList<>();
                    for (UUID u : UUIDHandler.getAllUUIDS()) {
                        if (!uuidExists(u)) {
                            toFetch.add(u);
                        }
                    }
                    if (toFetch.isEmpty()) {
                        if (whenDone != null) whenDone.run();
                        return;
                    }
                    FileUUIDHandler fileHandler = new FileUUIDHandler(SQLUUIDHandler.this.uuidWrapper);
                    fileHandler.startCaching(new Runnable() {
                        @Override
                        public void run() {
                            // If the file based UUID handler didn't cache it, then we can't cache offline mode
                            // Also, trying to cache based on files again, is useless as that's what the file based uuid cacher does
                            if (Settings.OFFLINE_MODE) {
                                if (whenDone != null) whenDone.run();
                                return;
                            }
                            if (!Settings.OFFLINE_MODE) {
                                PS.debug(C.PREFIX.s() + "&cWill fetch &6" + toFetch.size() + "&c from mojang!");
                                int i = 0;
                                Iterator<UUID> iterator = toFetch.iterator();
                                while (iterator.hasNext()) {
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
                                    PS.debug(C.PREFIX.s() + "&cWill attempt to fetch &6" + currentIteration.size() + "&c uuids from: &6" + url.toString());
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
                                    } catch (final Exception e) {
                                        e.printStackTrace();
                                    }
                                    i = 0;
                                }
                            }
                            if (whenDone != null) whenDone.run();
                        }
                    });
                } catch (SQLException e) {
                    throw new SQLUUIDHandlerException("Couldn't select :s", e);
                }
            }
        });
        return true;
    }
    
    @Override
    public void fetchUUID(final String name, final RunnableVal<UUID> ifFetch) {
        PS.debug(C.PREFIX.s() + "UUID for '" + name + "' was null. We'll cache this from the mojang servers!");
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
                    ifFetch.value = UUID.fromString(object.getJSONObject(name).getString("dashed"));
                    add(new StringWrapper(name), ifFetch.value);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                TaskManager.runTask(ifFetch);
            }
        });
    }
    
    @Override
    public void handleShutdown() {
        super.handleShutdown();
        try {
            getConnection().close();
        } catch (SQLException e) {
            throw new SQLUUIDHandlerException("Couldn't close database connection", e);
        }
    }
    
    @Override
    public boolean add(final StringWrapper name, final UUID uuid) {
        // Ignoring duplicates
        if (super.add(name, uuid)) {
            TaskManager.runTaskAsync(new Runnable() {
                @Override
                public void run() {
                    try {
                        PreparedStatement statement = getConnection().prepareStatement("INSERT INTO usercache (`uuid`, `username`) VALUES(?, ?)");
                        statement.setString(1, uuid.toString());
                        statement.setString(2, name.toString());
                        statement.execute();
                        PS.debug(C.PREFIX.s() + "&cAdded '&6" + uuid + "&c' - '&6" + name + "&c'");
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }
            });
            return true;
        }
        return false;
    }
    
    /**
     * This isn't used as any UUID that is unknown is bulk cached (in lots of 16)
     * @param uuid
     * @return
     */
    @Deprecated
    public String getName__unused__(final UUID uuid) {
        PS.debug(C.PREFIX.s() + "Name for '" + uuid + "' was null. We'll cache this from the mojang servers!");
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
        return null;
    }

    @Override
    public void rename(final UUID uuid, final StringWrapper name) {
        super.rename(uuid, name);
        TaskManager.runTaskAsync(new Runnable() {
            @Override
            public void run() {
                try {
                    PreparedStatement statement = getConnection().prepareStatement("UPDATE usercache SET `username`=? WHERE `uuid`=?");
                    statement.setString(1, name.value);
                    statement.setString(2, uuid.toString());
                    statement.execute();
                    PS.debug(C.PREFIX.s() + "Name change for '" + uuid + "' to '" + name.value + "'");
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
