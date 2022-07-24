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

import com.plotsquared.bukkit.BukkitPlatform;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.metadata.FixedMetadataValue;

public class TeleportEntityWrapper extends EntityWrapper {

    private Location oldLocation;
    private boolean gravityOld;
    private boolean invulnerableOld;
    private int fireTicksOld;
    private int livingTicksOld;

    public TeleportEntityWrapper(final Entity entity) {
        super(entity);
    }

    @Override
    public Entity spawn(final World world, final int xOffset, final int zOffset) {
        if (!getEntity().getLocation().getChunk().equals(oldLocation.getChunk())) {
            final Location oldLocation = this.oldLocation.clone();
            oldLocation.add(xOffset, 0, xOffset);
            getEntity().teleport(oldLocation);
            getEntity().setGravity(gravityOld);
            getEntity().setInvulnerable(invulnerableOld);
            getEntity().setFireTicks(fireTicksOld);
            getEntity().setTicksLived(livingTicksOld);
            getEntity().removeMetadata("ps-tmp-teleport", BukkitPlatform.getPlugin(BukkitPlatform.class));
        }
        return getEntity();
    }

    @Override
    public void saveEntity() {
        if (getEntity().hasMetadata("ps-tmp-teleport")) {
            this.oldLocation = (Location) this.getEntity().getMetadata("ps-tmp-teleport").get(0);
        } else {
            this.oldLocation = this.getEntity().getLocation();
        }

        // To account for offsets in the chunk manager
        this.oldLocation = oldLocation.clone();
        this.oldLocation.setX(this.getX());
        this.oldLocation.setY(this.getY());
        this.oldLocation.setZ(this.getZ());

        this.gravityOld = this.getEntity().hasGravity();
        this.getEntity().setGravity(false);
        this.invulnerableOld = this.getEntity().isInvulnerable();
        this.getEntity().setInvulnerable(true);
        this.fireTicksOld = this.getEntity().getFireTicks();
        this.livingTicksOld = this.getEntity().getTicksLived();
        this.getEntity().setMetadata(
                "ps-tmp-teleport",
                new FixedMetadataValue(BukkitPlatform.getPlugin(BukkitPlatform.class), oldLocation)
        );
        final Chunk newChunk = getNewChunk();
        this.getEntity().teleport(
                new Location(newChunk.getWorld(), newChunk.getX() << 4, 5000, newChunk.getZ() << 4));
    }

    private Chunk getNewChunk() {
        final Chunk oldChunk = oldLocation.getChunk();
        Chunk chunk = null;

        for (Chunk lChunk : oldChunk.getWorld().getLoadedChunks()) {
            if (!lChunk.equals(oldChunk) && lChunk.isLoaded()) {
                chunk = lChunk;
                break;
            }
        }
        if (chunk == null) {
            for (int dx = 1; dx < Integer.MAX_VALUE; dx++) {
                for (int dz = 0; dz < Integer.MAX_VALUE; dz++) {
                    if ((chunk = getChunkRelative(oldChunk, dx, dz)).isLoaded()) {
                        break;
                    } else if ((chunk = getChunkRelative(oldChunk, -dx, dz)).isLoaded()) {
                        break;
                    } else if ((chunk = getChunkRelative(oldChunk, dx, -dz)).isLoaded()) {
                        break;
                    } else if ((chunk = getChunkRelative(oldChunk, -dx, -dz)).isLoaded()) {
                        break;
                    }
                }
            }
        }
        return chunk;
    }

    private Chunk getChunkRelative(final Chunk chunk, final int dx, final int dz) {
        return chunk.getWorld().getChunkAt(chunk.getX() + dx, chunk.getZ() + dz);
    }

}
