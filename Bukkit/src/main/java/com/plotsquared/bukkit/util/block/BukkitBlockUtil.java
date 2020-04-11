package com.plotsquared.bukkit.util.block;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.world.block.BlockState;
import com.sk89q.worldedit.world.item.ItemType;
import org.bukkit.Material;
import org.bukkit.block.Block;

import java.util.function.Supplier;

public class BukkitBlockUtil {
    public static Supplier<ItemType> supplyItem(Block block) {
        return new Supplier<ItemType>() {
            @Override public ItemType get() {
                return BukkitAdapter.asItemType(block.getType());
            }
        };
    }

    public static Supplier<ItemType> supplyItem(Material type) {
        return () -> BukkitAdapter.asItemType(type);
    }

    public static BlockState get(Block block) {
        return get(block.getType());
    }

    public static BlockState get(Material material) {
        return BukkitAdapter.asBlockType(material).getDefaultState();
    }
}
