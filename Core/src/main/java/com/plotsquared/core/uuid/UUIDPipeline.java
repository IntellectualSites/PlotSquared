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
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.plotsquared.core.uuid;

import com.google.common.collect.Lists;
import com.plotsquared.core.PlotSquared;
import com.plotsquared.core.configuration.Settings;
import com.plotsquared.core.configuration.caption.TranslatableCaption;
import com.plotsquared.core.player.ConsolePlayer;
import com.plotsquared.core.util.ThreadUtils;
import com.plotsquared.core.util.task.TaskManager;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
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

    private static final Logger logger = LoggerFactory.getLogger("P2/" + UUIDPipeline.class.getSimpleName());
    private static final MiniMessage MINI_MESSAGE = MiniMessage.builder().build();

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
    public UUIDPipeline(@Nonnull final Executor executor) {
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
    public void registerService(@Nonnull final UUIDService uuidService) {
        this.serviceList.add(uuidService);
    }

    /**
     * Register a mapping consumer
     *
     * @param mappingConsumer Consumer to register
     */
    public void registerConsumer(@Nonnull final Consumer<List<UUIDMapping>> mappingConsumer) {
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
    public void consume(@Nonnull final List<UUIDMapping> mappings) {
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
    public void consume(@Nonnull final UUIDMapping mapping) {
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
    public void storeImmediately(@Nonnull final String username, @Nonnull final UUID uuid) {
        this.consume(new UUIDMapping(uuid, username));
    }

    /**
     * Get a single UUID from a username. This is blocking.
     *
     * @param username Username
     * @param timeout  Timeout in milliseconds
     * @return The mapped uuid. Will return null if the request timed out.
     */
    @Nullable public UUID getSingle(@Nonnull final String username, final long timeout) {
        ThreadUtils.catchSync("Blocking UUID retrieval from the main thread");
        try {
            final List<UUIDMapping> mappings = this.getUUIDs(Collections.singletonList(username)).get(timeout, TimeUnit.MILLISECONDS);
            if (mappings.size() == 1) {
                return mappings.get(0).getUuid();
            }
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        } catch (TimeoutException ignored) {
            // This is completely valid, we just don't care anymore
            if (Settings.DEBUG) {
                logger.warn("[P2] (UUID) Request for {} timed out", username);
            }
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
    @Nullable public String getSingle(@Nonnull final UUID uuid, final long timeout) {
        ThreadUtils.catchSync("Blocking username retrieval from the main thread");
        try {
            final List<UUIDMapping> mappings = this.getNames(Collections.singletonList(uuid)).get(timeout, TimeUnit.MILLISECONDS);
            if (mappings.size() == 1) {
                return mappings.get(0).getUsername();
            }
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        } catch (TimeoutException ignored) {
            // This is completely valid, we just don't care anymore
            if (Settings.DEBUG) {
                logger.warn("[P2] (UUID) Request for {} timed out", uuid);
            }
        }
        return null;
    }

    /**
     * Get a single UUID from a username. This is non-blocking.
     *
     * @param username Username
     * @param uuid     UUID consumer
     */
    public void getSingle(@Nonnull final String username, @Nonnull final BiConsumer<UUID, Throwable> uuid) {
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
    public void getSingle(@Nonnull final UUID uuid, @Nonnull final BiConsumer<String, Throwable> username) {
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
    public CompletableFuture<List<UUIDMapping>> getNames(@Nonnull final Collection<UUID> requests, final long timeout) {
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
    public CompletableFuture<List<UUIDMapping>> getUUIDs(@Nonnull final Collection<String> requests, final long timeout) {
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
    public CompletableFuture<List<UUIDMapping>> getNames(@Nonnull final Collection<UUID> requests) {
        if (requests.isEmpty()) {
            return CompletableFuture.completedFuture(Collections.emptyList());
        }

        final List<UUIDService> serviceList = this.getServiceListInstance();
        final List<UUIDMapping> mappings = new ArrayList<>(requests.size());
        final List<UUID> remainingRequests = new ArrayList<>(requests);

        for (final UUIDService service : serviceList) {
            // We can chain multiple synchronous
            // ones in a row
            if (service.canBeSynchronous()) {
                final List<UUIDMapping> completedRequests = service.getNames(remainingRequests);
                for (final UUIDMapping mapping : completedRequests) {
                    remainingRequests.remove(mapping.getUuid());
                }
                mappings.addAll(completedRequests);
            } else {
                break;
            }
            if (remainingRequests.isEmpty()) {
                return CompletableFuture.completedFuture(mappings);
            }
        }

        return CompletableFuture.supplyAsync(() -> {
            for (final UUIDService service : serviceList) {
                final List<UUIDMapping> completedRequests = service.getNames(remainingRequests);
                for (final UUIDMapping mapping : completedRequests) {
                    remainingRequests.remove(mapping.getUuid());
                }
                mappings.addAll(completedRequests);
                if (remainingRequests.isEmpty()) {
                    break;
                }
            }

            if (mappings.size() == requests.size()) {
                this.consume(mappings);
                return mappings;
            } else if (Settings.DEBUG) {
                logger.info("[P2] (UUID) Failed to find all usernames");
            }

            if (Settings.UUID.UNKNOWN_AS_DEFAULT) {
                for (final UUID uuid : remainingRequests) {
                    mappings.add(new UUIDMapping(uuid,
                        MINI_MESSAGE.stripTokens(TranslatableCaption.of("info.unknown").getComponent(ConsolePlayer.getConsole()))));
                }
                return mappings;
            } else {
                throw new ServiceError("End of pipeline");
            }
        }, this.executor);
    }

    /**
     * Asynchronously attempt to fetch the mapping from a list of names
     *
     * @param requests Names
     * @return Mappings
     */
    public CompletableFuture<List<UUIDMapping>> getUUIDs(@Nonnull final Collection<String> requests) {
        if (requests.isEmpty()) {
            return CompletableFuture.completedFuture(Collections.emptyList());
        }

        final List<UUIDService> serviceList = this.getServiceListInstance();
        final List<UUIDMapping> mappings = new ArrayList<>(requests.size());
        final List<String> remainingRequests = new ArrayList<>(requests);

        for (final UUIDService service : serviceList) {
            // We can chain multiple synchronous
            // ones in a row
            if (service.canBeSynchronous()) {
                final List<UUIDMapping> completedRequests = service.getUUIDs(remainingRequests);
                for (final UUIDMapping mapping : completedRequests) {
                    remainingRequests.remove(mapping.getUsername());
                }
                mappings.addAll(completedRequests);
            } else {
                break;
            }
            if (remainingRequests.isEmpty()) {
                return CompletableFuture.completedFuture(mappings);
            }
        }

        return CompletableFuture.supplyAsync(() -> {
            for (final UUIDService service : serviceList) {
                final List<UUIDMapping> completedRequests = service.getUUIDs(remainingRequests);
                for (final UUIDMapping mapping : completedRequests) {
                    remainingRequests.remove(mapping.getUsername());
                }
                mappings.addAll(completedRequests);
                if (remainingRequests.isEmpty()) {
                    break;
                }
            }

            if (mappings.size() == requests.size()) {
                this.consume(mappings);
                return mappings;
            } else if (Settings.DEBUG) {
                logger.info("[P2] (UUID) Failed to find all UUIDs");
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
    @Nonnull public final Collection<UUIDMapping> getAllImmediately() {
        final Set<UUIDMapping> mappings = new LinkedHashSet<>();
        for (final UUIDService service : this.getServiceListInstance()) {
            mappings.addAll(service.getImmediately());
        }
        return mappings;
    }

    /**
     * Get a single UUID mapping immediately, if possible
     *
     * @param object Username ({@link String}) or {@link UUID}
     * @return Mapping, if it could be found immediately
     */
    @Nullable public final UUIDMapping getImmediately(@Nonnull final Object object) {
        for (final UUIDService uuidService : this.getServiceListInstance()) {
            final UUIDMapping mapping = uuidService.getImmediately(object);
            if (mapping != null) {
                return mapping;
            }
        }
        return null;
    }

}
