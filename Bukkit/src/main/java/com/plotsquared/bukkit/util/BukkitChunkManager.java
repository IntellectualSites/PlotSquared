package com.plotsquared.bukkit.util;

import com.intellectualcrafters.plot.PS;
import com.intellectualcrafters.plot.generator.AugmentedUtils;
import com.intellectualcrafters.plot.object.*;
import com.intellectualcrafters.plot.object.Location;
import com.intellectualcrafters.plot.util.*;
import com.plotsquared.bukkit.object.entity.EntityWrapper;
import org.bukkit.*;
import org.bukkit.block.*;
import org.bukkit.block.banner.Pattern;
import org.bukkit.entity.*;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.Map.Entry;

public class BukkitChunkManager extends ChunkManager {

    private static HashMap<BlockLoc, ItemStack[]> chestContents;
    private static HashMap<BlockLoc, ItemStack[]> furnaceContents;
    private static HashMap<BlockLoc, ItemStack[]> dispenserContents;
    private static HashMap<BlockLoc, ItemStack[]> dropperContents;
    private static HashMap<BlockLoc, ItemStack[]> brewingStandContents;
    private static HashMap<BlockLoc, ItemStack[]> beaconContents;
    private static HashMap<BlockLoc, ItemStack[]> hopperContents;
    private static HashMap<BlockLoc, Short[]> furnaceTime;
    private static HashMap<BlockLoc, Object[]> skullData;
    private static HashMap<BlockLoc, Material> jukeDisc;
    private static HashMap<BlockLoc, Short> brewTime;
    private static HashMap<BlockLoc, EntityType> spawnerData;
    private static HashMap<BlockLoc, String> cmdData;
    private static HashMap<BlockLoc, String[]> signContents;
    private static HashMap<BlockLoc, Note> noteBlockContents;
    private static HashMap<BlockLoc, List<Pattern>> bannerPatterns;
    private static HashMap<BlockLoc, DyeColor> bannerBase;
    private static HashSet<EntityWrapper> entities;
    private static HashMap<PlotLoc, PlotBlock[]> allblocks;

    public static void initMaps() {
        chestContents = new HashMap<>();
        furnaceContents = new HashMap<>();
        dispenserContents = new HashMap<>();
        dropperContents = new HashMap<>();
        brewingStandContents = new HashMap<>();
        beaconContents = new HashMap<>();
        hopperContents = new HashMap<>();
        furnaceTime = new HashMap<>();
        skullData = new HashMap<>();
        brewTime = new HashMap<>();
        jukeDisc = new HashMap<>();
        spawnerData = new HashMap<>();
        noteBlockContents = new HashMap<>();
        signContents = new HashMap<>();
        cmdData = new HashMap<>();
        bannerBase = new HashMap<>();
        bannerPatterns = new HashMap<>();
        entities = new HashSet<>();
        allblocks = new HashMap<>();
    }

    public static boolean isIn(final RegionWrapper region, final int x, final int z) {
        return x >= region.minX && x <= region.maxX && z >= region.minZ && z <= region.maxZ;
    }

    public static void saveEntitiesOut(final Chunk chunk, final RegionWrapper region) {
        for (final Entity entity : chunk.getEntities()) {
            final Location loc = BukkitUtil.getLocation(entity);
            final int x = loc.getX();
            final int z = loc.getZ();
            if (isIn(region, x, z)) {
                continue;
            }
            if (entity.getVehicle() != null) {
                continue;
            }
            final EntityWrapper wrap = new EntityWrapper(entity, (short) 2);
            entities.add(wrap);
        }
    }

    public static void saveEntitiesIn(final Chunk chunk, final RegionWrapper region) {
        saveEntitiesIn(chunk, region, 0, 0, false);
    }

    public static void saveEntitiesIn(final Chunk chunk, final RegionWrapper region, final int offset_x, final int offset_z, final boolean delete) {
        for (final Entity entity : chunk.getEntities()) {
            final Location loc = BukkitUtil.getLocation(entity);
            final int x = loc.getX();
            final int z = loc.getZ();
            if (!isIn(region, x, z)) {
                continue;
            }
            if (entity.getVehicle() != null) {
                continue;
            }
            final EntityWrapper wrap = new EntityWrapper(entity, (short) 2);
            wrap.x += offset_x;
            wrap.z += offset_z;
            entities.add(wrap);
            if (delete) {
                if (!(entity instanceof Player)) {
                    entity.remove();
                }
            }
        }
    }

    public static void restoreEntities(final World world, final int x_offset, final int z_offset) {
        for (final EntityWrapper entity : entities) {
            try {
                entity.spawn(world, x_offset, z_offset);
            } catch (final Exception e) {
                PS.debug("Failed to restore entity (e): " + entity.x + "," + entity.y + "," + entity.z + " : " + entity.type);
                e.printStackTrace();
            }
        }
        entities.clear();
    }

