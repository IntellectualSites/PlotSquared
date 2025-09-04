package com.plotsquared.core.persistence.config;

import com.google.inject.Singleton;
import com.plotsquared.core.configuration.Storage;

import java.util.HashMap;
import java.util.Map;

/**
 * Builds JPA/Hibernate properties based on Storage configuration.
 */
@Singleton
public class JpaPropertiesProvider {

    /**
     * Create a map of JPA properties suitable for EntityManagerFactory creation.
     */
    public Map<String, Object> getProperties() {
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

        // Schema is managed by Flyway; only validate with Hibernate
        props.put("hibernate.hbm2ddl.auto", "create");
        props.put("hibernate.show_sql", false);
        props.put("hibernate.format_sql", false);

        // Apply dynamic table prefixing
        props.put("hibernate.physical_naming_strategy", new PrefixedNamingStrategy(Storage.PREFIX));

        return props;
    }
}
