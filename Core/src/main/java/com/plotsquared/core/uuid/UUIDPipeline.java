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
import com.plotsquared.core.PlotSquared;
import com.plotsquared.core.configuration.Settings;
import com.plotsquared.core.util.ThreadUtils;
import com.plotsquared.core.util.task.TaskManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

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
    private final List<Consumer<List<UUIDMapping>>> consumerList;
    private final ScheduledExecutorService timeoutExecutor;

    /**
     * Construct a new UUID pipeline
     *
     * @param executor Executor that is used to run asynchronous tasks inside
     *                 of the pipeline
     */
    public UUIDPipeline(@NotNull final Executor executor) {
        this.executor = executor;
        this.serviceList = Lists.newLinkedList();
        this.consumerList = Lists.newLinkedList();
        this.timeoutExecutor = Executors.newSingleThreadScheduledExecutor();
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
    public void registerConsumer(@NotNull final Consumer<List<UUIDMapping>> mappingConsumer) {
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

    /**
     * Let all consumers act on the given mapping.
     *
     * @param mappings Mappings
     */
    public void consume(@NotNull final List<UUIDMapping> mappings) {
        final Runnable runnable = () -> {
            for (final Consumer<List<UUIDMapping>> consumer : this.consumerList) {
                consumer.accept(mappings);
            }
        };
        if (PlotSquared.get().isMainThread(Thread.currentThread())) {
            TaskManager.runTaskAsync(runnable);
        } else {
            runnable.run();
        }
    }

    /**
     * Consume a single mapping
     *
     * @param mapping Mapping to consume
     */
    public void consume(@NotNull final UUIDMapping mapping) {
        this.consume(Collections.singletonList(mapping));
    }

    /**
     * This will store the given username-UUID pair directly, and overwrite
     * any existing caches. This can be used to update usernames automatically
     * whenever a player joins the server, to make sure an up-to-date UUID
     * mapping is stored
     *
     * @param username Player username
     * @param uuid     Player uuid
     */
    public void storeImmediately(@NotNull final String username, @NotNull final UUID uuid) {
        this.consume(new UUIDMapping(uuid, username));
    }

    /**
     * Get a single UUID from a username. This is blocking.
     *
     * @param username Username
     * @param timeout  Timeout in milliseconds
     * @return The mapped uuid. Will return null if the request timed out.
     */
    @Nullable public UUID getSingle(@NotNull final String username, final long timeout) {
        ThreadUtils.catchSync("Blocking UUID retrieval from the main thread");
        try {
            this.getUUIDs(Collections.singletonList(username)).get(timeout, TimeUnit.MILLISECONDS);
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        } catch (TimeoutException ignored) {
            // This is completely valid, we just don't care anymore
        }
        return null;
    }

    /**
     * Get a single username from a UUID. This is blocking.
     *
     * @param uuid    UUID
     * @param timeout Timeout in milliseconds
     * @return The mapped username. Will return null if the request timeout.
     */
    @Nullable public String getSingle(@NotNull final UUID uuid, final long timeout) {
        ThreadUtils.catchSync("Blocking username retrieval from the main thread");
        try {
            this.getNames(Collections.singletonList(uuid)).get(timeout, TimeUnit.MILLISECONDS);
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        } catch (TimeoutException ignored) {
            // This is completely valid, we just don't care anymore
        }
        return null;
    }

    /**
     * Get a single UUID from a username. This is non-blocking.
     *
     * @param username Username
     * @param uuid     UUID consumer
     */
    public void getSingle(@NotNull final String username,
        @NotNull final BiConsumer<UUID, Throwable> uuid) {
        this.getUUIDs(Collections.singletonList(username)).applyToEither(timeoutAfter(Settings.UUID.NON_BLOCKING_TIMEOUT), Function.identity())
            .whenComplete((uuids, throwable) -> {
            if (throwable != null) {
                uuid.accept(null, throwable);
            } else {
                if (!uuids.isEmpty()) {
                    uuid.accept(uuids.get(0).getUuid(), null);
                } else {
                    uuid.accept(null, null);
                }
            }
        });
    }

    /**
     * Get a single username from a UUID. This is non-blocking.
     *
     * @param uuid     UUID
     * @param username Username consumer
     */
    public void getSingle(@NotNull final UUID uuid,
        @NotNull final BiConsumer<String, Throwable> username) {
        this.getNames(Collections.singletonList(uuid)).applyToEither(timeoutAfter(Settings.UUID.NON_BLOCKING_TIMEOUT), Function.identity())
            .whenComplete((uuids, throwable) -> {
            if (throwable != null) {
                username.accept(null, throwable);
            } else {
                if (!uuids.isEmpty()) {
                    username.accept(uuids.get(0).getUsername(), null);
                } else {
                    username.accept(null, null);
                }
            }
        });
    }

    /**
     * Asynchronously attempt to fetch the mapping from a list of UUIDs.
     * <p>
     * This will timeout after the specified time and throws a {@link TimeoutException}
     * if this happens
     *
     * @param requests UUIDs
     * @param timeout  Timeout in milliseconds
     * @return Mappings
     */
    public CompletableFuture<List<UUIDMapping>> getNames(@NotNull final Collection<UUID> requests,
        final long timeout) {
        return this.getNames(requests).applyToEither(timeoutAfter(timeout), Function.identity());
    }

    /**
     * Asynchronously attempt to fetch the mapping from a list of names.
     * <p>
     * This will timeout after the specified time and throws a {@link TimeoutException}
     * if this happens
     *
     * @param requests Names
     * @param timeout  Timeout in milliseconds
     * @return Mappings
     */
    public CompletableFuture<List<UUIDMapping>> getUUIDs(@NotNull final Collection<String> requests,
        final long timeout) {
        return this.getUUIDs(requests).applyToEither(timeoutAfter(timeout), Function.identity());
    }

    private CompletableFuture<List<UUIDMapping>> timeoutAfter(final long timeout) {
        final CompletableFuture<List<UUIDMapping>> result = new CompletableFuture<>();
        this.timeoutExecutor.schedule(() -> result.completeExceptionally(new TimeoutException()), timeout, TimeUnit.MILLISECONDS);
        return result;
    }

    /**
     * Asynchronously attempt to fetch the mapping from a list of UUIDs
     *
     * @param requests UUIDs
     * @return Mappings
     */
    public CompletableFuture<List<UUIDMapping>> getNames(@NotNull final Collection<UUID> requests) {
        if (requests.isEmpty()) {
            return CompletableFuture.completedFuture(Collections.emptyList());
        }
        final List<UUIDService> serviceList = this.getServiceListInstance();
        return CompletableFuture.supplyAsync(() -> {
            final List<UUIDMapping> mappings = new ArrayList<>(requests.size());
            final List<UUID> remainingRequests = new ArrayList<>(requests);

            for (final UUIDService service : serviceList) {
                if (remainingRequests.isEmpty()) {
                    break;
                }
                final List<UUIDMapping> completedRequests = service.getNames(remainingRequests);
                for (final UUIDMapping mapping : completedRequests) {
                    remainingRequests.remove(mapping.getUuid());
                }
                mappings.addAll(completedRequests);
            }

            if (mappings.size() == requests.size()) {
                this.consume(mappings);
                return mappings;
            }

            throw new ServiceError("End of pipeline");
        }, this.executor);
    }

    /**
     * Asynchronously attempt to fetch the mapping from a list of names
     *
     * @param requests Names
     * @return Mappings
     */
    public CompletableFuture<List<UUIDMapping>> getUUIDs(
        @NotNull final Collection<String> requests) {
        if (requests.isEmpty()) {
            return CompletableFuture.completedFuture(Collections.emptyList());
        }
        final List<UUIDService> serviceList = this.getServiceListInstance();
        return CompletableFuture.supplyAsync(() -> {
            final List<UUIDMapping> mappings = new ArrayList<>(requests.size());
            final List<String> remainingRequests = new ArrayList<>(requests);

            for (final UUIDService service : serviceList) {
                if (remainingRequests.isEmpty()) {
                    break;
                }
                final List<UUIDMapping> completedRequests = service.getUUIDs(remainingRequests);
                for (final UUIDMapping mapping : completedRequests) {
                    remainingRequests.remove(mapping.getUsername());
                }
                mappings.addAll(completedRequests);
            }

            if (mappings.size() == requests.size()) {
                this.consume(mappings);
                return mappings;
            }

            throw new ServiceError("End of pipeline");
        }, this.executor);
    }

    /**
     * Get as many UUID mappings as possible under the condition
     * that the operation cannot be blocking (for an extended amount of time)
     *
     * @return All mappings that could be provided immediately
     */
    @NotNull public final Collection<UUIDMapping> getAllImmediately() {
        final Set<UUIDMapping> mappings = new LinkedHashSet<>();
        for (final UUIDService service : this.getServiceListInstance()) {
            mappings.addAll(service.getImmediately());
        }
        return mappings;
    }

}
