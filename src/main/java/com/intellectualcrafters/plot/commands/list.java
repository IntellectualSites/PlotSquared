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
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map.Entry;
import java.util.UUID;

import com.intellectualcrafters.plot.PS;
import com.intellectualcrafters.plot.PS.SortType;
import com.intellectualcrafters.plot.config.C;
import com.intellectualcrafters.plot.flag.Flag;
import com.intellectualcrafters.plot.flag.FlagManager;
import com.intellectualcrafters.plot.object.Plot;
import com.intellectualcrafters.plot.object.PlotMessage;
import com.intellectualcrafters.plot.object.PlotPlayer;
import com.intellectualcrafters.plot.object.Rating;
import com.intellectualcrafters.plot.util.EconHandler;
import com.intellectualcrafters.plot.util.MainUtil;
import com.intellectualcrafters.plot.util.MathMan;
import com.intellectualcrafters.plot.util.Permissions;
import com.intellectualcrafters.plot.util.StringComparison;
import com.intellectualcrafters.plot.util.StringMan;
import com.intellectualcrafters.plot.util.UUIDHandler;
import com.plotsquared.general.commands.CommandDeclaration;

@CommandDeclaration(
command = "list",
aliases = { "l" },
description = "List plots",
permission = "plots.list",
category = CommandCategory.INFO)
public class list extends SubCommand
{

    private String[] getArgumentList(final PlotPlayer player)
    {
        final List<String> args = new ArrayList<>();
        if ((EconHandler.manager != null) && Permissions.hasPermission(player, "plots.list.forsale"))
        {
            args.add("forsale");
        }
        if (Permissions.hasPermission(player, "plots.list.mine"))
        {
            args.add("mine");
        }
        if (Permissions.hasPermission(player, "plots.list.shared"))
        {
            args.add("shared");
        }
        if (Permissions.hasPermission(player, "plots.list.world"))
        {
            args.add("world");
        }
        if (Permissions.hasPermission(player, "plots.list.top"))
        {
            args.add("top");
        }
        if (Permissions.hasPermission(player, "plots.list.all"))
        {
            args.add("all");
        }
        if (Permissions.hasPermission(player, "plots.list.unowned"))
        {
            args.add("unowned");
        }
        if (Permissions.hasPermission(player, "plots.list.unknown"))
        {
            args.add("unknown");
        }
        if (Permissions.hasPermission(player, "plots.list.player"))
        {
            args.add("<player>");
        }
        if (Permissions.hasPermission(player, "plots.list.world"))
        {
            args.add("<world>");
        }
        if (Permissions.hasPermission(player, "plots.list.done"))
        {
            args.add("done");
        }
        return args.toArray(new String[args.size()]);
    }

    public void noArgs(final PlotPlayer plr)
    {
        MainUtil.sendMessage(plr, C.SUBCOMMAND_SET_OPTIONS_HEADER.s() + getArgumentList(getArgumentList(plr)));
    }

