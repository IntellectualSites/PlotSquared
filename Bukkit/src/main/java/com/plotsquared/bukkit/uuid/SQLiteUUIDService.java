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
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.plotsquared.bukkit.uuid;

import com.plotsquared.core.PlotSquared;
import com.plotsquared.core.database.SQLite;
import com.plotsquared.core.util.FileUtils;
import com.plotsquared.core.uuid.UUIDMapping;
import com.plotsquared.core.uuid.UUIDService;

import javax.annotation.Nonnull;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

/**
 * UUID service that uses the (legacy) SQL UUID cache
 */
public class SQLiteUUIDService implements UUIDService, Consumer<List<UUIDMapping>> {

    private final SQLite sqlite;

    public SQLiteUUIDService(final String fileName) {
        this.sqlite =
            new SQLite(FileUtils.getFile(PlotSquared.platform().getDirectory(), fileName));
        try {
            this.sqlite.openConnection();
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }

        try (PreparedStatement stmt = getConnection().prepareStatement(
            "CREATE TABLE IF NOT EXISTS `usercache` (uuid VARCHAR(32) NOT NULL, username VARCHAR(32) NOT NULL, PRIMARY KEY (uuid))")) {
            stmt.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private Connection getConnection() {
        synchronized (this.sqlite) {
            return this.sqlite.getConnection();
        }
    }

    @Override @Nonnull public List<UUIDMapping> getNames(@Nonnull final List<UUID> uuids) {
        final List<UUIDMapping> mappings = new ArrayList<>(uuids.size());
        try (final PreparedStatement statement = getConnection()
            .prepareStatement("SELECT `username` FROM `usercache` WHERE `uuid` = ?")) {
            for (final UUID uuid : uuids) {
                statement.setString(1, uuid.toString());
                try (final ResultSet resultSet = statement.executeQuery()) {
                    if (resultSet.next()) {
                        mappings.add(new UUIDMapping(uuid, resultSet.getString("username")));
                    }
                }
            }
        } catch (final Exception e) {
            e.printStackTrace();
        }
        return mappings;
    }

    @Override @Nonnull public List<UUIDMapping> getUUIDs(@Nonnull List<String> usernames) {
        final List<UUIDMapping> mappings = new ArrayList<>(usernames.size());
        try (final PreparedStatement statement = getConnection()
            .prepareStatement("SELECT `uuid` FROM `usercache` WHERE `username` = ?")) {
            for (final String username : usernames) {
                statement.setString(1, username);
                try (final ResultSet resultSet = statement.executeQuery()) {
                    if (resultSet.next()) {
                        mappings.add(new UUIDMapping(UUID.fromString(resultSet.getString("uuid")),
                            username));
                    }
                }
            }
        } catch (final Exception e) {
            e.printStackTrace();
        }
        return mappings;
    }

    @Override public void accept(final List<UUIDMapping> uuidWrappers) {
        try (final PreparedStatement statement = getConnection()
            .prepareStatement("INSERT OR REPLACE INTO `usercache` (`uuid`, `username`) VALUES(?, ?)")) {
            for (final UUIDMapping mapping : uuidWrappers) {
                statement.setString(1, mapping.getUuid().toString());
                statement.setString(2, mapping.getUsername());
                statement.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Read the entire cache at once
     *
     * @return All read mappings
     */
    @Nonnull public List<UUIDMapping> getAll() {
        final List<UUIDMapping> mappings = new LinkedList<>();
        try (final PreparedStatement statement = getConnection().prepareStatement("SELECT * FROM `usercache`")) {
            try (final ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    mappings.add(new UUIDMapping(UUID.fromString(resultSet.getString("uuid")), resultSet.getString("username")));
                }
            }
        } catch (final Exception e) {
            e.printStackTrace();
        }
        return mappings;
    }


}
