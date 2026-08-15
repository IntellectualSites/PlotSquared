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

import com.google.common.util.concurrent.Striped;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.concurrent.locks.Lock;
import java.util.function.Consumer;

/**
 * A repository for keyed {@link java.util.concurrent.locks.Lock locks}
 */
@SuppressWarnings("UnstableApiUsage")
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
    public @NonNull Lock getLock(final @NonNull LockKey key) {
        return this.striped.get(key);
    }

    /**
     * Consume a lock
     *
     * @param key      Lock key
     * @param consumer Lock consumer
     */
    public void useLock(final @NonNull LockKey key, final @NonNull Consumer<Lock> consumer) {
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
    public void useLock(final @NonNull LockKey key, final @NonNull Runnable runnable) {
        try (LockAccess ignored = lock(key)) {
            runnable.run();
        }
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
    public @NonNull LockAccess lock(final @NonNull LockKey key) {
        final Lock lock = this.getLock(key);
        lock.lock();
        return new LockAccess(lock);
    }


    public static class LockAccess implements AutoCloseable {

        private final Lock lock;

        private LockAccess(final @NonNull Lock lock) {
            this.lock = lock;
        }

        @Override
        public void close() {
            this.lock.unlock();
        }

    }

}
