/*
 * PlotSquared, a land and world management plugin for Minecraft.
 * Copyright (C) IntellectualSites <https://intellectualsites.com>
 * Copyright (C) IntellectualSites team and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.plotsquared.core.database;

import com.plotsquared.core.util.StringMan;

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
