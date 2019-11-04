package com.github.intellectualsites.plotsquared.bukkit.object;

import com.github.intellectualsites.plotsquared.plot.util.block.BlockUtil;

import com.github.intellectualsites.plotsquared.plot.object.LazyBlock;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.world.block.BlockState;
import com.sk89q.worldedit.world.block.BlockState;
import org.bukkit.Material;
import org.bukkit.block.Block;

import java.util.function.Supplier;

public class BukkitBlockUtil {
    public static Supplier<BlockState> supply(Block block) {
        return () -> BukkitAdapter.asBlockType(block.getType()).getDefaultState();
    }

    public static Supplier<BlockState> supply(Material type) {
        return () -> BukkitAdapter.asBlockType(type).getDefaultState();
    }

    public static BlockState get(Block block) {
        return get(block.getType());
    }

    public static BlockState get(Material material) {
        return BukkitAdapter.asBlockType(material).getDefaultState();
    }
}
