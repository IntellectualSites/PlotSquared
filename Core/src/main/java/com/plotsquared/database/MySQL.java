package com.plotsquared.database;

import com.plotsquared.config.Storage;
import com.plotsquared.util.StringMan;

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
