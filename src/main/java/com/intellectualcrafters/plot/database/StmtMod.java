package com.intellectualcrafters.plot.database;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import com.intellectualcrafters.plot.util.StringMan;

public abstract class StmtMod<T> {
    public abstract String getCreateMySQL(final int size);
    
    public String getCreateMySQL(final int size, final String query, final int params) {
        final StringBuilder statement = new StringBuilder(query);
        for (int i = 0; i < (size - 1); i++) {
            statement.append("(" + StringMan.repeat(",(?)", params).substring(1) + "),");
        }
        statement.append("(" + StringMan.repeat(",(?)", params).substring(1) + ")");
        return statement.toString();
    }
    
    public String getCreateSQLite(final int size, final String query, final int params) {
        final StringBuilder statement = new StringBuilder(query);
        final String modParams = StringMan.repeat(",?", params).substring(1);
        for (int i = 0; i < (size - 1); i++) {
            statement.append("UNION SELECT " + modParams + " ");
        }
        return statement.toString();
    }
    
    public abstract String getCreateSQLite(final int size);
    
    public abstract String getCreateSQL();
    
    public abstract void setMySQL(final PreparedStatement stmt, final int i, final T obj) throws SQLException;
    
    public abstract void setSQLite(final PreparedStatement stmt, final int i, final T obj) throws SQLException;
    
    public abstract void setSQL(final PreparedStatement stmt, final T obj) throws SQLException;
}
