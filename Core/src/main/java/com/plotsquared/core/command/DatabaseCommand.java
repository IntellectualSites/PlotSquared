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
package com.plotsquared.core.command;

import com.google.inject.Inject;
import com.plotsquared.core.configuration.Storage;
import com.plotsquared.core.configuration.caption.TranslatableCaption;
import com.plotsquared.core.persistence.config.DatabaseMigrationService;
import com.plotsquared.core.persistence.config.DataSourceProvider;
import com.plotsquared.core.player.PlotPlayer;
import com.plotsquared.core.util.task.RunnableVal;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.concurrent.CompletableFuture;

/**
 * Command for migrating databases between SQLite and MySQL, and from v7 to v8 format.
 *
 * @since 8.0.0
 * @version 1.0.0
 * @author TheMeinerLP
 * @author IntellectualSites
 */
@CommandDeclaration(
        command = "database",
        aliases = {"db", "migrate"},
        category = CommandCategory.ADMINISTRATION,
        requiredType = RequiredType.CONSOLE,
        permission = "plots.admin.database"
)
public class DatabaseCommand extends SubCommand {

    private final DatabaseMigrationService migrationService;
    private final DataSourceProvider dataSourceProvider;

    @Inject
    public DatabaseCommand(DatabaseMigrationService migrationService, DataSourceProvider dataSourceProvider) {
        this.migrationService = migrationService;
        this.dataSourceProvider = dataSourceProvider;
    }

    @Override
    public boolean onCommand(PlotPlayer<?> player, String[] args) {
        if (args.length == 0) {
            player.sendMessage(TranslatableCaption.of("database.usage"));
            return false;
        }

        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "migrate-to-mysql":
                if (args.length < 5) {
                    player.sendMessage(TranslatableCaption.of("database.migrate_mysql_usage"));
                    return false;
                }
                migrateToMySQL(player, args[1], args[2], args[3], args[4]);
                break;

            case "migrate-to-sqlite":
                if (args.length < 2) {
                    player.sendMessage(TranslatableCaption.of("database.migrate_sqlite_usage"));
                    return false;
                }
                migrateToSQLite(player, args[1]);
                break;

            case "migrate-from-v7":
                migrateFromV7(player);
                break;

            case "status":
                showDatabaseStatus(player);
                break;

            default:
                player.sendMessage(TranslatableCaption.of("database.unknown_subcommand"));
                return false;
        }

        return true;
    }

    private void migrateToMySQL(PlotPlayer<?> player, String host, String port, String database, String username) {
        player.sendMessage(TranslatableCaption.of("database.migration_started"));

        CompletableFuture.runAsync(() -> {
            try {
                // Create target MySQL DataSource
                String mysqlUrl = "jdbc:mysql://" + host + ":" + port + "/" + database + "?useSSL=false&allowPublicKeyRetrieval=true";
                DataSource targetDataSource = dataSourceProvider.createDataSource(mysqlUrl, username,
                    System.getProperty("mysql.password", ""), "com.mysql.cj.jdbc.Driver");

                // Get current SQLite DataSource
                DataSource sourceDataSource = dataSourceProvider.createDataSource();

                try (Connection sourceConn = sourceDataSource.getConnection();
                     Connection targetConn = targetDataSource.getConnection()) {

                    migrationService.migrateBetweenDatabaseTypes(sourceConn, targetConn);

                    player.sendMessage(TranslatableCaption.of("database.migration_completed"));
                }

            } catch (Exception e) {
                player.sendMessage(TranslatableCaption.of("database.migration_failed"));
                e.printStackTrace();
            }
        });
    }

    private void migrateToSQLite(PlotPlayer<?> player, String sqliteFile) {
        player.sendMessage(TranslatableCaption.of("database.migration_started"));

        CompletableFuture.runAsync(() -> {
            try {
                // Create target SQLite DataSource
                String sqliteUrl = "jdbc:sqlite:" + sqliteFile + ".db";
                DataSource targetDataSource = dataSourceProvider.createDataSource(sqliteUrl, null, null, "org.sqlite.JDBC");

                // Get current MySQL DataSource
                DataSource sourceDataSource = dataSourceProvider.createDataSource();

                try (Connection sourceConn = sourceDataSource.getConnection();
                     Connection targetConn = targetDataSource.getConnection()) {

                    migrationService.migrateBetweenDatabaseTypes(sourceConn, targetConn);

                    player.sendMessage(TranslatableCaption.of("database.migration_completed"));
                }

            } catch (Exception e) {
                player.sendMessage(TranslatableCaption.of("database.migration_failed"));
                e.printStackTrace();
            }
        });
    }

    private void migrateFromV7(PlotPlayer<?> player) {
        player.sendMessage(TranslatableCaption.of("database.v7_migration_started"));

        CompletableFuture.runAsync(() -> {
            try {
                DataSource dataSource = dataSourceProvider.createDataSource();

                try (Connection connection = dataSource.getConnection()) {
                    if (migrationService.isV7Database(connection)) {
                        migrationService.migrateFromV7(connection);
                        player.sendMessage(TranslatableCaption.of("database.v7_migration_completed"));
                    } else {
                        player.sendMessage(TranslatableCaption.of("database.not_v7_database"));
                    }
                }

            } catch (Exception e) {
                player.sendMessage(TranslatableCaption.of("database.v7_migration_failed"));
                e.printStackTrace();
            }
        });
    }

    private void showDatabaseStatus(PlotPlayer<?> player) {
        try {
            DataSource dataSource = dataSourceProvider.createDataSource();

            try (Connection connection = dataSource.getConnection()) {
                String databaseType = Storage.MySQL.USE ? "MySQL" : "SQLite";
                boolean isV7 = migrationService.isV7Database(connection);

                player.sendMessage(TranslatableCaption.of("database.status_header"));
                player.sendMessage(TranslatableCaption.of("database.status_type", databaseType));
                player.sendMessage(TranslatableCaption.of("database.status_version", isV7 ? "v7" : "v8"));
                player.sendMessage(TranslatableCaption.of("database.status_prefix", Storage.PREFIX == null ? "none" : Storage.PREFIX));
            }

        } catch (Exception e) {
            player.sendMessage(TranslatableCaption.of("database.status_error"));
            e.printStackTrace();
        }
    }
}
