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

import com.intellectualcrafters.plot.PS;
import com.intellectualcrafters.plot.PS.SortType;
import com.intellectualcrafters.plot.config.C;
import com.intellectualcrafters.plot.flag.Flag;
import com.intellectualcrafters.plot.flag.FlagManager;
import com.intellectualcrafters.plot.object.Plot;
import com.intellectualcrafters.plot.object.PlotArea;
import com.intellectualcrafters.plot.object.PlotMessage;
import com.intellectualcrafters.plot.object.PlotPlayer;
import com.intellectualcrafters.plot.object.Rating;
import com.intellectualcrafters.plot.object.RunnableVal3;
import com.intellectualcrafters.plot.util.*;
import com.plotsquared.general.commands.CommandDeclaration;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.UUID;

@CommandDeclaration(
command = "list",
aliases = { "l", "find", "search" },
description = "List plots",
permission = "plots.list",
category = CommandCategory.INFO,
usage = "/plot list <forsale|mine|shared|world|top|all|unowned|unknown|player|world|done|fuzzy <search...>> [#]")
public class list extends SubCommand {
    
    private String[] getArgumentList(final PlotPlayer player) {
        final List<String> args = new ArrayList<>();
        if ((EconHandler.manager != null) && Permissions.hasPermission(player, "plots.list.forsale")) {
            args.add("forsale");
        }
        if (Permissions.hasPermission(player, "plots.list.mine")) {
            args.add("mine");
        }
        if (Permissions.hasPermission(player, "plots.list.shared")) {
            args.add("shared");
        }
        if (Permissions.hasPermission(player, "plots.list.world")) {
            args.add("world");
        }
        if (Permissions.hasPermission(player, "plots.list.top")) {
            args.add("top");
        }
        if (Permissions.hasPermission(player, "plots.list.all")) {
            args.add("all");
        }
        if (Permissions.hasPermission(player, "plots.list.unowned")) {
            args.add("unowned");
        }
        if (Permissions.hasPermission(player, "plots.list.unknown")) {
            args.add("unknown");
        }
        if (Permissions.hasPermission(player, "plots.list.player")) {
            args.add("<player>");
        }
        if (Permissions.hasPermission(player, "plots.list.world")) {
            args.add("<world>");
        }
        if (Permissions.hasPermission(player, "plots.list.done")) {
            args.add("done");
        }
        if (Permissions.hasPermission(player, "plots.list.expired")) {
            args.add("expired");
        }
        if (Permissions.hasPermission(player, "plots.list.fuzzy")) {
            args.add("fuzzy <search...>");
        }
        return args.toArray(new String[args.size()]);
    }
    
    public void noArgs(final PlotPlayer plr) {
        MainUtil.sendMessage(plr, C.SUBCOMMAND_SET_OPTIONS_HEADER.s() + getArgumentList(plr));
    }
    
