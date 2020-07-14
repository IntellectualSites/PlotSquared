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

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

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
    @Nonnull List<UUIDMapping> getNames(@Nonnull final List<UUID> uuids);

    /**
     * Attempt to complete the given requests. Returns the mappings
     * that could be created by this server
     *
     * @param usernames Requests
     * @return Completed requests
     */
    @Nonnull List<UUIDMapping> getUUIDs(@Nonnull final List<String> usernames);

    /**
     * Get as many UUID mappings as possible under the condition
     * that the operation cannot be blocking (for an extended amount of time)
     *
     * @return All mappings that could be provided immediately
     */
    default @Nonnull Collection<UUIDMapping> getImmediately() {
        return Collections.emptyList();
    }

    /**
     * Check whether or not this service can be safely used synchronously
     * without blocking the server for an extended amount of time.
     *
     * @return True if the service can be used synchronously
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
    default @Nullable UUIDMapping getImmediately(@Nonnull final Object object) {
        return null;
    }

}
