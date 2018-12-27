package com.github.intellectualsites.plotsquared.plot.object.schematic;

import com.google.common.collect.Maps;
import com.sk89q.jnbt.*;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.extension.input.InputParseException;
import com.sk89q.worldedit.extension.input.ParserContext;
import com.sk89q.worldedit.extent.clipboard.BlockArrayClipboard;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.NBTSchematicReader;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.world.block.BlockState;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkNotNull;

public class SpongeSchematicReader extends NBTSchematicReader {

    private final NBTInputStream inputStream;
/*    private static final List<NBTCompatibilityHandler> COMPATIBILITY_HANDLERS = new ArrayList<>();

    static {
        // If NBT Compat handlers are needed - add them here.
    }*/

    public SpongeSchematicReader(NBTInputStream inputStream) {
        checkNotNull(inputStream);
        this.inputStream = inputStream;
    }


    @Override public Clipboard read() throws IOException {
        NamedTag rootTag = inputStream.readNamedTag();
        if (!rootTag.getName().equals("Schematic")) {
            throw new IOException("Tag 'Schematic' does not exist or is not first");
        }
        CompoundTag schematicTag = (CompoundTag) rootTag.getTag();

        // Check
        Map<String, Tag> schematic = schematicTag.getValue();
        int version = requireTag(schematic, "Version", IntTag.class).getValue();
        switch (version) {
            case 1:
                return readVersion1(schematic);
            default:
                throw new IOException("This schematic version is currently not supported");
        }
    }

    private Clipboard readVersion1(Map<String, Tag> schematic) throws IOException {
        BlockVector3 origin;
        Region region;

        int width = requireTag(schematic, "Width", ShortTag.class).getValue();
        int height = requireTag(schematic, "Height", ShortTag.class).getValue();
        int length = requireTag(schematic, "Length", ShortTag.class).getValue();

        origin = BlockVector3.at(0, 0, 0);
        region =
            new CuboidRegion(origin, origin.add(width, height, length).subtract(BlockVector3.ONE));

        int paletteMax = requireTag(schematic, "PaletteMax", IntTag.class).getValue();
        Map<String, Tag> paletteObject =
            requireTag(schematic, "Palette", CompoundTag.class).getValue();
        if (paletteObject.size() != paletteMax) {
            throw new IOException("Differing given palette size to actual size");
        }

        Map<Integer, BlockState> palette = new HashMap<>();

        ParserContext parserContext = new ParserContext();
        parserContext.setRestricted(false);
        parserContext.setTryLegacy(false);
        parserContext.setPreferringWildcard(false);

        for (String palettePart : paletteObject.keySet()) {
            int id = requireTag(paletteObject, palettePart, IntTag.class).getValue();
            BlockState state;
            try {
                state = WorldEdit.getInstance().getBlockFactory()
                    .parseFromInput(palettePart, parserContext).toImmutableState();
            } catch (InputParseException e) {
                throw new IOException("Invalid BlockState in schematic: " + palettePart
                    + ". Are you missing a mod or using a schematic made in a newer version of Minecraft?");
            }
            palette.put(id, state);
        }

        byte[] blocks = requireTag(schematic, "BlockData", ByteArrayTag.class).getValue();

        Map<BlockVector3, Map<String, Tag>> tileEntitiesMap = new HashMap<>();
        try {
            List<Map<String, Tag>> tileEntityTags =
                requireTag(schematic, "TileEntities", ListTag.class).getValue().stream()
                    .map(tag -> (CompoundTag) tag).map(CompoundTag::getValue)
                    .collect(Collectors.toList());

            for (Map<String, Tag> tileEntity : tileEntityTags) {
                int[] pos = requireTag(tileEntity, "Pos", IntArrayTag.class).getValue();
                tileEntitiesMap.put(BlockVector3.at(pos[0], pos[1], pos[2]), tileEntity);
            }
        } catch (Exception e) {
            throw new IOException("Failed to load Tile Entities: " + e.getMessage());
        }

        BlockArrayClipboard clipboard = new BlockArrayClipboard(region);
        clipboard.setOrigin(origin);

        int index = 0;
        int i = 0;
        int value = 0;
        int varintLength = 0;
        while (i < blocks.length) {
            value = 0;
            varintLength = 0;

            while (true) {
                value |= (blocks[i] & 127) << (varintLength++ * 7);
                if (varintLength > 5) {
                    throw new RuntimeException("VarInt too big (probably corrupted data)");
                }
                if ((blocks[i] & 128) != 128) {
                    i++;
                    break;
                }
                i++;
            }
            // index = (y * length + z) * width + x
            int y = index / (width * length);
            int z = (index % (width * length)) / width;
            int x = (index % (width * length)) % width;
            BlockState state = palette.get(value);
            BlockVector3 pt = BlockVector3.at(x, y, z);
            try {
                if (tileEntitiesMap.containsKey(pt)) {
                    Map<String, Tag> values = Maps.newHashMap(tileEntitiesMap.get(pt));
/*                    for (NBTCompatibilityHandler handler : COMPATIBILITY_HANDLERS) {
                        if (handler.isAffectedBlock(state)) {
                            handler.updateNBT(state, values);
                        }
                    }*/
                    values.put("x", new IntTag(pt.getBlockX()));
                    values.put("y", new IntTag(pt.getBlockY()));
                    values.put("z", new IntTag(pt.getBlockZ()));
                    values.put("id", values.get("Id"));
                    values.remove("Id");
                    values.remove("Pos");
                    clipboard.setBlock(clipboard.getMinimumPoint().add(pt),
                        state.toBaseBlock(new CompoundTag(values)));
                } else {
                    clipboard.setBlock(clipboard.getMinimumPoint().add(pt), state);
                }
            } catch (WorldEditException e) {
                throw new IOException("Failed to load a block in the schematic");
            }

            index++;
        }

        return clipboard;
    }

    @Override public void close() throws IOException {
        inputStream.close();
    }
}
