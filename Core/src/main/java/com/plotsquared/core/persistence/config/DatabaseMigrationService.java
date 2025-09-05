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
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Service for handling database migrations between different PlotSquared versions
 * and database systems (SQLite <-> MySQL).
 *
 * Note: v7 to v8 migration is now handled completely by Liquibase changesets.
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
     * This is now mainly used for informational purposes since Liquibase handles the migration.
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
     * Migrates data from one database type to another (e.g., SQLite to MySQL).
     * This is the main functionality remaining in this service since schema migration
     * is now handled by Liquibase.
     */
    public void migrateBetweenDatabaseTypes(Connection sourceConnection, Connection targetConnection) throws SQLException {
        LOGGER.info("Starting cross-database migration...");

        try {
            targetConnection.setAutoCommit(false);

            String prefix = Storage.PREFIX == null ? "" : Storage.PREFIX;

            // Get all tables from source database
            List<String> tablesToMigrate = getExistingTables(sourceConnection, prefix);

            for (String tableName : tablesToMigrate) {
                migrateTable(sourceConnection, targetConnection, tableName);
            }

            targetConnection.commit();
            LOGGER.info("Cross-database migration completed successfully.");

        } catch (SQLException e) {
            targetConnection.rollback();
            LOGGER.severe("Cross-database migration failed: " + e.getMessage());
            throw e;
        } finally {
            targetConnection.setAutoCommit(true);
        }
    }

    private List<String> getExistingTables(Connection connection, String prefix) throws SQLException {
        List<String> tables = new ArrayList<>();
        DatabaseMetaData metaData = connection.getMetaData();

        try (ResultSet rs = metaData.getTables(null, null, prefix + "%", new String[]{"TABLE"})) {
            while (rs.next()) {
                String tableName = rs.getString("TABLE_NAME");
                // Skip backup and temporary tables, and Liquibase tables
                if (!tableName.contains("_backup_") && !tableName.contains("_v7_temp") &&
                    !tableName.contains("DATABASECHANGELOG")) {
                    tables.add(tableName);
                }
            }
        }

        return tables;
    }

    private void migrateTable(Connection source, Connection target, String tableName) throws SQLException {
        LOGGER.info("Migrating table: " + tableName);

        // First, clear the target table
        try (Statement stmt = target.createStatement()) {
            stmt.execute("DELETE FROM " + tableName);
        }

        // Get all data from source table
        try (Statement sourceStmt = source.createStatement();
             ResultSet rs = sourceStmt.executeQuery("SELECT * FROM " + tableName)) {

            if (!rs.next()) {
                LOGGER.info("Table " + tableName + " is empty, skipping");
                return;
            }

            // Get column metadata
            int columnCount = rs.getMetaData().getColumnCount();
            List<String> columnNames = new ArrayList<>();
            for (int i = 1; i <= columnCount; i++) {
                columnNames.add(rs.getMetaData().getColumnName(i));
            }

            // Build insert statement
            StringBuilder insertQuery = new StringBuilder("INSERT INTO " + tableName + " (");
            insertQuery.append(String.join(", ", columnNames));
            insertQuery.append(") VALUES (");
            for (int i = 0; i < columnCount; i++) {
                if (i > 0) insertQuery.append(", ");
                insertQuery.append("?");
            }
            insertQuery.append(")");

            try (PreparedStatement insertStmt = target.prepareStatement(insertQuery.toString())) {
                rs.beforeFirst();

                int count = 0;
                while (rs.next()) {
                    for (int i = 1; i <= columnCount; i++) {
                        insertStmt.setObject(i, rs.getObject(i));
                    }
                    insertStmt.addBatch();

                    if (++count % 1000 == 0) {
                        insertStmt.executeBatch();
                    }
                }

                insertStmt.executeBatch();
                LOGGER.info("Migrated " + count + " rows from table " + tableName);
            }
        }
    }

    private boolean tableExists(Connection connection, String tableName) throws SQLException {
        DatabaseMetaData metaData = connection.getMetaData();
        try (ResultSet tables = metaData.getTables(null, null, tableName, new String[]{"TABLE"})) {
            return tables.next();
        }
    }
}
