package com.plotsquared.bukkit.util;

import java.util.Arrays;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.MaterialData;
import org.bukkit.material.Sandstone;
import org.bukkit.material.Step;
import org.bukkit.material.Tree;
import org.bukkit.material.WoodenStep;
import org.bukkit.material.Wool;

import com.intellectualcrafters.plot.object.Location;
import com.intellectualcrafters.plot.object.PlotBlock;
import com.intellectualcrafters.plot.object.PlotPlayer;
import com.intellectualcrafters.plot.object.schematic.PlotItem;
import com.intellectualcrafters.plot.util.BlockManager;
import com.intellectualcrafters.plot.util.MathMan;
import com.intellectualcrafters.plot.util.StringComparison;
import com.intellectualcrafters.plot.util.StringMan;
import com.intellectualcrafters.plot.util.UUIDHandler;
import com.plotsquared.bukkit.object.BukkitPlayer;

public class BukkitUtil extends BlockManager
{
    private static String lastString = null;
    private static World lastWorld = null;

    private static Player lastPlayer = null;
    private static PlotPlayer lastPlotPlayer = null;

    public static void removePlayer(final String plr)
    {
        lastPlayer = null;
        lastPlotPlayer = null;
        UUIDHandler.getPlayers().remove(plr);
    }

    // These weren't being used, but they might be useful later, so I'm just commenting them out
    //    private static int getMaxHeight(final String world) {
    //        return getWorld(world).getMaxHeight();
    //    }
    //
    //    private static void unloadChunkAt(String worldname, int X, int Z, boolean save, boolean safe) {
    //        final World world = getWorld(worldname);
    //        world.unloadChunk(X, Z, save, safe);
    //    }
    //
    //    private static void loadChunkAt(final String worldname, int X, int Z, boolean force) {
    //        final World world = getWorld(worldname);
    //        world.loadChunk(X, Z, force);
    //    }
    //
    //    private static Chunk getChunkAt(final String worldname, final int x, final int z) {
    //        final World world = getWorld(worldname);
    //        return world.getChunkAt(x, z);
    //    }
    //
    //    private static void teleportPlayer(final Player player, final Location loc) {
    //        final org.bukkit.Location bukkitLoc = new org.bukkit.Location(getWorld(loc.getWorld()), loc.getX(), loc.getY(), loc.getZ());
    //        player.teleport(bukkitLoc);
    //    }
    //
    //    private static void setBiome(final String worldname, final int pos1_x, final int pos1_z, final int pos2_x, final int pos2_z, final String biome) {
    //        final Biome b = Biome.valueOf(biome.toUpperCase());
    //        final World world = getWorld(worldname);
    //        for (int x = pos1_x; x <= pos2_x; x++) {
    //            for (int z = pos1_z; z <= pos2_z; z++) {
    //                if (world.getBiome(x, z) == b) {
    //                    continue;
    //                }
    //                world.setBiome(x, z, b);
    //            }
    //        }
    //    }
    //
    //    private static void refreshChunk(final String name, final int x, final int z) {
    //        World world = getWorld(name);
    //        world.refreshChunk(x, z);
    //        world.loadChunk(x, z);
    //    }
    //
    //    private static void regenerateChunk(final String world, final int x, final int z) {
    //        World worldObj = getWorld(world);
    //        Chunk chunk = worldObj.getChunkAt(x, z);
    //        if (chunk.isLoaded() || chunk.load(false)) {
    //            ChunkManager.manager.regenerateChunk(world, new ChunkLoc(x, z));
    //        }
    //    }
    //
    //    private static Location getLocationFull(final org.bukkit.Location loc) {
    //        final String world = loc.getWorld().getName();
    //        return new Location(world, loc.getBlockX(), loc.getBlockY(), loc.getBlockZ(), loc.getYaw(), loc.getPitch());
    //    }
    //
    //    private static int getViewDistance() {
    //        return Bukkit.getViewDistance();
    //    }

    ////////////////////////////////////////////////////////////////////////
    /////////////////// USED BY EVENT SYSTEM AND SUCH //////////////////////
    ////////////////////////////////////////////////////////////////////////
    public static PlotPlayer getPlayer(final OfflinePlayer op)
    {
        if (op.isOnline()) { return getPlayer(op.getPlayer()); }
        final Player player = OfflinePlayerUtil.loadPlayer(op);
        player.loadData();
        return new BukkitPlayer(player, true);
    }

