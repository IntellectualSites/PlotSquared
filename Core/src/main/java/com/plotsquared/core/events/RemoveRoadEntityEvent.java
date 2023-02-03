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

import java.util.UUID;

public class RemoveRoadEntityEvent extends EntityEvent implements CancellablePlotEvent {

    private Result eventResult;

    /**
     * RemoveRoadEntityEvent: Called when an entity on road is removed.
     *
     * @param entity The entity to remove
     * @param entityType The type of the entity
     * @param entityUuid The uuid of the entity
     * @param entityId The id of the entity
     */
    public RemoveRoadEntityEvent(Entity entity, EntityType entityType, UUID entityUuid, int entityId) {
        super(entity, entityType, entityUuid, entityId);
    }

    @Override
    public Result getEventResult() {
        return this.eventResult;
    }

    @Override
    public void setEventResult(Result eventResult) {
        this.eventResult = eventResult;
    }

}
