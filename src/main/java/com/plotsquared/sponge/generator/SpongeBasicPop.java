package com.plotsquared.sponge.generator;

import java.util.HashMap;
import java.util.HashSet;

import org.spongepowered.api.world.World;

import com.intellectualcrafters.plot.object.PlotBlock;
import com.intellectualcrafters.plot.object.PlotLoc;
import com.intellectualcrafters.plot.object.PseudoRandom;
import com.intellectualcrafters.plot.object.RegionWrapper;
import com.intellectualcrafters.plot.object.schematic.PlotItem;
import com.intellectualcrafters.plot.util.BlockManager;
import com.plotsquared.sponge.SpongeMain;

public class SpongeBasicPop extends SpongePlotPopulator<SpongeBasicGen> {

    public SpongeBasicPop(SpongeBasicGen generator) {
        super(generator);
    }

    @Override
    public void populate(World world, RegionWrapper requiredRegion, PseudoRandom random, int cx, int cz) {
        int sx = (short) ((this.X - generator.plotworld.ROAD_OFFSET_X) % generator.size);
        int sz = (short) ((this.Z - generator.plotworld.ROAD_OFFSET_Z) % generator.size);
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
                    if (generator.plotworld.PLOT_SCHEMATIC) {
                        final PlotLoc loc = new PlotLoc(absX, absZ);
                        final HashMap<Short, Short> blocks = generator.plotworld.G_SCH.get(loc);
                        if (blocks != null) {
                            final HashMap<Short, Byte> datas = generator.plotworld.G_SCH_DATA.get(loc);
                            for (final short y : blocks.keySet()) {
                                Byte data = datas.get(y);
                                setBlock(x, (short) (generator.plotheight + y), z, SpongeMain.THIS.getBlockState(new PlotBlock(blocks.get(y), data == null ? 0 : data)));
                            }
                        }
                        if (generator.plotworld.G_SCH_STATE != null) {
                            HashSet<PlotItem> states = generator.plotworld.G_SCH_STATE.get(loc);
                            if (states != null) {
                                for (PlotItem items : states) {
                                    items.x = this.X + x;
                                    items.z = this.Z + z;
                                    BlockManager.manager.addItems(generator.plotworld.worldname, items);
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
                        if (!generator.plotworld.ROAD_SCHEMATIC_ENABLED) {
                            setBlock(x, generator.wallheight + 1, z, generator.wall);
                        }
                    }
                    // road
                    else {
                        for (short y = 1; y <= generator.roadheight; y++) {
                            setBlock(x, y, z, generator.roadblock);
                        }
                    }
                    if (generator.plotworld.ROAD_SCHEMATIC_ENABLED) {
                        final PlotLoc loc = new PlotLoc(absX, absZ);
                        final HashMap<Short, Short> blocks = generator.plotworld.G_SCH.get(loc);
                        if (blocks != null) {
                            final HashMap<Short, Byte> datas = generator.plotworld.G_SCH_DATA.get(loc);
                            for (final short y : blocks.keySet()) {
                                Byte data = datas.get(y);
                                setBlock(x, (short) (generator.plotheight + y), z, SpongeMain.THIS.getBlockState(new PlotBlock(blocks.get(y), data == null ? 0 : data)));
                            }
                        }
                    }
                }
            }
        }
    }
}
