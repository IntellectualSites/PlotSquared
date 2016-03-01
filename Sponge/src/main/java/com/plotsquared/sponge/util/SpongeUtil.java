package com.plotsquared.sponge.util;

import com.flowpowered.math.vector.Vector3d;
import com.flowpowered.math.vector.Vector3i;
import com.intellectualcrafters.plot.PS;
import com.intellectualcrafters.plot.config.C;
import com.intellectualcrafters.plot.object.Location;
import com.intellectualcrafters.plot.object.PlotBlock;
import com.intellectualcrafters.plot.object.PlotPlayer;
import com.intellectualcrafters.plot.object.RegionWrapper;
import com.intellectualcrafters.plot.object.schematic.PlotItem;
import com.intellectualcrafters.plot.util.MathMan;
import com.intellectualcrafters.plot.util.ReflectionUtils;
import com.intellectualcrafters.plot.util.StringComparison;
import com.intellectualcrafters.plot.util.StringMan;
import com.intellectualcrafters.plot.util.UUIDHandler;
import com.intellectualcrafters.plot.util.WorldUtil;
import com.plotsquared.sponge.SpongeMain;
import com.plotsquared.sponge.object.SpongePlayer;
import net.minecraft.block.Block;
import net.minecraft.world.biome.BiomeGenBase;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.block.tileentity.Sign;
import org.spongepowered.api.block.tileentity.TileEntity;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.mutable.tileentity.SignData;
import org.spongepowered.api.data.property.block.SolidCubeProperty;
import org.spongepowered.api.data.value.mutable.ListValue;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.serializer.TextSerializers;
import org.spongepowered.api.text.translation.Translation;
import org.spongepowered.api.world.World;
import org.spongepowered.api.world.biome.BiomeType;
import org.spongepowered.api.world.biome.BiomeTypes;
import org.spongepowered.api.world.extent.Extent;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

public class SpongeUtil extends WorldUtil {
    
    public static Location getLocation(final Entity player) {
        final String world = player.getWorld().getName();
        final org.spongepowered.api.world.Location loc = player.getLocation();
        final Vector3i pos = loc.getBlockPosition();
        return new Location(world, pos.getX(), pos.getY(), pos.getZ());
    }
    
    private static BiomeType[] biomes;
    private static HashMap<String, Integer> biomeMap;
    
    public static BiomeType getBiome(String biome) {
        if (biomes == null) {
            initBiomeCache();
        }
        return biomes[biomeMap.get(biome.toUpperCase())];
    }
    
    public static <T> T getCause(Cause cause, Class<T> clazz) {
        Optional<?> root = Optional.of(cause.root());
        if (root.isPresent()) {
            Object source = root.get();
            if (clazz.isInstance(source)) {
                return (T) source;
            }
        }
        return null;
    }

    public static void printCause(String method, Cause cause) {
        System.out.println(method + ": " + cause.toString());
        System.out.println(method + ": " + cause.getClass());
        System.out.println(method + ": " + StringMan.getString(cause.all()));
        System.out.println(method + ": " + (cause.root()));
    }

