////////////////////////////////////////////////////////////////////////////////////////////////////
// PlotSquared - A plot manager and world generator for the Bukkit API                             /
// Copyright (c) 2014 IntellectualSites/IntellectualCrafters                                       /
//                                                                                                 /
// This program is free software; you can redistribute it and/or modify                            /
// it under the terms of the GNU General Public License as published by                            /
// the Free Software Foundation; either version 3 of the License, or                               /
// (at your option) any later version.                                                             /
//                                                                                                 /
// This program is distributed in the hope that it will be useful,                                 /
// but WITHOUT ANY WARRANTY; without even the implied warranty of                                  /
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the                                   /
// GNU General Public License for more details.                                                    /
//                                                                                                 /
// You should have received a copy of the GNU General Public License                               /
// along with this program; if not, write to the Free Software Foundation,                         /
// Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301  USA                               /
//                                                                                                 /
// You can contact us via: support@intellectualsites.com                                           /
////////////////////////////////////////////////////////////////////////////////////////////////////
package com.intellectualcrafters.plot.commands;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.GZIPOutputStream;

import com.intellectualcrafters.plot.PlotSquared;
import com.intellectualcrafters.plot.config.C;
import com.intellectualcrafters.plot.object.PlotPlayer;
import com.intellectualcrafters.plot.object.PlotWorld;
import com.intellectualcrafters.plot.util.BlockManager;
import com.intellectualcrafters.plot.util.MainUtil;

public class Template extends SubCommand {
    public Template() {
        super("template", "plots.admin", "Create or use a world template", "template", "", CommandCategory.DEBUG, true);
    }

    @Override
    public boolean execute(final PlotPlayer plr, final String... args) {
        if (args.length != 2) {
            MainUtil.sendMessage(plr, C.COMMAND_SYNTAX, "/plot template <import|export> <world>");
            return false;
        }
        final String world = args[1];
        final PlotWorld plotworld = PlotSquared.getPlotWorld(world);
        if (!BlockManager.manager.isWorld(world) || (plotworld == null)) {
            MainUtil.sendMessage(plr, C.NOT_VALID_PLOT_WORLD);
            return false;
        }
        switch (args[0].toLowerCase()) {
            case "import": {
                // TODO import template
                MainUtil.sendMessage(plr, "TODO");
                return true;
            }
            case "export": {
                MainUtil.sendMessage(plr, "TODO");
            }
        }
        // TODO allow world settings (including schematics to be packed into a single file)
        // TODO allow world created based on these packaged files
        return true;
    }

    public void gzipIt(final String output, final String input) {
        final byte[] buffer = new byte[1024];
        try {
            final GZIPOutputStream gzos = new GZIPOutputStream(new FileOutputStream(output));
            final FileInputStream in = new FileInputStream(input);
            int len;
            while ((len = in.read(buffer)) > 0) {
                gzos.write(buffer, 0, len);
            }
            in.close();
            gzos.finish();
            gzos.close();
        } catch (final IOException ex) {
            ex.printStackTrace();
        }
    }
}
