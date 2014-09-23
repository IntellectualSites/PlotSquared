package com.intellectualcrafters.plot.database;

import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.sql.*;
import java.util.logging.Level;

/**
 * Connects to and uses a SQLite database
 * @author Citymonstret
 * @author tips48
 */
public class SQLite extends Database {

    private Connection connection;
    private final String dbLocation;
    /**
     * Creates a new SQLite instance
     *
     * @param plugin
     * Plugin instance
     * @param dbLocation
     * Location of the Database (Must end in .db)
     */
    public SQLite(Plugin plugin, String dbLocation) {
        super(plugin);
        this.dbLocation = dbLocation;
    }
    @Override
    public Connection openConnection() throws SQLException,
            ClassNotFoundException {
        if (checkConnection()) {
            return connection;
        }
        if (!plugin.getDataFolder().exists()) {
            plugin.getDataFolder().mkdirs();
        }
        File file = new File(plugin.getDataFolder(), dbLocation);
        if (!(file.exists())) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().log(Level.SEVERE,
                        "Unable to create database!");
            }
        }
        Class.forName("org.sqlite.JDBC");
        connection = DriverManager
                .getConnection("jdbc:sqlite:"
                        + plugin.getDataFolder().toPath().toString() + "/"
                        + dbLocation);
        return connection;
    }

    @Override
    public boolean checkConnection() throws SQLException {
        return connection != null && !connection.isClosed();
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
        return true;
    }

    @Override
    public ResultSet querySQL(String query) throws SQLException,
            ClassNotFoundException {
        if (checkConnection()) {
            openConnection();
        }

        Statement statement = connection.createStatement();

        ResultSet result = statement.executeQuery(query);

        return result;
    }

    @Override
    public int updateSQL(String query) throws SQLException,
            ClassNotFoundException {
        if (checkConnection()) {
            openConnection();
        }

        Statement statement = connection.createStatement();

        int result = statement.executeUpdate(query);

        return result;
    }
}