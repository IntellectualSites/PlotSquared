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
public class MySQL extends Database
{
    private final String user;
    private final String database;
    private final String password;
    private final String port;
    private final String hostname;
    private Connection connection;

    /**
     * Creates a new MySQL instance
     *
     * @param hostname Name of the host
     * @param port     Port number
     * @param database Database name
     * @param username Username
     * @param password Password
     */
    public MySQL(final String hostname, final String port, final String database, final String username, final String password)
    {
        this.hostname = hostname;
        this.port = port;
        this.database = database;
        user = username;
        this.password = password;
        connection = null;
    }

    @Override
    public Connection forceConnection() throws SQLException, ClassNotFoundException
    {
        Class.forName("com.mysql.jdbc.Driver");
        connection = DriverManager.getConnection("jdbc:mysql://" + hostname + ":" + port + "/" + database, user, password);
        return connection;
    }

    @Override
    public Connection openConnection() throws SQLException, ClassNotFoundException
    {
        if (checkConnection()) { return connection; }
        Class.forName("com.mysql.jdbc.Driver");
        connection = DriverManager.getConnection("jdbc:mysql://" + hostname + ":" + port + "/" + database, user, password);
        return connection;
    }

    @Override
    public boolean checkConnection() throws SQLException
    {
        return (connection != null) && !connection.isClosed();
    }

    @Override
    public Connection getConnection()
    {
        return connection;
    }

    @Override
    public boolean closeConnection() throws SQLException
    {
        if (connection == null) { return false; }
        connection.close();
        connection = null;
        return true;
    }

    @Override
    public ResultSet querySQL(final String query) throws SQLException, ClassNotFoundException
    {
        if (checkConnection())
        {
            openConnection();
        }
        final Statement statement = connection.createStatement();
        return statement.executeQuery(query);
    }

    @Override
    public int updateSQL(final String query) throws SQLException, ClassNotFoundException
    {
        if (checkConnection())
        {
            openConnection();
        }
        final Statement statement = connection.createStatement();
        return statement.executeUpdate(query);
    }
}
