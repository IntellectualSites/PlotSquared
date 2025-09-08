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
import liquibase.resource.ClassLoaderResourceAccessor;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.logging.Logger;

/**
 * Eager bootstrap that executes Liquibase migrations during application startup.
 *
 * @since 8.0.0
 * @version 1.0.0
 * @author TheMeinerLP
 * @author IntellectualSites
 */
@Singleton
public final class LiquibaseBootstrap {
    private static final Logger LOGGER = Logger.getLogger(LiquibaseBootstrap.class.getName());

    @Inject
    public LiquibaseBootstrap(DataSource dataSource) {
        syncThreadForServiceLoader(() -> {
            try (Connection connection = dataSource.getConnection()) {
                Database database = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(new JdbcConnection(connection));

                // Run Liquibase migrations - this will handle both v7->v8 migration and new v8 installations
                Liquibase liquibase = new Liquibase("db/changelog/db.changelog-master.xml",
                        new ClassLoaderResourceAccessor(), database);

                liquibase.setChangeLogParameter("PREFIX", Storage.PREFIX == null ? "" : Storage.PREFIX);
                liquibase.setChangeLogParameter("prefix", Storage.PREFIX == null ? "" : Storage.PREFIX);

                liquibase.update(new Contexts(), new LabelExpression());
                LOGGER.info("Liquibase migration completed successfully.");

            } catch (Exception e) {
                LOGGER.severe("Liquibase migration failed: " + e.getMessage());
            }
        });
    }

    void syncThreadForServiceLoader(Runnable runnable) {
        Thread currentThread = Thread.currentThread();
        ClassLoader originalClassLoader = currentThread.getContextClassLoader();
        ClassLoader pluginClassLoader = this.getClass().getClassLoader();
        try {
            currentThread.setContextClassLoader(pluginClassLoader);
            runnable.run();
        } finally {
            currentThread.setContextClassLoader(originalClassLoader);
        }
    }
}
