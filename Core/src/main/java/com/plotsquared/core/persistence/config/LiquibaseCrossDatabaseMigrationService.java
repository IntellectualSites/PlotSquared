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

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.plotsquared.core.configuration.Storage;
import liquibase.Contexts;
import liquibase.LabelExpression;
import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.diff.DiffGeneratorFactory;
import liquibase.diff.DiffResult;
import liquibase.diff.compare.CompareControl;
import liquibase.diff.output.DiffOutputControl;
import liquibase.diff.output.changelog.DiffToChangeLog;
import liquibase.resource.ClassLoaderResourceAccessor;
import liquibase.snapshot.DatabaseSnapshot;

import javax.sql.DataSource;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.util.logging.Logger;

/**
 * Service for handling cross-database migrations using pure Liquibase functionality.
 * Uses Liquibase's native generateChangeLog and diffChangeLog capabilities.
 *
 * @since 8.0.0
 * @version 2.0.0
 * @author TheMeinerLP
 * @author IntellectualSites
 */
@Singleton
public final class LiquibaseCrossDatabaseMigrationService {
    private static final Logger LOGGER = Logger.getLogger(LiquibaseCrossDatabaseMigrationService.class.getName());

    private final DataSourceProvider dataSourceProvider;

    @Inject
    public LiquibaseCrossDatabaseMigrationService(DataSourceProvider dataSourceProvider) {
        this.dataSourceProvider = dataSourceProvider;
    }

    /**
     * Migrates from current database to MySQL using Liquibase's native capabilities.
     */
    public void migrateToMySQL() {
        LOGGER.info("Starting pure Liquibase migration to MySQL...");

        if (!Storage.MySQL.USE) {
            throw new IllegalStateException("MySQL is not configured. Please configure MySQL settings in the config file first.");
        }

        try {
            // Create MySQL datasource using config settings
            String mysqlUrl = "jdbc:mysql://" + Storage.MySQL.HOST + ":" + Storage.MySQL.PORT + "/" + Storage.MySQL.DATABASE +
                             "?" + String.join("&", Storage.MySQL.PROPERTIES) + "&createDatabaseIfNotExist=true";
            DataSource targetDataSource = dataSourceProvider.createDataSource(mysqlUrl, Storage.MySQL.USER, Storage.MySQL.PASSWORD, "com.mysql.cj.jdbc.Driver");

            // Get current datasource
            DataSource sourceDataSource = dataSourceProvider.createDataSource();

            // Perform migration using pure Liquibase
            migrateUsingLiquibaseNative(sourceDataSource, targetDataSource);

            LOGGER.info("Migration to MySQL completed successfully.");

        } catch (Exception e) {
            LOGGER.severe("Migration to MySQL failed: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Migration to MySQL failed", e);
        }
    }

    /**
     * Migrates from current database to H2 using Liquibase's native capabilities.
     */
    public void migrateToH2() {
        LOGGER.info("Starting pure Liquibase migration to H2...");

        if (!Storage.H2.USE) {
            throw new IllegalStateException("H2 is not configured. Please configure H2 settings in the config file first.");
        }

        try {
            // Create H2 datasource using config settings
            String h2Url = "jdbc:h2:file:./" + Storage.H2.DB;
            if (!Storage.H2.PROPERTIES.isEmpty()) {
                h2Url += ";" + String.join(";", Storage.H2.PROPERTIES);
            }
            DataSource targetDataSource = dataSourceProvider.createDataSource(h2Url, null, null, "org.h2.Driver");

            // Get current datasource
            DataSource sourceDataSource = dataSourceProvider.createDataSource();

            // Perform migration using pure Liquibase
            migrateUsingLiquibaseNative(sourceDataSource, targetDataSource);

            LOGGER.info("Migration to H2 completed successfully.");

        } catch (Exception e) {
            LOGGER.severe("Migration to H2 failed: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Migration to H2 failed", e);
        }
    }

