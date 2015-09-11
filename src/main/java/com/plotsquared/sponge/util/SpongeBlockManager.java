package com.plotsquared.sponge.util;

import java.util.ArrayList;
import java.util.List;

import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.block.tileentity.Sign;
import org.spongepowered.api.block.tileentity.TileEntity;
import org.spongepowered.api.data.manipulator.mutable.tileentity.SignData;
import org.spongepowered.api.data.value.mutable.ListValue;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.world.World;
import org.spongepowered.api.world.biome.BiomeType;
import org.spongepowered.api.world.biome.BiomeTypes;

import com.google.common.base.Optional;
import com.intellectualcrafters.plot.object.Location;
import com.intellectualcrafters.plot.object.PlotBlock;
import com.intellectualcrafters.plot.object.schematic.PlotItem;
import com.intellectualcrafters.plot.util.BlockManager;
import com.intellectualcrafters.plot.util.MathMan;
import com.intellectualcrafters.plot.util.StringComparison;
import com.plotsquared.sponge.SpongeMain;

public class SpongeBlockManager extends BlockManager
{

    @Override
    public boolean isBlockSolid(final PlotBlock block)
    {
        final BlockState state = SpongeMain.THIS.getBlockState(block);
        final BlockType type = state.getType();
        return type.isSolidCube() && !type.isAffectedByGravity();
    }

    @Override
    public StringComparison<PlotBlock>.ComparisonResult getClosestBlock(String name)
    {
        try
        {
            double match;
            short id;
            byte data;
            final String[] split = name.split(":");
            if (split.length == 2)
            {
                data = Byte.parseByte(split[1]);
                name = split[0];
            }
            else
            {
                data = 0;
            }
            if (MathMan.isInteger(split[0]))
            {
                id = Short.parseShort(split[0]);
                match = 0;
            }
            else
            {
                final StringComparison<BlockState>.ComparisonResult comparison = new StringComparison<BlockState>(name, SpongeMain.THIS.getAllStates())
                {
                    @Override
                    public String getString(final BlockState o)
                    {
                        return o.getType().getId();
                    };
                }.getBestMatchAdvanced();
                match = comparison.match;
                id = SpongeMain.THIS.getPlotBlock(comparison.best).id;
            }
            final PlotBlock block = new PlotBlock(id, data);
            final StringComparison<PlotBlock> outer = new StringComparison<PlotBlock>();
            return outer.new ComparisonResult(match, block);

        }
        catch (final Exception e)
        {}
        return null;
    }

    @Override
    public String getClosestMatchingName(final PlotBlock block)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String[] getBiomeList()
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean addItems(final String world, final PlotItem items)
    {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public int getBiomeFromString(final String biome)
    {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public PlotBlock getPlotBlockFromString(final String block)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public int getHeighestBlock(final String worldname, final int x, final int z)
    {
        final World world = SpongeUtil.getWorld(worldname);
        if (world == null) { return 64; }
        for (int y = 255; y > 0; y--)
        {
            final BlockState block = world.getBlock(x, y, z);
            if ((block != null) && (block.getType() != BlockTypes.AIR)) { return y + 1; }
        }
        return 64;
    }

    @Override
    public String getBiome(final String world, final int x, final int z)
    {
        return SpongeUtil.getWorld(world).getBiome(x, z).getName().toUpperCase();
    }

    @Override
    public PlotBlock getBlock(final Location loc)
    {
        final BlockState state = SpongeUtil.getWorld(loc.getWorld()).getBlock(loc.getX(), loc.getY(), loc.getZ());
        PlotBlock block = SpongeMain.THIS.getPlotBlock(state);
        if (block == null)
        {
            block = SpongeMain.THIS.registerBlock(state);
        }
        return block;
    }

    @Override
    public Location getSpawn(final String world)
    {
        final World worldObj = SpongeUtil.getWorld(world);
        worldObj.getSpawnLocation();
        final Location result = SpongeUtil.getLocation(world, SpongeUtil.getWorld(world).getSpawnLocation());
        result.setY(getHeighestBlock(world, result.getX(), result.getZ()));
        return result;
    }

    @Override
    public String[] getSign(final Location loc)
    {
        final World world = SpongeUtil.getWorld(loc.getWorld());
        final Optional<TileEntity> block = world.getTileEntity(loc.getX(), loc.getY(), loc.getZ());
        if (!block.isPresent()) { return null; }
        final TileEntity tile = block.get();
        if (!(tile instanceof Sign)) { return null; }
        final Sign sign = (Sign) tile;
        final Optional<SignData> optional = sign.getOrCreate(SignData.class);
        if (!optional.isPresent()) { return null; }
        final String[] result = new String[4];
        final ListValue<Text> lines = optional.get().lines();
        for (int i = 0; i < 4; i++)
        {
            result[i] = lines.get(i).toString();
        }
        return result;
    }

    @Override
    public boolean isWorld(final String world)
    {
        return SpongeUtil.getWorld(world) != null;
    }

    @Override
    public void functionSetBlocks(final String worldname, final int[] xv, final int[] yv, final int[] zv, final int[] id, final byte[] data)
    {
        for (int i = 0; i < xv.length; i++)
        {
            functionSetBlock(worldname, xv[i], yv[i], zv[i], id[i], data[i]);
        }
    }

    @Override
    public void functionSetSign(final String worldname, final int x, final int y, final int z, final String[] lines)
    {
        final World world = SpongeUtil.getWorld(worldname);
        world.setBlock(x, y, z, BlockTypes.WALL_SIGN.getDefaultState());
        final Optional<TileEntity> block = world.getTileEntity(x, y, z);
        if (!block.isPresent()) { return; }
        final TileEntity tile = block.get();
        if (!(tile instanceof Sign)) { return; }
        final Sign sign = (Sign) tile;
        final List<Text> text = new ArrayList<>(4);
        for (int i = 0; i < 4; i++)
        {
            text.add(SpongeMain.THIS.getText(lines[i]));
        }
        try
        {
            final Optional<SignData> optional = sign.getOrCreate(SignData.class);
            if (optional.isPresent())
            {
                final SignData offering = optional.get();
                offering.lines().set(0, SpongeMain.THIS.getText(lines[0]));
                offering.lines().set(1, SpongeMain.THIS.getText(lines[1]));
                offering.lines().set(2, SpongeMain.THIS.getText(lines[2]));
                offering.lines().set(3, SpongeMain.THIS.getText(lines[3]));
                sign.offer(offering);
            }
        }
        catch (final NullPointerException e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public void functionSetBlock(final String worldname, final int x, final int y, final int z, final int id, final byte data)
    {
        BlockState state;
        if (data == 0)
        {
            state = SpongeMain.THIS.getBlockState(id);
        }
        else
        {
            state = SpongeMain.THIS.getBlockState(new PlotBlock((short) id, data));
        }
        if (state == null) { return; }
        final World world = SpongeUtil.getWorld(worldname);
        final BlockState block = world.getBlock(x, y, z);
        if (block != state)
        {
            world.setBlock(x, y, z, state);
        }

    }

    @Override
    public void functionSetBiomes(final String worldname, final int[] xv, final int[] zv, final String biomeName)
    {
        BiomeType biome;
        try
        {
            biome = (BiomeType) BiomeTypes.class.getField(biomeName.toUpperCase()).get(null);
        }
        catch (final Exception e)
        {
            e.printStackTrace();
            biome = BiomeTypes.FOREST;
        }
        for (int i = 0; i < xv.length; i++)
        {
            SpongeUtil.getWorld(worldname).setBiome(xv[i], zv[i], biome);
        }
    }

}
