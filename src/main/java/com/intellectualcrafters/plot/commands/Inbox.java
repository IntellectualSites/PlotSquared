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
import java.util.List;

import org.apache.commons.lang.StringUtils;

import com.intellectualcrafters.plot.config.C;
import com.intellectualcrafters.plot.object.Plot;
import com.intellectualcrafters.plot.object.PlotPlayer;
import com.intellectualcrafters.plot.object.RunnableVal;
import com.intellectualcrafters.plot.object.comment.CommentInbox;
import com.intellectualcrafters.plot.object.comment.CommentManager;
import com.intellectualcrafters.plot.object.comment.PlotComment;
import com.intellectualcrafters.plot.util.MainUtil;
import com.intellectualcrafters.plot.util.StringMan;

public class Inbox extends SubCommand {
    public Inbox() {
        super(Command.INBOX, "Review the comments for a plot", "inbox [inbox] [delete <index>|clear|page]", CommandCategory.ACTIONS, true);
    }
    
    public void displayComments(PlotPlayer player, List<PlotComment> oldComments, int page) {
        if (oldComments == null || oldComments.size() == 0) {
            MainUtil.sendMessage(player, C.INBOX_EMPTY);
            return;
        }
        PlotComment[] comments = oldComments.toArray(new PlotComment[oldComments.size()]);
        if (page < 0) {
            page = 0;
        }
        // Get the total pages
        // int totalPages = ((int) Math.ceil(12 *
        final int totalPages = (int) Math.ceil(comments.length / 12);
        if (page > totalPages) {
            page = totalPages;
        }
        // Only display 12 per page
        int max = (page * 12) + 12;
        if (max > comments.length) {
            max = comments.length;
        }
        final StringBuilder string = new StringBuilder();
        string.append(StringMan.replaceAll(C.COMMENT_LIST_HEADER_PAGED.s(), "%amount%", comments.length, "%cur", page + 1, "%max", totalPages + 1, "%word", "all") + "\n");
        PlotComment c;
        // This might work xD
        for (int x = (page * 12); x < max; x++) {
            c = comments[x];
            String color;
            if (player.getName().equals(c.senderName)) {
                color = "&a";
            }
            else {
                color = "&7";
            }
            string.append("&8[&7#" + x + "&8][&7" + c.world + ";" + c.id + "&8][&6" + c.senderName + "&8]" + color + c.comment + "\n");
        }
        MainUtil.sendMessage(player, string.toString());
    }

    @Override
    public boolean execute(final PlotPlayer player, final String... args) {
        final Plot plot = MainUtil.getPlot(player.getLocation());
        if (args.length == 0) {
            sendMessage(player, C.COMMAND_SYNTAX, "/plot inbox [inbox] [delete <index>|clear|page]");
            for (final CommentInbox inbox : CommentManager.inboxes.values()) {
                if (inbox.canRead(plot, player)) {
                    if (!inbox.getComments(plot, new RunnableVal() {
                        @Override
                        public void run() {
                            if (value != null) {
                                int total = 0;
                                int unread = 0;
                                for (PlotComment comment : (ArrayList<PlotComment>) value) {
                                    total++;
                                    if (comment.timestamp > CommentManager.getTimestamp(player, inbox.toString())) {
                                        unread++;
                                    }
                                }
                                if (total != 0) {
                                    String color;
                                    if (unread > 0) {
                                        color = "&c";
                                    }
                                    else {
                                        color = "";
                                    }
                                    sendMessage(player, C.INBOX_ITEM, color + inbox.toString() + " (" + total + "/" + unread + ")");
                                    return;
                                }
                            }
                            sendMessage(player, C.INBOX_ITEM, inbox.toString());
                        }
                    })) {
                        sendMessage(player, C.INBOX_ITEM, inbox.toString());
                    }
                }
            }
            return false;
        }
        final CommentInbox inbox = CommentManager.inboxes.get(args[0].toLowerCase());
        if (inbox == null) {
            sendMessage(player, C.INVALID_INBOX, StringUtils.join(CommentManager.inboxes.keySet(),", "));
            return false;
        }
        player.setMeta("inbox:" + inbox.toString(), System.currentTimeMillis());
        final int page;
        if (args.length > 1) {
            switch (args[1].toLowerCase()) {
                case "delete": {
                    if (!inbox.canModify(plot, player)) {
                        sendMessage(player, C.NO_PERM_INBOX_MODIFY);
                    }
                    if (args.length != 3) {
                        sendMessage(player, C.COMMAND_SYNTAX, "/plot inbox " + inbox.toString() + " delete <index>");
                    }
                    final int index;
                    try {
                        index = Integer.parseInt(args[2]);
                        if (index < 1) {
                            sendMessage(player, C.NOT_VALID_INBOX_INDEX, index + "");
                            return false;
                        }
                    }
                    catch (NumberFormatException e) {
                        sendMessage(player, C.COMMAND_SYNTAX, "/plot inbox " + inbox.toString() + " delete <index>");
                        return false;
                    }
                    
                    if (!inbox.getComments(plot, new RunnableVal() {
                        @Override
                        public void run() {
                            List<PlotComment> comments = (List<PlotComment>) value;
                            if (index > comments.size()) {
                                sendMessage(player, C.NOT_VALID_INBOX_INDEX, index + "");
                            }
                            PlotComment comment = comments.get(index - 1);
                            inbox.removeComment(plot, comment);
                            plot.settings.removeComment(comment);
                            MainUtil.sendMessage(player, C.COMMENT_REMOVED, comment.comment);
                        }
                    })) {
                        sendMessage(player, C.NOT_IN_PLOT);
                        return false;
                    }
                    return true;
                }
                case "clear": {
                    if (!inbox.canModify(plot, player)) {
                        sendMessage(player, C.NO_PERM_INBOX_MODIFY);
                    }
                    inbox.clearInbox(plot);
                    ArrayList<PlotComment> comments = plot.settings.getComments(inbox.toString());
                    if (comments != null) {
                        plot.settings.removeComments(comments);
                    }
                    MainUtil.sendMessage(player, C.COMMENT_REMOVED, "*");
                    return true;
                }
                default: {
                    try {
                        page = Integer.parseInt(args[1]) ;
                    }
                    catch (NumberFormatException e) {
                        sendMessage(player, C.COMMAND_SYNTAX, "/plot inbox [inbox] [delete <index>|clear|page]");
                        return false;
                    };
                }
            }
        }
        else {
            page = 1;
        }
        if (!inbox.canRead(plot, player)) {
            sendMessage(player, C.NO_PERM_INBOX);
            return false;
        }
        if (!inbox.getComments(plot, new RunnableVal() {
            @Override
            public void run() {
                List<PlotComment> comments = (List<PlotComment>) value;
                displayComments(player, comments, page);
            }
        })) {
            if (plot == null) {
                sendMessage(player, C.NOT_IN_PLOT);
            }
            else {
                sendMessage(player, C.PLOT_UNOWNED);
            }
            return false;
        }
        return true;
    }
}