    public static void restoreBlocks(final World world, final int x_offset, final int z_offset) {
        for (final Entry<BlockLoc, ItemStack[]> blockLocEntry : chestContents.entrySet()) {
            try {
                final Block block =
                        world.getBlockAt(blockLocEntry.getKey().x + x_offset, blockLocEntry.getKey().y, blockLocEntry.getKey().z + z_offset);
                final BlockState state = block.getState();
                if (state instanceof Chest) {
                    final InventoryHolder chest = (InventoryHolder) state;
                    chest.getInventory().setContents(blockLocEntry.getValue());
                    state.update(true);
                } else {
                    PS.debug("&c[WARN] Plot clear failed to regenerate chest: " + (blockLocEntry.getKey().x + x_offset) + "," + blockLocEntry
                            .getKey().y + "," + (blockLocEntry.getKey().z + z_offset));
                }
            } catch (IllegalArgumentException e) {
                PS.debug("&c[WARN] Plot clear failed to regenerate chest (e): " + (blockLocEntry.getKey().x + x_offset) + "," + blockLocEntry
                        .getKey().y + "," + (blockLocEntry.getKey().z + z_offset));
            }
        }
        for (final Entry<BlockLoc, String[]> blockLocEntry : signContents.entrySet()) {
            try {
                final Block block =
                        world.getBlockAt(blockLocEntry.getKey().x + x_offset, blockLocEntry.getKey().y, blockLocEntry.getKey().z + z_offset);
                final BlockState state = block.getState();
                if (state instanceof Sign) {
                    final Sign sign = (Sign) state;
                    int i = 0;
                    for (final String line : blockLocEntry.getValue()) {
                        sign.setLine(i, line);
                        i++;
                    }
                    state.update(true);
                } else {
                    PS.debug(
                            "&c[WARN] Plot clear failed to regenerate sign: " + (blockLocEntry.getKey().x + x_offset) + "," + blockLocEntry.getKey().y
                                    + "," + (
                                    blockLocEntry.getKey().z + z_offset));
                }
            } catch (IndexOutOfBoundsException e) {
                PS.debug("&c[WARN] Plot clear failed to regenerate sign: " + (blockLocEntry.getKey().x + x_offset) + "," + blockLocEntry.getKey().y
                        + "," + (
                        blockLocEntry.getKey().z + z_offset));
            }
        }
        for (final Entry<BlockLoc, ItemStack[]> blockLocEntry : dispenserContents.entrySet()) {
            try {
                final Block block =
                        world.getBlockAt(blockLocEntry.getKey().x + x_offset, blockLocEntry.getKey().y, blockLocEntry.getKey().z + z_offset);
                final BlockState state = block.getState();
                if (state instanceof Dispenser) {
                    ((InventoryHolder) state).getInventory().setContents(blockLocEntry.getValue());
                    state.update(true);
                } else {
                    PS.debug("&c[WARN] Plot clear failed to regenerate dispenser: " + (blockLocEntry.getKey().x + x_offset) + "," + blockLocEntry
                            .getKey().y + "," + (blockLocEntry.getKey().z + z_offset));
                }
            } catch (IllegalArgumentException e) {
                PS.debug("&c[WARN] Plot clear failed to regenerate dispenser (e): " + (blockLocEntry.getKey().x + x_offset) + "," + blockLocEntry
                        .getKey().y + "," + (blockLocEntry.getKey().z + z_offset));
            }
        }
        for (final Entry<BlockLoc, ItemStack[]> blockLocEntry : dropperContents.entrySet()) {
            try {
                final Block block =
                        world.getBlockAt(blockLocEntry.getKey().x + x_offset, blockLocEntry.getKey().y, blockLocEntry.getKey().z + z_offset);
                final BlockState state = block.getState();
                if (state instanceof Dropper) {
                    ((InventoryHolder) state).getInventory().setContents(blockLocEntry.getValue());
                    state.update(true);
                } else {
                    PS.debug("&c[WARN] Plot clear failed to regenerate dispenser: " + (blockLocEntry.getKey().x + x_offset) + "," + blockLocEntry
                            .getKey().y + "," + (blockLocEntry.getKey().z + z_offset));
                }
            } catch (IllegalArgumentException e) {
                PS.debug("&c[WARN] Plot clear failed to regenerate dispenser (e): " + (blockLocEntry.getKey().x + x_offset) + "," + blockLocEntry
                        .getKey().y + "," + (blockLocEntry.getKey().z + z_offset));
            }
        }
        for (final Entry<BlockLoc, ItemStack[]> blockLocEntry : beaconContents.entrySet()) {
            try {
                final Block block =
                        world.getBlockAt(blockLocEntry.getKey().x + x_offset, blockLocEntry.getKey().y, blockLocEntry.getKey().z + z_offset);
                final BlockState state = block.getState();
                if (state instanceof Beacon) {
                    ((InventoryHolder) state).getInventory().setContents(blockLocEntry.getValue());
                    state.update(true);
                } else {
                    PS.debug("&c[WARN] Plot clear failed to regenerate beacon: " + (blockLocEntry.getKey().x + x_offset) + "," + blockLocEntry
                            .getKey().y + "," + (blockLocEntry.getKey().z + z_offset));
                }
            } catch (IllegalArgumentException e) {
                PS.debug("&c[WARN] Plot clear failed to regenerate beacon (e): " + (blockLocEntry.getKey().x + x_offset) + "," + blockLocEntry
                        .getKey().y + "," + (blockLocEntry.getKey().z + z_offset));
            }
        }
        for (final Entry<BlockLoc, Material> blockLocMaterialEntry : jukeDisc.entrySet()) {
            try {
                final Block block =
                        world.getBlockAt(blockLocMaterialEntry.getKey().x + x_offset, blockLocMaterialEntry.getKey().y, blockLocMaterialEntry
                                .getKey().z + z_offset);
                final BlockState state = block.getState();
                if (state instanceof Jukebox) {
                    ((Jukebox) state).setPlaying(blockLocMaterialEntry.getValue());
                    state.update(true);
                } else {
                    PS.debug("&c[WARN] Plot clear failed to restore jukebox: " + (blockLocMaterialEntry.getKey().x + x_offset) + ","
                            + blockLocMaterialEntry
                            .getKey().y + "," + (
                            blockLocMaterialEntry.getKey().z + z_offset));
                }
            } catch (final Exception e) {
                PS.debug("&c[WARN] Plot clear failed to regenerate jukebox (e): " + (blockLocMaterialEntry.getKey().x + x_offset) + ","
                        + blockLocMaterialEntry
                        .getKey().y + "," + (
                        blockLocMaterialEntry.getKey().z + z_offset));
            }
        }
        for (final Entry<BlockLoc, Object[]> blockLocEntry : skullData.entrySet()) {
            try {
                final Block block =
                        world.getBlockAt(blockLocEntry.getKey().x + x_offset, blockLocEntry.getKey().y, blockLocEntry.getKey().z + z_offset);
                final BlockState state = block.getState();
                if (state instanceof Skull) {
                    final Object[] data = blockLocEntry.getValue();
                    if (data[0] != null) {
                        ((Skull) state).setOwner((String) data[0]);
                    }
                    if ((Integer) data[1] != 0) {
                        ((Skull) state).setRotation(BlockFace.values()[(int) data[1]]);
                    }
                    if ((Integer) data[2] != 0) {
                        ((Skull) state).setSkullType(SkullType.values()[(int) data[2]]);
                    }
                    state.update(true);
                } else {
                    PS.debug("&c[WARN] Plot clear failed to restore skull: " + (blockLocEntry.getKey().x + x_offset) + "," + blockLocEntry.getKey().y
                            + "," + (
                            blockLocEntry.getKey().z + z_offset));
                }
            } catch (final Exception e) {
                PS.debug("&c[WARN] Plot clear failed to regenerate skull (e): " + (blockLocEntry.getKey().x + x_offset) + "," + blockLocEntry
                        .getKey().y + "," + (blockLocEntry.getKey().z + z_offset));
            }
        }
        for (final Entry<BlockLoc, ItemStack[]> blockLocEntry : hopperContents.entrySet()) {
            try {
                final Block block =
                        world.getBlockAt(blockLocEntry.getKey().x + x_offset, blockLocEntry.getKey().y, blockLocEntry.getKey().z + z_offset);
                final BlockState state = block.getState();
                if (state instanceof Hopper) {
                    ((InventoryHolder) state).getInventory().setContents(blockLocEntry.getValue());
                    state.update(true);
                } else {
                    PS.debug("&c[WARN] Plot clear failed to regenerate hopper: " + (blockLocEntry.getKey().x + x_offset) + "," + blockLocEntry
                            .getKey().y + "," + (blockLocEntry.getKey().z + z_offset));
                }
            } catch (IllegalArgumentException e) {
                PS.debug("&c[WARN] Plot clear failed to regenerate hopper (e): " + (blockLocEntry.getKey().x + x_offset) + "," + blockLocEntry
                        .getKey().y + "," + (blockLocEntry.getKey().z + z_offset));
            }
        }
        for (final Entry<BlockLoc, Note> blockLocNoteEntry : noteBlockContents.entrySet()) {
            try {
                final Block block = world.getBlockAt(
                        blockLocNoteEntry.getKey().x + x_offset, blockLocNoteEntry.getKey().y, blockLocNoteEntry.getKey().z + z_offset);
                final BlockState state = block.getState();
                if (state instanceof NoteBlock) {
                    ((NoteBlock) state).setNote(blockLocNoteEntry.getValue());
                    state.update(true);
                } else {
                    PS.debug("&c[WARN] Plot clear failed to regenerate note block: " + (blockLocNoteEntry.getKey().x + x_offset) + ","
                            + blockLocNoteEntry
                            .getKey().y + "," + (
                            blockLocNoteEntry.getKey().z + z_offset));
                }
            } catch (final Exception e) {
                PS.debug("&c[WARN] Plot clear failed to regenerate note block (e): " + (blockLocNoteEntry.getKey().x + x_offset) + ","
                        + blockLocNoteEntry
                        .getKey().y + "," + (
                        blockLocNoteEntry.getKey().z + z_offset));
            }
        }
        for (final Entry<BlockLoc, Short> blockLocShortEntry : brewTime.entrySet()) {
            try {
                final Block block = world.getBlockAt(
                        blockLocShortEntry.getKey().x + x_offset, blockLocShortEntry.getKey().y, blockLocShortEntry.getKey().z + z_offset);
                final BlockState state = block.getState();
                if (state instanceof BrewingStand) {
                    ((BrewingStand) state).setBrewingTime(blockLocShortEntry.getValue());
                } else {
                    PS.debug("&c[WARN] Plot clear failed to restore brewing stand cooking: " + (blockLocShortEntry.getKey().x + x_offset) + ","
                            + blockLocShortEntry
                            .getKey().y + "," + (
                            blockLocShortEntry.getKey().z + z_offset));
                }
            } catch (final Exception e) {
                PS.debug("&c[WARN] Plot clear failed to restore brewing stand cooking (e): " + (blockLocShortEntry.getKey().x + x_offset) + "," +
                        blockLocShortEntry
                                .getKey().y + "," + (
                        blockLocShortEntry.getKey().z + z_offset));
            }
        }
        for (final Entry<BlockLoc, EntityType> blockLocEntityTypeEntry : spawnerData.entrySet()) {
            try {
                final Block block =
                        world.getBlockAt(blockLocEntityTypeEntry.getKey().x + x_offset, blockLocEntityTypeEntry.getKey().y, blockLocEntityTypeEntry
                                .getKey().z + z_offset);
                final BlockState state = block.getState();
                if (state instanceof CreatureSpawner) {
                    ((CreatureSpawner) state).setSpawnedType(blockLocEntityTypeEntry.getValue());
                    state.update(true);
                } else {
                    PS.debug("&c[WARN] Plot clear failed to restore spawner type: " + (blockLocEntityTypeEntry.getKey().x + x_offset) + ","
                            + blockLocEntityTypeEntry
                            .getKey().y + "," + (
                            blockLocEntityTypeEntry.getKey().z + z_offset));
                }
            } catch (final Exception e) {
                PS.debug("&c[WARN] Plot clear failed to restore spawner type (e): " + (blockLocEntityTypeEntry.getKey().x + x_offset) + "," +
                        blockLocEntityTypeEntry
                                .getKey().y + "," + (
                        blockLocEntityTypeEntry.getKey().z + z_offset));
            }
        }
        for (final Entry<BlockLoc, String> blockLocStringEntry : cmdData.entrySet()) {
            try {
                final Block block = world.getBlockAt(
                        blockLocStringEntry.getKey().x + x_offset, blockLocStringEntry.getKey().y, blockLocStringEntry.getKey().z + z_offset);
                final BlockState state = block.getState();
                if (state instanceof CommandBlock) {
                    ((CommandBlock) state).setCommand(blockLocStringEntry.getValue());
                    state.update(true);
                } else {
                    PS.debug("&c[WARN] Plot clear failed to restore command block: " + (blockLocStringEntry.getKey().x + x_offset) + ","
                            + blockLocStringEntry
                            .getKey().y + "," + (
                            blockLocStringEntry.getKey().z + z_offset));
                }
            } catch (final Exception e) {
                PS.debug("&c[WARN] Plot clear failed to restore command block (e): " + (blockLocStringEntry.getKey().x + x_offset) + ","
                        + blockLocStringEntry
                        .getKey().y + "," + (
                        blockLocStringEntry.getKey().z + z_offset));
            }
        }
        for (final Entry<BlockLoc, ItemStack[]> blockLocEntry : brewingStandContents.entrySet()) {
            try {
                final Block block =
                        world.getBlockAt(blockLocEntry.getKey().x + x_offset, blockLocEntry.getKey().y, blockLocEntry.getKey().z + z_offset);
                final BlockState state = block.getState();
                if (state instanceof BrewingStand) {
                    ((InventoryHolder) state).getInventory().setContents(blockLocEntry.getValue());
                    state.update(true);
                } else {
                    PS.debug("&c[WARN] Plot clear failed to regenerate brewing stand: " + (blockLocEntry.getKey().x + x_offset) + "," + blockLocEntry
                            .getKey().y + "," + (
                            blockLocEntry.getKey().z
                                    + z_offset));
                }
            } catch (IllegalArgumentException e) {
                PS.debug("&c[WARN] Plot clear failed to regenerate brewing stand (e): " + (blockLocEntry.getKey().x + x_offset) + "," + blockLocEntry
                        .getKey().y + "," + (
                        blockLocEntry.getKey().z
                                + z_offset));
            }
        }
        for (final Entry<BlockLoc, Short[]> blockLocEntry : furnaceTime.entrySet()) {
            try {
                final Block block =
                        world.getBlockAt(blockLocEntry.getKey().x + x_offset, blockLocEntry.getKey().y, blockLocEntry.getKey().z + z_offset);
                final BlockState state = block.getState();
                if (state instanceof Furnace) {
                    final Short[] time = blockLocEntry.getValue();
                    ((Furnace) state).setBurnTime(time[0]);
                    ((Furnace) state).setCookTime(time[1]);
                } else {
                    PS.debug("&c[WARN] Plot clear failed to restore furnace cooking: " + (blockLocEntry.getKey().x + x_offset) + "," + blockLocEntry
                            .getKey().y + "," + (
                            blockLocEntry.getKey().z + z_offset));
                }
            } catch (final Exception e) {
                PS.debug("&c[WARN] Plot clear failed to restore furnace cooking (e): " + (blockLocEntry.getKey().x + x_offset) + "," + blockLocEntry
                        .getKey().y + "," + (
                        blockLocEntry.getKey().z + z_offset));
            }
        }
        for (final Entry<BlockLoc, ItemStack[]> blockLocEntry : furnaceContents.entrySet()) {
            try {
                final Block block =
                        world.getBlockAt(blockLocEntry.getKey().x + x_offset, blockLocEntry.getKey().y, blockLocEntry.getKey().z + z_offset);
                final BlockState state = block.getState();
                if (state instanceof Furnace) {
                    ((InventoryHolder) state).getInventory().setContents(blockLocEntry.getValue());
                    state.update(true);
                } else {
                    PS.debug("&c[WARN] Plot clear failed to regenerate furnace: " + (blockLocEntry.getKey().x + x_offset) + "," + blockLocEntry
                            .getKey().y + "," + (blockLocEntry.getKey().z + z_offset));
                }
            } catch (IllegalArgumentException e) {
                PS.debug("&c[WARN] Plot clear failed to regenerate furnace (e): " + (blockLocEntry.getKey().x + x_offset) + "," + blockLocEntry
                        .getKey().y + "," + (blockLocEntry.getKey().z + z_offset));
            }
        }
        for (final Entry<BlockLoc, DyeColor> blockLocByteEntry : bannerBase.entrySet()) {
            try {
                final Block block = world.getBlockAt(
                        blockLocByteEntry.getKey().x + x_offset, blockLocByteEntry.getKey().y, blockLocByteEntry.getKey().z + z_offset);
                final BlockState state = block.getState();
                if (state instanceof Banner) {
                    final Banner banner = (Banner) state;
                    final DyeColor base = blockLocByteEntry.getValue();
                    final List<Pattern> patterns = bannerPatterns.get(blockLocByteEntry.getKey());
                    banner.setBaseColor(base);
                    banner.setPatterns(patterns);
                    state.update(true);
                } else {
                    PS.debug("&c[WARN] Plot clear failed to regenerate banner: " + (blockLocByteEntry.getKey().x + x_offset) + "," + blockLocByteEntry
                            .getKey().y + "," + (
                            blockLocByteEntry.getKey().z + z_offset));
                }
            } catch (final Exception e) {
                PS.debug("&c[WARN] Plot clear failed to regenerate banner (e): " + (blockLocByteEntry.getKey().x + x_offset) + "," + blockLocByteEntry
                        .getKey().y + "," + (
                        blockLocByteEntry.getKey().z + z_offset));
            }
        }
    }

