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
    public InstallationState getDatabaseVersion(Connection connection) throws SQLException {
        if (!isV7Database(connection)) {
            String prefix = Storage.PREFIX == null ? "" : Storage.PREFIX;
            try (ResultSet tables = connection.getMetaData().getTables(null, null, prefix + "plot", new String[]{"TABLE"})) {
                if (!tables.next()) {
                    return InstallationState.FRESH_INSTALLATION;
                }
            }
            return InstallationState.NO_MIGRATION_NEEDED;
        }
        return InstallationState.UPGRADE_FROM_V7;
    }
}