    @Override
    public boolean onCommand(final PlotPlayer plr, final String[] args) {
        if (args.length < 1) {
            noArgs(plr);
            return false;
        }
        int page = 0;
        if (args.length > 1) {
            try {
                page = Integer.parseInt(args[args.length - 1]);
                --page;
                if (page < 0) {
                    page = 0;
                }
            } catch (final Exception e) {
                page = -1;
            }
        }
        
        List<Plot> plots = null;
        
        final String world = plr.getLocation().getWorld();
        final PlotArea area = plr.getApplicablePlotArea();
        final String arg = args[0].toLowerCase();
        boolean sort = true;
        switch (arg) {
            case "mine": {
                if (!Permissions.hasPermission(plr, "plots.list.mine")) {
                    MainUtil.sendMessage(plr, C.NO_PERMISSION, "plots.list.mine");
                    return false;
                }
                sort = false;
                plots = PS.get().sortPlotsByTemp(PS.get().getBasePlots(plr));
                break;
            }
            case "shared": {
                if (!Permissions.hasPermission(plr, "plots.list.shared")) {
                    MainUtil.sendMessage(plr, C.NO_PERMISSION, "plots.list.shared");
                    return false;
                }
                plots = new ArrayList<Plot>();
                for (final Plot plot : PS.get().getPlots()) {
                    if (plot.getTrusted().contains(plr.getUUID()) || plot.getMembers().contains(plr.getUUID())) {
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
                plots = new ArrayList<>(PS.get().getPlots(world));
                break;
            }
            case "expired": {
                if (!Permissions.hasPermission(plr, "plots.list.expired")) {
                    MainUtil.sendMessage(plr, C.NO_PERMISSION, "plots.list.expired");
                    return false;
                }
                plots = ExpireManager.IMP == null ? new ArrayList<Plot>() : new ArrayList<>(ExpireManager.IMP.getPendingExpired());
                break;
            }
            case "area": {
                if (!Permissions.hasPermission(plr, "plots.list.area")) {
                    MainUtil.sendMessage(plr, C.NO_PERMISSION, "plots.list.area");
                    return false;
                }
                if (!Permissions.hasPermission(plr, "plots.list.world." + world)) {
                    MainUtil.sendMessage(plr, C.NO_PERMISSION, "plots.list.world." + world);
                    return false;
                }
                plots = area == null ? new ArrayList<Plot>() : new ArrayList<Plot>(area.getPlots());
                break;
            }
            case "all": {
                if (!Permissions.hasPermission(plr, "plots.list.all")) {
                    MainUtil.sendMessage(plr, C.NO_PERMISSION, "plots.list.all");
                    return false;
                }
                plots = new ArrayList<>(PS.get().getPlots());
                break;
            }
            case "done": {
                if (!Permissions.hasPermission(plr, "plots.list.done")) {
                    MainUtil.sendMessage(plr, C.NO_PERMISSION, "plots.list.done");
                    return false;
                }
                plots = new ArrayList<>();
                for (final Plot plot : PS.get().getPlots()) {
                    final Flag flag = plot.getFlags().get("done");
                    if (flag == null) {
                        continue;
                    }
                    plots.add(plot);
                }
                Collections.sort(plots, new Comparator<Plot>() {
                    @Override
                    public int compare(final Plot a, final Plot b) {
                        final String va = a.getFlags().get("done").getValueString();
                        final String vb = b.getFlags().get("done").getValueString();
                        if (MathMan.isInteger(va)) {
                            if (MathMan.isInteger(vb)) {
                                return Integer.parseInt(vb) - Integer.parseInt(va);
                            }
                            return -1;
                        }
                        return 1;
                    }
                });
                sort = false;
                break;
            }
            case "top": {
                if (!Permissions.hasPermission(plr, "plots.list.top")) {
                    MainUtil.sendMessage(plr, C.NO_PERMISSION, "plots.list.top");
                    return false;
                }
                plots = new ArrayList<>(PS.get().getPlots());
                Collections.sort(plots, new Comparator<Plot>() {
                    @Override
                    public int compare(final Plot p1, final Plot p2) {
                        double v1 = 0;
                        double v2 = 0;
                        final int p1s = p1.getSettings().ratings != null ? p1.getSettings().ratings.size() : 0;
                        final int p2s = p2.getSettings().ratings != null ? p2.getSettings().ratings.size() : 0;
                        if ((p1.getSettings().ratings != null) && (p1s > 0)) {
                            for (final Entry<UUID, Rating> entry : p1.getRatings().entrySet()) {
                                final double av = entry.getValue().getAverageRating();
                                v1 += av * av;
                            }
                            v1 /= p1s;
                            v1 += p1s;
                        }
                        if ((p2.getSettings().ratings != null) && (p2s > 0)) {
                            for (final Entry<UUID, Rating> entry : p2.getRatings().entrySet()) {
                                final double av = entry.getValue().getAverageRating();
                                v2 += av * av;
                            }
                            v2 /= p2s;
                            v2 += p2s;
                        }
                        if ((v2 == v1) && (v2 != 0)) {
                            return p2s - p1s;
                        }
                        return (int) Math.signum(v2 - v1);
                    }
                });
                sort = false;
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
                plots = new ArrayList<>();
                for (final Plot plot : PS.get().getPlots()) {
                    final Flag price = FlagManager.getPlotFlagRaw(plot, "price");
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
                plots = new ArrayList<>();
                for (final Plot plot : PS.get().getPlots()) {
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
                plots = new ArrayList<>();
                for (final Plot plot : PS.get().getPlots()) {
                    if (plot.owner == null) {
                        continue;
                    }
                    if (UUIDHandler.getName(plot.owner) == null) {
                        plots.add(plot);
                    }
                }
                break;
            }
            case "fuzzy": {
                if (!Permissions.hasPermission(plr, "plots.list.fuzzy")) {
                    MainUtil.sendMessage(plr, C.NO_PERMISSION, "plots.list.fuzzy");
                    return false;
                }
                if (args.length < (page == -1 ? 2 : 3)) {
                    C.COMMAND_SYNTAX.send(plr, "/plot list fuzzy <search...> [#]");
                    return false;
                }
                String term = StringMan.join(Arrays.copyOfRange(args, 1, args.length - 2), " ");
                plots = MainUtil.getPlotsBySearch(term);
                sort = false;
                break;
            }
            default: {
                if (PS.get().hasPlotArea(args[0])) {
                    if (!Permissions.hasPermission(plr, "plots.list.world")) {
                        MainUtil.sendMessage(plr, C.NO_PERMISSION, "plots.list.world");
                        return false;
                    }
                    if (!Permissions.hasPermission(plr, "plots.list.world." + args[0])) {
                        MainUtil.sendMessage(plr, C.NO_PERMISSION, "plots.list.world." + args[0]);
                        return false;
                    }
                    plots = new ArrayList<>(PS.get().getPlots(args[0]));
                    break;
                }
                UUID uuid = UUIDHandler.getUUID(args[0], null);
                if (uuid == null) {
                    try {
                        uuid = UUID.fromString(args[0]);
                    } catch (final Exception e) {}
                }
                if (uuid != null) {
                    if (!Permissions.hasPermission(plr, "plots.list.player")) {
                        MainUtil.sendMessage(plr, C.NO_PERMISSION, "plots.list.player");
                        return false;
                    }
                    sort = false;
                    plots = PS.get().sortPlotsByTemp(PS.get().getPlots(uuid));
                    break;
                }
            }
        }
        
        if (plots == null) {
            sendMessage(plr, C.DID_YOU_MEAN, new StringComparison<String>(args[0], new String[] { "mine", "shared", "world", "all" }).getBestMatch());
            return false;
        }

        if (plots.isEmpty()) {
            MainUtil.sendMessage(plr, C.FOUND_NO_PLOTS);
            return false;
        }
        displayPlots(plr, plots, 12, page, area, args, sort);
        return true;
    }
    
    public void displayPlots(final PlotPlayer player, List<Plot> plots, final int pageSize, int page, final PlotArea area, final String[] args, final boolean sort) {
        // Header
        Iterator<Plot> iter = plots.iterator();
        while (iter.hasNext()) {
            if (!iter.next().isBasePlot()) {
                iter.remove();
            }
        }
        if (sort) {
            plots = PS.get().sortPlots(plots, SortType.CREATION_DATE, area);
        }
        this.<Plot> paginate(player, plots, pageSize, page, new RunnableVal3<Integer, Plot, PlotMessage>() {
            @Override
            public void run(Integer i, Plot plot, PlotMessage message) {
                String color;
                if (plot.owner == null) {
                    color = "$3";
                } else if (plot.isOwner(player.getUUID())) {
                    color = "$1";
                } else if (plot.isAdded(player.getUUID())) {
                    color = "$4";
                } else if (plot.isDenied(player.getUUID())) {
                    color = "$2";
                } else {
                    color = "$1";
                }
                final PlotMessage trusted = new PlotMessage().text(C.color(C.PLOT_INFO_TRUSTED.s().replaceAll("%trusted%", MainUtil.getPlayerList(plot.getTrusted())))).color("$1");
                final PlotMessage members = new PlotMessage().text(C.color(C.PLOT_INFO_MEMBERS.s().replaceAll("%members%", MainUtil.getPlayerList(plot.getMembers())))).color("$1");
                String strFlags = StringMan.join(plot.getFlags().values(), ",");
                if (strFlags.isEmpty()) {
                    strFlags = C.NONE.s();
                }
                final PlotMessage flags = new PlotMessage().text(C.color(C.PLOT_INFO_FLAGS.s().replaceAll("%flags%", strFlags))).color("$1");
                message.text("[").color("$3").text(i + "").command("/plot visit " + plot.getArea() + ";" + plot.getId()).tooltip("/plot visit " + plot.getArea() + ";" + plot.getId()).color("$1")
                .text("]")
                .color("$3").text(" " + plot.toString()).tooltip(trusted, members, flags).command("/plot info " + plot.getArea() + ";" + plot.getId()).color(color).text(" - ").color("$2");
                String prefix = "";
                for (final UUID uuid : plot.getOwners()) {
                    final String name = UUIDHandler.getName(uuid);
                    if (name == null) {
                        message = message.text(prefix).color("$4").text("unknown").color("$2").tooltip(uuid.toString()).suggest(uuid.toString());
                    } else {
                        final PlotPlayer pp = UUIDHandler.getPlayer(uuid);
                        if (pp != null) {
                            message = message.text(prefix).color("$4").text(name).color("$1").tooltip(new PlotMessage("Online").color("$4"));
                        } else {
                            message = message.text(prefix).color("$4").text(name).color("$1").tooltip(new PlotMessage("Offline").color("$3"));
                        }
                    }
                    prefix = ", ";
                }
            }
        }, "/plot list " + args[0], C.PLOT_LIST_HEADER_PAGED.s());
    }
    
    private String getArgumentList(final String[] strings) {
        final StringBuilder builder = new StringBuilder();
        String prefix = "";
        for (final String s : strings) {
            builder.append(prefix + s);
            prefix = " | ";
        }
        return builder.toString();
    }
}
