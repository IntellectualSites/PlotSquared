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
package com.plotsquared.core.persistence.config;

import com.google.inject.Singleton;
import com.plotsquared.core.configuration.Storage;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Logger;

/**
 * Service for providing database information and status.
 * All database migrations are now handled completely by Liquibase.
 *
 * @since 8.0.0
 * @version 1.0.0
 * @author TheMeinerLP
 * @author IntellectualSites
 */
@Singleton
public final class DatabaseMigrationService {
    private static final Logger LOGGER = Logger.getLogger(DatabaseMigrationService.class.getName());

    /**
     * Checks if the current database is using the v7 schema format.
     * This is used for informational purposes since Liquibase handles the migration automatically.
     */
    public boolean isV7Database(Connection connection) throws SQLException {
        String prefix = Storage.PREFIX == null ? "" : Storage.PREFIX;
        DatabaseMetaData metaData = connection.getMetaData();

        try (ResultSet tables = metaData.getTables(null, null, prefix + "plot", new String[]{"TABLE"})) {
            if (!tables.next()) {
                // No plot table exists, assume new installation
                return false;
            }
        }

        // Check if the plot table has v7 structure (no auto-increment ID column)
        try (ResultSet columns = metaData.getColumns(null, null, prefix + "plot", null)) {
            boolean hasIdColumn = false;
            boolean hasV7Columns = false;

            while (columns.next()) {
                String columnName = columns.getString("COLUMN_NAME");
                if ("id".equalsIgnoreCase(columnName)) {
                    hasIdColumn = true;
                }
                if ("plot_id_x".equalsIgnoreCase(columnName) || "plot_id_z".equalsIgnoreCase(columnName)) {
                    hasV7Columns = true;
                }
            }

            // v7 has plot_id_x/plot_id_z but no auto-increment ID column
            // v8 has both the coordinates AND an auto-increment ID column
            return hasV7Columns && !hasIdColumn;
        }
    }

    /**
     * Gets the current database version information.
     */
    public String getDatabaseVersion(Connection connection) throws SQLException {
        if (isV7Database(connection)) {
            return "v7 (will be migrated to v8 by Liquibase)";
        } else {
            String prefix = Storage.PREFIX == null ? "" : Storage.PREFIX;
            try (ResultSet tables = connection.getMetaData().getTables(null, null, prefix + "plot", new String[]{"TABLE"})) {
                if (tables.next()) {
                    return "v8";
                } else {
                    return "new installation";
                }
            }
        }
    }

    /**
     * Gets database statistics for informational purposes.
     */
    public String getDatabaseStatistics(Connection connection) throws SQLException {
        String prefix = Storage.PREFIX == null ? "" : Storage.PREFIX;
        StringBuilder stats = new StringBuilder();

        try {
            stats.append("Database Statistics:\n");
            stats.append("- Version: ").append(getDatabaseVersion(connection)).append("\n");
            stats.append("- Prefix: ").append(prefix.isEmpty() ? "none" : prefix).append("\n");

            // Count plots if table exists
            try (ResultSet tables = connection.getMetaData().getTables(null, null, prefix + "plot", new String[]{"TABLE"})) {
                if (tables.next()) {
                    try (var stmt = connection.createStatement();
                         var rs = stmt.executeQuery("SELECT COUNT(*) FROM " + prefix + "plot")) {
                        if (rs.next()) {
                            stats.append("- Total plots: ").append(rs.getInt(1)).append("\n");
                        }
                    }
                }
            }

        } catch (SQLException e) {
            stats.append("- Error retrieving statistics: ").append(e.getMessage());
        }

        return stats.toString();
    }

    /**
     * Checks if a table exists in the database.
     */
    public boolean tableExists(Connection connection, String tableName) throws SQLException {
        DatabaseMetaData metaData = connection.getMetaData();
        try (ResultSet tables = metaData.getTables(null, null, tableName, new String[]{"TABLE"})) {
            return tables.next();
        }
    }
}
