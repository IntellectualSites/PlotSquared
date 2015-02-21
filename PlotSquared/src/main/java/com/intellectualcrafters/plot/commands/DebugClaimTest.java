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

import java.util.ArrayList;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;

import com.google.common.collect.BiMap;
import com.intellectualcrafters.plot.PlotSquared;
import com.intellectualcrafters.plot.config.C;
import com.intellectualcrafters.plot.database.DBFunc;
import com.intellectualcrafters.plot.events.PlayerClaimPlotEvent;
import com.intellectualcrafters.plot.object.Plot;
import com.intellectualcrafters.plot.object.PlotId;
import com.intellectualcrafters.plot.object.PlotManager;
import com.intellectualcrafters.plot.object.PlotWorld;
import com.intellectualcrafters.plot.object.StringWrapper;
import com.intellectualcrafters.plot.util.MainUtil;
import com.intellectualcrafters.plot.util.bukkit.BukkitPlayerFunctions;
import com.intellectualcrafters.plot.util.bukkit.UUIDHandler;

/**
 * @author Citymonstret
 */
public class DebugClaimTest extends SubCommand {
    public DebugClaimTest() {
        super(Command.DEBUGCLAIMTEST, "If you accidentally delete your database, this command will attempt to restore all plots based on the data from the plot signs. Execution time may vary", "debugclaimtest", CommandCategory.DEBUG, false);
    }
    
    @SuppressWarnings("unused")
    public static boolean claimPlot(final Player player, final Plot plot, final boolean teleport) {
        return claimPlot(player, plot, teleport, "");
    }
    
    public static boolean claimPlot(final Player player, final Plot plot, final boolean teleport, @SuppressWarnings("unused") final String schematic) {
        final PlayerClaimPlotEvent event = new PlayerClaimPlotEvent(player, plot, true);
        Bukkit.getPluginManager().callEvent(event);
        if (!event.isCancelled()) {
            MainUtil.createPlot(player, plot);
            MainUtil.setSign(player, plot);
            MainUtil.sendMessage(BukkitUtil.getPlayer(player), C.CLAIMED);
            if (teleport) {
                PlotSquared.teleportPlayer(player, BukkitUtil.getLocation(entity), plot);
            }
        }
        return event.isCancelled();
    }
    
    @Override
    public boolean execute(final PlotPlayer plr, final String... args) {
        if (plr == null) {
            if (args.length < 3) {
                return !BukkitPlayerFunctions.sendMessage(null, "If you accidentally delete your database, this command will attempt to restore all plots based on the data from the plot signs. \n\n&cMissing world arg /plot debugclaimtest {world} {PlotId min} {PlotId max}");
            }
            final World world = Bukkit.getWorld(args[0]);
            if ((world == null) || !PlotSquared.isPlotWorld(world)) {
                return !BukkitPlayerFunctions.sendMessage(null, "&cInvalid plot world!");
            }
            PlotId min, max;
            try {
                final String[] split1 = args[1].split(";");
                final String[] split2 = args[2].split(";");
                min = new PlotId(Integer.parseInt(split1[0]), Integer.parseInt(split1[1]));
                max = new PlotId(Integer.parseInt(split2[0]), Integer.parseInt(split2[1]));
            } catch (final Exception e) {
                return !BukkitPlayerFunctions.sendMessage(null, "&cInvalid min/max values. &7The values are to Plot IDs in the format &cX;Y &7where X,Y are the plot coords\nThe conversion will only check the plots in the selected area.");
            }
            BukkitPlayerFunctions.sendMessage(null, "&3Sign Block&8->&3PlotSquared&8: &7Beginning sign to plot conversion. This may take a while...");
            BukkitPlayerFunctions.sendMessage(null, "&3Sign Block&8->&3PlotSquared&8: Found an excess of 250,000 chunks. Limiting search radius... (~3.8 min)");
            final PlotManager manager = PlotSquared.getPlotManager(world);
            final PlotWorld plotworld = PlotSquared.getPlotWorld(world);
            final ArrayList<Plot> plots = new ArrayList<>();
            for (final PlotId id : BukkitPlayerFunctions.getPlotSelectionIds(min, max)) {
                final Plot plot = MainUtil.getPlot(world, id);
                final boolean contains = PlotSquared.getPlots(world).containsKey(plot.id);
                if (contains) {
                    BukkitPlayerFunctions.sendMessage(null, " - &cDB Already contains: " + plot.id);
                    continue;
                }
                final Location loc = manager.getSignLoc(world, plotworld, plot);
                final Chunk chunk = world.getChunkAt(loc);
                if (!chunk.isLoaded()) {
                    final boolean result = chunk.load(false);
                    if (!result) {
                        continue;
                    }
                }
                final Block block = world.getBlockAt(loc);
                if (block != null) {
                    if (block.getState() instanceof Sign) {
                        final Sign sign = (Sign) block.getState();
                        String line = sign.getLine(2);
                        if ((line != null) && (line.length() > 2)) {
                            line = line.substring(2);
                            final BiMap<StringWrapper, UUID> map = UUIDHandler.getUuidMap();
                            UUID uuid = (map.get(new StringWrapper(line)));
                            if (uuid == null) {
                                for (final StringWrapper string : map.keySet()) {
                                    if (string.value.toLowerCase().startsWith(line.toLowerCase())) {
                                        uuid = map.get(string);
                                        break;
                                    }
                                }
                            }
                            if (uuid == null) {
                                uuid = UUIDHandler.getUUID(line);
                            }
                            if (uuid != null) {
                                BukkitPlayerFunctions.sendMessage(null, " - &aFound plot: " + plot.id + " : " + line);
                                plot.owner = uuid;
                                plot.hasChanged = true;
                                plots.add(plot);
                            } else {
                                BukkitPlayerFunctions.sendMessage(null, " - &cInvalid playername: " + plot.id + " : " + line);
                            }
                        }
                    }
                }
            }
            if (plots.size() > 0) {
                BukkitPlayerFunctions.sendMessage(null, "&3Sign Block&8->&3PlotSquared&8: &7Updating '" + plots.size() + "' plots!");
                DBFunc.createPlots(plots);
                DBFunc.createAllSettingsAndHelpers(plots);
                for (final Plot plot : plots) {
                    PlotSquared.updatePlot(plot);
                }
                BukkitPlayerFunctions.sendMessage(null, "&3Sign Block&8->&3PlotSquared&8: &7Complete!");
            } else {
                BukkitPlayerFunctions.sendMessage(null, "No plots were found for the given search.");
            }
        } else {
            BukkitPlayerFunctions.sendMessage(plr, "&6This command can only be executed by console as it has been deemed unsafe if abused.");
        }
        return true;
    }
}
