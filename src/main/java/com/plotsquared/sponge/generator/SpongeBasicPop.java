package com.plotsquared.sponge.generator;

import java.util.HashMap;
import java.util.HashSet;

import org.spongepowered.api.world.World;

import com.intellectualcrafters.plot.object.PlotBlock;
import com.intellectualcrafters.plot.object.PlotLoc;
import com.intellectualcrafters.plot.object.PseudoRandom;
import com.intellectualcrafters.plot.object.RegionWrapper;
import com.intellectualcrafters.plot.object.schematic.PlotItem;
import com.intellectualcrafters.plot.util.WorldUtil;
import com.plotsquared.sponge.SpongeMain;

public class SpongeBasicPop extends SpongePlotPopulator<SpongeBasicGen> {
    
    public SpongeBasicPop(final SpongeBasicGen generator) {
        super(generator);
    }
    
    @Override
    public void populate(final World world, final RegionWrapper requiredRegion, final PseudoRandom random, final int cx, final int cz) {
        int sx = (short) ((X - generator.plotworld.ROAD_OFFSET_X) % generator.size);
        int sz = (short) ((Z - generator.plotworld.ROAD_OFFSET_Z) % generator.size);
        if (sx < 0) {
            sx += generator.size;
        }
        if (sz < 0) {
            sz += generator.size;
        }
        for (short x = 0; x < 16; x++) {
            for (short z = 0; z < 16; z++) {
                final int absX = ((sx + x) % generator.size);
                final int absZ = ((sz + z) % generator.size);
                final boolean gx = absX > generator.pathWidthLower;
                final boolean gz = absZ > generator.pathWidthLower;
                final boolean lx = absX < generator.pathWidthUpper;
                final boolean lz = absZ < generator.pathWidthUpper;
                // inside plot
                if (gx && gz && lx && lz) {
                    for (short y = 1; y < generator.plotheight; y++) {
                        setBlock(x, y, z, generator.filling);
                    }
                    setBlock(x, (short) generator.plotheight, z, generator.plotfloors);
                    if ((generator.plotworld.TYPE != 0) && (generator.plotworld.TERRAIN < 2)) {
                        for (int y = generator.plotheight + 1; y < 128; y++) {
                            setBlock(x, y, z, generator.AIR);
                        }
                    }
                    if (generator.plotworld.PLOT_SCHEMATIC) {
                        final PlotLoc loc = new PlotLoc(absX, absZ);
                        final HashMap<Short, Short> blocks = generator.plotworld.G_SCH.get(loc);
                        if (blocks != null) {
                            final HashMap<Short, Byte> datas = generator.plotworld.G_SCH_DATA.get(loc);
                            if (datas != null) {
                                for (final short y : blocks.keySet()) {
                                    final Byte data = datas.get(y);
                                    setBlock(x, (short) (generator.plotheight + y), z, SpongeMain.THIS.getBlockState(new PlotBlock(blocks.get(y), data == null ? 0 : data)));
                                }
                            } else {
                                for (final short y : blocks.keySet()) {
                                    setBlock(x, (short) (generator.plotheight + y), z, SpongeMain.THIS.getBlockState(blocks.get(y)));
                                }
                            }
                        }
                        if (generator.plotworld.G_SCH_STATE != null) {
                            final HashSet<PlotItem> states = generator.plotworld.G_SCH_STATE.get(loc);
                            if (states != null) {
                                for (final PlotItem items : states) {
                                    items.x = X + x;
                                    items.z = Z + z;
                                    WorldUtil.IMP.addItems(generator.plotworld.worldname, items);
                                }
                            }
                        }
                    }
                } else if (generator.pathsize != 0) {
                    // wall
                    if (((absX >= generator.pathWidthLower) && (absX <= generator.pathWidthUpper) && (absZ >= generator.pathWidthLower) && (absZ <= generator.pathWidthUpper))) {
                        for (short y = 1; y <= generator.wallheight; y++) {
                            setBlock(x, y, z, generator.wallfilling);
                        }
                        if ((generator.plotworld.TYPE != 0) && (generator.plotworld.TERRAIN < 3)) {
                            for (int y = generator.wallheight + 2; y < 128; y++) {
                                setBlock(x, y, z, generator.AIR);
                            }
                        }
                        if (!generator.plotworld.ROAD_SCHEMATIC_ENABLED) {
                            setBlock(x, generator.wallheight + 1, z, generator.wall);
                        }
                    }
                    // road
                    else {
                        for (short y = 1; y <= generator.roadheight; y++) {
                            setBlock(x, y, z, generator.roadblock);
                        }
                        if ((generator.plotworld.TYPE != 0) && (generator.plotworld.TERRAIN < 3)) {
                            for (int y = generator.roadheight + 1; y < 128; y++) {
                                setBlock(x, y, z, generator.AIR);
                            }
                        }
                    }
                    if (generator.plotworld.ROAD_SCHEMATIC_ENABLED) {
                        final PlotLoc loc = new PlotLoc(absX, absZ);
                        final HashMap<Short, Short> blocks = generator.plotworld.G_SCH.get(loc);
                        if (blocks != null) {
                            final HashMap<Short, Byte> datas = generator.plotworld.G_SCH_DATA.get(loc);
                            if (datas != null) {
                                for (final short y : blocks.keySet()) {
                                    final Byte data = datas.get(y);
                                    setBlock(x, (short) (generator.plotheight + y), z, SpongeMain.THIS.getBlockState(new PlotBlock(blocks.get(y), data == null ? 0 : data)));
                                }
                            } else {
                                for (final short y : blocks.keySet()) {
                                    setBlock(x, (short) (generator.plotheight + y), z, SpongeMain.THIS.getBlockState(blocks.get(y)));
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
