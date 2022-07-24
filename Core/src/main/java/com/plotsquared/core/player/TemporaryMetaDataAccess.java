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

final class TemporaryMetaDataAccess<T> extends MetaDataAccess<T> {

    TemporaryMetaDataAccess(
            final @NonNull PlotPlayer<?> player,
            final @NonNull MetaDataKey<T> metaDataKey,
            final LockRepository.@NonNull LockAccess lockAccess
    ) {
        super(player, metaDataKey, lockAccess);
    }

    @Override
    public boolean isPresent() {
        this.checkClosed();
        return this.getPlayer().getMeta(this.getMetaDataKey().toString()) != null;
    }

    @SuppressWarnings("unchecked")
    @Override
    public @Nullable T remove() {
        this.checkClosed();
        final Object old = getPlayer().deleteMeta(this.getMetaDataKey().toString());
        if (old == null) {
            return null;
        }
        return (T) old;
    }

    @Override
    public void set(final @NonNull T value) {
        this.checkClosed();
        this.getPlayer().setMeta(this.getMetaDataKey().toString(), value);
    }

    @NonNull
    @Override
    public Optional<T> get() {
        this.checkClosed();
        return Optional.ofNullable(this.getPlayer().getMeta(this.getMetaDataKey().toString()));
    }

}
