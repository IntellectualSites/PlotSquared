package com.intellectualcrafters.plot.database;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.bukkit.plugin.Plugin;

/**
* Abstract Database class, serves as a base for any connection method (MySQL,
* SQLite, etc.)
*
* @author -_Husky_-
* @author tips48
*/
public abstract class Database {

/**
* Plugin instance, use for plugin.getDataFolder()
*/
protected Plugin plugin;

/**
* Creates a new Database
*
* @param plugin
* Plugin instance
*/
protected Database(Plugin plugin) {
this.plugin = plugin;
}

/**
* Opens a connection with the database
*
* @return Opened connection
* @throws SQLException
* if the connection can not be opened
* @throws ClassNotFoundException
* if the driver cannot be found
*/
public abstract Connection openConnection() throws SQLException,
ClassNotFoundException;

/**
* Checks if a connection is open with the database
*
* @return true if the connection is open
* @throws SQLException
* if the connection cannot be checked
*/
public abstract boolean checkConnection() throws SQLException;

/**
* Gets the connection with the database
*
* @return Connection with the database, null if none
*/
public abstract Connection getConnection();

/**
* Closes the connection with the database
*
* @return true if successful
* @throws SQLException
* if the connection cannot be closed
*/
public abstract boolean closeConnection() throws SQLException;

/**
* Executes a SQL Query<br>
*
* If the connection is closed, it will be opened
*
* @param query
* Query to be run
* @return the results of the query
* @throws SQLException
* If the query cannot be executed
* @throws ClassNotFoundException
* If the driver cannot be found; see {@link #openConnection()}
*/
public abstract ResultSet querySQL(String query) throws SQLException,
ClassNotFoundException;

/**
* Executes an Update SQL Query<br>
* See {@link java.sql.Statement#executeUpdate(String)}<br>
* If the connection is closed, it will be opened
*
* @param query
* Query to be run
* @return Result Code, see {@link java.sql.Statement#executeUpdate(String)}
* @throws SQLException
* If the query cannot be executed
* @throws ClassNotFoundException
* If the driver cannot be found; see {@link #openConnection()}
*/
public abstract int updateSQL(String query) throws SQLException,
ClassNotFoundException;
}