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
import com.plotsquared.core.configuration.caption.TranslatableCaption;
import com.plotsquared.core.persistence.config.LiquibaseCrossDatabaseMigrationService;
import com.plotsquared.core.player.PlotPlayer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    private static final Logger LOGGER = LoggerFactory.getLogger(DatabaseCommand.class);

    private final LiquibaseCrossDatabaseMigrationService liquibaseMigrationService;

    @Inject
    public DatabaseCommand(LiquibaseCrossDatabaseMigrationService liquibaseMigrationService) {
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

            case "migrate-to-h2":
                migrateToH2(player);
                break;

            case "backup":
                if (args.length < 2) {
                    player.sendMessage(TranslatableCaption.of("database.backup_usage"));
                    return false;
                }
                createBackup(player, args[1]);
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
                LOGGER.error("Database migration to MySQL failed", e);
            }
        });
    }

    private void migrateToH2(PlotPlayer<?> player) {
        player.sendMessage(TranslatableCaption.of("database.migration_sqlite_started"));

        CompletableFuture.runAsync(() -> {
            try {
                liquibaseMigrationService.migrateToH2();

                player.sendMessage(TranslatableCaption.of("database.migration_completed"));
                player.sendMessage(TranslatableCaption.of("database.update_config_reminder"));

            } catch (IllegalStateException e) {
                player.sendMessage(TranslatableCaption.of("database.sqlite_not_configured"));
            } catch (Exception e) {
                player.sendMessage(TranslatableCaption.of("database.migration_failed"));
                LOGGER.error("Database migration to SQLite failed", e);
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
}
