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
package com.github.intellectualsites.plotsquared.plot.database;

import com.github.intellectualsites.plotsquared.plot.config.Storage;
import com.github.intellectualsites.plotsquared.plot.util.StringMan;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Connects to and uses a MySQL database
 *
 * @author -_Husky_-
 * @author tips48
 */
public class MySQL extends Database {

    private final String user;
    private final String database;
    private final String password;
    private final String port;
    private final String hostname;
    private Connection connection;

    /**
     * Creates a new MySQL instance.
     *
     * @param hostname Name of the host
     * @param port     Port number
     * @param database Database name
     * @param username Username
     * @param password Password
     */
    public MySQL(String hostname, String port, String database, String username, String password) {
        this.hostname = hostname;
        this.port = port;
        this.database = database;
        this.user = username;
        this.password = password;
        this.connection = null;
    }

    @Override public Connection forceConnection() throws SQLException {
        this.connection = DriverManager.getConnection(
            "jdbc:mysql://" + this.hostname + ':' + this.port + '/' + this.database + "?"
                + StringMan.join(Storage.MySQL.PROPERTIES, "&"), this.user, this.password);
        return this.connection;
    }

    @Override public Connection openConnection() throws SQLException {
        if (checkConnection()) {
            return this.connection;
        }
        return forceConnection();
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

    @Override public ResultSet querySQL(String query) throws SQLException {
        if (checkConnection()) {
            openConnection();
        }
        try (Statement statement = this.connection.createStatement()) {
            return statement.executeQuery(query);
        }
    }

    @Override public int updateSQL(String query) throws SQLException {
        if (checkConnection()) {
            openConnection();
        }
        try (Statement statement = this.connection.createStatement()) {
            return statement.executeUpdate(query);
        }
    }
}