    public static PlotPlayer getPlayer(final Player player)
    {
        if (player == lastPlayer) { return lastPlotPlayer; }
        final String name = player.getName();
        final PlotPlayer pp = UUIDHandler.getPlayers().get(name);
        if (pp != null) { return pp; }
        lastPlotPlayer = new BukkitPlayer(player);
        UUIDHandler.getPlayers().put(name, lastPlotPlayer);
        lastPlayer = player;
        return lastPlotPlayer;
    }

    public static Location getLocation(final org.bukkit.Location loc)
    {
        return new Location(loc.getWorld().getName(), MathMan.roundInt(loc.getX()), MathMan.roundInt(loc.getY()), MathMan.roundInt(loc.getZ()));
    }

    public static org.bukkit.Location getLocation(final Location loc)
    {
        return new org.bukkit.Location(getWorld(loc.getWorld()), loc.getX(), loc.getY(), loc.getZ());
    }

    public static World getWorld(final String string)
    {
        if (StringMan.isEqual(string, lastString))
        {
            if (lastWorld != null) { return lastWorld; }
        }
        final World world = Bukkit.getWorld(string);
        lastString = string;
        lastWorld = world;
        return world;
    }

    public static String getWorld(final Entity entity)
    {
        return entity.getWorld().getName();
    }

    public static List<Entity> getEntities(final String worldname)
    {
        return getWorld(worldname).getEntities();
    }

