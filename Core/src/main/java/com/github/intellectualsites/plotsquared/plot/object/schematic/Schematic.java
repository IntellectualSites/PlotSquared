package com.github.intellectualsites.plotsquared.plot.object.schematic;

import com.sk89q.jnbt.NBTOutputStream;
import com.sk89q.jnbt.Tag;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.extent.clipboard.BlockArrayClipboard;
import com.sk89q.worldedit.extent.clipboard.io.SpongeSchematicWriter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.world.block.BaseBlock;
import lombok.Getter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class Schematic {
    // Lossy but fast
    @Getter private final BlockArrayClipboard clipboard;
    @Getter private Map<String, Tag> flags = new HashMap<>();

    public Schematic(BlockArrayClipboard clip) {
        this.clipboard = clip;
    }

    public void setFlags(Map<String, Tag> flags) {
        this.flags = flags == null ? new HashMap<>() : flags;
    }

    public boolean setBlock(BlockVector3 position, BaseBlock block) throws WorldEditException {
        if (clipboard.getRegion().contains(position)) {
            BlockVector3 v = position.subtract(clipboard.getRegion().getMinimumPoint());
            clipboard.setBlock(v, block);
            return true;
        } else {
            return false;
        }
    }

    public void save(File file) throws IOException {
        try (SpongeSchematicWriter ssw = new SpongeSchematicWriter(
            new NBTOutputStream(new FileOutputStream(file)))) {
            ssw.write(clipboard);
        }
    }
}