    public static void saveBlocks(final World world, int maxY, final int x, final int z, final int offset_x, final int offset_z,
            boolean storeNormal) {
        maxY = Math.min(255, maxY);
        PlotBlock[] ids = storeNormal ? new PlotBlock[maxY + 1] : null;
        for (short y = 0; y <= maxY; y++) {
            final Block block = world.getBlockAt(x, y, z);
            final Material id = block.getType();
            if (!id.equals(Material.AIR)) {
                if (storeNormal) {
                    ids[y] = new PlotBlock((short) id.getId(), block.getData());
                }
                try {
                    BlockLoc bl = new BlockLoc(x + offset_x, y, z + offset_z);
                    if (block.getState() instanceof InventoryHolder) {
                        final InventoryHolder inventoryHolder = (InventoryHolder) block.getState();
                        final ItemStack[] inventory = inventoryHolder.getInventory().getContents().clone();
                        if (id == Material.CHEST) {
                            chestContents.put(bl, inventory);
                        } else if (id == Material.DISPENSER) {
                            dispenserContents.put(bl, inventory);
                        } else if (id == Material.BEACON) {
                            beaconContents.put(bl, inventory);
                        } else if (id == Material.DROPPER) {
                            dropperContents.put(bl, inventory);
                        } else if (id == Material.HOPPER) {
                            hopperContents.put(bl, inventory);
                        } else if (id == Material.BREWING_STAND) {
                            final BrewingStand brewingStand = (BrewingStand) inventoryHolder;
                            final short time = (short) brewingStand.getBrewingTime();
                            if (time > 0) {
                                brewTime.put(bl, time);
                            }
                            final ItemStack[] invBre = brewingStand.getInventory().getContents().clone();
                            brewingStandContents.put(bl, invBre);
                        } else if (id == Material.FURNACE || id == Material.BURNING_FURNACE) {
                            final Furnace furnace = (Furnace) inventoryHolder;
                            final short burn = furnace.getBurnTime();
                            final short cook = furnace.getCookTime();
                            final ItemStack[] invFur = furnace.getInventory().getContents().clone();
                            furnaceContents.put(bl, invFur);
                            if (cook != 0) {
                                furnaceTime.put(bl, new Short[]{burn, cook});
                            }
                        }
                    } else if (block.getState() instanceof CreatureSpawner) {
                        final CreatureSpawner spawner = (CreatureSpawner) block.getState();
                        final EntityType type = spawner.getSpawnedType();
                        if (type != null) {
                            spawnerData.put(bl, type);
                        }
                    } else if (block.getState() instanceof CommandBlock) {
                        final CommandBlock cmd = (CommandBlock) block.getState();
                        final String string = cmd.getCommand();
                        if (string != null && !string.isEmpty()) {
                            cmdData.put(bl, string);
                        }
                    } else if (block.getState() instanceof NoteBlock) {
                        final NoteBlock noteBlock = (NoteBlock) block.getState();
                        final Note note = noteBlock.getNote();
                        noteBlockContents.put(bl, note);
                    } else if (block.getState() instanceof Jukebox) {
                        final Jukebox jukebox = (Jukebox) block.getState();
                        final Material playing = jukebox.getPlaying();
                        if (playing != null) {
                            jukeDisc.put(bl, playing);
                        }
                    } else if (block.getState() instanceof Skull) {
                        final Skull skull = (Skull) block.getState();
                        final String o = skull.getOwner();
                        final byte skulltype = getOrdinal(SkullType.values(), skull.getSkullType());
                        skull.getRotation();
                        final short rot = getOrdinal(BlockFace.values(), skull.getRotation());
                        skullData.put(bl, new Object[]{o, rot, skulltype});
                    } else if (block.getState() instanceof Banner) {
                        final Banner banner = (Banner) block.getState();
                        final DyeColor base = banner.getBaseColor();
                        bannerBase.put(bl, base);
                        bannerPatterns.put(bl, banner.getPatterns());

                    }
                } catch (final Exception e) {
                    PS.debug("------------ FAILED TO DO SOMETHING --------");
                    e.printStackTrace();
                    PS.debug("------------ but we caught it ^ --------");
                }
            }
        }
        final PlotLoc loc = new PlotLoc(x, z);
        allblocks.put(loc, ids);
    }

