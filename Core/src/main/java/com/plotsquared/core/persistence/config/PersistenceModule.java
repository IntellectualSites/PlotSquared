package com.plotsquared.core.persistence.config;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.plotsquared.core.configuration.Storage;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import org.flywaydb.core.Flyway;

import java.util.HashMap;
import java.util.Map;

public class PersistenceModule extends AbstractModule {

    @Provides
    EntityManager provideEm(EntityManagerFactory emf) {
        return emf.createEntityManager();
    }

    @Provides
    @Singleton
    EntityManagerFactory provideEmf() {
        Map<String, Object> props = new HashMap<>();

        if (Storage.MySQL.USE) {
            String url = "jdbc:mysql://" + Storage.MySQL.HOST + ":" + Storage.MySQL.PORT + "/" + Storage.MySQL.DATABASE
                    + "?" + String.join("&", Storage.MySQL.PROPERTIES);
            props.put("jakarta.persistence.jdbc.url", url);
            props.put("jakarta.persistence.jdbc.user", Storage.MySQL.USER);
            props.put("jakarta.persistence.jdbc.password", Storage.MySQL.PASSWORD);
            props.put("jakarta.persistence.jdbc.driver", "com.mysql.cj.jdbc.Driver");
            props.put("hibernate.dialect", "org.hibernate.dialect.MySQL8Dialect");
        } else if (Storage.SQLite.USE) {
            String url = "jdbc:sqlite:" + Storage.SQLite.DB + ".db";
            props.put("jakarta.persistence.jdbc.url", url);
            props.put("jakarta.persistence.jdbc.driver", "org.sqlite.JDBC");
            props.put("hibernate.dialect", "org.hibernate.community.dialect.SQLiteDialect");
        }

        // Schema via Flyway; only validate with Hibernate
        props.put("hibernate.hbm2ddl.auto", "validate");
        props.put("hibernate.show_sql", false);
        props.put("hibernate.format_sql", false);

        // Apply dynamic table prefixing
        props.put("hibernate.physical_naming_strategy", new PrefixedNamingStrategy(Storage.PREFIX));

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
        return Flyway.configure()
                .dataSource(url, user, pass)
                .locations("classpath:db/migration")
                .baselineOnMigrate(true)
                .load();
    }
}
