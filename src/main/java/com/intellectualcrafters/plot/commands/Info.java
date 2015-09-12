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
import java.util.UUID;
import java.util.regex.Matcher;

import com.intellectualcrafters.plot.config.C;
import com.intellectualcrafters.plot.config.Settings;
import com.intellectualcrafters.plot.database.DBFunc;
import com.intellectualcrafters.plot.flag.Flag;
import com.intellectualcrafters.plot.flag.FlagManager;
import com.intellectualcrafters.plot.object.Location;
import com.intellectualcrafters.plot.object.Plot;
import com.intellectualcrafters.plot.object.PlotId;
import com.intellectualcrafters.plot.object.PlotInventory;
import com.intellectualcrafters.plot.object.PlotItemStack;
import com.intellectualcrafters.plot.object.PlotPlayer;
import com.intellectualcrafters.plot.util.BlockManager;
import com.intellectualcrafters.plot.util.MainUtil;
import com.intellectualcrafters.plot.util.StringMan;
import com.intellectualcrafters.plot.util.TaskManager;
import com.intellectualcrafters.plot.util.UUIDHandler;
import com.plotsquared.general.commands.CommandDeclaration;

@CommandDeclaration(
command = "info",
aliases = { "i" },
description = "Display plot info",
usage = "/plot info <id>",
category = CommandCategory.INFO)
public class Info extends SubCommand
{

    public static String getPlayerList(final Collection<UUID> uuids)
    {
        final ArrayList<UUID> l = new ArrayList<>(uuids);
        if ((l == null) || (l.size() < 1)) { return C.NONE.s(); }
        final String c = C.PLOT_USER_LIST.s();
        final StringBuilder list = new StringBuilder();
        for (int x = 0; x < l.size(); x++)
        {
            if ((x + 1) == l.size())
            {
                list.append(c.replace("%user%", getPlayerName(l.get(x))).replace(",", ""));
            }
            else
            {
                list.append(c.replace("%user%", getPlayerName(l.get(x))));
            }
        }
        return list.toString();
    }

    public static String getPlayerName(final UUID uuid)
    {
        if (uuid == null) { return C.UNKNOWN.s(); }
        if (uuid.equals(DBFunc.everyone) || uuid.toString().equalsIgnoreCase(DBFunc.everyone.toString())) { return "everyone"; }
        final String name = UUIDHandler.getName(uuid);
        if (name == null) { return "unknown"; }
        return name;
    }

    @Override
    public boolean onCommand(final PlotPlayer player, String[] args)
    {
        String arg = null;
        Plot plot;
        if (args.length > 0)
        {
            arg = args[0] + "";
        }
        if (arg != null)
        {
            switch (arg)
            {
                case "trusted":
                case "alias":
                case "inv":
                case "biome":
                case "denied":
                case "flags":
                case "id":
                case "size":
                case "members":
                case "owner":
                case "rating":
                    plot = MainUtil.getPlotFromString(player, null, false);
                    break;
                default:
                    plot = MainUtil.getPlotFromString(player, arg, false);
                    if (args.length == 2)
                    {
                        arg = args[1];
                    }
                    else
                    {
                        arg = null;
                    }
                    break;
            }
        }
        else
        {
            plot = MainUtil.getPlotFromString(player, null, false);
        }
        if ((plot == null) && (arg != null))
        {
            plot = MainUtil.getPlotFromString(player, null, false);
        }
        if (plot == null)
        {
            MainUtil.sendMessage(player, C.NOT_IN_PLOT);
            return false;
        }
        if (arg != null)
        {
            if (args.length == 1)
            {
                args = new String[0];
            }
            else
            {
                args = new String[] { args[1] };
            }
        }
        if ((args.length == 1) && args[0].equalsIgnoreCase("inv"))
        {
            final PlotInventory inv = new PlotInventory(player)
            {
                @Override
                public boolean onClick(final int index)
                {
                    // TODO InfoInventory not implemented yet!!!!!!!!
                    // See plot rating or musicsubcommand on examples
                    return false;
                }
            };
            final UUID uuid = player.getUUID();
            final String name = MainUtil.getName(plot.owner);
            inv.setItem(1, new PlotItemStack(388, (short) 0, 1, "&cPlot Info", new String[] {
                "&cID: &6" + plot.getId().toString(),
                "&cOwner: &6" + name,
                "&cAlias: &6" + plot.getSettings().getAlias(),
                "&cBiome: &6" + plot.getBiome().toString().replaceAll("_", "").toLowerCase(),
                "&cCan Build: &6" + plot.isAdded(uuid),
                "&cIs Denied: &6" + plot.isDenied(uuid) }));
            inv.setItem(1, new PlotItemStack(388, (short) 0, 1, "&cTrusted", new String[] { "&cAmount: &6" + plot.getTrusted().size(), "&8Click to view a list of the trusted users" }));
            inv.setItem(1, new PlotItemStack(388, (short) 0, 1, "&cMembers", new String[] { "&cAmount: &6" + plot.getMembers().size(), "&8Click to view a list of plot members" }));
            inv.setItem(1, new PlotItemStack(388, (short) 0, 1, "&cDenied", new String[] { "&cDenied", "&cAmount: &6" + plot.getDenied().size(), "&8Click to view a list of denied players" }));
            inv.setItem(1, new PlotItemStack(388, (short) 0, 1, "&cFlags", new String[] { "&cFlags", "&cAmount: &6" + plot.getSettings().flags.size(), "&8Click to view a list of plot flags" }));
            inv.openInventory();
            return true;
        }
        final boolean hasOwner = plot.hasOwner();
        boolean containsEveryone;
        boolean trustedEveryone;
        // Wildcard player {added}
        {
            containsEveryone = (plot.getTrusted() != null) && plot.getTrusted().contains(DBFunc.everyone);
            trustedEveryone = (plot.getMembers() != null) && plot.getMembers().contains(DBFunc.everyone);
        }
        // Unclaimed?
        if (!hasOwner && !containsEveryone && !trustedEveryone)
        {
            MainUtil.sendMessage(player, C.PLOT_INFO_UNCLAIMED, (plot.id.x + ";" + plot.id.y));
            return true;
        }
        String info = C.PLOT_INFO.s();
        if (arg != null)
        {
            info = getCaption(arg);
            if (info == null)
            {
                MainUtil.sendMessage(player, "&6Categories&7: &amembers&7, &aalias&7, &abiome&7, &adenied&7, &aflags&7, &aid&7, &asize&7, &atrusted&7, &aowner&7, &arating");
                return false;
            }
            formatAndSend(info, plot.world, plot, player, true);
        }
        else
        {
            formatAndSend(info, plot.world, plot, player, false);
        }
        return true;
    }

