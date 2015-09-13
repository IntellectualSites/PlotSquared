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

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import com.intellectualcrafters.plot.PS;

/**
 * Connects to and uses a SQLite database
 *
 * @author tips48
 */
public class SQLite extends Database {
    private final String dbLocation;
    private Connection connection;
    
    /**
     * Creates a new SQLite instance
     *
     * @param dbLocation Location of the Database (Must end in .db)
     */
    public SQLite(final String dbLocation) {
        this.dbLocation = dbLocation;
    }
    
    @Override
    public Connection openConnection() throws SQLException, ClassNotFoundException {
        if (checkConnection()) {
            return connection;
        }
        if (!PS.get().IMP.getDirectory().exists()) {
            PS.get().IMP.getDirectory().mkdirs();
        }
        final File file = new File(dbLocation);
        if (!(file.exists())) {
            try {
                file.createNewFile();
            } catch (final IOException e) {
                PS.debug("&cUnable to create database!");
            }
        }
        Class.forName("org.sqlite.JDBC");
        connection = DriverManager.getConnection("jdbc:sqlite:" + dbLocation);
        return connection;
    }
    
    @Override
    public boolean checkConnection() throws SQLException {
        return (connection != null) && !connection.isClosed();
    }
    
    @Override
    public Connection getConnection() {
        return connection;
    }
    
    @Override
    public boolean closeConnection() throws SQLException {
        if (connection == null) {
            return false;
        }
        connection.close();
        connection = null;
        return true;
    }
    
    @Override
    public ResultSet querySQL(final String query) throws SQLException, ClassNotFoundException {
        if (checkConnection()) {
            openConnection();
        }
        final Statement statement = connection.createStatement();
        return statement.executeQuery(query);
    }
    
    @Override
    public int updateSQL(final String query) throws SQLException, ClassNotFoundException {
        if (checkConnection()) {
            openConnection();
        }
        final Statement statement = connection.createStatement();
        return statement.executeUpdate(query);
    }
    
    @Override
    public Connection forceConnection() throws SQLException, ClassNotFoundException {
        Class.forName("org.sqlite.JDBC");
        connection = DriverManager.getConnection("jdbc:sqlite:" + dbLocation);
        return connection;
    }
}
