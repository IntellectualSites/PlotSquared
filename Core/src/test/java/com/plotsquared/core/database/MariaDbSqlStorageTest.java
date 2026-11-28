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

import com.plotsquared.core.configuration.Storage;
import org.junit.jupiter.api.Tag;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.mariadb.MariaDBContainer;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Runs the SQL storage characterisation tests against MariaDB in a Testcontainers container. Every test gets its
 * own database inside the shared container. Skipped when Docker is not available.
 */
@Tag("integration")
@Testcontainers(disabledWithoutDocker = true)
class MariaDbSqlStorageTest extends AbstractSqlStorageTest {

    @Container
    private static final MariaDBContainer MARIADB = new MariaDBContainer("mariadb:11.4");

    private String databaseName;
    private List<String> previousProperties;

    @Override
    protected Dialect dialect() {
        return Dialect.MARIADB;
    }

    @Override
    protected Database createDatabase() throws SQLException {
        this.databaseName = "psq_" + UUID.randomUUID().toString().replace("-", "");
        // MariaDB Connector/J 4 only accepts the jdbc:mysql: scheme that MySQL.java builds when this is set.
        // The list is restored in dropDatabase.
        this.previousProperties = Storage.MySQL.PROPERTIES;
        List<String> properties = new ArrayList<>(this.previousProperties);
        properties.add("permitMysqlScheme=true");
        Storage.MySQL.PROPERTIES = properties;
        try (Connection root = rootConnection(""); Statement statement = root.createStatement()) {
            statement.execute("CREATE DATABASE `" + this.databaseName + "`");
        }
        return new MySQL(
                MARIADB.getHost(), String.valueOf(MARIADB.getMappedPort(3306)),
                this.databaseName, "root", MARIADB.getPassword()
        );
    }

    @Override
    protected Connection openConnection() throws SQLException {
        return rootConnection(this.databaseName);
    }

    @Override
    protected void dropDatabase() throws SQLException {
        if (this.previousProperties != null) {
            Storage.MySQL.PROPERTIES = this.previousProperties;
            this.previousProperties = null;
        }
        if (this.databaseName == null) {
            return;
        }
        try (Connection root = rootConnection(""); Statement statement = root.createStatement()) {
            statement.execute("DROP DATABASE IF EXISTS `" + this.databaseName + "`");
        }
    }

    private static Connection rootConnection(String database) throws SQLException {
        String url = "jdbc:mariadb://" + MARIADB.getHost() + ":" + MARIADB.getMappedPort(3306) + "/" + database;
        return DriverManager.getConnection(url, "root", MARIADB.getPassword());
    }

}
