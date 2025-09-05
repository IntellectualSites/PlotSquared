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
import org.flywaydb.core.Flyway;

import java.util.logging.Logger;

/**
 * Eager bootstrap that executes Flyway migrations during application startup.
 *
 * @since 8.0.0
 * @version 1.0.0
 * @author TheMeinerLP
 * @author IntellectualSites
 */
@Singleton
public final class FlywayBootstrap {
    private static final Logger LOGGER = Logger.getLogger(FlywayBootstrap.class.getName());

    @Inject
    public FlywayBootstrap(Flyway flyway) {
        try {
            flyway.migrate();
            LOGGER.info("Flyway migration complete.");
        } catch (Exception e) {
            LOGGER.severe("Flyway migration failed: " + e.getMessage());
        }
    }
}
