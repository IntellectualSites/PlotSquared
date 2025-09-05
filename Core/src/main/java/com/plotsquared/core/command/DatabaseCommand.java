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
import com.plotsquared.core.persistence.config.LiquibaseCrossDatabaseMigrationService;
import com.plotsquared.core.player.PlotPlayer;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.concurrent.CompletableFuture;

/**
 * Command for migrating databases between SQLite and MySQL using Liquibase.
 * All migrations now run completely through Liquibase without native SQL queries.
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
    private final LiquibaseCrossDatabaseMigrationService liquibaseMigrationService;

    @Inject
    public DatabaseCommand(DatabaseMigrationService migrationService,
                          DataSourceProvider dataSourceProvider,
                          LiquibaseCrossDatabaseMigrationService liquibaseMigrationService) {
        this.migrationService = migrationService;
        this.dataSourceProvider = dataSourceProvider;
        this.liquibaseMigrationService = liquibaseMigrationService;
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
                migrateToMySQL(player);
                break;

            case "migrate-to-sqlite":
                migrateToSQLite(player);
                break;

            case "backup":
                if (args.length < 2) {
                    player.sendMessage(TranslatableCaption.of("database.backup_usage"));
                    return false;
                }
                createBackup(player, args[1]);
                break;

            case "status":
                showDatabaseStatus(player);
                break;

            case "stats":
                showDatabaseStats(player);
                break;

            default:
                player.sendMessage(TranslatableCaption.of("database.unknown_subcommand"));
                player.sendMessage(TranslatableCaption.of("database.available_commands"));
                return false;
        }

        return true;
    }

    private void migrateToMySQL(PlotPlayer<?> player) {
        player.sendMessage(TranslatableCaption.of("database.migration_mysql_started"));

        CompletableFuture.runAsync(() -> {
            try {
                liquibaseMigrationService.migrateToMySQL();

                player.sendMessage(TranslatableCaption.of("database.migration_completed"));
                player.sendMessage(TranslatableCaption.of("database.update_config_reminder"));

            } catch (IllegalStateException e) {
                player.sendMessage(TranslatableCaption.of("database.mysql_not_configured"));
            } catch (Exception e) {
                player.sendMessage(TranslatableCaption.of("database.migration_failed"));
                e.printStackTrace();
            }
        });
    }

    private void migrateToSQLite(PlotPlayer<?> player) {
        player.sendMessage(TranslatableCaption.of("database.migration_sqlite_started"));

        CompletableFuture.runAsync(() -> {
            try {
                liquibaseMigrationService.migrateToSQLite();

                player.sendMessage(TranslatableCaption.of("database.migration_completed"));
                player.sendMessage(TranslatableCaption.of("database.update_config_reminder"));

            } catch (IllegalStateException e) {
                player.sendMessage(TranslatableCaption.of("database.sqlite_not_configured"));
            } catch (Exception e) {
                player.sendMessage(TranslatableCaption.of("database.migration_failed"));
                e.printStackTrace();
            }
        });
    }

    private void createBackup(PlotPlayer<?> player, String backupSuffix) {
        player.sendMessage(TranslatableCaption.of("database.backup_started"));

        CompletableFuture.runAsync(() -> {
            try {
                liquibaseMigrationService.migrateToBackupDatabase(backupSuffix);

                player.sendMessage(TranslatableCaption.of("database.backup_completed"));

            } catch (Exception e) {
                player.sendMessage(TranslatableCaption.of("database.backup_failed"));
                e.printStackTrace();
            }
        });
    }

    private void showDatabaseStatus(PlotPlayer<?> player) {
        try {
            DataSource dataSource = dataSourceProvider.createDataSource();

            try (Connection connection = dataSource.getConnection()) {
                String databaseType = Storage.MySQL.USE ? "MySQL" : "SQLite";
                String version = migrationService.getDatabaseVersion(connection);

                player.sendMessage(TranslatableCaption.of("database.status_header"));
                player.sendMessage(TranslatableCaption.of("database.status_type", databaseType));
                player.sendMessage(TranslatableCaption.of("database.status_version", version));
                player.sendMessage(TranslatableCaption.of("database.status_prefix", Storage.PREFIX == null ? "none" : Storage.PREFIX));
            }

        } catch (Exception e) {
            player.sendMessage(TranslatableCaption.of("database.status_error"));
            e.printStackTrace();
        }
    }

    private void showDatabaseStats(PlotPlayer<?> player) {
        try {
            DataSource dataSource = dataSourceProvider.createDataSource();

            try (Connection connection = dataSource.getConnection()) {
                String stats = migrationService.getDatabaseStatistics(connection);

                for (String line : stats.split("\n")) {
                    player.sendMessage(TranslatableCaption.of("database.stats_line", line));
                }
            }

        } catch (Exception e) {
            player.sendMessage(TranslatableCaption.of("database.stats_error"));
            e.printStackTrace();
        }
    }
}
