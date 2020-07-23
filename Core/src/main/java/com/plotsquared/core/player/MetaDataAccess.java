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
package com.plotsquared.core.player;

import com.plotsquared.core.synchronization.LockRepository;

import javax.annotation.Nonnull;
import java.util.Optional;

/**
 * Access to player meta data
 *
 * @param <P> Player type
 * @param <T> Meta data type
 */
public abstract class MetaDataAccess<T> implements AutoCloseable {

    private final PlotPlayer<?> player;
    private final MetaDataKey<T> metaDataKey;
    private final LockRepository.LockAccess lockAccess;

    MetaDataAccess(@Nonnull final PlotPlayer<?> player, @Nonnull final MetaDataKey<T> metaDataKey,
        @Nonnull final LockRepository.LockAccess lockAccess) {
        this.player = player;
        this.metaDataKey = metaDataKey;
        this.lockAccess = lockAccess;
    }

    /**
     * Check if the player has meta data stored with the given key
     *
     * @return {@code true} if player has meta data with this key, or
     * {@code false}
     */
    public abstract boolean has();

    /**
     * Remove the stored value meta data
     */
    public abstract void remove();

    /**
     * Set the meta data value
     *
     * @param value New value
     */
    public abstract void set(@Nonnull final T value);

    /**
     * Get the stored meta data value
     *
     * @return Stored value, or {@link Optional#empty()}
     */
    @Nonnull public abstract Optional<T> get();

    @Override public final void close() {
        this.lockAccess.close();
    }

    /**
     * Get the owner of the meta data
     *
     * @return Player
     */
    @Nonnull public PlotPlayer<?> getPlayer() {
        return this.player;
    }

    /**
     * Get the meta data key
     *
     * @return Meta data key
     */
    @Nonnull public MetaDataKey<T> getMetaDataKey() {
        return this.metaDataKey;
    }

}
