package com.plotsquared.core.database;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Abstract Database class, serves as a base for any connection method (MySQL, SQLite, etc.)
 *
 * @author -_Husky_-
 * @author tips48
 */
public abstract class Database {

    public abstract Connection forceConnection() throws SQLException, ClassNotFoundException;

    /**
     * Opens a connection with the database.
     *
     * @return Opened connection
     * @throws SQLException           if the connection can not be opened
     * @throws ClassNotFoundException if the driver cannot be found
     */
    public abstract Connection openConnection() throws SQLException, ClassNotFoundException;

    /**
     * Checks if a connection is open with the database.
     *
     * @return true if the connection is open
     * @throws SQLException if the connection cannot be checked
     */
    public abstract boolean checkConnection() throws SQLException;

    /**
     * Gets the connection with the database.
     *
     * @return Connection with the database, null if none
     */
    public abstract Connection getConnection();

    /**
     * Closes the connection with the database.
     *
     * @return true if successful
     * @throws SQLException if the connection cannot be closed
     */
    public abstract boolean closeConnection() throws SQLException;

    /**
     * Executes a SQL Query.
     * If the connection is closed, it will be opened.
     *
     * @param query Query to be run
     * @return the results of the query
     * @throws SQLException           If the query cannot be executed
     * @throws ClassNotFoundException If the driver cannot be found; see {@link #openConnection()}
     */
    public abstract ResultSet querySQL(String query) throws SQLException, ClassNotFoundException;

    /**
     * Executes an Update SQL Query.
     * See {@link Statement#executeUpdate(String)}.
     * If the connection is closed, it will be opened.
     *
     * @param query Query to be run
     * @return Result Code, see {@link Statement#executeUpdate(String)}
     * @throws SQLException           If the query cannot be executed
     * @throws ClassNotFoundException If the driver cannot be found; see {@link #openConnection()}
     */
    public abstract int updateSQL(String query) throws SQLException, ClassNotFoundException;
}
