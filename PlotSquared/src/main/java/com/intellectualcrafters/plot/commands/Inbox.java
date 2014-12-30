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

import com.intellectualcrafters.plot.PlotMain;
import com.intellectualcrafters.plot.config.C;
import com.intellectualcrafters.plot.database.DBFunc;
import com.intellectualcrafters.plot.object.Plot;
import com.intellectualcrafters.plot.object.PlotComment;
import com.intellectualcrafters.plot.util.PlayerFunctions;
import com.intellectualcrafters.plot.util.UUIDHandler;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class Inbox extends SubCommand {

    public Inbox() {
        super(Command.INBOX, "Review a the comments for a plot", "inbox", CommandCategory.ACTIONS, true);
    }

    @Override
    public boolean execute(final Player plr, final String... args) {
        if (!PlayerFunctions.isInPlot(plr)) {
            PlayerFunctions.sendMessage(plr, C.NOT_IN_PLOT);
            return false;
        }
        final Plot plot = PlayerFunctions.getCurrentPlot(plr);
        if (!plot.hasOwner()) {
            PlayerFunctions.sendMessage(plr, C.NOT_IN_PLOT);
            return false;
        }

        Integer tier;
        final UUID uuid = UUIDHandler.getUUID(plr);
        if (PlotMain.hasPermission(plr, "plots.admin")) {
            tier = 0;
        } else if (plot.owner == uuid) {
            tier = 1;
        } else if (plot.helpers.contains(uuid)) {
            tier = 2;
        } else if (plot.trusted.contains(uuid)) {
            tier = 3;
        } else {
            tier = 4;
        }

        if (args.length > 0) {
            switch (args[0].toLowerCase()) {
                case "admin":
                    if (tier <= 0) {
                        tier = 0;
                    } else {
                        PlayerFunctions.sendMessage(plr, C.NO_PERM_INBOX);
                        return false;
                    }
                    break;
                case "owner":
                    if (tier <= 1) {
                        tier = 1;
                    } else {
                        PlayerFunctions.sendMessage(plr, C.NO_PERM_INBOX);
                        return false;
                    }
                    break;
                case "helper":
                    if (tier <= 2) {
                        tier = 2;
                    } else {
                        PlayerFunctions.sendMessage(plr, C.NO_PERM_INBOX);
                        return false;
                    }
                    break;
                case "trusted":
                    if (tier <= 3) {
                        tier = 3;
                    } else {
                        PlayerFunctions.sendMessage(plr, C.NO_PERM_INBOX);
                        return false;
                    }
                    break;
                case "everyone":
                    if (tier <= 4) {
                        tier = 4;
                    } else {
                        PlayerFunctions.sendMessage(plr, C.NO_PERM_INBOX);
                        return false;
                    }
                    break;
                case "default":
                    PlayerFunctions.sendMessage(plr, C.INVALID_INBOX, Arrays.copyOfRange(new String[]{"admin", "owner", "helper", "trusted", "everyone"}, tier, 4));
                    return false;
            }
        }

        final String world = plr.getWorld().getName();
        final int tier2 = tier;

        Bukkit.getScheduler().runTaskAsynchronously(PlotMain.getMain(), new Runnable() {
            @Override
            public void run() {
                ArrayList<PlotComment> comments = plot.settings.getComments(tier2);
                if (comments == null) {
                    comments = DBFunc.getCommenst(world, plot, tier2);
                    plot.settings.setComments(comments);
                }

                if (args.length == 2) {
                    final String[] split = args[1].toLowerCase().split(":");
                    if (!split[0].equals("clear")) {
                        PlayerFunctions.sendMessage(plr, "&c/plot inbox [tier] [clear][:#]");
                        return;
                    }
                    if (split.length > 1) {
                        try {
                            final int index = Integer.parseInt(split[1]);
                            final PlotComment comment = comments.get(index - 1);
                            DBFunc.removeComment(world, plot, comment);
                            plot.settings.removeComment(comment);
                            PlayerFunctions.sendMessage(plr, C.COMMENT_REMOVED, "1 comment");
                            return;
                        } catch (final Exception e) {
                            PlayerFunctions.sendMessage(plr, "&cInvalid index:\n/plot inbox [tier] [clear][:#]");
                            return;
                        }
                    }
                    for (final PlotComment comment : comments) {
                        DBFunc.removeComment(world, plot, comment);
                    }
                    plot.settings.removeComments(comments);
                    PlayerFunctions.sendMessage(plr, C.COMMENT_REMOVED, "all comments in that category");
                } else {
                    final List<String> recipients = Arrays.asList("A", "O", "H", "T", "E");
                    int count = 1;
                    final StringBuilder message = new StringBuilder();
                    String prefix = "";
                    for (final PlotComment comment : comments) {
                        message.append(prefix).append("[").append(count).append("]&6[&c").append(recipients.get(tier2)).append("&6] &7").append(comment.senderName).append("&f: ").append(comment.comment);
                        prefix = "\n";
                        count++;
                    }
                    if (comments.size() == 0) {
                        message.append("&cNo messages.");
                    }
                    PlayerFunctions.sendMessage(plr, message.toString());
                }
            }
        });
        return true;
    }
}
