/*
 *       _____  _       _    _____                                _
 *      |  __ \| |     | |  / ____|                              | |
 *      | |__) | | ___ | |_| (___   __ _ _   _  __ _ _ __ ___  __| |
 *      |  ___/| |/ _ \| __|\___ \ / _` | | | |/ _` | '__/ _ \/ _` |
 *      | |    | | (_) | |_ ____) | (_| | |_| | (_| | | |  __/ (_| |
 *      |_|    |_|\___/ \__|_____/ \__, |\__,_|\__,_|_|  \___|\__,_|
 *                                    | |
 *                                    |_|
 *            PlotSquared plot management system for Minecraft
 *                  Copyright (C) 2020 IntellectualSites
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.plotsquared.core.uuid;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

/**
 * UUID service backed by a Guava Cache
 */
public class CacheUUIDService implements UUIDService, Consumer<List<UUIDMapping>> {

    private final Cache<String, UUIDMapping> usernameCache;
    private final Cache<UUID, UUIDMapping> uuidCache;

    /**
     * Construct a new Cache UUID service with a maximum number of entries.
     * Because it stores the mappings in two ways, the actual number
     * of entries is two times the specified size
     *
     * @param size Maximum number of entries
     */
    public CacheUUIDService(final int size) {
        this.usernameCache = CacheBuilder.newBuilder().maximumSize(size).build();
        this.uuidCache = CacheBuilder.newBuilder().maximumSize(size).build();
    }

    @Override @NotNull public List<UUIDMapping> getNames(@NotNull final List<UUID> uuids) {
        final List<UUIDMapping> mappings = new ArrayList<>(uuids.size());
        mappings.addAll(this.uuidCache.getAllPresent(uuids).values());
        return mappings;
    }

    @Override @NotNull public List<UUIDMapping> getUUIDs(@NotNull final List<String> usernames) {
        final List<UUIDMapping> mappings = new ArrayList<>(usernames.size());
        mappings.addAll(this.usernameCache.getAllPresent(usernames).values());
        return mappings;
    }

    @Override public void accept(final List<UUIDMapping> uuidMappings) {
        for (final UUIDMapping mapping : uuidMappings) {
            this.uuidCache.put(mapping.getUuid(), mapping);
            this.usernameCache.put(mapping.getUsername(), mapping);
        }
    }

    @Override @NotNull public Collection<UUIDMapping> getImmediately() {
        return this.usernameCache.asMap().values();
    }

    @Override public boolean canBeSynchronous() {
        return true;
    }

    @Override @Nullable public UUIDMapping getImmediately(@NotNull final Object object) {
        final List<UUIDMapping> list;
        if (object instanceof String) {
            list = getUUIDs(Collections.singletonList((String) object));
        } else if (object instanceof UUID) {
            list = getNames(Collections.singletonList((UUID) object));
        } else {
            list = Collections.emptyList();
        }
        if (list.isEmpty()) {
            return null;
        }
        return list.get(0);
    }

}
