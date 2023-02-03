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
import com.sk89q.worldedit.world.entity.EntityType;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.UUID;

public abstract class EntityEvent {

    private final Entity entity;
    private final EntityType entityType;

    private final UUID entityUuid;
    private final int entityId;

    private String name;

    public EntityEvent(Entity entity, final EntityType entityType, final UUID entityUuid, final int entityId) {
        this.entity = entity;
        this.entityType = entityType;
        this.entityUuid = entityUuid;
        this.entityId = entityId;
    }

    /**
     * Obtain the entity involved in the event
     *
     * @return Entity
     */
    public Entity getEntity() {
        return this.entity;
    }

    /**
     * Obtain the {@link EntityType} of the entity involved in the event
     *
     * @return {@link EntityType} of entity
     */
    public EntityType getEntityType() {
        return this.entityType;
    }

    /**
     * Obtain the UUID of the entity involved in the event
     *
     * @return UUID of entity
     */
    public UUID getEntityUuid() {
        return this.entityUuid;
    }

    /**
     * Obtain the entity id of the entity involved in the event
     *
     * @return id of entity
     */
    public int getEntityId() {
        return this.entityId;
    }

    public String getName() {
        return this.name;
    }

    /**
     * Obtain the event's class name
     *
     * @return the event class name
     */
    public @NonNull String getEventName() {
        if (this.name == null) {
            this.name = this.getClass().getSimpleName();
        }
        return this.name;
    }

}
