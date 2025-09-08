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

import com.sk89q.worldedit.entity.Entity;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * @since 6.11.1
 */
public abstract class EntityEvent {

    private final Entity entity;

    private String name;

    /**
     * @since 6.11.0
     */
    public EntityEvent(Entity entity) {
        this.entity = entity;
    }

    /**
     * Obtain the entity involved in the event
     *
     * @return Entity
     * @since 6.11.0
     */
    public Entity getEntity() {
        return this.entity;
    }

    /**
     * Obtain the event's class name
     *
     * @return the event class name
     * @since 6.11.0
     */
    @NonNull
    public String getEventName() {
        if (this.name == null) {
            this.name = this.getClass().getSimpleName();
        }
        return this.name;
    }

}
