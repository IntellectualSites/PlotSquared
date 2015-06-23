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
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import com.intellectualcrafters.plot.PlotSquared;
import com.intellectualcrafters.plot.config.C;
import com.intellectualcrafters.plot.flag.Flag;
import com.intellectualcrafters.plot.flag.FlagManager;
import com.intellectualcrafters.plot.object.Plot;
import com.intellectualcrafters.plot.object.PlotPlayer;
import com.intellectualcrafters.plot.util.EconHandler;
import com.intellectualcrafters.plot.util.MainUtil;
import com.intellectualcrafters.plot.util.Permissions;
import com.intellectualcrafters.plot.util.StringComparison;
import com.intellectualcrafters.plot.util.bukkit.UUIDHandler;

/**
 * @author Citymonstret
 */
public class list extends SubCommand {
    public list() {
        super(Command.LIST, "List all plots", "list {mine|shared|all|world|forsale}", CommandCategory.INFO, false);
    }

    private static String getName(final UUID id) {
        if (id == null) {
            return "none";
        }
        final String name = UUIDHandler.getName(id);
        if (name == null) {
            return "unknown";
        }
        return name;
    }
    
    public void noArgs(PlotPlayer plr) {
        final StringBuilder builder = new StringBuilder();
        builder.append(C.SUBCOMMAND_SET_OPTIONS_HEADER.s());
        if (plr != null) {
            if (EconHandler.manager != null) {
                builder.append(getArgumentList(new String[] { "mine", "shared", "world", "all", "unowned", "unknown", "forsale", "<player>", "<world>"}));
            }
            else {
                builder.append(getArgumentList(new String[] { "mine", "shared", "world", "all", "unowned", "unknown", "<player>", "<world>"}));
            }
        } else {
            builder.append(getArgumentList(new String[] { "world", "all", "unowned", "unknown", "<player>", "<world>"}));
        }
        MainUtil.sendMessage(plr, builder.toString());
    }

    @Override
    public boolean execute(final PlotPlayer plr, final String... args) {
        if (args.length < 1) {
            noArgs(plr);
            return false;
        }
        int page = 0;
        if (args.length > 1) {
            try {
                page = Integer.parseInt(args[1]);
                --page;
                if (page < 0) {
                    page = 0;
                }
            } catch (final Exception e) {
                page = 0;
            }
        }
        
        Collection<Plot> plots = null;
        
        String world;
        if (plr != null) {
            world = plr.getLocation().getWorld();
        }
        else {
            Set<String> worlds = PlotSquared.getPlotWorlds();
            if (worlds.size() == 0) {
                world = "world";
            }
            else {
                world = worlds.iterator().next();
            }
        }
        
        String arg = args[0].toLowerCase();
        switch (arg) {
            case "mine": {
                if (plr == null) {
                    break;
                }
                if (!Permissions.hasPermission(plr, "plots.list.mine")) {
                    MainUtil.sendMessage(plr, C.NO_PERMISSION, "plots.list.mine");
                    return false;
                }
                plots = PlotSquared.getPlots(plr);
                break;
            }
            case "shared": {
                if (plr == null) {
                    break;
                }
                if (!Permissions.hasPermission(plr, "plots.list.shared")) {
                    MainUtil.sendMessage(plr, C.NO_PERMISSION, "plots.list.shared");
                    return false;
                }
                plots = new ArrayList<Plot>();
                for (Plot plot : PlotSquared.getPlots()) {
                    if (plot.trusted.contains(plr.getUUID()) || plot.members.contains(plr.getUUID())) {
                        plots.add(plot);
                    }
                }
                break;
            }
            case "world": {
                if (!Permissions.hasPermission(plr, "plots.list.world")) {
                    MainUtil.sendMessage(plr, C.NO_PERMISSION, "plots.list.world");
                    return false;
                }
                if (!Permissions.hasPermission(plr, "plots.list.world." + world)) {
                    MainUtil.sendMessage(plr, C.NO_PERMISSION, "plots.list.world." + world);
                    return false;
                }
                plots = PlotSquared.getPlots(world).values();
                break;
            }
            case "all": {
                if (!Permissions.hasPermission(plr, "plots.list.all")) {
                    MainUtil.sendMessage(plr, C.NO_PERMISSION, "plots.list.all");
                    return false;
                }
                plots = PlotSquared.getPlots();
                break;
            }
            case "forsale": {
                if (!Permissions.hasPermission(plr, "plots.list.forsale")) {
                    MainUtil.sendMessage(plr, C.NO_PERMISSION, "plots.list.forsale");
                    return false;
                }
                if (EconHandler.manager == null) {
                    break;
                }
                plots = new HashSet<>();
                for (Plot plot : PlotSquared.getPlots()) {
                    final Flag price = FlagManager.getPlotFlag(plot, "price");
                    if (price != null) {
                        plots.add(plot);
                    }
                }
                break;
            }
            case "unowned": {
                if (!Permissions.hasPermission(plr, "plots.list.unowned")) {
                    MainUtil.sendMessage(plr, C.NO_PERMISSION, "plots.list.unowned");
                    return false;
                }
                plots = new HashSet<>();
                for (Plot plot : PlotSquared.getPlots()) {
                    if (plot.owner == null) {
                        plots.add(plot);
                    }
                }
                break;
            }
            case "unknown": {
                if (!Permissions.hasPermission(plr, "plots.list.unknown")) {
                    MainUtil.sendMessage(plr, C.NO_PERMISSION, "plots.list.unknown");
                    return false;
                }
                plots = new HashSet<>();
                for (Plot plot : PlotSquared.getPlots()) {
                    if (plot.owner == null) {
                        continue;
                    }
                    if (UUIDHandler.getName(plot.owner) == null) {
                        plots.add(plot);
                    }
                }
                break;
            }
            default: {
                if (PlotSquared.isPlotWorld(args[0])) {
                    if (!Permissions.hasPermission(plr, "plots.list.world")) {
                        MainUtil.sendMessage(plr, C.NO_PERMISSION, "plots.list.world");
                        return false;
                    }
                    if (!Permissions.hasPermission(plr, "plots.list.world." + args[0])) {
                        MainUtil.sendMessage(plr, C.NO_PERMISSION, "plots.list.world." + args[0]);
                        return false;
                    }
                    plots = PlotSquared.getPlots(args[0]).values();
                    break;
                }
                UUID uuid = UUIDHandler.getUUID(args[0]);
                if (uuid != null) {
                    if (!Permissions.hasPermission(plr, "plots.list.player")) {
                        MainUtil.sendMessage(plr, C.NO_PERMISSION, "plots.list.player");
                        return false;
                    }
                    plots = PlotSquared.getPlots(uuid);
                    break;
                }
            }
        }
        
        if (plots == null) {
            sendMessage(plr, C.DID_YOU_MEAN, new StringComparison(args[0], new String[] { "mine", "shared", "world", "all" }).getBestMatch());
            return false;
        }
        
        if (plots.size() == 0) {
            MainUtil.sendMessage(plr, C.FOUND_NO_PLOTS);
            return false;
        }
        
        displayPlots(plr, plots, 12, page, world);
        return true;
    }
    
