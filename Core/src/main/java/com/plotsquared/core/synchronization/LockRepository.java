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
package com.plotsquared.core.synchronization;

import com.google.common.util.concurrent.Striped;

import javax.annotation.Nonnull;
import java.util.concurrent.locks.Lock;
import java.util.function.Consumer;

/**
 * A repository for keyed {@link java.util.concurrent.locks.Lock locks}
 */
public final class LockRepository {

    private final Striped<Lock> striped;

    public LockRepository() {
        this.striped = Striped.lock(LockKey.recognizedKeys().size());
    }

    /**
     * Get the lock corresponding to the given lock key
     *
     * @param key Lock key
     * @return Lock
     */
    @Nonnull public Lock getLock(@Nonnull final LockKey key) {
        return this.striped.get(key);
    }

    /**
     * Consume a lock
     *
     * @param key      Lock key
     * @param consumer Lock consumer
     */
    public void useLock(@Nonnull final LockKey key, @Nonnull final Consumer<Lock> consumer) {
        consumer.accept(this.getLock(key));
    }

    /**
     * Wait for the lock to become available, and run
     * the given runnable, then unlock the lock. This is
     * a blocking method.
     *
     * @param key      Lock key
     * @param runnable Action to run when the lock is available
     */
    public void useLock(@Nonnull final LockKey key, @Nonnull final Runnable runnable) {
        this.useLock(key, lock -> {
            lock.lock();
            runnable.run();
            lock.unlock();
        });
    }

    /**
     * Wait for a lock to be available, lock it and return
     * an {@link AutoCloseable} instance that locks the key.
     * <p>
     * This is meant to be used with try-with-resources, like such:
     * <pre>{@code
     * try (final LockAccess lockAccess = lockRepository.lock(LockKey.of("your.key"))) {
     *      // use lock
     * }
     * }</pre>
     *
     * @param key Lock key
     * @return Lock access. Must be closed.
     */
    @Nonnull public LockAccess lock(@Nonnull final LockKey key) {
        final Lock lock = this.getLock(key);
        lock.lock();
        return new LockAccess(lock);
    }


    public static class LockAccess implements AutoCloseable {

        private final Lock lock;

        private LockAccess(@Nonnull final Lock lock) {
            this.lock = lock;
        }

        @Override public void close() {
            this.lock.unlock();
        }

    }

}
