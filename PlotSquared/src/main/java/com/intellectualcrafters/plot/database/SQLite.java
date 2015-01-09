////////////////////////////////////////////////////////////////////////////////////////////////////
// PlotSquared - A plot manager and world generator for the Bukkit API                             /
// Copyright (c) 2014 IntellectualSites/IntellectualCrafters                                       /
//                                                                                                 /
// This program is free software; you can redistribute it and/or modify                            /
// it under the terms of the GNU General Public License as published by                            /
// the Free Software Foundation; either version 3 of the License, or                               /
// (at your option) any later version.                                                             /
//                                                                                                 /
// This program is distributed in the hope that it will be useful,                                 /
// but WITHOUT ANY WARRANTY; without even the implied warranty of                                  /
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the                                   /
// GNU General Public License for more details.                                                    /
//                                                                                                 /
// You should have received a copy of the GNU General Public License                               /
// along with this program; if not, write to the Free Software Foundation,                         /
// Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301  USA                               /
//                                                                                                 /
// You can contact us via: support@intellectualsites.com                                           /
////////////////////////////////////////////////////////////////////////////////////////////////////

package com.intellectualcrafters.plot.database;

import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.sql.*;
import java.util.logging.Level;

/**
 * Connects to and uses a SQLite database
 *
 * @author Citymonstret
 * @author tips48
 */
public class SQLite extends Database {

    private final String dbLocation;
    private Connection connection;

    /**
     * Creates a new SQLite instance
     *
     * @param plugin     Plugin instance
     * @param dbLocation Location of the Database (Must end in .db)
     */
    public SQLite(final Plugin plugin, final String dbLocation) {
        super(plugin);
        this.dbLocation = dbLocation;
    }

    @Override
    public Connection openConnection() throws SQLException, ClassNotFoundException {
        if (checkConnection()) {
            return this.connection;
        }
        if (!this.plugin.getDataFolder().exists()) {
            this.plugin.getDataFolder().mkdirs();
        }
        final File file = new File(this.dbLocation);
        if (!(file.exists())) {
            try {
                file.createNewFile();
            } catch (final IOException e) {
                this.plugin.getLogger().log(Level.SEVERE, "Unable to create database!");
            }
        }
        Class.forName("org.sqlite.JDBC");
        this.connection = DriverManager.getConnection("jdbc:sqlite:" + this.dbLocation);
        return this.connection;
    }

    @Override
    public boolean checkConnection() throws SQLException {
        return (this.connection != null) && !this.connection.isClosed();
    }

    @Override
    public Connection getConnection() {
        return this.connection;
    }

    @Override
    public boolean closeConnection() throws SQLException {
        if (this.connection == null) {
            return false;
        }
        this.connection.close();
        return true;
    }

    @Override
    public ResultSet querySQL(final String query) throws SQLException, ClassNotFoundException {
        if (checkConnection()) {
            openConnection();
        }

        final Statement statement = this.connection.createStatement();

        return statement.executeQuery(query);
    }

    @Override
    public int updateSQL(final String query) throws SQLException, ClassNotFoundException {
        if (checkConnection()) {
            openConnection();
        }

        final Statement statement = this.connection.createStatement();

        return statement.executeUpdate(query);
    }
}
