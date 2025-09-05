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
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import javax.sql.DataSource;
import java.util.logging.Logger;

/**
 * Provides configured DataSource instances for database connections.
 *
 * @since 8.0.0
 * @version 1.0.0
 * @author TheMeinerLP
 * @author IntellectualSites
 */
@Singleton
public final class DataSourceProvider {
    private static final Logger LOGGER = Logger.getLogger(DataSourceProvider.class.getName());

    /**
     * Creates a DataSource based on current storage configuration.
     */
    public DataSource createDataSource() {
        HikariConfig config = new HikariConfig();

        if (Storage.MySQL.USE) {
            String url = "jdbc:mysql://" + Storage.MySQL.HOST + ":" + Storage.MySQL.PORT + "/" + Storage.MySQL.DATABASE
                    + "?" + String.join("&", Storage.MySQL.PROPERTIES);
            config.setJdbcUrl(url);
            config.setUsername(Storage.MySQL.USER);
            config.setPassword(Storage.MySQL.PASSWORD);
            config.setDriverClassName("com.mysql.cj.jdbc.Driver");
        } else if (Storage.SQLite.USE) {
            String url = "jdbc:sqlite:" + Storage.SQLite.DB + ".db";
            config.setJdbcUrl(url);
            config.setDriverClassName("org.sqlite.JDBC");
        }

        config.setMaximumPoolSize(10);
        config.setMinimumIdle(2);
        config.setConnectionTimeout(30000);
        config.setIdleTimeout(600000);
        config.setLeakDetectionThreshold(60000);

        return new HikariDataSource(config);
    }

    /**
     * Creates a DataSource for a specific database configuration (used for migration between databases).
     */
    public DataSource createDataSource(String jdbcUrl, String username, String password, String driverClass) {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(jdbcUrl);
        if (username != null) config.setUsername(username);
        if (password != null) config.setPassword(password);
        config.setDriverClassName(driverClass);

        config.setMaximumPoolSize(5);
        config.setMinimumIdle(1);
        config.setConnectionTimeout(30000);

        return new HikariDataSource(config);
    }
}
