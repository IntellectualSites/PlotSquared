package com.intellectualcrafters.plot.database;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;

import org.bukkit.plugin.Plugin;

/**
 * Connects to and uses a SQLite database
 *
 * @author Citymonstret
 * @author tips48
 */
public class SQLite extends Database {

    private Connection   connection;
    private final String dbLocation;

    /**
     * Creates a new SQLite instance
     *
     * @param plugin
     *            Plugin instance
     * @param dbLocation
     *            Location of the Database (Must end in .db)
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
        final File file = new File(this.plugin.getDataFolder(), this.dbLocation);
        if (!(file.exists())) {
            try {
                file.createNewFile();
            }
            catch (final IOException e) {
                this.plugin.getLogger().log(Level.SEVERE, "Unable to create database!");
            }
        }
        Class.forName("org.sqlite.JDBC");
        this.connection = DriverManager.getConnection("jdbc:sqlite:" + this.plugin.getDataFolder().toPath().toString() + "/" + this.dbLocation);
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

        final ResultSet result = statement.executeQuery(query);

        return result;
    }

    @Override
    public int updateSQL(final String query) throws SQLException, ClassNotFoundException {
        if (checkConnection()) {
            openConnection();
        }

        final Statement statement = this.connection.createStatement();

        final int result = statement.executeUpdate(query);

        return result;
    }
}