    private static byte getOrdinal(final Object[] list, final Object value) {
        for (byte i = 0; i < list.length; i++) {
            if (list[i].equals(value)) {
                return i;
            }
        }
        return 0;
    }

    public static void swapChunk(final World world1, final World world2, final Chunk pos1, final Chunk pos2, final RegionWrapper r1,
            final RegionWrapper r2) {
        initMaps();
        final int relX = r2.minX - r1.minX;
        final int relZ = r2.minZ - r1.minZ;

        saveEntitiesIn(pos1, r1, relX, relZ, true);
        saveEntitiesIn(pos2, r2, -relX, -relZ, true);

        final int sx = pos1.getX() << 4;
        final int sz = pos1.getZ() << 4;

        String worldname1 = world1.getName();
        String worldname2 = world2.getName();

        for (int x = Math.max(r1.minX, sx); x <= Math.min(r1.maxX, sx + 15); x++) {
            for (int z = Math.max(r1.minZ, sz); z <= Math.min(r1.maxZ, sz + 15); z++) {
                saveBlocks(world1, 256, sx, sz, relX, relZ, false);
                for (int y = 0; y < 256; y++) {
                    final Block block1 = world1.getBlockAt(x, y, z);
                    final int id1 = block1.getTypeId();
                    final byte data1 = block1.getData();
                    final int xx = x + relX;
                    final int zz = z + relZ;
                    final Block block2 = world2.getBlockAt(xx, y, zz);
                    final int id2 = block2.getTypeId();
                    final byte data2 = block2.getData();
                    if (id1 == 0) {
                        if (id2 != 0) {
                            SetQueue.IMP.setBlock(worldname1, x, y, z, (short) id2, data2);
                            SetQueue.IMP.setBlock(worldname2, xx, y, zz, (short) 0, (byte) 0);
                        }
                    } else if (id2 == 0) {
                        SetQueue.IMP.setBlock(worldname1, x, y, z, (short) 0, (byte) 0);
                        SetQueue.IMP.setBlock(worldname2, xx, y, zz, (short) id1, data1);
                    } else if (id1 == id2) {
                        if (data1 != data2) {
                            block1.setData(data2);
                            block2.setData(data1);
                        }
                    } else {
                        SetQueue.IMP.setBlock(worldname1, x, y, z, (short) id2, data2);
                        SetQueue.IMP.setBlock(worldname2, xx, y, zz, (short) id1, data1);
                    }
                }
            }
        }
        while (SetQueue.IMP.forceChunkSet()) {
            ;
        }
        restoreBlocks(world1, 0, 0);
        restoreEntities(world1, 0, 0);
    }

