package com.github.intellectualsites.plotsquared.bukkit.object.entity;

import com.github.intellectualsites.plotsquared.plot.util.block.BlockUtil;

import com.github.intellectualsites.plotsquared.bukkit.BukkitMain;
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

    @Override public Entity spawn(final World world, final int xOffset, final int zOffset) {
        if (!getEntity().getLocation().getChunk().equals(oldLocation.getChunk())) {
            final Location oldLocation = this.oldLocation.clone();
            oldLocation.add(xOffset, 0, xOffset);
            getEntity().teleport(oldLocation);
            getEntity().setGravity(gravityOld);
            getEntity().setInvulnerable(invulnerableOld);
            getEntity().setFireTicks(fireTicksOld);
            getEntity().setTicksLived(livingTicksOld);
            getEntity().removeMetadata("ps-tmp-teleport", BukkitMain.getPlugin(BukkitMain.class));
        }
        return getEntity();
    }

    @Override public void saveEntity() {
        if (getEntity().hasMetadata("ps-tmp-teleport")) {
            this.oldLocation = (Location) this.getEntity().getMetadata("ps-tmp-teleport").get(0);
        } else {
            this.oldLocation = this.getEntity().getLocation();
        }

        // To account for offsets in the chunk manager
        this.oldLocation = oldLocation.clone();
        this.oldLocation.setX(this.x);
        this.oldLocation.setY(this.y);
        this.oldLocation.setZ(this.z);

        this.gravityOld = this.getEntity().hasGravity();
        this.getEntity().setGravity(false);
        this.invulnerableOld = this.getEntity().isInvulnerable();
        this.getEntity().setInvulnerable(true);
        this.fireTicksOld = this.getEntity().getFireTicks();
        this.livingTicksOld = this.getEntity().getTicksLived();
        this.getEntity().setMetadata("ps-tmp-teleport",
            new FixedMetadataValue(BukkitMain.getPlugin(BukkitMain.class), oldLocation));
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
