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
package com.plotsquared.bukkit.entity;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.checkerframework.checker.nullness.qual.NonNull;

public abstract class EntityWrapper {

    protected final float yaw;
    protected final float pitch;
    private final Entity entity;
    private final EntityType type;
    public double x;
    public double y;
    public double z;

    EntityWrapper(final @NonNull Entity entity) {
        this.entity = entity;
        this.type = entity.getType();

        final Location location = entity.getLocation();
        this.x = location.getX();
        this.y = location.getY();
        this.z = location.getZ();
        this.yaw = location.getYaw();
        this.pitch = location.getPitch();
    }

    @SuppressWarnings("deprecation")
    @Override
    public String toString() {
        return String.format("[%s, x=%s, y=%s, z=%s]", type.getName(), x, y, z);
    }

    public abstract Entity spawn(World world, int xOffset, int zOffset);

    public abstract void saveEntity();

    public float getYaw() {
        return this.yaw;
    }

    public float getPitch() {
        return this.pitch;
    }

    public Entity getEntity() {
        return this.entity;
    }

    public EntityType getType() {
        return this.type;
    }

    public double getX() {
        return this.x;
    }

    public double getY() {
        return this.y;
    }

    public double getZ() {
        return this.z;
    }

}