    @Override
    public Set<ChunkLoc> getChunkChunks(final String world) {
        Set<ChunkLoc> chunks = super.getChunkChunks(world);
        for (final Chunk chunk : Bukkit.getWorld(world).getLoadedChunks()) {
            final ChunkLoc loc = new ChunkLoc(chunk.getX() >> 5, chunk.getZ() >> 5);
            if (!chunks.contains(loc)) {
                chunks.add(loc);
            }
        }
        return chunks;
    }

    @Override
    public void regenerateChunk(final String world, final ChunkLoc loc) {
        final World worldObj = Bukkit.getWorld(world);
        worldObj.regenerateChunk(loc.x, loc.z);
        SetQueue.IMP.queue.sendChunk(world, Collections.singletonList(loc));
        for (Entry<String, PlotPlayer> entry : UUIDHandler.getPlayers().entrySet()) {
            PlotPlayer pp = entry.getValue();
            Location pLoc = pp.getLocation();
            if (!StringMan.isEqual(world, pLoc.getWorld()) || !pLoc.getChunkLoc().equals(loc)) {
                continue;
            }
            pLoc.setY(WorldUtil.IMP.getHighestBlock(world, pLoc.getX(), pLoc.getZ()));
            pp.teleport(pLoc);
        }
    }

    @Override
    public boolean copyRegion(final Location pos1, final Location pos2, final Location newPos, final Runnable whenDone) {
        final int relX = newPos.getX() - pos1.getX();
        final int relZ = newPos.getZ() - pos1.getZ();

        final int relCX = relX >> 4;
        final int relCZ = relZ >> 4;

        final RegionWrapper region = new RegionWrapper(pos1.getX(), pos2.getX(), pos1.getZ(), pos2.getZ());
        final World oldWorld = Bukkit.getWorld(pos1.getWorld());
        final World newWorld = Bukkit.getWorld(newPos.getWorld());
        final String newWorldname = newWorld.getName();
        final List<ChunkLoc> chunks = new ArrayList<>();

        ChunkManager.chunkTask(pos1, pos2, new RunnableVal<int[]>() {
            @Override
            public void run(int[] value) {
                initMaps();

                final int bx = value[2];
                final int bz = value[3];

                final int tx = value[4];
                final int tz = value[5];

                // Load chunks
                final ChunkLoc loc1 = new ChunkLoc(value[0], value[1]);
                final ChunkLoc loc2 = new ChunkLoc(loc1.x + relCX, loc1.z + relCZ);
                final Chunk c1 = oldWorld.getChunkAt(loc1.x, loc1.z);
                final Chunk c2 = newWorld.getChunkAt(loc2.x, loc2.z);
                c1.load(true);
                c2.load(true);
                chunks.add(loc2);
                // entities
                saveEntitiesIn(c1, region);
                // copy chunk
                setChunkInPlotArea(null, new RunnableVal<PlotChunk<?>>() {
                    @Override
                    public void run(PlotChunk<?> value) {
                        for (int x = bx & 15; x <= (tx & 15); x++) {
                            for (int z = bz & 15; z <= (tz & 15); z++) {
                                for (int y = 1; y < 256; y++) {
                                    Block block = c1.getBlock(x, y, z);
                                    Material id = block.getType();
                                    switch (id) {
                                        case AIR:
                                        case GRASS:
                                        case COBBLESTONE:
                                        case GRAVEL:
                                        case GOLD_ORE:
                                        case IRON_ORE:
                                        case GLASS:
                                        case LAPIS_ORE:
                                        case LAPIS_BLOCK:
                                        case WEB:
                                        case DEAD_BUSH:
                                        case YELLOW_FLOWER:
                                        case BROWN_MUSHROOM:
                                        case RED_MUSHROOM:
                                        case GOLD_BLOCK:
                                        case IRON_BLOCK:
                                        case BRICK:
                                        case TNT:
                                        case BOOKSHELF:
                                        case MOSSY_COBBLESTONE:
                                        case OBSIDIAN:
                                        case FIRE:
                                        case REDSTONE_WIRE:
                                        case DIAMOND_ORE:
                                        case DIAMOND_BLOCK:
                                        case WORKBENCH:
                                        case SOIL:
                                        case BEDROCK:
                                        case WATER:
                                        case STATIONARY_WATER:
                                        case LAVA:
                                        case STATIONARY_LAVA:
                                        case REDSTONE_ORE:
                                        case GLOWING_REDSTONE_ORE:
                                        case SNOW:
                                        case ICE:
                                        case SNOW_BLOCK:
                                        case CACTUS:
                                        case CLAY:
                                        case SUGAR_CANE_BLOCK:
                                        case FENCE:
                                        case NETHERRACK:
                                        case SOUL_SAND:
                                        case IRON_FENCE:
                                        case THIN_GLASS:
                                        case MELON_BLOCK:
                                        case MYCEL:
                                        case NETHER_BRICK:
                                        case NETHER_FENCE:
                                        case ENDER_STONE:
                                        case DRAGON_EGG:
                                        case EMERALD_ORE:
                                        case EMERALD_BLOCK:
                                        case SLIME_BLOCK:
                                        case BARRIER:
                                        case SEA_LANTERN:
                                        case HAY_BLOCK:
                                        case HARD_CLAY:
                                        case COAL_BLOCK:
                                        case PACKED_ICE:
                                        case DOUBLE_STONE_SLAB2:
                                        case STONE_SLAB2:
                                        case SPRUCE_FENCE:
                                        case BIRCH_FENCE:
                                        case JUNGLE_FENCE:
                                        case DARK_OAK_FENCE:
                                        case ACACIA_FENCE:
                                            value.setBlock(x, y, z, id.getId(), (byte) 0);
                                            break;
                                        default:
                                            value.setBlock(x, y, z, id.getId(), block.getData());
                                            break;
                                    }
                                }
                            }
                        }
                    }
                }, newWorldname, loc2);
                // restore chunk
                restoreBlocks(newWorld, relX, relZ);
                restoreEntities(newWorld, relX, relZ);
            }
        }, new Runnable() {
            @Override
            public void run() {
                SetQueue.IMP.queue.sendChunk(newWorldname, chunks);
                TaskManager.runTask(whenDone);
            }
        }, 5);
        return true;
    }

