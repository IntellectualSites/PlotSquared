package com.plotsquared.bukkit.uuid;

import com.google.common.collect.HashBiMap;
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
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayDeque;
import java.util.Collections;
import java.util.HashMap;
import java.util.UUID;

public class SQLUUIDHandler extends UUIDHandlerImplementation {
    
    final String PROFILE_URL = "https://sessionserver.mojang.com/session/minecraft/profile/";
    final int MAX_REQUESTS = 500;
    final int INTERVAL = 12000;
    final JSONParser jsonParser = new JSONParser();
    private final SQLite _sqLite;

    public SQLUUIDHandler(final UUIDWrapper wrapper) {
        super(wrapper);
        _sqLite = new SQLite("./plugins/PlotSquared/usercache.db");
        try {
            _sqLite.openConnection();
        } catch (final Exception e) {
            e.printStackTrace();
        }

        try {
            final PreparedStatement stmt = getConnection().prepareStatement(
            "CREATE TABLE IF NOT EXISTS `usercache` (uuid VARCHAR(32) NOT NULL, username VARCHAR(32) NOT NULL, PRIMARY KEY (uuid, username))");
            stmt.execute();
            stmt.close();
        } catch (final SQLException e) {
            e.printStackTrace();
        }
        startCaching(null);
    }
    
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
                    final PreparedStatement statement = getConnection().prepareStatement("SELECT `uuid`, `username` FROM `usercache`");
                    final ResultSet resultSet = statement.executeQuery();
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
                    final ArrayDeque<UUID> toFetch = new ArrayDeque<>();
                    for (final UUID u : UUIDHandler.getAllUUIDS()) {
                        if (!uuidExists(u)) {
                            toFetch.add(u);
                        }
                    }
                    if (toFetch.isEmpty()) {
                        if (whenDone != null) {
                            whenDone.run();
                        }
                        return;
                    }
                    final FileUUIDHandler fileHandler = new FileUUIDHandler(SQLUUIDHandler.this.uuidWrapper);
                    fileHandler.startCaching(new Runnable() {
                        @Override
                        public void run() {
                            // If the file based UUID handler didn't cache it, then we can't cache offline mode
                            // Also, trying to cache based on files again, is useless as that's what the file based uuid cacher does
                            if (Settings.OFFLINE_MODE) {
                                if (whenDone != null) {
                                    whenDone.run();
                                }
                                return;
                            }

                            TaskManager.runTaskAsync(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        if (toFetch.isEmpty()) {
                                            if (whenDone != null) {
                                                whenDone.run();
                                            }
                                            return;
                                        }
                                        for (int i = 0; i < Math.min(500, toFetch.size()); i++) {
                                            UUID uuid = toFetch.pop();
                                            HttpURLConnection connection = (HttpURLConnection) new URL(PROFILE_URL + uuid.toString().replace("-", "")).openConnection();
                                            InputStreamReader reader = new InputStreamReader(connection.getInputStream());
                                            JSONObject response = (JSONObject) jsonParser.parse(reader);
                                            String name = (String) response.get("name");
                                            if (name != null) {
                                                add(new StringWrapper(name), uuid);
                                            }
                                        }
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                    TaskManager.runTaskLaterAsync(this, INTERVAL);
                                }
                            });
                            /*
                             * This API is no longer accessible.
                             */
                            //                            if (!Settings.OFFLINE_MODE) {
                            //                                PS.debug(C.PREFIX.s() + "&cWill fetch &6" + toFetch.size() + "&c from mojang!");
                            //
                            //                                int i = 0;
                            //                                final Iterator<UUID> iterator = toFetch.iterator();
                            //                                while (iterator.hasNext()) {
                            //                                    final StringBuilder url = new StringBuilder("http://api.intellectualsites.com/uuid/?user=");
                            //                                    final List<UUID> currentIteration = new ArrayList<>();
                            //                                    while ((i++ <= 15) && iterator.hasNext()) {
                            //                                        final UUID _uuid = iterator.next();
                            //                                        url.append(_uuid.toString());
                            //                                        if (iterator.hasNext()) {
                            //                                            url.append(",");
                            //                                        }
                            //                                        currentIteration.add(_uuid);
                            //                                    }
                            //                                    PS.debug(C.PREFIX.s() + "&cWill attempt to fetch &6" + currentIteration.size() + "&c uuids from: &6" + url.toString());
                            //                                    try {
                            //                                        final HttpURLConnection connection = (HttpURLConnection) new URL(url.toString()).openConnection();
                            //                                        connection.setRequestProperty("User-Agent", "Mozilla/5.0");
                            //                                        final BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                            //                                        String line;
                            //                                        final StringBuilder rawJSON = new StringBuilder();
                            //                                        while ((line = reader.readLine()) != null) {
                            //                                            rawJSON.append(line);
                            //                                        }
                            //                                        reader.close();
                            //                                        final JSONObject object = new JSONObject(rawJSON.toString());
                            //                                        for (final UUID _u : currentIteration) {
                            //                                            final Object o = object.getJSONObject(_u.toString().replace("-", "")).get("username");
                            //                                            if ((o == null) || !(o instanceof String)) {
                            //                                                continue;
                            //                                            }
                            //                                            add(new StringWrapper(o.toString()), _u);
                            //                                        }
                            //                                    } catch (final Exception e) {
                            //                                        e.printStackTrace();
                            //                                    }
                            //                                    i = 0;
                            //                                }
                            //                            }
                            //                            if (whenDone != null) {
                            //                                whenDone.run();
                            //                            }
                        }
                    });
                } catch (final SQLException e) {
                    throw new SQLUUIDHandlerException("Couldn't select :s", e);
                }
            }
        });
        return true;
    }
    
    @Override
    public void fetchUUID(final String name, final RunnableVal<UUID> ifFetch) {
        PS.debug(C.PREFIX.s() + "UUID for '" + name + "' was null. We'll cache this from the mojang servers!");
        if (ifFetch == null) {
            return;
        }
        TaskManager.runTaskAsync(new Runnable() {
            @Override
            public void run() {
                try {
                    URL url = new URL(PROFILE_URL);
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("POST");
                    connection.setRequestProperty("Content-Type", "application/json");
                    connection.setUseCaches(false);
                    connection.setDoInput(true);
                    connection.setDoOutput(true);
                    String body = JSONArray.toJSONString(Collections.singletonList(name));
                    OutputStream stream = connection.getOutputStream();
                    stream.write(body.getBytes());
                    stream.flush();
                    stream.close();
                    JSONArray array = (JSONArray) jsonParser.parse(new InputStreamReader(connection.getInputStream()));
                    JSONObject jsonProfile = (JSONObject) array.get(0);
                    String id = (String) jsonProfile.get("type");
                    String name = (String) jsonProfile.get("name");
                    ifFetch.value = UUID.fromString(id.substring(0, 8) + "-" + id.substring(8, 12) + "-" + id.substring(12, 16) + "-" + id.substring(16, 20) + "-" + id.substring(20, 32));
                } catch (Exception e) {
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
        } catch (final SQLException e) {
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
                        final PreparedStatement statement = getConnection().prepareStatement("REPLACE INTO usercache (`uuid`, `username`) VALUES(?, ?)");
                        statement.setString(1, uuid.toString());
                        statement.setString(2, name.toString());
                        statement.execute();
                        PS.debug(C.PREFIX.s() + "&cAdded '&6" + uuid + "&c' - '&6" + name + "&c'");
                    } catch (final SQLException e) {
                        e.printStackTrace();
                    }
                }
            });
            return true;
        }
        return false;
    }
    
    /**
     * This is useful for name changes
     */
    @Override
    public void rename(final UUID uuid, final StringWrapper name) {
        super.rename(uuid, name);
        TaskManager.runTaskAsync(new Runnable() {
            @Override
            public void run() {
                try {
                    final PreparedStatement statement = getConnection().prepareStatement("UPDATE usercache SET `username`=? WHERE `uuid`=?");
                    statement.setString(1, name.value);
                    statement.setString(2, uuid.toString());
                    statement.execute();
                    PS.debug(C.PREFIX.s() + "Name change for '" + uuid + "' to '" + name.value + "'");
                } catch (final SQLException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private class SQLUUIDHandlerException extends RuntimeException {

        SQLUUIDHandlerException(final String s, final Throwable c) {
            super("SQLUUIDHandler caused an exception: " + s, c);
        }
    }
}
