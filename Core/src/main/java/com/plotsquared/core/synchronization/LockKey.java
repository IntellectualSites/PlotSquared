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
package com.plotsquared.core.synchronization;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Key used to access {@link java.util.concurrent.locks.Lock locks}
 * from a {@link LockRepository}
 */
public final class LockKey {

    private static final Map<String, LockKey> keyMap = new HashMap<>();
    private static final Object keyLock = new Object();

    private final String key;

    private LockKey(final @NonNull String key) {
        this.key = Preconditions.checkNotNull(key, "Key may not be null");
    }

    /**
     * Get a new named lock key
     *
     * @param key Key name
     * @return Lock key instance
     */
    public static @NonNull LockKey of(final @NonNull String key) {
        synchronized (keyLock) {
            return keyMap.computeIfAbsent(key, LockKey::new);
        }
    }

    /**
     * Get all currently recognized lock keys
     *
     * @return Currently recognized lock keys
     */
    @NonNull
    static Collection<LockKey> recognizedKeys() {
        return Collections.unmodifiableCollection(keyMap.values());
    }

    @Override
    public String toString() {
        return "LockKey{" + "key='" + key + '\'' + '}';
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final LockKey lockKey = (LockKey) o;
        return Objects.equal(this.key, lockKey.key);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(this.key);
    }

}
