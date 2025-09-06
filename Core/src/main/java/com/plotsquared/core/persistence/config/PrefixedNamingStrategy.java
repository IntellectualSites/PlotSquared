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

import org.hibernate.boot.model.naming.Identifier;
import org.hibernate.boot.model.naming.PhysicalNamingStrategy;
import org.hibernate.engine.jdbc.env.spi.JdbcEnvironment;
import org.jetbrains.annotations.NotNull;

/**
 * A PhysicalNamingStrategy that adds a specified prefix to all table names.
 * This is useful for avoiding naming conflicts in shared databases.
 *
 * @since 8.0.0
 * @version 1.0.0
 * @author TheMeinerLP
 * @author IntellectualSites
 */
public class PrefixedNamingStrategy implements PhysicalNamingStrategy {

    private final String prefix;

    public PrefixedNamingStrategy(@NotNull String prefix) {
        this.prefix = prefix;
    }

    @Override
    public Identifier toPhysicalTableName(Identifier name, JdbcEnvironment env) {
        if (name == null) {
            return null;
        }
        return Identifier.toIdentifier((prefix + name.getText()).toUpperCase(), name.isQuoted());
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
        return Identifier.toIdentifier(n.getText().toUpperCase());
    }

}
