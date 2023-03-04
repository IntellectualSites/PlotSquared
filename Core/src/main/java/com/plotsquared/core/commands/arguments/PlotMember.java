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
package com.plotsquared.core.commands.arguments;

import cloud.commandframework.context.CommandContext;
import com.plotsquared.core.commands.parsers.PlotMemberParser;
import com.plotsquared.core.database.DBFunc;
import com.plotsquared.core.player.PlotPlayer;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.UUID;

public sealed interface PlotMember {

    PlotMember EVERYONE = new Everyone();

    default @NonNull UUID uuid(@NonNull CommandContext<PlotPlayer<?>> context) {
        return this.uuid();
    }

    @NonNull UUID uuid();

    sealed interface PlayerLike extends PlotMember {
    }

    record Player(@NonNull UUID uuid) implements PlayerLike {
    }

    final class LazyPlayer implements PlayerLike {

        private final String candidate;
        private final UuidSupplier uuidSupplier;
        private @MonotonicNonNull UUID cachedUuid = null;

        public LazyPlayer(
                final @NonNull String candidate,
                final @NonNull UuidSupplier uuidSupplier
        ) {
            this.candidate = candidate;
            this.uuidSupplier = uuidSupplier;
        }

        public @NonNull UUID uuid() {
            throw new UnsupportedOperationException();
        }

        @Override
        public synchronized @NonNull UUID uuid(final @NonNull CommandContext<PlotPlayer<?>> context) {
            if (this.cachedUuid == null) {
                try {
                    this.cachedUuid = this.uuidSupplier.uuid();
                } catch (Exception ignored) {
                }

                // The player didn't exist :-(
                if (this.cachedUuid == null) {
                    throw new PlotMemberParser.TargetParseException(this.candidate, context);
                }
            }
            return this.cachedUuid;
        }

        @FunctionalInterface
        public interface UuidSupplier {
            @Nullable UUID uuid() throws Exception;
        }
    }

    final class Everyone implements PlotMember {

        @Override
        public @NonNull UUID uuid() {
            return DBFunc.EVERYONE;
        }
    }
}
