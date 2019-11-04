package com.github.intellectualsites.plotsquared.bukkit.uuid;

import com.github.intellectualsites.plotsquared.plot.util.block.BlockUtil;

import java.io.File;
import java.io.FilenameFilter;

public class DatFileFilter implements FilenameFilter {

    @Override public boolean accept(File dir, String name) {
        return name.endsWith(".dat");
    }
}
