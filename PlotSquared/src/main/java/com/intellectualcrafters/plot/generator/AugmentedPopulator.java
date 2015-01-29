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
	
	private final int bx;
	private final int bz;
	private final int tx;
	private final int tz;
	
	public BlockWrapper getBlock(int X, int Z, int i, int j, short[][] r, boolean c) {
		int y = (i << 4) + (j >> 8);
		int a = (j - ((y & 0xF) << 8));
		int z = (a >> 4);
		int x = a - (z << 4);
		return (c && (z < bz || z > tz || x < bx || x > tx)) ? null : new BlockWrapper(x, y, z, r[i][j], (byte) 0);
	}
	
	public AugmentedPopulator(String world, PlotGenerator generator, PlotCluster cluster) {
		this.generator = generator;
		this.plotworld = generator.getNewPlotWorld(world);
		this.manager = generator.getPlotManager();

		World bukkitWorld = Bukkit.getWorld(world);
		
		Location b = manager.getPlotBottomLocAbs(plotworld, cluster.getP1());
		Location t = manager.getPlotTopLocAbs(plotworld, cluster.getP2()).add(1,0,1);
		
		this.bx = b.getBlockX();
		this.bz = b.getBlockZ();
		
		this.tx = t.getBlockX();
		this.tz = t.getBlockZ();
		
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
		
		boolean inX1 = (x > bx && x < tx);
		boolean inX2 = (x2 > bx && x2 < tx);
		boolean inZ1 = (z > bz && z < tz);
		boolean inZ2 = (z2 > bz && z2 < tz);
		
		
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