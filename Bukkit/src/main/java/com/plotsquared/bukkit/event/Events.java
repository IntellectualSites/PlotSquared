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
package com.plotsquared.bukkit.event;

import org.bukkit.Bukkit;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;

/**
 * Small utility to fire an event and cancel the original if the fired event was cancelled.
 */
public final class Events {

    private Events() {}

    /**
     * Fire the {@code eventToFire} and cancel the original if the fired event is cancelled.
     *
     * @param original the original event to potentially cancel
     * @param eventToFire the event to fire to consider cancelling the original event
     * @param <T> an event that can be fired and is cancellable
     * @return true if the event was fired and it caused the original event to be cancelled
     */
    public static <T extends Event & Cancellable> boolean fireToCancel(Cancellable original, T eventToFire) {
        Bukkit.getServer().getPluginManager().callEvent(eventToFire);
        if (eventToFire.isCancelled()) {
            original.setCancelled(true);
            return true;
        }

        return false;
    }
}