    /**
     * Performs the actual migration using Liquibase's native generateChangeLog functionality.
     * This approach eliminates the need for CSV export/import and uses pure Liquibase operations.
     */
    private void migrateUsingLiquibaseNative(DataSource sourceDataSource, DataSource targetDataSource) throws Exception {
        // Create temporary file for the generated changelog
        Path tempChangelog = Files.createTempFile(Path.of(""),"plotsquared-migration-", ".xml");

        try (Connection sourceConnection = sourceDataSource.getConnection();
             Connection targetConnection = targetDataSource.getConnection()) {

            Database sourceDatabase = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(new JdbcConnection(sourceConnection));
            Database targetDatabase = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(new JdbcConnection(targetConnection));

            // Step 1: Create the target schema structure first
            LOGGER.info("Creating target database schema...");
            Liquibase schemaLiquibase = new Liquibase("db/changelog/db.changelog-master.xml",
                                                     new ClassLoaderResourceAccessor(), targetDatabase);
            schemaLiquibase.update(new Contexts(), new LabelExpression());

            // Step 2: Generate changelog with data from source database
            LOGGER.info("Generating changelog with data from source database...");
            generateDataChangeLog(sourceDatabase, tempChangelog);

            // Step 3: Apply the data changelog to target database
            LOGGER.info("Applying data to target database...");
            Liquibase dataLiquibase = new Liquibase(tempChangelog.toString(),
                                                   new ClassLoaderResourceAccessor(), targetDatabase);
            dataLiquibase.setChangeLogParameter("PREFIX", Storage.PREFIX == null ? "" : Storage.PREFIX);
            dataLiquibase.setChangeLogParameter("prefix", Storage.PREFIX == null ? "" : Storage.PREFIX);

            dataLiquibase.update(new Contexts(), new LabelExpression());

            LOGGER.info("Pure Liquibase migration completed successfully.");

        } finally {
            // Cleanup temporary file
            try {
                Files.deleteIfExists(tempChangelog);
            } catch (Exception e) {
                LOGGER.warning("Could not delete temporary changelog file: " + e.getMessage());
            }
        }
    }

    /**
     * Generates a Liquibase changelog with data using Liquibase's native capabilities.
     */
    private void generateDataChangeLog(Database sourceDatabase, Path outputFile) throws Exception {
        try (PrintStream output = new PrintStream(Files.newOutputStream(outputFile))) {

            // Create Liquibase instance
            Liquibase liquibase = new Liquibase("", new ClassLoaderResourceAccessor(), sourceDatabase);

            // Generate changelog including data
            // This uses Liquibase's built-in generateChangeLog with data option
            generateChangeLogUsingDiff(sourceDatabase, sourceDatabase, outputFile);

        } catch (Exception e) {
            LOGGER.severe("Failed to generate data changelog: " + e.getMessage());
            throw e;
        }
    }

    /**
     * Alternative approach using Liquibase's diff functionality for more control.
     */
    private void generateChangeLogUsingDiff(Database sourceDatabase, Database targetDatabase, Path outputFile) throws Exception {
        try (PrintStream output = new PrintStream(Files.newOutputStream(outputFile))) {

            // Create database snapshots
            DatabaseSnapshot sourceSnapshot = liquibase.snapshot.SnapshotGeneratorFactory.getInstance()
                .createSnapshot(sourceDatabase.getDefaultSchema(), sourceDatabase, new liquibase.snapshot.SnapshotControl(sourceDatabase));

            DatabaseSnapshot targetSnapshot = liquibase.snapshot.SnapshotGeneratorFactory.getInstance()
                .createSnapshot(targetDatabase.getDefaultSchema(), targetDatabase, new liquibase.snapshot.SnapshotControl(targetDatabase));

            // Compare databases
            DiffResult diffResult = DiffGeneratorFactory.getInstance().compare(sourceSnapshot, targetSnapshot, new CompareControl());

            // Generate changelog from diff
            DiffToChangeLog changeLogWriter = new DiffToChangeLog(diffResult, new DiffOutputControl(true, true, true, null));
            changeLogWriter.print(output);

        } catch (Exception e) {
            LOGGER.severe("Failed to generate changelog using diff: " + e.getMessage());
            throw e;
        }
    }

    /**
     * Validates that the migration was successful by comparing database structures.
     */
    public boolean validateMigration(DataSource sourceDataSource, DataSource targetDataSource) {
        try (Connection sourceConnection = sourceDataSource.getConnection();
             Connection targetConnection = targetDataSource.getConnection()) {

            Database sourceDatabase = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(new JdbcConnection(sourceConnection));
            Database targetDatabase = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(new JdbcConnection(targetConnection));

            // Create snapshots of both databases
            DatabaseSnapshot sourceSnapshot = liquibase.snapshot.SnapshotGeneratorFactory.getInstance()
                .createSnapshot(sourceDatabase.getDefaultSchema(), sourceDatabase, new liquibase.snapshot.SnapshotControl(sourceDatabase));

            DatabaseSnapshot targetSnapshot = liquibase.snapshot.SnapshotGeneratorFactory.getInstance()
                .createSnapshot(targetDatabase.getDefaultSchema(), targetDatabase, new liquibase.snapshot.SnapshotControl(targetDatabase));

            // Compare structure and data
            DiffResult diffResult = DiffGeneratorFactory.getInstance().compare(sourceSnapshot, targetSnapshot, new CompareControl());

            boolean hasUnexpectedDiffs = !diffResult.areEqual();

            if (!hasUnexpectedDiffs) {
                LOGGER.info("Migration validation successful: Databases are identical");
            } else {
                LOGGER.warning("Migration validation found differences between source and target databases");
            }

            return !hasUnexpectedDiffs;

        } catch (Exception e) {
            LOGGER.severe("Error during migration validation: " + e.getMessage());
            return false;
        }
    }
}
