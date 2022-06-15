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
package com.plotsquared.core.uuid;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * Service used to provide usernames from player UUIDs
 */
public interface UUIDService {

    /**
     * Attempt to complete the given requests. Returns the mappings
     * that could be created by this server
     *
     * @param uuids Requests
     * @return Completed requests
     */
    @NonNull List<@NonNull UUIDMapping> getNames(final @NonNull List<@NonNull UUID> uuids);

    /**
     * Attempt to complete the given requests. Returns the mappings
     * that could be created by this server
     *
     * @param usernames Requests
     * @return Completed requests
     */
    @NonNull List<@NonNull UUIDMapping> getUUIDs(final @NonNull List<@NonNull String> usernames);

    /**
     * Get as many UUID mappings as possible under the condition
     * that the operation cannot be blocking (for an extended amount of time)
     *
     * @return All mappings that could be provided immediately
     */
    default @NonNull Collection<@NonNull UUIDMapping> getImmediately() {
        return Collections.emptyList();
    }

    /**
     * Check whether or not this service can be safely used synchronously
     * without blocking the server for an extended amount of time.
     *
     * @return {@code true} if the service can be used synchronously
     */
    default boolean canBeSynchronous() {
        return false;
    }

    /**
     * Get a single UUID mapping immediately, if possible
     *
     * @param object Username ({@link String}) or {@link UUID}
     * @return Mapping, if it could be found immediately
     */
    default @Nullable UUIDMapping getImmediately(final @NonNull Object object) {
        return null;
    }

}