    public void displayPlots(PlotPlayer player, Collection<Plot> oldPlots, int pageSize, int page, String world) {
        List<Plot> plots;
        if (world != null) {
            plots = PlotSquared.sortPlots(oldPlots, world);
        }
        else {
            plots = PlotSquared.sortPlots(oldPlots);
        }
        if (page < 0) {
            page = 0;
        }
        final int totalPages = (int) Math.ceil(plots.size() / pageSize);
        if (page > totalPages) {
            page = totalPages;
        }
        // Only display 12!
        int max = (page * pageSize) + pageSize;
        if (max > plots.size()) {
            max = plots.size();
        }
        
        plots = plots.subList(page * pageSize, max);
        
        
        
        final StringBuilder string = new StringBuilder();
        string.append(C.PLOT_LIST_HEADER_PAGED.s().replaceAll("%cur", page + 1 + "").replaceAll("%max", totalPages + 1 + "").replaceAll("%word%", "all")).append("\n");
        Plot p;
        // This might work xD
        for (int x = (page * 12); x < max; x++) {
            p = (Plot) plots.toArray()[x];
            string.append(C.PLOT_LIST_ITEM_ORDERED.s().replaceAll("%in", x + 1 + "").replaceAll("%id", p.id.toString()).replaceAll("%world", p.world).replaceAll("%owner", getName(p.owner))).append("\n");
        }
        string.append(C.PLOT_LIST_FOOTER.s().replaceAll("%word%", "There is").replaceAll("%num%", plots.size() + "").replaceAll("%plot%", plots.size() == 1 ? "plot" : "plots"));
        MainUtil.sendMessage(player, string.toString());
    }
    
    private String getArgumentList(final String[] strings) {
        final StringBuilder builder = new StringBuilder();
        String prefix = "";
        for (final String s : strings) {
            builder.append(prefix + MainUtil.colorise('&', s));
            prefix = " | ";
        }
        return builder.toString();
    }
}