    private String getCaption(final String string)
    {
        switch (string)
        {
            case "trusted":
                return C.PLOT_INFO_TRUSTED.s();
            case "alias":
                return C.PLOT_INFO_ALIAS.s();
            case "biome":
                return C.PLOT_INFO_BIOME.s();
            case "denied":
                return C.PLOT_INFO_DENIED.s();
            case "flags":
                return C.PLOT_INFO_FLAGS.s();
            case "id":
                return C.PLOT_INFO_ID.s();
            case "size":
                return C.PLOT_INFO_SIZE.s();
            case "members":
                return C.PLOT_INFO_MEMBERS.s();
            case "owner":
                return C.PLOT_INFO_OWNER.s();
            case "rating":
                return C.PLOT_INFO_RATING.s();
            default:
                return null;
        }
    }

    private void formatAndSend(String info, final String world, final Plot plot, final PlotPlayer player, final boolean full)
    {
        final PlotId id = plot.id;
        final PlotId id2 = MainUtil.getTopPlot(plot).id;
        final int num = MainUtil.getPlotSelectionIds(id, id2).size();
        final String alias = plot.getSettings().getAlias().length() > 0 ? plot.getSettings().getAlias() : C.NONE.s();
        final Location top = MainUtil.getPlotTopLoc(world, plot.id);
        final Location bot = MainUtil.getPlotBottomLoc(world, plot.id).add(1, 0, 1);
        final String biome = BlockManager.manager.getBiome(plot.world, bot.getX() + ((top.getX() - bot.getX()) / 2), bot.getZ() + ((top.getZ() - bot.getZ()) / 2));
        final String trusted = getPlayerList(plot.getTrusted());
        final String members = getPlayerList(plot.getMembers());
        final String denied = getPlayerList(plot.getDenied());

        final Flag descriptionFlag = FlagManager.getPlotFlag(plot, "description");
        final String description = descriptionFlag == null ? C.NONE.s() : descriptionFlag.getValueString();

        final String flags = StringMan.replaceFromMap(
        "$2"
        + (StringMan.join(FlagManager.getPlotFlags(plot.world, plot.getSettings(), true).values(), "").length() > 0 ? StringMan.join(FlagManager.getPlotFlags(plot.world, plot.getSettings(), true)
        .values(), "$1, $2") : C.NONE.s()), C.replacements);
        final boolean build = plot.isAdded(player.getUUID());

        final String owner = plot.owner == null ? "unowned" : getPlayerList(plot.getOwners());

        info = info.replaceAll("%alias%", alias);
        info = info.replaceAll("%id%", id.toString());
        info = info.replaceAll("%id2%", id2.toString());
        info = info.replaceAll("%num%", num + "");
        info = info.replaceAll("%desc%", description);
        info = info.replaceAll("%biome%", biome);
        info = info.replaceAll("%owner%", owner);
        info = info.replaceAll("%members%", members);
        info = info.replaceAll("%trusted%", trusted);
        info = info.replaceAll("%helpers%", members);
        info = info.replaceAll("%denied%", denied);
        info = info.replaceAll("%flags%", Matcher.quoteReplacement(flags));
        info = info.replaceAll("%build%", build + "");
        info = info.replaceAll("%desc%", "No description set.");
        if (info.contains("%rating%"))
        {
            final String newInfo = info;
            TaskManager.runTaskAsync(new Runnable()
            {
                @Override
                public void run()
                {
                    int max = 10;
                    if ((Settings.RATING_CATEGORIES != null) && (Settings.RATING_CATEGORIES.size() > 0))
                    {
                        max = 8;
                    }
                    String info;
                    if (full && (Settings.RATING_CATEGORIES != null) && (Settings.RATING_CATEGORIES.size() > 1))
                    {
                        String rating = "";
                        String prefix = "";
                        final double[] ratings = MainUtil.getAverageRatings(plot);
                        for (int i = 0; i < ratings.length; i++)
                        {
                            rating += prefix + Settings.RATING_CATEGORIES.get(i) + "=" + String.format("%.1f", ratings[i]);
                            prefix = ",";
                        }
                        info = newInfo.replaceAll("%rating%", rating);
                    }
                    else
                    {
                        info = newInfo.replaceAll("%rating%", String.format("%.1f", MainUtil.getAverageRating(plot)) + "/" + max);
                    }
                    MainUtil.sendMessage(player, C.PLOT_INFO_HEADER);
                    MainUtil.sendMessage(player, info, false);
                    MainUtil.sendMessage(player, C.PLOT_INFO_FOOTER);
                }
            });
            return;
        }
        MainUtil.sendMessage(player, C.PLOT_INFO_HEADER);
        MainUtil.sendMessage(player, info, false);
    }
}
