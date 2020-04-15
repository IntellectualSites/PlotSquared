package com.plotsquared.core.plot.schematic;

import com.sk89q.worldedit.entity.BaseEntity;
import com.sk89q.worldedit.entity.Entity;
import com.sk89q.worldedit.extent.Extent;
import com.sk89q.worldedit.util.Location;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * An implementation of {@link Entity} that stores a {@link BaseEntity} with it.
 *
 * <p>Calls to {@link #getState()} return a clone.</p>
 */
abstract class StoredEntity implements Entity {

    private final Location location;
    private final BaseEntity entity;

    /**
     * Create a new instance.
     *
     * @param location the location
     * @param entity   the entity (which will be copied)
     */
    StoredEntity(Location location, BaseEntity entity) {
        checkNotNull(location);
        checkNotNull(entity);
        this.location = location;
        this.entity = new BaseEntity(entity);
    }

    /**
     * Get the entity state. This is not a copy.
     *
     * @return the entity
     */
    BaseEntity getEntity() {
        return entity;
    }

    @Override public BaseEntity getState() {
        return new BaseEntity(entity);
    }

    @Override public Location getLocation() {
        return location;
    }

    @Override public Extent getExtent() {
        return location.getExtent();
    }

}
