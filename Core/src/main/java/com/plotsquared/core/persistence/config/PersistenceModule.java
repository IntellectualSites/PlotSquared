package com.plotsquared.core.persistence.config;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.plotsquared.core.configuration.Storage;
import com.plotsquared.core.persistence.repository.api.ClusterHelperRepository;
import com.plotsquared.core.persistence.repository.api.ClusterInvitedRepository;
import com.plotsquared.core.persistence.repository.api.ClusterRepository;
import com.plotsquared.core.persistence.repository.api.ClusterSettingsRepository;
import com.plotsquared.core.persistence.repository.api.PlayerMetaRepository;
import com.plotsquared.core.persistence.repository.api.PlotCommentRepository;
import com.plotsquared.core.persistence.repository.api.PlotDeniedRepository;
import com.plotsquared.core.persistence.repository.api.PlotFlagRepository;
import com.plotsquared.core.persistence.repository.api.PlotMembershipRepository;
import com.plotsquared.core.persistence.repository.api.PlotRepository;
import com.plotsquared.core.persistence.repository.api.PlotSettingsRepository;
import com.plotsquared.core.persistence.repository.api.PlotTrustedRepository;
import com.plotsquared.core.persistence.repository.api.PlotRatingRepository;
import com.plotsquared.core.persistence.repository.jpa.ClusterHelperRepositoryJpa;
import com.plotsquared.core.persistence.repository.jpa.ClusterInvitedRepositoryJpa;
import com.plotsquared.core.persistence.repository.jpa.ClusterRepositoryJpa;
import com.plotsquared.core.persistence.repository.jpa.ClusterSettingsRepositoryJpa;
import com.plotsquared.core.persistence.repository.jpa.PlayerMetaRepositoryJpa;
import com.plotsquared.core.persistence.repository.jpa.PlotCommentRepositoryJpa;
import com.plotsquared.core.persistence.repository.jpa.PlotDeniedRepositoryJpa;
import com.plotsquared.core.persistence.repository.jpa.PlotFlagRepositoryJpa;
import com.plotsquared.core.persistence.repository.jpa.PlotMembershipRepositoryJpa;
import com.plotsquared.core.persistence.repository.jpa.PlotRepositoryJpa;
import com.plotsquared.core.persistence.repository.jpa.PlotSettingsRepositoryJpa;
import com.plotsquared.core.persistence.repository.jpa.PlotTrustedRepositoryJpa;
import com.plotsquared.core.persistence.repository.jpa.PlotRatingRepositoryJpa;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import org.flywaydb.core.Flyway;

import java.util.Map;

public class PersistenceModule extends AbstractModule {

    @Override
    protected void configure() {
        // Bind repository interfaces to JPA implementations
        bind(PlotRepository.class).to(PlotRepositoryJpa.class);
        bind(PlotFlagRepository.class).to(PlotFlagRepositoryJpa.class);
        bind(PlotCommentRepository.class).to(PlotCommentRepositoryJpa.class);
        bind(PlotRatingRepository.class).to(PlotRatingRepositoryJpa.class);
        bind(PlayerMetaRepository.class).to(PlayerMetaRepositoryJpa.class);
        bind(PlotSettingsRepository.class).to(PlotSettingsRepositoryJpa.class);
        bind(PlotMembershipRepository.class).to(PlotMembershipRepositoryJpa.class);
        bind(PlotTrustedRepository.class).to(PlotTrustedRepositoryJpa.class);
        bind(PlotDeniedRepository.class).to(PlotDeniedRepositoryJpa.class);
        bind(ClusterRepository.class).to(ClusterRepositoryJpa.class);
        bind(ClusterHelperRepository.class).to(ClusterHelperRepositoryJpa.class);
        bind(ClusterInvitedRepository.class).to(ClusterInvitedRepositoryJpa.class);
        bind(ClusterSettingsRepository.class).to(ClusterSettingsRepositoryJpa.class);

        // Eagerly run Flyway migrations on startup
        bind(FlywayBootstrap.class).asEagerSingleton();
    }

    @Provides
    EntityManager provideEm(EntityManagerFactory emf) {
        return emf.createEntityManager();
    }

    @Provides
    @Singleton
    EntityManagerFactory provideEmf(JpaPropertiesProvider jpaPropertiesProvider) {
        Map<String, Object> props = jpaPropertiesProvider.getProperties();
        return Persistence.createEntityManagerFactory("plotsquaredPU", props);
    }

    @Provides
    @Singleton
    Flyway provideFlyway() {
        String url;
        String user = null;
        String pass = null;
        if (Storage.MySQL.USE) {
            url = "jdbc:mysql://" + Storage.MySQL.HOST + ":" + Storage.MySQL.PORT + "/" + Storage.MySQL.DATABASE
                    + "?" + String.join("&", Storage.MySQL.PROPERTIES);
            user = Storage.MySQL.USER;
            pass = Storage.MySQL.PASSWORD;
        } else {
            url = "jdbc:sqlite:" + Storage.SQLite.DB + ".db";
        }
        // Support prefixed table names in SQL migrations via placeholders
        Map<String, String> placeholders = new java.util.HashMap<>();
        placeholders.put("prefix", Storage.PREFIX == null ? "" : Storage.PREFIX);

        return Flyway.configure()
                .dataSource(url, user, pass)
                .locations("classpath:db/migration")
                // Baseline an existing, unversioned schema to avoid destructive changes
                .baselineOnMigrate(true)
                .baselineVersion("0")
                .baselineDescription("Baseline before migrating to JPA-managed schema")
                // Prevent accidental data loss
                .cleanDisabled(true)
                // Enable ${prefix} usage in SQL files for table names
                .placeholderReplacement(true)
                .placeholders(placeholders)
                .load();
    }
}
