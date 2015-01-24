package com.intellectualcrafters.plot.generator;

import java.util.HashSet;
import java.util.Random;

import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.generator.BlockPopulator;

import com.intellectualcrafters.plot.object.BlockWrapper;
import com.intellectualcrafters.plot.object.PlotBlock;
import com.intellectualcrafters.plot.object.PlotGenerator;
import com.intellectualcrafters.plot.object.PlotId;
import com.intellectualcrafters.plot.object.PlotManager;
import com.intellectualcrafters.plot.object.PlotRegion;
import com.intellectualcrafters.plot.object.PlotWorld;
import com.intellectualcrafters.plot.util.PlotHelper;

public class AugmentedPopulator extends BlockPopulator {

	public final PlotGenerator generator;
	public final PlotWorld plotworld;
	public final PlotManager manager;
	public HashSet<PlotRegion> regions;
	
	
	public BlockWrapper getBlock(int i, int j, short[][] result) {
		int y_1 = (i << 4);
		int y_2 = (j >> 8);
		int y = y_1 + y_2;
		int z_1 = (j - ((y & 0xF) << 8));
		int z = (z_1 >> 4);
		int x = z_1 - (z << 4);
		
		short id = result[i][j];
		return new BlockWrapper(x, y, z, id, (byte) 0);
	}
	
	public AugmentedPopulator(PlotGenerator generator, PlotWorld plotworld, PlotManager manager, HashSet<PlotRegion> regions) {
		this.generator = generator;
		this.plotworld = plotworld;
		this.manager = manager;
		this.regions = regions;
	}
	
	// Check if the augmented populator contains the plot id
	public boolean contains(PlotId id) {
		// TODO check if any regions contain the id
		return false;
	}
	
	/**
	 * Returns false if the proposed region overlaps with an existing region
	 */
	public boolean addRegion(PlotRegion region) {
		boolean contains = false; //TODO check if any regions contain these plots
		if (contains) {// contains
			return false;
		}
		return regions.add(region);
	}
	
	@Override
	public void populate(World world, Random rand, Chunk chunk) {
		int X = chunk.getX();
		int Z = chunk.getZ();
		int x = X << 4;
		int z = Z << 4;
		short[][] result = generator.generateExtBlockSections(world, rand, X, Z, null);
		int d2_length = result[0].length;
		for(int i = 0; i < result.length; i++) {
			for(int j = 0; j < d2_length; j++) {
				BlockWrapper blockInfo = getBlock(i, j, result);
				PlotBlock plotblock = new PlotBlock((short) blockInfo.id, (byte) 0 );
				Block block = world.getBlockAt(x + blockInfo.x, blockInfo.y, z + blockInfo.z);
				PlotHelper.setBlock(block, plotblock);
			}
		}
	}
}
