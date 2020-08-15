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
package com.plotsquared.core.player;

import com.plotsquared.core.synchronization.LockRepository;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Optional;

final class PersistentMetaDataAccess<T> extends MetaDataAccess<T> {

    PersistentMetaDataAccess(@Nonnull final PlotPlayer<?> player,
                             @Nonnull final MetaDataKey<T> metaDataKey,
                             @Nonnull final LockRepository.LockAccess lockAccess) {
        super(player, metaDataKey, lockAccess);
    }

    @Override public boolean isPresent() {
        this.checkClosed();
        return this.getPlayer().hasPersistentMeta(getMetaDataKey().toString());
    }

    @Override @Nullable public T remove() {
        this.checkClosed();
        final Object old = this.getPlayer().removePersistentMeta(this.getMetaDataKey().toString());
        if (old == null) {
            return null;
        }
        return (T) old;
    }

    @Override public void set(@Nonnull T value) {
        this.checkClosed();
        this.getPlayer().setPersistentMeta(this.getMetaDataKey(), value);
    }

    @Nonnull @Override public Optional<T> get() {
        this.checkClosed();
        return Optional.ofNullable(this.getPlayer().getPersistentMeta(this.getMetaDataKey()));
    }

}
