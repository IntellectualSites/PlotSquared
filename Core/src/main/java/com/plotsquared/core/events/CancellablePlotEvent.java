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
package com.plotsquared.core.events;


import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * PlotSquared event with {@link Result} to cancel, force, or allow.
 */
public interface CancellablePlotEvent {

    @Nullable Result getEventResult();

    void setEventResult(@Nullable Result eventResult);

    /**
     * @deprecated No usage and not null-safe
     */
    @Deprecated(since = "TODO")
    default int getEventResultRaw() {
        return getEventResult() != null ? getEventResult().getValue() : -1;
    }

}
