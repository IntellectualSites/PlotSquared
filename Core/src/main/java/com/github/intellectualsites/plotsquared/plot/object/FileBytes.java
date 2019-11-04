package com.github.intellectualsites.plotsquared.plot.object;

import com.github.intellectualsites.plotsquared.plot.util.block.BlockUtil;

public class FileBytes {

    public final String path;
    public final byte[] data;

    public FileBytes(String path, byte[] data) {
        this.path = path;
        this.data = data;
    }
}
