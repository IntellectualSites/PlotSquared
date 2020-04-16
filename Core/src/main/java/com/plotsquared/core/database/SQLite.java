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
package com.plotsquared.core.database;

import com.plotsquared.core.PlotSquared;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Connects to and uses a SQLite database.
 */
public class SQLite extends Database {

    private final String dbLocation;
    private Connection connection;

    /**
     * Creates a new SQLite instance
     *
     * @param dbLocation Location of the Database (Must end in .db)
     */
    public SQLite(File dbLocation) {
        this.dbLocation = dbLocation.getAbsolutePath();
    }

    @Override public Connection openConnection() throws SQLException, ClassNotFoundException {
        if (checkConnection()) {
            return this.connection;
        }
        if (!PlotSquared.get().IMP.getDirectory().exists()) {
            PlotSquared.get().IMP.getDirectory().mkdirs();
        }
        File file = new File(this.dbLocation);
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException ignored) {
                PlotSquared.debug("&cUnable to create database!");
            }
        }
        Class.forName("org.sqlite.JDBC");
        this.connection = DriverManager.getConnection("jdbc:sqlite:" + this.dbLocation);
        return this.connection;
    }

    @Override public boolean checkConnection() throws SQLException {
        return (this.connection != null) && !this.connection.isClosed();
    }

    @Override public Connection getConnection() {
        return this.connection;
    }

    @Override public boolean closeConnection() throws SQLException {
        if (this.connection == null) {
            return false;
        }
        this.connection.close();
        this.connection = null;
        return true;
    }

    @Override public ResultSet querySQL(String query) throws SQLException, ClassNotFoundException {
        if (checkConnection()) {
            openConnection();
        }
        try (Statement statement = this.connection.createStatement()) {
            return statement.executeQuery(query);
        }
    }

    @Override public int updateSQL(String query) throws SQLException, ClassNotFoundException {
        if (checkConnection()) {
            openConnection();
        }
        try (Statement statement = this.connection.createStatement()) {
            return statement.executeUpdate(query);
        }
    }

    @Override public Connection forceConnection() throws SQLException, ClassNotFoundException {
        Class.forName("org.sqlite.JDBC");
        this.connection = DriverManager.getConnection("jdbc:sqlite:" + this.dbLocation);
        return this.connection;
    }
}
