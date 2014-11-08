package com.intellectualcrafters.jnbt;

import org.bukkit.World;

public class WorldEditUtils {
    public static void setNBT(final World world, final short id, final byte data, final int x, final int y, final int z, final com.intellectualcrafters.jnbt.CompoundTag tag) {

//        final LocalWorld bukkitWorld = BukkitUtil.getLocalWorld(world);

        // I need to somehow convert our CompoundTag to WorldEdit's

//        final BaseBlock block = new BaseBlock(5, 5, (CompoundTag) tag);
//        final Vector vector = new Vector(x, y, z);
//        try {
//            bukkitWorld.setBlock(vector, block);
//        }
//        catch (final WorldEditException e) {
//            e.printStackTrace();
//        }
    }
}
