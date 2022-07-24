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
package com.plotsquared.core.player;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.inject.TypeLiteral;
import com.plotsquared.core.synchronization.LockKey;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.HashMap;
import java.util.Map;

/**
 * Key used to access meta data
 *
 * @param <T> Meta data type
 */
public final class MetaDataKey<T> {

    private static final Map<String, MetaDataKey<?>> keyMap = new HashMap<>();
    private static final Object keyMetaData = new Object();

    private final String key;
    private final TypeLiteral<T> type;
    private final LockKey lockKey;

    private MetaDataKey(final @NonNull String key, final @NonNull TypeLiteral<T> type) {
        this.key = Preconditions.checkNotNull(key, "Key may not be null");
        this.type = Preconditions.checkNotNull(type, "Type may not be null");
        this.lockKey = LockKey.of(this.key);
    }

    /**
     * Get a new named lock key
     *
     * @param key  Key name
     * @param type type
     * @param <T>  Type
     * @return MetaData key instance
     */
    @SuppressWarnings("unchecked")
    public static @NonNull <T> MetaDataKey<T> of(final @NonNull String key, final @NonNull TypeLiteral<T> type) {
        synchronized (keyMetaData) {
            return (MetaDataKey<T>)
                    keyMap.computeIfAbsent(key, missingKey -> new MetaDataKey<>(missingKey, type));
        }
    }

    @Override
    public String toString() {
        return this.key;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final MetaDataKey<?> lockKey = (MetaDataKey<?>) o;
        return Objects.equal(this.key, lockKey.key);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(this.key);
    }

    /**
     * Get the {@link LockKey} associated with this key
     *
     * @return Lock key
     */
    public @NonNull LockKey getLockKey() {
        return this.lockKey;
    }

    /**
     * Get the meta data type
     *
     * @return Meta data type
     */
    public @NonNull TypeLiteral<T> getType() {
        return this.type;
    }

}
