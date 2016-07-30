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
import com.plotsquared.sponge.object.SpongePlayer;
import net.minecraft.block.Block;
import net.minecraft.world.biome.Biome;
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
import org.spongepowered.api.event.cause.NamedCause;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.serializer.TextSerializers;
import org.spongepowered.api.text.translation.Translation;
import org.spongepowered.api.world.World;
import org.spongepowered.api.world.biome.BiomeType;
import org.spongepowered.api.world.biome.BiomeTypes;
import org.spongepowered.api.world.extent.Extent;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

public class SpongeUtil extends WorldUtil {

    public static Cause CAUSE = Cause.of(NamedCause.source("PlotSquared"));
    private static BiomeType[] biomes;
    private static HashMap<String, Integer> biomeMap;
    private static HashMap<BlockState, PlotBlock> stateMap;
    private static BlockState[] stateArray;
    private static Player lastPlayer = null;
    private static PlotPlayer lastPlotPlayer = null;
    private static World lastWorld;
    private static String last;

    public static Location getLocation(Entity player) {
        String world = player.getWorld().getName();
        org.spongepowered.api.world.Location loc = player.getLocation();
        Vector3i pos = loc.getBlockPosition();
        return new Location(world, pos.getX(), pos.getY(), pos.getZ());
    }

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
        System.out.println(method + ": " + cause.root());
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
        return (BiomeType) Biome.getBiome(index);
    }

    public static Text getText(String m) {
        return TextSerializers.LEGACY_FORMATTING_CODE.deserialize(C.color(m));
    }

    public static Translation getTranslation(String m) {
        return new Translation() {

            @Override
            public String getId() {
                return m;
            }

            @Override
            public String get(Locale l, Object... args) {
                return m;
            }

            @Override
            public String get(Locale l) {
                return m;
            }
        };
    }

    private static void initBlockCache() {
        try {
            PS.debug("Caching block id/data: Please wait...");
            stateArray = new BlockState[Character.MAX_VALUE];
            stateMap = new HashMap<>();
            Method methodGetByCombinedId = ReflectionUtils
                    .findMethod(Class.forName("net.minecraft.block.Block"), true, Class.forName("net.minecraft.block.state.IBlockState"), int.class);
            for (int i = 0; i < Character.MAX_VALUE; i++) {
                try {
                    BlockState state = (BlockState) methodGetByCombinedId.invoke(null, i);
                    if (state.getType() == BlockTypes.AIR) {
                        continue;
                    }
                    PlotBlock plotBlock = PlotBlock.get((short) (i & 0xFFF), (byte) (i >> 12 & 0xF));
                    stateArray[i] = state;
                    stateMap.put(state, plotBlock);
                } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ignored) {
                }
            }
            PS.debug("Done!");
        } catch (ClassNotFoundException e) {
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

    public static Location getLocation(org.spongepowered.api.world.Location<World> block) {
        return getLocation(block.getExtent().getName(), block);
    }

    public static Location getLocationFull(Entity player) {
        String world = player.getWorld().getName();
        Vector3d rot = player.getRotation();
        float[] pitchYaw = MathMan.getPitchAndYaw((float) rot.getX(), (float) rot.getY(), (float) rot.getZ());
        org.spongepowered.api.world.Location loc = player.getLocation();
        Vector3i pos = loc.getBlockPosition();
        return new Location(world, pos.getX(), pos.getY(), pos.getZ(), pitchYaw[1], pitchYaw[0]);
    }

    public static PlotPlayer getPlayer(Player player) {
        if (player == lastPlayer) {
            return lastPlotPlayer;
        }
        String name = player.getName();
        PlotPlayer pp = UUIDHandler.getPlayer(name);
        if (pp != null) {
            return pp;
        }
        lastPlotPlayer = new SpongePlayer(player);
        UUIDHandler.getPlayers().put(name, lastPlotPlayer);
        lastPlayer = player;
        return lastPlotPlayer;
    }

    public static Player getPlayer(PlotPlayer player) {
        if (player instanceof SpongePlayer) {
            return ((SpongePlayer) player).player;
        }
        return null;
    }

    public static World getWorld(String world) {
        if (StringMan.isEqual(world, last)) {
            return lastWorld;
        }
        Optional<World> optional = Sponge.getServer().getWorld(world);
        if (!optional.isPresent()) {
            last = null;
            return lastWorld = null;
        }
        last = world;
        return lastWorld = optional.get();
    }

    public static void removePlayer(String player) {
        lastPlayer = null;
        lastPlotPlayer = null;
    }

    public static Location getLocation(String world, org.spongepowered.api.world.Location spawn) {
        return new Location(world, spawn.getBlockX(), spawn.getBlockY(), spawn.getBlockZ());
    }

    public static String getWorldName(org.spongepowered.api.world.Location origin) {
        Extent extent = origin.getExtent();
        if (extent == lastWorld) {
            return lastWorld.getName();
        }
        if (extent instanceof World) {
            lastWorld = (World) extent;
            return lastWorld.getName();
        }
        return null;
    }

    public static org.spongepowered.api.world.Location<World> getLocation(Location location) {
        Collection<World> worlds = Sponge.getServer().getWorlds();
        World world = Sponge.getServer().getWorld(location.getWorld()).orElse(worlds.toArray(new World[worlds.size()])[0]);
        return new org.spongepowered.api.world.Location<>(world, location.getX(), location.getY(), location.getZ());
    }

    public static Location getLocation(String world, Vector3i position) {
        return new Location(world, position.getX(), position.getY(), position.getZ());
    }

    public static Location getLocation(String world, Vector3d position) {
        return new Location(world, MathMan.roundInt(position.getX()), MathMan.roundInt(position.getY()), MathMan.roundInt(position.getZ()));
    }

    @Override
    public boolean isBlockSolid(PlotBlock block) {
        BlockState state = SpongeUtil.getBlockState(block.id, block.data);
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

            byte data;
            String[] split = name.split(":");
            if (split.length == 2) {
                data = Byte.parseByte(split[1]);
                name = split[0];
            } else {
                data = 0;
            }
            short id;
            double match;
            if (MathMan.isInteger(split[0])) {
                id = Short.parseShort(split[0]);
                match = 0;
            } else {
                List<BlockType> types = ReflectionUtils.getStaticFields(BlockTypes.class);
                StringComparison<BlockType>.ComparisonResult comparison =
                        new StringComparison<BlockType>(name, types.toArray(new BlockType[types.size()])) {
                            @Override
                            public String getString(BlockType type) {
                                return type.getId();
                            }
                        }.getBestMatchAdvanced();
                match = comparison.match;
                id = SpongeUtil.getPlotBlock(comparison.best.getDefaultState()).id;
            }
            PlotBlock block = PlotBlock.get(id, data);
            StringComparison<PlotBlock> outer = new StringComparison<PlotBlock>();
            return outer.new ComparisonResult(match, block);

        } catch (NumberFormatException ignored) {
        }
        return null;
    }

    @Override
    public String getClosestMatchingName(PlotBlock block) {
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
    public boolean addItems(String world, PlotItem items) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("NOT IMPLEMENTED YET");
    }

    @Override
    public int getBiomeFromString(String biome) {
        if (biomes == null) {
            initBiomeCache();
        }
        return biomeMap.get(biome.toUpperCase());
    }

    @Override
    public String getBiome(String world, int x, int z) {
        return SpongeUtil.getWorld(world).getBiome(x, z).getName().toUpperCase();
    }

    @Override
    public PlotBlock getBlock(Location location) {
        BlockState state = SpongeUtil.getWorld(location.getWorld()).getBlock(location.getX(), location.getY(), location.getZ());
        return SpongeUtil.getPlotBlock(state);
    }

    @Override
    public Location getSpawn(PlotPlayer plotPlayer) {
        World world = getWorld(plotPlayer.getLocation().getWorld());
        return SpongeUtil.getLocation(world.getSpawnLocation());
    }

    @Override
    public Location getSpawn(String world) {
        Location result = SpongeUtil.getLocation(world, SpongeUtil.getWorld(world).getSpawnLocation());
        result.setY(getHighestBlock(world, result.getX(), result.getZ()));
        return result;
    }

    @Override
    public void setSpawn(Location location) {
        World world = getWorld(location.getWorld());
        if (world != null) {
            world.getProperties().setSpawnPosition(new Vector3i(location.getX(), location.getY(), location.getZ()));
        }
    }

    @Override
    public void saveWorld(String worldName) {
        try {
            SpongeUtil.getWorld(worldName).save();
        } catch (IOException e) {
            e.printStackTrace();
            PS.debug("Failed to save world.");
        }
    }

    @Override
    public String[] getSign(Location location) {
        World world = SpongeUtil.getWorld(location.getWorld());
        Optional<TileEntity> block = world.getTileEntity(location.getX(), location.getY(), location.getZ());
        if (!block.isPresent()) {
            return null;
        }
        TileEntity tile = block.get();
        if (!(tile instanceof Sign)) {
            return null;
        }
        Sign sign = (Sign) tile;
        Optional<SignData> optional = sign.get(SignData.class);
        if (!optional.isPresent()) {
            return null;
        }
        String[] result = new String[4];
        ListValue<Text> lines = optional.get().lines();
        for (int i = 0; i < 4; i++) {
            result[i] = lines.get(i).toString();
        }
        return result;
    }

    @Override
    public boolean isWorld(String worldName) {
        return SpongeUtil.getWorld(worldName) != null;
    }

    @Override
    public String getMainWorld() {
        return Sponge.getServer().getWorlds().iterator().next().getName();
    }

    @Override
    public int getHighestBlock(String worldName, int x, int z) {
        World world = SpongeUtil.getWorld(worldName);
        if (world == null) {
            return 64;
        }
        for (int y = 255; y > 0; y--) {
            BlockState block = world.getBlock(x, y, z);
            if (block.getType() != BlockTypes.AIR) {
                return y + 1;
            }
        }
        return 64;
    }

    @Override
    public void setSign(String worldName, int x, int y, int z, String[] lines) {
        World world = SpongeUtil.getWorld(worldName);
        world.setBlock(x, y, z, BlockTypes.WALL_SIGN.getDefaultState(), CAUSE);
        Optional<TileEntity> block = world.getTileEntity(x, y, z);
        if (!block.isPresent()) {
            return;
        }
        TileEntity tile = block.get();
        if (!(tile instanceof Sign)) {
            return;
        }
        Sign sign = (Sign) tile;
        List<Text> text = new ArrayList<>(4);
        for (int i = 0; i < 4; i++) {
            text.add(SpongeUtil.getText(lines[i]));
        }
        sign.offer(Keys.SIGN_LINES, text);
    }

    @Override
    public void setBiomes(String worldName, RegionWrapper region, String biomename) {
        World world = SpongeUtil.getWorld(worldName);
        BiomeType biome = SpongeUtil.getBiome(biomename);
        for (int x = region.minX; x <= region.maxX; x++) {
            for (int z = region.minZ; z <= region.maxZ; z++) {
                world.setBiome(x, z, biome);
            }
        }
    }
}
