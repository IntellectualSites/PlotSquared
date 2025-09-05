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

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
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

import javax.sql.DataSource;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Guice module for configuring persistence-related bindings and providers.
 *
 * @since 8.0.0
 * @version 1.0.0
 * @author TheMeinerLP
 * @author IntellectualSites
 */
public final class PersistenceModule extends AbstractModule {

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

        // Bind configuration and migration services
        bind(JpaPropertiesProvider.class).asEagerSingleton();
        bind(DataSourceProvider.class).asEagerSingleton();
        bind(DatabaseMigrationService.class).asEagerSingleton();

        // Eagerly run Liquibase migrations on startup
        bind(LiquibaseBootstrap.class).asEagerSingleton();
    }

    @Provides
    @Singleton
    DataSource provideDataSource(DataSourceProvider dataSourceProvider) {
        return dataSourceProvider.createDataSource();
    }

    @Provides
    @Singleton
    EntityManagerFactory provideEmf(JpaPropertiesProvider jpaPropertiesProvider) {
        Map<String, Object> props = jpaPropertiesProvider.getProperties();
        return syncThreadForServiceLoader(() -> Persistence.createEntityManagerFactory("plotsquaredPU", props));
    }

    @Provides
    EntityManager provideEm(EntityManagerFactory emf) {
        return emf.createEntityManager();
    }

    private <T> T syncThreadForServiceLoader(Supplier<T> supplier) {
        Thread currentThread = Thread.currentThread();
        ClassLoader originalClassLoader = currentThread.getContextClassLoader();
        ClassLoader pluginClassLoader = this.getClass().getClassLoader();
        try {
            currentThread.setContextClassLoader(pluginClassLoader);
            return supplier.get();
        } finally {
            currentThread.setContextClassLoader(originalClassLoader);
        }
    }
}
