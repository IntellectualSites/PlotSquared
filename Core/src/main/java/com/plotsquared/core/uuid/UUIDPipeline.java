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

import com.google.common.collect.Lists;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Consumer;

/**
 * An UUID pipeline is essentially an ordered list of
 * {@link UUIDService uuid services} that each get the
 * opportunity of providing usernames or UUIDs.
 * <p>
 * Each request is then passed through a secondary list of
 * consumers, that can then be used to cache them, etc
 */
public class UUIDPipeline {

    private final Executor executor;
    private final List<UUIDService> serviceList;
    private final List<Consumer<UUIDMapping>> consumerList;

    public UUIDPipeline(@NotNull final Executor executor) {
        this.executor = executor;
        this.serviceList = Lists.newLinkedList();
        this.consumerList = Lists.newLinkedList();
    }

    /**
     * Register a UUID service
     *
     * @param uuidService UUID service to register
     */
    public void registerService(@NotNull final UUIDService uuidService) {
        this.serviceList.add(uuidService);
    }

    /**
     * Register a mapping consumer
     *
     * @param mappingConsumer Consumer to register
     */
    public void registerConsumer(@NotNull final Consumer<UUIDMapping> mappingConsumer) {
        this.consumerList.add(mappingConsumer);
    }

    /**
     * Get a copy of the service list
     *
     * @return Copy of service list
     */
    public List<UUIDService> getServiceListInstance() {
        return Collections.unmodifiableList(this.serviceList);
    }

    private void consume(@NotNull final UUIDMapping mapping) {
        for (final Consumer<UUIDMapping> consumer : this.consumerList) {
            consumer.accept(mapping);
        }
    }

    /**
     * Asynchronously attempt to fetch the mapping from a given UUID or username
     *
     * @param requests UUIDs or usernames
     * @return Future that may complete with the mapping
     */
    public CompletableFuture<Collection<UUIDMapping>> get(@NotNull final Collection<Object> requests) {
        final List<UUIDService> serviceList = this.getServiceListInstance();
        return CompletableFuture.supplyAsync(() -> {
            final List<UUIDMapping> mappings = new ArrayList<>(requests.size());
            outer: for (final Object request : requests) {
                if (!(request instanceof String) && !(request instanceof UUID)) {
                    throw new IllegalArgumentException("Request has to be either a username or UUID");
                }
                for (final UUIDService service : serviceList) {
                    final Optional<?> result = service.get(request);
                    if (result.isPresent()) {
                        final String username = request instanceof String ? (String) request
                            : (String) result.get();
                        final UUID uuid = request instanceof UUID ? (UUID) request
                            : (UUID) result.get();
                        final UUIDMapping mapping = new UUIDMapping(uuid, username);
                        this.consume(mapping);
                        mappings.add(mapping);
                        continue outer;
                    }
                }
                throw new ServiceError("End of pipeline");
            }
            return mappings;
        }, this.executor);
    }

}
