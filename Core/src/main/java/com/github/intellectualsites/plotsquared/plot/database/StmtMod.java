package com.github.intellectualsites.plotsquared.plot.database;

import com.github.intellectualsites.plotsquared.plot.util.block.BlockUtil;

import com.github.intellectualsites.plotsquared.plot.util.StringMan;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public abstract class StmtMod<T> {

    public abstract String getCreateMySQL(int size);

    public String getCreateMySQL(int size, String query, int params) {
        StringBuilder statement = new StringBuilder(query);
        for (int i = 0; i < size - 1; i++) {
            statement.append('(').append(StringMan.repeat(",?", params).substring(1)).append("),");
        }
        statement.append('(').append(StringMan.repeat(",?", params).substring(1)).append(')');
        return statement.toString();
    }

    public String getCreateSQLite(int size, String query, int params) {
        String modParams = StringMan.repeat(",?", params).substring(1);
        return IntStream.range(0, size - 1).mapToObj(i -> "UNION SELECT " + modParams + ' ')
            .collect(Collectors.joining("", query, ""));
    }

    public abstract String getCreateSQLite(int size);

    public abstract String getCreateSQL();

    public abstract void setMySQL(PreparedStatement stmt, int i, T obj) throws SQLException;

    public abstract void setSQLite(PreparedStatement stmt, int i, T obj) throws SQLException;

    public abstract void setSQL(PreparedStatement stmt, T obj) throws SQLException;
}
