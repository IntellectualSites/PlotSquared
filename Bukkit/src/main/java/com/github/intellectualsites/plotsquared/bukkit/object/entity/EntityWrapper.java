package com.github.intellectualsites.plotsquared.bukkit.object.entity;

import com.github.intellectualsites.plotsquared.plot.util.block.BlockUtil;

import lombok.Getter;
import lombok.NonNull;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;

@Getter public abstract class EntityWrapper {

    protected final float yaw;
    protected final float pitch;
    private final Entity entity;
    private final EntityType type;
    public double x;
    public double y;
    public double z;

    EntityWrapper(@NonNull final Entity entity) {
        this.entity = entity;
        this.type = entity.getType();

        final Location location = entity.getLocation();
        this.x = location.getX();
        this.y = location.getY();
        this.z = location.getZ();
        this.yaw = location.getYaw();
        this.pitch = location.getPitch();
    }

    @SuppressWarnings("deprecation") @Override public String toString() {
        return String.format("[%s, x=%s, y=%s, z=%s]", type.getName(), x, y, z);
    }

    public abstract Entity spawn(World world, int xOffset, int zOffset);

    public abstract void saveEntity();

}