    public static Location getLocation(final Entity entity)
    {
        final org.bukkit.Location loc = entity.getLocation();
        final String world = loc.getWorld().getName();
        return new Location(world, loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
    }

    public static Location getLocationFull(final Entity entity)
    {
        final org.bukkit.Location loc = entity.getLocation();
        return new Location(loc.getWorld().getName(), MathMan.roundInt(loc.getX()), MathMan.roundInt(loc.getY()), MathMan.roundInt(loc.getZ()), loc.getYaw(), loc.getPitch());
    }

    ////////////////////////////////////////////////////////////////////////

    ////////////////////////////////////////////////////////////////////////
    ////////////////////// CLASS ONLY METHODS //////////////////////////////
    ////////////////////////////////////////////////////////////////////////
    private static void setBlock(final World world, final int x, final int y, final int z, final int id, final byte data)
    {
        try
        {
            BukkitSetBlockManager.setBlockManager.set(world, x, y, z, id, data);
        }
        catch (final Throwable e)
        {
            BukkitSetBlockManager.setBlockManager = new SetBlockSlow();
            BukkitSetBlockManager.setBlockManager.set(world, x, y, z, id, data);
        }
    }

    ////////////////////////////////////////////////////////////////////////

    @Override
    public boolean isWorld(final String world)
    {
        return getWorld(world) != null;
    }

    @Override
    public String getBiome(final String world, final int x, final int z)
    {
        return getWorld(world).getBiome(x, z).name();
    }

    @Override
    public void functionSetBlocks(final String worldname, final int[] x, final int[] y, final int[] z, final int[] id, final byte[] data)
    {
        final World world = getWorld(worldname);
        for (int i = 0; i < x.length; i++)
        {
            BukkitUtil.setBlock(world, x[i], y[i], z[i], id[i], data[i]);
        }
    }

    @Override
    public void functionSetSign(final String worldname, final int x, final int y, final int z, final String[] lines)
    {
        final World world = getWorld(worldname);
        final Block block = world.getBlockAt(x, y, z);
        block.setType(Material.AIR);
        block.setTypeIdAndData(Material.WALL_SIGN.getId(), (byte) 2, false);
        final BlockState blockstate = block.getState();
        if ((blockstate instanceof Sign))
        {
            for (int i = 0; i < lines.length; i++)
            {
                ((Sign) blockstate).setLine(i, lines[i]);
            }
            ((Sign) blockstate).update(true);
        }
    }

    @Override
    public void functionSetBiomes(final String worldname, final int[] x, final int[] z, final String biomeStr)
    {
        final World world = getWorld(worldname);
        final Biome biome = Biome.valueOf(biomeStr.toUpperCase());
        for (int i = 0; i < x.length; i++)
        {
            world.setBiome(x[i], z[i], biome);
        }
    }

    @Override
    public void functionSetBlock(final String worldname, final int x, final int y, final int z, final int id, final byte data)
    {
        BukkitUtil.setBlock(getWorld(worldname), x, y, z, id, data);
    }

    @Override
    public String[] getSign(final Location loc)
    {
        final Block block = getWorld(loc.getWorld()).getBlockAt(loc.getX(), loc.getY(), loc.getZ());
        if (block != null)
        {
            if (block.getState() instanceof Sign)
            {
                final Sign sign = (Sign) block.getState();
                return sign.getLines();
            }
        }
        return null;
    }

    @Override
    public Location getSpawn(final String world)
    {
        final org.bukkit.Location temp = getWorld(world).getSpawnLocation();
        return new Location(world, temp.getBlockX(), temp.getBlockY(), temp.getBlockZ(), temp.getYaw(), temp.getPitch());
    }

    @Override
    public int getHeighestBlock(final String world, final int x, final int z)
    {
        return getWorld(world).getHighestBlockAt(x, z).getY();
    }

    @Override
    public int getBiomeFromString(final String biomeStr)
    {
        try
        {
            final Biome biome = Biome.valueOf(biomeStr.toUpperCase());
            if (biome == null) { return -1; }
            return Arrays.asList(Biome.values()).indexOf(biome);
        }
        catch (final IllegalArgumentException e)
        {
            return -1;
        }
    }

    @Override
    public String[] getBiomeList()
    {
        final Biome[] biomes = Biome.values();
        final String[] list = new String[biomes.length];
        for (int i = 0; i < biomes.length; i++)
        {
            list[i] = biomes[i].name();
        }
        return list;
    }

    @Override
    public PlotBlock getPlotBlockFromString(final String block)
    {
        final Material material = Material.valueOf(block.toUpperCase());
        if (material == null) { return new PlotBlock((short) -1, (byte) 0); }
        return new PlotBlock((short) material.getId(), (byte) 0);
    }

    @Override
    public boolean addItems(final String worldname, final PlotItem items)
    {
        final World world = getWorld(worldname);
        final Block block = world.getBlockAt(items.x, items.y, items.z);
        if (block == null) { return false; }
        final BlockState state = block.getState();
        if ((state != null) && (state instanceof InventoryHolder))
        {
            final InventoryHolder holder = ((InventoryHolder) state);
            final Inventory inv = holder.getInventory();
            for (int i = 0; i < items.id.length; i++)
            {
                final ItemStack item = new ItemStack(items.id[i], items.amount[i], items.data[i]);
                inv.addItem(item);
            }
            state.update(true);
            return true;
        }
        return false;

    }

    @Override
    public boolean isBlockSolid(final PlotBlock block)
    {
        try
        {
            final Material material = Material.getMaterial(block.id);
            if (material.isBlock() && material.isSolid() && !material.hasGravity())
            {
                final Class<? extends MaterialData> data = material.getData();
                if ((data.equals(MaterialData.class) && !material.isTransparent() && material.isOccluding())
                || data.equals(Tree.class)
                || data.equals(Sandstone.class)
                || data.equals(Wool.class)
                || data.equals(Step.class)
                || data.equals(WoodenStep.class))
                {
                    switch (material)
                    {
                        case NOTE_BLOCK:
                        case MOB_SPAWNER:
                        {
                            return false;
                        }
                        default:
                            return true;
                    }
                }
            }
            return false;
        }
        catch (final Exception e)
        {
            return false;
        }
    }

    @Override
    public String getClosestMatchingName(final PlotBlock block)
    {
        try
        {
            return Material.getMaterial(block.id).name();
        }
        catch (final Exception e)
        {
            return null;
        }
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
                final StringComparison<Material>.ComparisonResult comparison = new StringComparison<Material>(name, Material.values()).getBestMatchAdvanced();
                match = comparison.match;
                id = (short) comparison.best.getId();
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
    public PlotBlock getBlock(final Location loc)
    {
        final World world = getWorld(loc.getWorld());
        final Block block = world.getBlockAt(loc.getX(), loc.getY(), loc.getZ());
        if (block == null) { return new PlotBlock((short) 0, (byte) 0); }
        return new PlotBlock((short) block.getTypeId(), block.getData());
    }
}
