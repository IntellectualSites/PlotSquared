////////////////////////////////////////////////////////////////////////////////////////////////////
// PlotSquared - A plot manager and world generator for the Bukkit API                             /
// Copyright (c) 2014 IntellectualSites/IntellectualCrafters                                       /
//                                                                                                 /
// This program is free software; you can redistribute it and/or modify                            /
// it under the terms of the GNU General Public License as published by                            /
// the Free Software Foundation; either version 3 of the License, or                               /
// (at your option) any later version.                                                             /
//                                                                                                 /
// This program is distributed in the hope that it will be useful,                                 /
// but WITHOUT ANY WARRANTY; without even the implied warranty of                                  /
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the                                   /
// GNU General Public License for more details.                                                    /
//                                                                                                 /
// You should have received a copy of the GNU General Public License                               /
// along with this program; if not, write to the Free Software Foundation,                         /
// Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301  USA                               /
//                                                                                                 /
// You can contact us via: support@intellectualsites.com                                           /
////////////////////////////////////////////////////////////////////////////////////////////////////

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
//            // TODO Auto-generated catch block
//            e.printStackTrace();
//        }
    }
}
