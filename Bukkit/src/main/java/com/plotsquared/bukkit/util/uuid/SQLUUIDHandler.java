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
package com.plotsquared.bukkit.util.uuid;

import com.google.common.collect.HashBiMap;
import com.plotsquared.core.PlotSquared;
import com.plotsquared.core.configuration.Captions;
import com.plotsquared.core.configuration.Settings;
import com.plotsquared.core.database.SQLite;
import com.plotsquared.core.util.MainUtil;
import com.plotsquared.core.util.StringWrapper;
import com.plotsquared.core.util.task.RunnableVal;
import com.plotsquared.core.util.task.TaskManager;
import com.plotsquared.core.util.uuid.UUIDHandler;
import com.plotsquared.core.util.uuid.UUIDHandlerImplementation;
import com.plotsquared.core.util.uuid.UUIDWrapper;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.io.InputStream;
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
import java.util.concurrent.TimeUnit;

public class SQLUUIDHandler extends UUIDHandlerImplementation {

    final int MAX_REQUESTS = 500;
    private final String PROFILE_URL =
        "https://sessionserver.mojang.com/session/minecraft/profile/";
    private final JSONParser jsonParser = new JSONParser();
    private final SQLite sqlite;

    public SQLUUIDHandler(UUIDWrapper wrapper) {
        super(wrapper);
        this.sqlite =
            new SQLite(MainUtil.getFile(PlotSquared.get().IMP.getDirectory(), "usercache.db"));
        try {
            this.sqlite.openConnection();
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }

        try (PreparedStatement stmt = getConnection().prepareStatement(
            "CREATE TABLE IF NOT EXISTS `usercache` (uuid VARCHAR(32) NOT NULL, username VARCHAR(32) NOT NULL, PRIMARY KEY (uuid, username))")) {
            stmt.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        startCaching(null);
    }

    private Connection getConnection() {
        synchronized (this.sqlite) {
            return this.sqlite.getConnection();
        }
    }

    @Override public boolean startCaching(final Runnable whenDone) {
        if (!super.startCaching(whenDone)) {
            return false;
        }
        TaskManager.runTaskAsync(() -> {
            try {
                HashBiMap<StringWrapper, UUID> toAdd = HashBiMap.create(new HashMap<>());
                try (PreparedStatement statement = getConnection()
                    .prepareStatement("SELECT `uuid`, `username` FROM `usercache`");
                    ResultSet resultSet = statement.executeQuery()) {
                    while (resultSet.next()) {
                        StringWrapper username = new StringWrapper(resultSet.getString("username"));
                        UUID uuid = UUID.fromString(resultSet.getString("uuid"));
                        toAdd.put(new StringWrapper(username.value), uuid);
                    }
                }
                add(toAdd);
                // This should be called as long as there are some unknown plots
                final ArrayDeque<UUID> toFetch = new ArrayDeque<>();
                for (UUID u : UUIDHandler.getAllUUIDS()) {
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
                FileUUIDHandler fileHandler = new FileUUIDHandler(SQLUUIDHandler.this.uuidWrapper);
                fileHandler.startCaching(() -> {
                    // If the file based UUID handler didn't cache it, then we can't cache offline mode
                    // Also, trying to cache based on files again, is useless as that's what the file based uuid cacher does
                    if (Settings.UUID.OFFLINE) {
                        if (whenDone != null) {
                            whenDone.run();
                        }
                        return;
                    }

                    TaskManager.runTaskAsync(() -> {
                        while (!toFetch.isEmpty()) {
                            try {
                                for (int i = 0; i < Math.min(MAX_REQUESTS, toFetch.size()); i++) {
                                    UUID uuid = toFetch.pop();
                                    HttpURLConnection connection = (HttpURLConnection) new URL(
                                        SQLUUIDHandler.this.PROFILE_URL + uuid.toString()
                                            .replace("-", "")).openConnection();
                                    try (InputStream con = connection.getInputStream()) {
                                        InputStreamReader reader = new InputStreamReader(con);
                                        JSONObject response =
                                            (JSONObject) SQLUUIDHandler.this.jsonParser
                                                .parse(reader);
                                        String name = (String) response.get("name");
                                        if (name != null) {
                                            add(new StringWrapper(name), uuid);
                                        }
                                    }
                                    connection.disconnect();
                                }
                            } catch (IOException | ParseException e) {
                                PlotSquared.debug(
                                    "Invalid response from Mojang: Some UUIDs will be cached later. (`unknown` until then or player joins)");
                            }
                            try {
                                //Mojang allows requests every 10 minutes according to https://wiki.vg/Mojang_API
                                //15 Minutes is chosen here since system timers are not always precise
                                //and it should provide enough time where Mojang won't block requests.
                                TimeUnit.MINUTES.sleep(15);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                                break;
                            }
                        }
                        if (whenDone != null) {
                            whenDone.run();
                        }
                    });
                });
            } catch (SQLException e) {
                throw new SQLUUIDHandlerException("Couldn't select :s", e);
            }
        });
        return true;
    }

    @Override public void fetchUUID(final String name, final RunnableVal<UUID> ifFetch) {
        PlotSquared.debug(Captions.PREFIX + "UUID for '" + name
            + "' was null. We'll cache this from the Mojang servers!");
        if (ifFetch == null) {
            return;
        }
        TaskManager.runTaskAsync(() -> {
            try {
                URL url = new URL(SQLUUIDHandler.this.PROFILE_URL);
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
                JSONArray array = (JSONArray) SQLUUIDHandler.this.jsonParser
                    .parse(new InputStreamReader(connection.getInputStream()));
                JSONObject jsonProfile = (JSONObject) array.get(0);
                String id = (String) jsonProfile.get("id");
                String name1 = (String) jsonProfile.get("name");
                ifFetch.value = UUID.fromString(
                    id.substring(0, 8) + '-' + id.substring(8, 12) + '-' + id.substring(12, 16)
                        + '-' + id.substring(16, 20) + '-' + id.substring(20, 32));
            } catch (IOException | ParseException e) {
                e.printStackTrace();
            }
            TaskManager.runTask(ifFetch);
        });
    }

    @Override public void handleShutdown() {
        super.handleShutdown();
        try {
            getConnection().close();
        } catch (SQLException e) {
            throw new SQLUUIDHandlerException("Couldn't close database connection", e);
        }
    }

    /**
     * This is useful for name changes
     */
    @Override public void rename(final UUID uuid, final StringWrapper name) {
        super.rename(uuid, name);
        TaskManager.runTaskAsync(() -> {
            try (PreparedStatement statement = getConnection()
                .prepareStatement("UPDATE usercache SET `username`=? WHERE `uuid`=?")) {
                statement.setString(1, name.value);
                statement.setString(2, uuid.toString());
                statement.execute();
                PlotSquared.debug(
                    Captions.PREFIX + "Name change for '" + uuid + "' to '" + name.value + '\'');
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }

    @Override public boolean add(final StringWrapper name, final UUID uuid) {
        // Ignoring duplicates
        if (super.add(name, uuid)) {
            TaskManager.runTaskAsync(() -> {
                try (PreparedStatement statement = getConnection()
                    .prepareStatement("REPLACE INTO usercache (`uuid`, `username`) VALUES(?, ?)")) {
                    statement.setString(1, uuid.toString());
                    statement.setString(2, name.toString());
                    statement.execute();
                    PlotSquared
                        .debug(Captions.PREFIX + "&cAdded '&6" + uuid + "&c' - '&6" + name + "&c'");
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            });
            return true;
        }
        return false;
    }

    private static class SQLUUIDHandlerException extends RuntimeException {

        SQLUUIDHandlerException(String s, Throwable c) {
            super("SQLUUIDHandler caused an exception: " + s, c);
        }
    }
}
