package com.plotsquared.core.persistence.config;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.flywaydb.core.Flyway;

import java.util.logging.Logger;

/**
 * Eager bootstrap that executes Flyway migrations during application startup.
 */
@Singleton
public class FlywayBootstrap {
    private static final Logger LOGGER = Logger.getLogger(FlywayBootstrap.class.getName());

    @Inject
    public FlywayBootstrap(Flyway flyway) {
        try {
            flyway.migrate();
            LOGGER.info("Flyway migration complete.");
        } catch (Exception e) {
            LOGGER.severe("Flyway migration failed: " + e.getMessage());
            throw e;
        }
    }
}