    public static void initBiomeCache() {
        try {
            Field[] fields = BiomeTypes.class.getFields();
            biomes = new BiomeType[fields.length];
            biomeMap = new HashMap<>();
            for (int i = 0; i < fields.length; i++) {
                Field field = fields[i];
                String name = field.getName();
                biomeMap.put(name, i);
                biomes[i] = (BiomeType) field.get(null);
            }

        } catch (IllegalArgumentException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    public static BiomeType getBiome(int index) {
        return (BiomeType) BiomeGenBase.getBiome(index);
    }

    public static Text getText(String m) {
        return TextSerializers.LEGACY_FORMATTING_CODE.deserialize(C.color(m));
    }

    public static Translation getTranslation(final String m) {
        return new Translation() {

            @Override
            public String getId() {
                return m;
            }

            @Override
            public String get(final Locale l, final Object... args) {
                return m;
            }

            @Override
            public String get(final Locale l) {
                return m;
            }
        };
    }

    private static HashMap<BlockState, PlotBlock> stateMap;
    private static BlockState[] stateArray;
    
    private static void initBlockCache() {
        try {
            PS.debug("Caching block id/data: Please wait...");
            stateArray = new BlockState[Character.MAX_VALUE];
            stateMap = new HashMap<>();
            Method methodGetByCombinedId = ReflectionUtils.findMethod(Class.forName("net.minecraft.block.Block"), true, Class.forName("net.minecraft.block.state.IBlockState"), int.class);
            for (int i = 0; i < Character.MAX_VALUE; i++) {
                try {
                    BlockState state = (BlockState) methodGetByCombinedId.invoke(null, i);
                    if (state.getType() == BlockTypes.AIR) {
                        continue;
                    }
                    PlotBlock plotBlock = new PlotBlock((short) (i & 0xFFF), (byte) (i >> 12 & 0xF));
                    stateArray[i] = state;
                    stateMap.put(state, plotBlock);
                } catch (Throwable e) {}
            }
            PS.debug("Done!");
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }
    
    public static BlockState getBlockState(int id, int data) {
        return (BlockState) Block.getBlockById(id).getStateFromMeta(data);
    }

    public static PlotBlock getPlotBlock(BlockState state) {
        if (stateMap == null) {
            initBlockCache();
        }
        return stateMap.get(state);
    }

    public static Location getLocation(final org.spongepowered.api.world.Location<World> block) {
        return getLocation(block.getExtent().getName(), block);
    }
    
    public static Location getLocationFull(final Entity player) {
        final String world = player.getWorld().getName();
        final Vector3d rot = player.getRotation();
        final float[] pitchYaw = MathMan.getPitchAndYaw((float) rot.getX(), (float) rot.getY(), (float) rot.getZ());
        final org.spongepowered.api.world.Location loc = player.getLocation();
        final Vector3i pos = loc.getBlockPosition();
        return new Location(world, pos.getX(), pos.getY(), pos.getZ(), pitchYaw[1], pitchYaw[0]);
    }
    
    private static Player lastPlayer = null;
    private static PlotPlayer lastPlotPlayer = null;
    
    public static PlotPlayer getPlayer(final Player player) {
        if (player == lastPlayer) {
            return lastPlotPlayer;
        }
        final String name = player.getName();
        final PlotPlayer pp = UUIDHandler.getPlayer(name);
        if (pp != null) {
            return pp;
        }
        lastPlotPlayer = new SpongePlayer(player);
        UUIDHandler.getPlayers().put(name, lastPlotPlayer);
        lastPlayer = player;
        return lastPlotPlayer;
    }
    
    public static Player getPlayer(final PlotPlayer player) {
        if (player instanceof SpongePlayer) {
            return ((SpongePlayer) player).player;
        }
        return null;
    }
    
    private static World lastWorld;
    private static String last;

    public static World getWorld(final String world) {
        if (StringMan.isEqual(world, last)) {
            return lastWorld;
        }
        final Optional<World> optional = Sponge.getServer().getWorld(world);
        if (!optional.isPresent()) {
            last = null;
            return lastWorld = null;
        }
        last = world;
        return lastWorld = optional.get();
    }
    
    public static void removePlayer(final String player) {
        lastPlayer = null;
        lastPlotPlayer = null;
    }
    
    public static Location getLocation(final String world, final org.spongepowered.api.world.Location spawn) {
        return new Location(world, spawn.getBlockX(), spawn.getBlockY(), spawn.getBlockZ());
    }
    
    public static String getWorldName(final org.spongepowered.api.world.Location origin) {
        final Extent extent = origin.getExtent();
        if (extent == lastWorld) {
            return lastWorld.getName();
        }
        if (extent instanceof World) {
            lastWorld = (World) extent;
            return lastWorld.getName();
        }
        return null;
    }
    
    public static org.spongepowered.api.world.Location getLocation(final Location loc) {
        final Optional<World> world = SpongeMain.THIS.getServer().getWorld(loc.getWorld());
        if (!world.isPresent()) {
            return null;
        }
        return new org.spongepowered.api.world.Location(world.get(), loc.getX(), loc.getY(), loc.getZ());
    }
    
    public static Location getLocation(String world, Vector3i position) {
        return new Location(world, position.getX(), position.getY(), position.getZ());
    }
    
    public static Location getLocation(String world, Vector3d position) {
        return new Location(world, MathMan.roundInt(position.getX()), MathMan.roundInt(position.getY()), MathMan.roundInt(position.getZ()));
    }
    
    @Override
    public boolean isBlockSolid(final PlotBlock block) {
        final BlockState state = SpongeUtil.getBlockState(block.id, block.data);
        Optional<SolidCubeProperty> property = state.getType().getProperty(SolidCubeProperty.class);
        if (property.isPresent()) {
            return property.get().getValue();
        } else {
            return false;
        }
    }
    
    @Override
    public StringComparison<PlotBlock>.ComparisonResult getClosestBlock(String name) {
        try {

            double match;
            short id;
            byte data;
            final String[] split = name.split(":");
            if (split.length == 2) {
                data = Byte.parseByte(split[1]);
                name = split[0];
            } else {
                data = 0;
            }
            if (MathMan.isInteger(split[0])) {
                id = Short.parseShort(split[0]);
                match = 0;
            } else {
                List<BlockType> types = ReflectionUtils.<BlockType>getStaticFields(BlockTypes.class);
                final StringComparison<BlockType>.ComparisonResult comparison =
                        new StringComparison<BlockType>(name, types.toArray(new BlockType[types.size()])) {
                            @Override
                            public String getString(final BlockType type) {
                                return type.getId();
                            }
                        }.getBestMatchAdvanced();
                match = comparison.match;
                id = SpongeUtil.getPlotBlock(comparison.best.getDefaultState()).id;
            }
            final PlotBlock block = new PlotBlock(id, data);
            final StringComparison<PlotBlock> outer = new StringComparison<PlotBlock>();
            return outer.new ComparisonResult(match, block);

        } catch (NumberFormatException e) {
        }
        return null;
    }
    
    @Override
    public String getClosestMatchingName(final PlotBlock block) {
        // TODO Auto-generated method stub
        return null;
    }
    
    @Override
    public String[] getBiomeList() {
        if (biomes == null) {
            initBiomeCache();
        }
        return biomeMap.keySet().toArray(new String[biomeMap.size()]);
    }
    
    @Override
    public boolean addItems(final String world, final PlotItem items) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("NOT IMPLEMENTED YET");
    }
    
    @Override
    public int getBiomeFromString(final String biome) {
        if (biomes == null) {
            initBiomeCache();
        }
        return biomeMap.get(biome.toUpperCase());
    }
    
    @Override
    public String getBiome(final String world, final int x, final int z) {
        return SpongeUtil.getWorld(world).getBiome(x, z).getName().toUpperCase();
    }
    
    @Override
    public PlotBlock getBlock(final Location loc) {
        final BlockState state = SpongeUtil.getWorld(loc.getWorld()).getBlock(loc.getX(), loc.getY(), loc.getZ());
        return SpongeUtil.getPlotBlock(state);
    }
    
    @Override
    public Location getSpawn(final String world) {
        final Location result = SpongeUtil.getLocation(world, SpongeUtil.getWorld(world).getSpawnLocation());
        result.setY(getHighestBlock(world, result.getX(), result.getZ()));
        return result;
    }
    
    @Override
    public String[] getSign(final Location loc) {
        final World world = SpongeUtil.getWorld(loc.getWorld());
        final Optional<TileEntity> block = world.getTileEntity(loc.getX(), loc.getY(), loc.getZ());
        if (!block.isPresent()) {
            return null;
        }
        final TileEntity tile = block.get();
        if (!(tile instanceof Sign)) {
            return null;
        }
        final Sign sign = (Sign) tile;
        final Optional<SignData> optional = sign.get(SignData.class);
        if (!optional.isPresent()) {
            return null;
        }
        final String[] result = new String[4];
        ListValue<Text> lines = optional.get().lines();
        for (int i = 0; i < 4; i++) {
            result[i] = lines.get(i).toString();
        }
        return result;
    }
    
    @Override
    public boolean isWorld(final String world) {
        return SpongeUtil.getWorld(world) != null;
    }
    
    @Override
    public String getMainWorld() {
        return Sponge.getServer().getWorlds().iterator().next().getName();
    }
    
    @Override
    public int getHighestBlock(String worldname, int x, int z) {
        final World world = SpongeUtil.getWorld(worldname);
        if (world == null) {
            return 64;
        }
        for (int y = 255; y > 0; y--) {
            final BlockState block = world.getBlock(x, y, z);
            if (block.getType() != BlockTypes.AIR) {
                return y + 1;
            }
        }
        return 64;
    }
    
    @Override
    public void setSign(String worldname, int x, int y, int z, String[] lines) {
        final World world = SpongeUtil.getWorld(worldname);
        world.setBlock(x, y, z, BlockTypes.WALL_SIGN.getDefaultState());
        final Optional<TileEntity> block = world.getTileEntity(x, y, z);
        if (!block.isPresent()) {
            return;
        }
        final TileEntity tile = block.get();
        if (!(tile instanceof Sign)) {
            return;
        }
        final Sign sign = (Sign) tile;
        final List<Text> text = new ArrayList<>(4);
        for (int i = 0; i < 4; i++) {
            text.add(SpongeUtil.getText(lines[i]));
        }
        sign.offer(Keys.SIGN_LINES, text);
    }
    
    @Override
    public void setBiomes(String worldname, RegionWrapper region, String biomename) {
        final World world = SpongeUtil.getWorld(worldname);
        final BiomeType biome = SpongeUtil.getBiome(biomename);
        for (int x = region.minX; x <= region.maxX; x++) {
            for (int z = region.minZ; z <= region.maxZ; z++) {
                world.setBiome(x, z, biome);
            }
        }
    }
}
