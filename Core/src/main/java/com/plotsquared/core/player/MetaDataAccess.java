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
package com.plotsquared.core.player;

import com.plotsquared.core.synchronization.LockRepository;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Optional;

/**
 * Access to player meta data
 *
 * @param <T> Meta data type
 */
public abstract class MetaDataAccess<T> implements AutoCloseable {

    private final PlotPlayer<?> player;
    private final MetaDataKey<T> metaDataKey;
    private final LockRepository.LockAccess lockAccess;
    private boolean closed = false;

    MetaDataAccess(
            final @NonNull PlotPlayer<?> player,
            final @NonNull MetaDataKey<T> metaDataKey,
            final LockRepository.@NonNull LockAccess lockAccess
    ) {
        this.player = player;
        this.metaDataKey = metaDataKey;
        this.lockAccess = lockAccess;
    }

    @SuppressWarnings("unchecked")
    private static <E extends Throwable> void sneakyThrow(final Throwable e) throws E {
        throw (E) e;
    }

    /**
     * Check if the player has meta data stored with the given key
     *
     * @return {@code true} if player has meta data with this key, or
     *         {@code false}
     */
    public abstract boolean isPresent();

    /**
     * Remove the stored value meta data
     *
     * @return Old value, or {@code null}
     */
    public @Nullable
    abstract T remove();

    /**
     * Set the meta data value
     *
     * @param value New value
     */
    public abstract void set(final @NonNull T value);

    /**
     * Get the stored meta data value
     *
     * @return Stored value, or {@link Optional#empty()}
     */
    public @NonNull
    abstract Optional<T> get();

    @Override
    public final void close() {
        this.lockAccess.close();
        this.closed = true;
    }

    /**
     * Get the owner of the meta data
     *
     * @return Player
     */
    public @NonNull PlotPlayer<?> getPlayer() {
        return this.player;
    }

    /**
     * Get the meta data key
     *
     * @return Meta data key
     */
    public @NonNull MetaDataKey<T> getMetaDataKey() {
        return this.metaDataKey;
    }

    /**
     * Check whether or not the meta data access has been closed.
     * After being closed, all attempts to access the meta data
     * through the instance, will lead to {@link IllegalAccessException}
     * being thrown
     *
     * @return {@code true} if the access has been closed
     */
    public boolean isClosed() {
        return this.closed;
    }

    protected void checkClosed() {
        if (this.closed) {
            sneakyThrow(new IllegalAccessException("The meta data access instance has been closed"));
        }
    }


}