    public void saveRegion(final World world, int x1, int x2, int z1, int z2) {
        if (z1 > z2) {
            final int tmp = z1;
            z1 = z2;
            z2 = tmp;
        }
        if (x1 > x2) {
            final int tmp = x1;
            x1 = x2;
            x2 = tmp;
        }
        for (int x = x1; x <= x2; x++) {
            for (int z = z1; z <= z2; z++) {
                saveBlocks(world, 256, x, z, 0, 0, true);
            }
        }
    }

    @Override
    public boolean regenerateRegion(final Location pos1, final Location pos2, final boolean ignoreAugment, final Runnable whenDone) {
        final String world = pos1.getWorld();

        final int p1x = pos1.getX();
        final int p1z = pos1.getZ();
        final int p2x = pos2.getX();
        final int p2z = pos2.getZ();
        final int bcx = p1x >> 4;
        final int bcz = p1z >> 4;
        final int tcx = p2x >> 4;
        final int tcz = p2z >> 4;

        final List<ChunkLoc> chunks = new ArrayList<>();

        for (int x = bcx; x <= tcx; x++) {
            for (int z = bcz; z <= tcz; z++) {
                chunks.add(new ChunkLoc(x, z));
            }
        }
        final World worldObj = Bukkit.getWorld(world);
        TaskManager.runTask(new Runnable() {
            @Override
            public void run() {
                final long start = System.currentTimeMillis();
                while (!chunks.isEmpty() && System.currentTimeMillis() - start < 5) {
                    final ChunkLoc chunk = chunks.remove(0);
                    final int x = chunk.x;
                    final int z = chunk.z;
                    final int xxb = x << 4;
                    final int zzb = z << 4;
                    final int xxt = xxb + 15;
                    final int zzt = zzb + 15;
                    final Chunk chunkObj = worldObj.getChunkAt(x, z);
                    if (!chunkObj.load(false)) {
                        continue;
                    }
                    RegionWrapper currentPlotClear = new RegionWrapper(pos1.getX(), pos2.getX(), pos1.getZ(), pos2.getZ());
                    if (xxb >= p1x && xxt <= p2x && zzb >= p1z && zzt <= p2z) {
                        AugmentedUtils.bypass(ignoreAugment, new Runnable() {
                            @Override
                            public void run() {
                                regenerateChunk(world, chunk);
                            }
                        });
                        continue;
                    }
                    boolean checkX1 = false;

                    int xxb2;

                    if (x == bcx) {
                        xxb2 = p1x - 1;
                        checkX1 = true;
                    } else {
                        xxb2 = xxb;
                    }
                    boolean checkX2 = false;
                    int xxt2;
                    if (x == tcx) {
                        xxt2 = p2x + 1;
                        checkX2 = true;
                    } else {
                        xxt2 = xxt;
                    }
                    boolean checkZ1 = false;
                    int zzb2;
                    if (z == bcz) {
                        zzb2 = p1z - 1;
                        checkZ1 = true;
                    } else {
                        zzb2 = zzb;
                    }
                    boolean checkZ2 = false;
                    int zzt2;
                    if (z == tcz) {
                        zzt2 = p2z + 1;
                        checkZ2 = true;
                    } else {
                        zzt2 = zzt;
                    }
                    initMaps();
                    if (checkX1) {
                        saveRegion(worldObj, xxb, xxb2, zzb2, zzt2); //
                    }
                    if (checkX2) {
                        saveRegion(worldObj, xxt2, xxt, zzb2, zzt2); //
                    }
                    if (checkZ1) {
                        saveRegion(worldObj, xxb2, xxt2, zzb, zzb2); //
                    }
                    if (checkZ2) {
                        saveRegion(worldObj, xxb2, xxt2, zzt2, zzt); //
                    }
                    if (checkX1 && checkZ1) {
                        saveRegion(worldObj, xxb, xxb2, zzb, zzb2); //
                    }
                    if (checkX2 && checkZ1) {
                        saveRegion(worldObj, xxt2, xxt, zzb, zzb2); // ?
                    }
                    if (checkX1 && checkZ2) {
                        saveRegion(worldObj, xxb, xxb2, zzt2, zzt); // ?
                    }
                    if (checkX2 && checkZ2) {
                        saveRegion(worldObj, xxt2, xxt, zzt2, zzt); //
                    }
                    saveEntitiesOut(chunkObj, currentPlotClear);
                    AugmentedUtils.bypass(ignoreAugment, new Runnable() {
                        @Override
                        public void run() {
                            setChunkInPlotArea(null, new RunnableVal<PlotChunk<?>>() {
                                @Override
                                public void run(PlotChunk<?> value) {
                                    int cx = value.getX();
                                    int cz = value.getZ();
                                    int bx = cx << 4;
                                    int bz = cz << 4;
                                    for (int x = 0; x < 16; x++) {
                                        for (int z = 0; z < 16; z++) {
                                            PlotLoc loc = new PlotLoc(bx + x, bz + z);
                                            PlotBlock[] ids = allblocks.get(loc);
                                            if (ids != null) {
                                                for (int y = 0; y < Math.min(128, ids.length); y++) {
                                                    PlotBlock id = ids[y];
                                                    if (id != null) {
                                                        value.setBlock(x, y, z, id);
                                                    } else {
                                                        value.setBlock(x, y, z, 0, (byte) 0);
                                                    }
                                                }
                                                for (int y = Math.min(128, ids.length); y < ids.length; y++) {
                                                    PlotBlock id = ids[y];
                                                    if (id != null) {
                                                        value.setBlock(x, y, z, id);
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }, world, chunk);
                        }
                    });
                    restoreBlocks(worldObj, 0, 0);
                    restoreEntities(worldObj, 0, 0);
                }
                if (!chunks.isEmpty()) {
                    TaskManager.runTaskLater(this, 1);
                } else {
                    TaskManager.runTaskLater(whenDone, 1);
                }
            }
        });
        return true;
    }

    @Override
    public void clearAllEntities(final Location pos1, final Location pos2) {
        final String world = pos1.getWorld();
        final List<Entity> entities = BukkitUtil.getEntities(world);
        final int bx = pos1.getX();
        final int bz = pos1.getZ();
        final int tx = pos2.getX();
        final int tz = pos2.getZ();
        for (final Entity entity : entities) {
            if (!(entity instanceof Player)) {
                final org.bukkit.Location loc = entity.getLocation();
                if (loc.getX() >= bx && loc.getX() <= tx && loc.getZ() >= bz && loc.getZ() <= tz) {
                    entity.remove();
                }
            }
        }
    }

    @Override
    public boolean loadChunk(final String world, final ChunkLoc loc, final boolean force) {
        return BukkitUtil.getWorld(world).getChunkAt(loc.x, loc.z).load(force);
    }

    @Override
    public void unloadChunk(final String world, final ChunkLoc loc, final boolean save, final boolean safe) {
        if (!PS.get().isMainThread(Thread.currentThread())) {
            TaskManager.runTask(new Runnable() {
                @Override
                public void run() {
                    BukkitUtil.getWorld(world).unloadChunk(loc.x, loc.z, save, safe);
                }
            });
        } else {
            BukkitUtil.getWorld(world).unloadChunk(loc.x, loc.z, save, safe);
        }
    }

    @Override
    public void swap(final Location bot1, final Location top1, final Location bot2, final Location top2, final Runnable whenDone) {
        final RegionWrapper region1 = new RegionWrapper(bot1.getX(), top1.getX(), bot1.getZ(), top1.getZ());
        final RegionWrapper region2 = new RegionWrapper(bot2.getX(), top2.getX(), bot2.getZ(), top2.getZ());
        final World world1 = Bukkit.getWorld(bot1.getWorld());
        final World world2 = Bukkit.getWorld(bot2.getWorld());

        final int relX = bot2.getX() - bot1.getX();
        final int relZ = bot2.getZ() - bot1.getZ();

        for (int x = bot1.getX() >> 4; x <= top1.getX() >> 4; x++) {
            for (int z = bot1.getZ() >> 4; z <= top1.getZ() >> 4; z++) {
                final Chunk chunk1 = world1.getChunkAt(x, z);
                final Chunk chunk2 = world2.getChunkAt(x + (relX >> 4), z + (relZ >> 4));
                swapChunk(world1, world2, chunk1, chunk2, region1, region2);
            }
        }
        TaskManager.runTaskLater(whenDone, 1);
    }

    @Override
    public int[] countEntities(final Plot plot) {
        PlotArea area = plot.getArea();
        final World world = BukkitUtil.getWorld(area.worldname);

        final Location bot = plot.getBottomAbs();
        final Location top = plot.getTopAbs();
        final int bx = bot.getX() >> 4;
        final int bz = bot.getZ() >> 4;

        final int tx = top.getX() >> 4;
        final int tz = top.getZ() >> 4;

        final int size = tx - bx << 4;

        final Set<Chunk> chunks = new HashSet<>();
        for (int X = bx; X <= tx; X++) {
            for (int Z = bz; Z <= tz; Z++) {
                if (world.isChunkLoaded(X, Z)) {
                    chunks.add(world.getChunkAt(X, Z));
                }
            }
        }

        boolean doWhole = false;
        List<Entity> entities = null;
        if (size > 200) {
            entities = world.getEntities();
            if (entities.size() < 16 + size * size / 64) {
                doWhole = true;
            }
        }

        final int[] count = new int[6];
        if (doWhole) {
            for (final Entity entity : entities) {
                final org.bukkit.Location loc = entity.getLocation();
                final Chunk chunk = loc.getChunk();
                if (chunks.contains(chunk)) {
                    final int X = chunk.getX();
                    final int Z = chunk.getX();
                    if (X > bx && X < tx && Z > bz && Z < tz) {
                        count(count, entity);
                    } else {
                        Plot other = area.getPlot(BukkitUtil.getLocation(loc));
                        if (plot.equals(other)) {
                            count(count, entity);
                        }
                    }
                }
            }
        } else {
            for (final Chunk chunk : chunks) {
                final int X = chunk.getX();
                final int Z = chunk.getX();
                final Entity[] ents = chunk.getEntities();
                for (final Entity entity : ents) {
                    if (X == bx || X == tx || Z == bz || Z == tz) {
                        Plot other = area.getPlot(BukkitUtil.getLocation(entity));
                        if (plot.equals(other)) {
                            count(count, entity);
                        }
                    } else {
                        count(count, entity);
                    }
                }
            }
        }
        return count;
    }

    private void count(final int[] count, final Entity entity) {
        switch (entity.getType()) {
            case PLAYER:
                // not valid
                return;
            case SMALL_FIREBALL:
            case FIREBALL:
            case DROPPED_ITEM:
            case EGG:
            case THROWN_EXP_BOTTLE:
            case SPLASH_POTION:
            case SNOWBALL:
            case ENDER_PEARL:
            case ARROW:
                // projectile
            case PRIMED_TNT:
            case FALLING_BLOCK:
                // Block entities
            case ENDER_CRYSTAL:
            case COMPLEX_PART:
            case FISHING_HOOK:
            case ENDER_SIGNAL:
            case EXPERIENCE_ORB:
            case LEASH_HITCH:
            case FIREWORK:
            case WEATHER:
            case LIGHTNING:
            case WITHER_SKULL:
            case UNKNOWN:
                // non moving / unremovable
                break;
            case ITEM_FRAME:
            case PAINTING:
            case ARMOR_STAND:
                count[5]++;
                // misc
            case MINECART:
            case MINECART_CHEST:
            case MINECART_COMMAND:
            case MINECART_FURNACE:
            case MINECART_HOPPER:
            case MINECART_MOB_SPAWNER:
            case MINECART_TNT:
            case BOAT:
                count[4]++;
                break;
            case RABBIT:
            case SHEEP:
            case MUSHROOM_COW:
            case OCELOT:
            case PIG:
            case HORSE:
            case SQUID:
            case VILLAGER:
            case IRON_GOLEM:
            case WOLF:
            case CHICKEN:
            case COW:
            case SNOWMAN:
            case BAT:
                // animal
                count[3]++;
                count[1]++;
                break;
            case BLAZE:
            case CAVE_SPIDER:
            case CREEPER:
            case ENDERMAN:
            case ENDERMITE:
            case ENDER_DRAGON:
            case GHAST:
            case GIANT:
            case GUARDIAN:
            case MAGMA_CUBE:
            case PIG_ZOMBIE:
            case SILVERFISH:
            case SKELETON:
            case SLIME:
            case SPIDER:
            case WITCH:
            case WITHER:
            case ZOMBIE:
                // monster
                count[3]++;
                count[2]++;
                break;
            default:
                if (entity instanceof Creature) {
                    count[3]++;
                    if (entity instanceof Animals) {
                        count[1]++;
                    } else {
                        count[2]++;
                    }
                } else {
                    count[4]++;
                }
        }
        count[0]++;
    }
}
