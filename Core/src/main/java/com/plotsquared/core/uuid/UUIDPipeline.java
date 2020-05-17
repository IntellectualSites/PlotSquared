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

import java.util.List;
import java.util.ListIterator;
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
        final List<UUIDService> serviceList = Lists.newLinkedList(this.serviceList);
        serviceList.add(EndOfPipeline.instance);
        return serviceList;
    }

    private void consume(@NotNull final UUIDMapping mapping) {
        for (final Consumer<UUIDMapping> consumer : this.consumerList) {
            consumer.accept(mapping);
        }
    }

    /**
     * Asynchronously attempt to fetch the mapping from a given UUID or username
     *
     * @param request UUID or username
     * @return Future that may complete with the mapping
     */
    public CompletableFuture<UUIDMapping> get(@NotNull final Object request) {
        if (!(request instanceof String) && !(request instanceof UUID)) {
            throw new IllegalArgumentException("Request has to be either a username or UUID");
        }
        final CompletableFuture<UUIDMapping> future = new CompletableFuture<>();
        final ListIterator<UUIDService> serviceListIterator
            = this.getServiceListInstance().listIterator();
        final Runnable[] runnable = new Runnable[1];
        runnable[0] = () -> {
            if (serviceListIterator.hasNext()) {
                final UUIDService uuidService = serviceListIterator.next();
                uuidService.get(request).whenCompleteAsync(((result, throwable) -> {
                    if (throwable != null) {
                        if (throwable instanceof ServiceFailure) {
                            try {
                                runnable[0].run();
                            } catch (final Throwable inner) {
                                future.completeExceptionally(inner);
                            }
                        } else {
                            future.completeExceptionally(throwable);
                        }
                    } else {
                        final String username = request instanceof String ? (String) request
                            : (String) result;
                        final UUID uuid = request instanceof UUID ? (UUID) request
                            : (UUID) result;
                        final UUIDMapping mapping = new UUIDMapping(uuid, username);
                        future.complete(mapping);
                        this.consume(mapping);
                    }
                }), this.executor);
            } else {
                throw new ServiceError("Pipeline is incomplete");
            }
        };
        try {
            // Start the pipeline traversal
            runnable[0].run();
        } catch (final Throwable throwable) {
            future.completeExceptionally(throwable);
        }
        return future;
    }

    /**
     * Indicates that the end of the pipeline has been reached, this
     * will cause the request to fail, as no service was able to
     * fulfil the request
     */
    private static class EndOfPipeline implements UUIDService {

        public static final EndOfPipeline instance = new EndOfPipeline();

        @Override @NotNull public CompletableFuture<String> get(@NotNull final UUID uuid) {
            final CompletableFuture<String> future = new CompletableFuture<>();
            future.completeExceptionally(new ServiceError("End of pipeline"));
            return future;
        }

        @Override @NotNull public CompletableFuture<UUID> get(@NotNull final String username) {
            final CompletableFuture<UUID> future = new CompletableFuture<>();
            future.completeExceptionally(new ServiceError("End of pipeline"));
            return future;
        }
    }

}