    @Override
    public boolean onCommand(final PlotPlayer plr, final String[] args)
    {
        if (args.length < 1)
        {
            noArgs(plr);
            return false;
        }
        int page = 0;
        if (args.length > 1)
        {
            try
            {
                page = Integer.parseInt(args[1]);
                --page;
                if (page < 0)
                {
                    page = 0;
                }
            }
            catch (final Exception e)
            {
                page = 0;
            }
        }

        List<Plot> plots = null;

        final String world = plr.getLocation().getWorld();
        final String arg = args[0].toLowerCase();
        boolean sort = true;
        switch (arg)
        {
            case "mine":
            {
                if (!Permissions.hasPermission(plr, "plots.list.mine"))
                {
                    MainUtil.sendMessage(plr, C.NO_PERMISSION, "plots.list.mine");
                    return false;
                }
                sort = false;
                plots = PS.get().sortPlotsByTemp(PS.get().getPlots(plr));
                break;
            }
            case "shared":
            {
                if (!Permissions.hasPermission(plr, "plots.list.shared"))
                {
                    MainUtil.sendMessage(plr, C.NO_PERMISSION, "plots.list.shared");
                    return false;
                }
                plots = new ArrayList<Plot>();
                for (final Plot plot : PS.get().getPlots())
                {
                    if (plot.getTrusted().contains(plr.getUUID()) || plot.getMembers().contains(plr.getUUID()))
                    {
                        plots.add(plot);
                    }
                }
                break;
            }
            case "world":
            {
                if (!Permissions.hasPermission(plr, "plots.list.world"))
                {
                    MainUtil.sendMessage(plr, C.NO_PERMISSION, "plots.list.world");
                    return false;
                }
                if (!Permissions.hasPermission(plr, "plots.list.world." + world))
                {
                    MainUtil.sendMessage(plr, C.NO_PERMISSION, "plots.list.world." + world);
                    return false;
                }
                plots = new ArrayList<>(PS.get().getPlotsInWorld(world));
                break;
            }
            case "all":
            {
                if (!Permissions.hasPermission(plr, "plots.list.all"))
                {
                    MainUtil.sendMessage(plr, C.NO_PERMISSION, "plots.list.all");
                    return false;
                }
                plots = new ArrayList<>(PS.get().getPlots());
                break;
            }
            case "done":
            {
                if (!Permissions.hasPermission(plr, "plots.list.done"))
                {
                    MainUtil.sendMessage(plr, C.NO_PERMISSION, "plots.list.done");
                    return false;
                }
                plots = new ArrayList<>();
                String match;
                if (args.length == 2)
                {
                    match = args[1];
                }
                else
                {
                    match = null;
                }
                for (final Plot plot : PS.get().getPlots())
                {
                    final Flag flag = plot.getSettings().flags.get("done");
                    if (flag == null)
                    {
                        continue;
                    }
                    if (match != null)
                    {
                        try
                        {
                            if (flag.getValueString().matches(match))
                            {
                                plots.add(plot);
                            }
                        }
                        catch (final Exception e)
                        {
                            break;
                        }
                    }
                    else
                    {
                        plots.add(plot);
                    }
                }
                Collections.sort(plots, new Comparator<Plot>()
                {
                    @Override
                    public int compare(final Plot a, final Plot b)
                    {
                        final String va = a.getSettings().flags.get("done").getValueString();
                        final String vb = b.getSettings().flags.get("done").getValueString();
                        if (MathMan.isInteger(va))
                        {
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
            case "top":
            {
                if (!Permissions.hasPermission(plr, "plots.list.top"))
                {
                    MainUtil.sendMessage(plr, C.NO_PERMISSION, "plots.list.top");
                    return false;
                }
                plots = new ArrayList<>(PS.get().getPlots());
                Collections.sort(plots, new Comparator<Plot>()
                {
                    @Override
                    public int compare(final Plot p1, final Plot p2)
                    {
                        double v1 = 0;
                        double v2 = 0;
                        final int p1s = p1.getSettings().ratings != null ? p1.getSettings().ratings.size() : 0;
                        final int p2s = p2.getSettings().ratings != null ? p2.getSettings().ratings.size() : 0;
                        if ((p1.getSettings().ratings != null) && (p1s > 0))
                        {
                            for (final Entry<UUID, Rating> entry : p1.getRatings().entrySet())
                            {
                                final double av = entry.getValue().getAverageRating();
                                v1 += av * av;
                            }
                            v1 /= p1s;
                            v1 += p1s;
                        }
                        if ((p2.getSettings().ratings != null) && (p2s > 0))
                        {
                            for (final Entry<UUID, Rating> entry : p2.getRatings().entrySet())
                            {
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
            case "forsale":
            {
                if (!Permissions.hasPermission(plr, "plots.list.forsale"))
                {
                    MainUtil.sendMessage(plr, C.NO_PERMISSION, "plots.list.forsale");
                    return false;
                }
                if (EconHandler.manager == null)
                {
                    break;
                }
                plots = new ArrayList<>();
                for (final Plot plot : PS.get().getPlots())
                {
                    final Flag price = FlagManager.getPlotFlag(plot, "price");
                    if (price != null)
                    {
                        plots.add(plot);
                    }
                }
                break;
            }
            case "unowned":
            {
                if (!Permissions.hasPermission(plr, "plots.list.unowned"))
                {
                    MainUtil.sendMessage(plr, C.NO_PERMISSION, "plots.list.unowned");
                    return false;
                }
                plots = new ArrayList<>();
                for (final Plot plot : PS.get().getPlots())
                {
                    if (plot.owner == null)
                    {
                        plots.add(plot);
                    }
                }
                break;
            }
            case "unknown":
            {
                if (!Permissions.hasPermission(plr, "plots.list.unknown"))
                {
                    MainUtil.sendMessage(plr, C.NO_PERMISSION, "plots.list.unknown");
                    return false;
                }
                plots = new ArrayList<>();
                for (final Plot plot : PS.get().getPlots())
                {
                    if (plot.owner == null)
                    {
                        continue;
                    }
                    if (UUIDHandler.getName(plot.owner) == null)
                    {
                        plots.add(plot);
                    }
                }
                break;
            }
            default:
            {
                if (PS.get().isPlotWorld(args[0]))
                {
                    if (!Permissions.hasPermission(plr, "plots.list.world"))
                    {
                        MainUtil.sendMessage(plr, C.NO_PERMISSION, "plots.list.world");
                        return false;
                    }
                    if (!Permissions.hasPermission(plr, "plots.list.world." + args[0]))
                    {
                        MainUtil.sendMessage(plr, C.NO_PERMISSION, "plots.list.world." + args[0]);
                        return false;
                    }
                    plots = new ArrayList<>(PS.get().getPlotsInWorld(args[0]));
                    break;
                }
                UUID uuid = UUIDHandler.getUUID(args[0], null);
                if (uuid == null)
                {
                    try
                    {
                        uuid = UUID.fromString(args[0]);
                    }
                    catch (final Exception e)
                    {}
                }
                if (uuid != null)
                {
                    if (!Permissions.hasPermission(plr, "plots.list.player"))
                    {
                        MainUtil.sendMessage(plr, C.NO_PERMISSION, "plots.list.player");
                        return false;
                    }
                    sort = false;
                    plots = PS.get().sortPlotsByTemp(PS.get().getPlots(uuid));
                    break;
                }
            }
        }

        if (plots == null)
        {
            sendMessage(plr, C.DID_YOU_MEAN, new StringComparison<String>(args[0], new String[] { "mine", "shared", "world", "all" }).getBestMatch());
            return false;
        }

        if (plots.size() == 0)
        {
            MainUtil.sendMessage(plr, C.FOUND_NO_PLOTS);
            return false;
        }
        displayPlots(plr, plots, 12, page, world, args, sort);
        return true;
    }

    public void displayPlots(final PlotPlayer player, List<Plot> plots, final int pageSize, int page, final String world, final String[] args, final boolean sort)
    {
        if (sort)
        {
            plots = PS.get().sortPlots(plots, SortType.DISTANCE_FROM_ORIGIN, world);
        }
        if (page < 0)
        {
            page = 0;
        }
        final int totalPages = (int) Math.ceil(plots.size() / pageSize);
        if (page > totalPages)
        {
            page = totalPages;
        }
        // Only display pageSize!
        int max = (page * pageSize) + pageSize;
        if (max > plots.size())
        {
            max = plots.size();
        }

        final List<Plot> subList = plots.subList(page * pageSize, max);

        // Header
        final String header = C.PLOT_LIST_HEADER_PAGED.s()
        .replaceAll("%cur", page + 1 + "")
        .replaceAll("%max", totalPages + 1 + "")
        .replaceAll("%amount%", plots.size() + "")
        .replaceAll("%word%", "all");
        MainUtil.sendMessage(player, header);

        int i = page * pageSize;
        for (final Plot plot : subList)
        {
            if (plot.getSettings().isMerged())
            {
                if (!MainUtil.getBottomPlot(plot).equals(plot))
                {
                    continue;
                }
            }
            i++;
            String color;
            if (plot.owner == null)
            {
                color = "$3";
            }
            else if (plot.isOwner(player.getUUID()))
            {
                color = "$1";
            }
            else if (plot.isAdded(player.getUUID()))
            {
                color = "$4";
            }
            else if (plot.isDenied(player.getUUID()))
            {
                color = "$2";
            }
            else
            {
                color = "$1";
            }
            final PlotMessage trusted =
            new PlotMessage()
            .text(C.color(C.PLOT_INFO_TRUSTED.s().replaceAll("%trusted%", Info.getPlayerList(plot.getTrusted()))))
            .color("$1");

            final PlotMessage members =
            new PlotMessage()
            .text(C.color(C.PLOT_INFO_MEMBERS.s().replaceAll("%members%", Info.getPlayerList(plot.getMembers()))))
            .color("$1");

            String strFlags = StringMan.join(plot.getSettings().flags.values(), ",");
            if (strFlags.length() == 0)
            {
                strFlags = C.NONE.s();
            }

            final PlotMessage flags =
            new PlotMessage()
            .text(C.color(C.PLOT_INFO_FLAGS.s().replaceAll("%flags%", strFlags)))
            .color("$1");

            PlotMessage message = new PlotMessage()
            .text("[")
            .color("$3")
            .text(i + "")
            .command("/plot visit " + plot.world + ";" + plot.id)
            .tooltip("/plot visit " + plot.world + ";" + plot.id)
            .color("$1")
            .text("]")
            .color("$3")
            .text(" " + plot.toString())

            .tooltip(trusted, members, flags)
            .command("/plot info " + plot.world + ";" + plot.id)

            .color(color)
            .text(" - ")
            .color("$2");
            String prefix = "";
            for (final UUID uuid : plot.getOwners())
            {
                final String name = UUIDHandler.getName(uuid);
                if (name == null)
                {
                    message = message
                    .text(prefix)
                    .color("$4")
                    .text("unknown")
                    .color("$2")
                    .tooltip(uuid.toString())
                    .suggest(uuid.toString());
                }
                else
                {
                    final PlotPlayer pp = UUIDHandler.getPlayer(uuid);
                    if (pp != null)
                    {
                        message = message
                        .text(prefix)
                        .color("$4")
                        .text(name).color("$1")
                        .tooltip(new PlotMessage("Online").color("$4"));
                    }
                    else
                    {
                        message = message
                        .text(prefix)
                        .color("$4")
                        .text(name).color("$1")
                        .tooltip(new PlotMessage("Offline").color("$3"));
                    }
                }
                prefix = ", ";
            }
            message.send(player);
        }
        if ((page < totalPages) && (page > 0))
        {
            // back | next
            new PlotMessage()
            .text("<-")
            .color("$1")
            .command("/plot list " + args[0] + " " + (page))
            .text(" | ")
            .color("$3")
            .text("->")
            .color("$1")
            .command("/plot list " + args[0] + " " + (page + 2))
            .text(C.CLICKABLE.s())
            .color("$2")
            .send(player);
            return;
        }
        if ((page == 0) && (totalPages != 0))
        {
            // next
            new PlotMessage()
            .text("<-")
            .color("$3")
            .text(" | ")
            .color("$3")
            .text("->")
            .color("$1")
            .command("/plot list " + args[0] + " " + (page + 2))
            .text(C.CLICKABLE.s())
            .color("$2")
            .send(player);
            return;
        }
        if ((page == totalPages) && (totalPages != 0))
        {
            // back
            new PlotMessage()
            .text("<-")
            .color("$1")
            .command("/plot list " + args[0] + " " + (page))
            .text(" | ")
            .color("$3")
            .text("->")
            .color("$3")
            .text(C.CLICKABLE.s())
            .color("$2")
            .send(player);
            return;
        }
    }

    private String getArgumentList(final String[] strings)
    {
        final StringBuilder builder = new StringBuilder();
        String prefix = "";
        for (final String s : strings)
        {
            builder.append(prefix + s);
            prefix = " | ";
        }
        return builder.toString();
    }
}
