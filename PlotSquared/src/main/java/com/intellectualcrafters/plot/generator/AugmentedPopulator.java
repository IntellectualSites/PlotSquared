package com.intellectualcrafters.plot.generator;

import java.util.HashSet;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.generator.BlockPopulator;

import com.intellectualcrafters.plot.object.BlockWrapper;
import com.intellectualcrafters.plot.object.PlotBlock;
import com.intellectualcrafters.plot.object.PlotGenerator;
import com.intellectualcrafters.plot.object.PlotId;
import com.intellectualcrafters.plot.object.PlotManager;
import com.intellectualcrafters.plot.object.PlotCluster;
import com.intellectualcrafters.plot.object.PlotWorld;
import com.intellectualcrafters.plot.util.PlotHelper;

public class AugmentedPopulator extends BlockPopulator {

	public final PlotWorld plotworld;
	public final PlotManager manager;
	public final PlotGenerator generator;
	
	private final int botx;
	private final int botz;
	private final int topx;
	private final int topz;
	
	public BlockWrapper getBlock(int X, int Z, int i, int j, short[][] result, boolean check) {
		int y_1 = (i << 4);
		int y_2 = (j >> 8);
		int y = y_1 + y_2;
		int z_1 = (j - ((y & 0xF) << 8));
		int z = (z_1 >> 4);
		int x;
		if (check) {
			if (z < botz || z > topz) {
				return null;
			}
			x = z_1 - (z << 4);
			if (x < botx || x > topx) {
				return null;
			}
		}
		else {
			x = z_1 - (z << 4);
		}
		short id = result[i][j];
		return new BlockWrapper(x, y, z, id, (byte) 0);
	}
	
	public AugmentedPopulator(String world, PlotGenerator generator, PlotCluster cluster) {
		this.generator = generator;
		this.plotworld = generator.getNewPlotWorld(world);
		this.manager = generator.getPlotManager();

		World bukkitWorld = Bukkit.getWorld(world);
		
		Location bot = manager.getPlotBottomLocAbs(plotworld, cluster.getP1());
		Location top = manager.getPlotTopLocAbs(plotworld, cluster.getP2()).add(1,0,1);
		
		this.botx = bot.getBlockX();
		this.botz = bot.getBlockZ();
		
		this.topx = top.getBlockX();
		this.topz = top.getBlockZ();
		
		// Add the populator
		bukkitWorld.getPopulators().add(this);
	}
	
	@Override
	public void populate(World world, Random rand, Chunk chunk) {
		int X = chunk.getX();
		int Z = chunk.getZ();
		int x = X << 4;
		int z = Z << 4;
		int x2 = x + 15;
		int z2 = z + 15;
		
		boolean inX1 = (x > botx && x < topx);
		boolean inX2 = (x2 > botx && x2 < topx);
		boolean inZ1 = (z > botz && z < topz);
		boolean inZ2 = (z2 > botz && z2 < topz);
		
		
		boolean inX = inX1 || inX2;
		boolean inZ = inZ1 || inZ2;

		if (!inX || !inZ) {
			return;
		}
		boolean check;
		if (!inX1 || !inX2 || !inZ1 || inZ2) {
			check = true;
		}
		else {
			check = false;
		}
		short[][] result = generator.generateExtBlockSections(world, rand, X, Z, null);
		int d2_length = result[0].length;
		for(int i = 0; i < result.length; i++) {
			for(int j = 0; j < d2_length; j++) {
				BlockWrapper blockInfo = getBlock(x, z, i, j, result, check);
				if (blockInfo == null) {
					continue;
				}
				PlotBlock plotblock = new PlotBlock((short) blockInfo.id, (byte) 0 );
				Block block = world.getBlockAt(x + blockInfo.x, blockInfo.y, z + blockInfo.z);
				PlotHelper.setBlock(block, plotblock);
			}
		}
	}
}