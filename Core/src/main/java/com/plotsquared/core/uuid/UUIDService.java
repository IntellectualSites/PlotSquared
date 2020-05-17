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

import org.jetbrains.annotations.NotNull;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Service used to provide usernames from player UUIDs
 */
public interface UUIDService {

    default CompletableFuture<?> get(@NotNull final Object request) {
        if (request instanceof UUID) {
            return get((UUID) request);
        } else if (request instanceof String) {
            return get((String) request);
        } else {
            throw new IllegalArgumentException("Request has to be either a username or UUID");
        }
    }

    /**
     * Get a stored username from the service if it exists.
     * This should <b>not</b> trigger any fetching of
     * usernames from other services.
     * <p>
     * If the username isn't stored in this service,
     * this completes with an empty optional.
     *
     * @param uuid Player UUID
     * @return Future that may contain the username if it exists
     */
    @NotNull CompletableFuture<String> get(@NotNull final UUID uuid);

    /**
     * Get a stored UUID from the service if it exists.
     * This should <b>not</b> trigger any fetching of
     * UUID from other services.
     * <p>
     * If the UUID isn't stored in this service,
     * this completes with an empty optional.
     *
     * @param username Player username
     * @return Future that may contain the UUID if it exists
     */
    @NotNull CompletableFuture<UUID> get(@NotNull final String username);

}
