package com.plotsquared.core.persistence.config;

import org.hibernate.boot.model.naming.Identifier;
import org.hibernate.boot.model.naming.PhysicalNamingStrategy;
import org.hibernate.engine.jdbc.env.spi.JdbcEnvironment;

public class PrefixedNamingStrategy implements PhysicalNamingStrategy {

    private final String prefix;

    public PrefixedNamingStrategy(String prefix) {
        this.prefix = prefix == null ? "" : prefix;
    }

    @Override
    public Identifier toPhysicalTableName(Identifier name, JdbcEnvironment env) {
        if (name == null) {
            return null;
        }
        return Identifier.toIdentifier(prefix + name.getText(), name.isQuoted());
    }

    @Override
    public Identifier toPhysicalCatalogName(Identifier n, JdbcEnvironment e) {
        return n;
    }

    @Override
    public Identifier toPhysicalSchemaName(Identifier n, JdbcEnvironment e) {
        return n;
    }

    @Override
    public Identifier toPhysicalSequenceName(Identifier n, JdbcEnvironment e) {
        return n;
    }

    @Override
    public Identifier toPhysicalColumnName(Identifier n, JdbcEnvironment e) {
        return n;
    }

}
