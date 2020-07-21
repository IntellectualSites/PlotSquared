/*
 *       _____  _       _    _____                                _
 *      |  __ \| |     | |  / ____|                              | |
 *      | |__) | | ___ | |_| (___   __ _ _   _  __ _ _ __ ___  __| |
 *      |  ___/| |/ _ \| __|\___ \ / _` | | | |/ _` | '__/ _ \/ _` |
 *      | |    | | (_) | |_ ____) | (_| | |_| | (_| | | |  __/ (_| |
 *      |_|    |_|\___/ \__|_____/ \__, |\__,_|\__,_|_|  \___|\__,_|
 *                                    | |
 *                                    |_|
 *            PlotSquared plot management system for Minecraft
 *                  Copyright (C) 2020 IntellectualSites
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.plotsquared.core.command;

import com.plotsquared.core.configuration.Captions;
import com.plotsquared.core.configuration.caption.StaticCaption;
import com.plotsquared.core.configuration.caption.TranslatableCaption;
import com.plotsquared.core.player.PlotPlayer;
import com.plotsquared.core.plot.Plot;
import com.plotsquared.core.plot.comment.CommentInbox;
import com.plotsquared.core.plot.comment.CommentManager;
import com.plotsquared.core.plot.comment.PlotComment;
import com.plotsquared.core.util.StringMan;
import com.plotsquared.core.util.task.RunnableVal;
import net.kyori.adventure.text.minimessage.Template;

import java.util.List;

@CommandDeclaration(command = "inbox",
    description = "Review the comments for a plot",
    usage = "/plot inbox [inbox] [delete <index>|clear|page]",
    permission = "plots.inbox",
    category = CommandCategory.CHAT,
    requiredType = RequiredType.PLAYER)
public class Inbox extends SubCommand {

    public void displayComments(PlotPlayer player, List<PlotComment> oldComments, int page) {
        if (oldComments == null || oldComments.isEmpty()) {
            player.sendMessage(TranslatableCaption.of("comment.inbox_empty"));
            return;
        }
        PlotComment[] comments = oldComments.toArray(new PlotComment[0]);
        if (page < 0) {
            page = 0;
        }
        // Get the total pages
        // int totalPages = ((int) Math.ceil(12 *
        int totalPages = (int) Math.ceil(comments.length / 12);
        if (page > totalPages) {
            page = totalPages;
        }
        // Only display 12 per page
        int max = page * 12 + 12;
        if (max > comments.length) {
            max = comments.length;
        }
        StringBuilder string = new StringBuilder();
        string.append(StringMan
            .replaceAll(Captions.COMMENT_LIST_HEADER_PAGED.getTranslated(), "%amount%",
                comments.length, "%cur", page + 1, "%max", totalPages + 1, "%word", "all") + '\n');

        // This might work xD
        for (int x = page * 12; x < max; x++) {
            PlotComment comment = comments[x];
            String color;
            if (player.getName().equals(comment.senderName)) {
                color = "&a";
            } else {
                color = "&7";
            }
            string.append("&8[&7#").append(x + 1).append("&8][&7").append(comment.world).append(';')
                .append(comment.id).append("&8][&6").append(comment.senderName).append("&8]")
                .append(color).append(comment.comment).append('\n');
        }
        player.sendMessage(StaticCaption.of(string.toString()));
    }

    @Override public boolean onCommand(final PlotPlayer<?> player, String[] args) {
        final Plot plot = player.getCurrentPlot();
        if (plot == null) {
            player.sendMessage(TranslatableCaption.of("errors.not_in_plot"));
            return false;
        }
        if (!plot.hasOwner()) {
            player.sendMessage(TranslatableCaption.of("info.plot_unowned"));
            return false;
        }
        if (args.length == 0) {
            sendUsage(player);
            for (final CommentInbox inbox : CommentManager.inboxes.values()) {
                if (inbox.canRead(plot, player)) {
                    if (!inbox.getComments(plot, new RunnableVal<List<PlotComment>>() {
                        @Override public void run(List<PlotComment> value) {
                            if (value != null) {
                                int total = 0;
                                int unread = 0;
                                for (PlotComment comment : value) {
                                    total++;
                                    if (comment.timestamp > CommentManager
                                        .getTimestamp(player, inbox.toString())) {
                                        unread++;
                                    }
                                }
                                if (total != 0) {
                                    String color;
                                    if (unread > 0) {
                                        color = "&c";
                                    } else {
                                        color = "";
                                    }
                                    sendMessage(player, Captions.INBOX_ITEM,
                                        color + inbox.toString() + " (" + total + '/' + unread
                                            + ')');
                                    return;
                                }
                            }
                            player.sendMessage(
                                    TranslatableCaption.of("comment.inbox_item"),
                                    Template.of("value", inbox.toString())
                            );
                        }
                    })) {
                        player.sendMessage(
                                TranslatableCaption.of("comment.inbox_item"),
                                Template.of("value", inbox.toString())
                        );
                    }
                }
            }
            return false;
        }
        final CommentInbox inbox = CommentManager.inboxes.get(args[0].toLowerCase());
        if (inbox == null) {
            sendMessage(player, Captions.INVALID_INBOX,
                StringMan.join(CommentManager.inboxes.keySet(), ", "));
            return false;
        }
        player.setMeta("inbox:" + inbox.toString(), System.currentTimeMillis());
        final int page;
        if (args.length > 1) {
            switch (args[1].toLowerCase()) {
                case "delete":
                    if (!inbox.canModify(plot, player)) {
                        player.sendMessage(TranslatableCaption.of("comment.no_perm_inbox_modify"));
                        return false;
                    }
                    if (args.length != 3) {
                        player.sendMessage(
                                TranslatableCaption.of("commandconfig.command_syntax"),
                                Template.of("value", "/plot inbox " + inbox.toString() + " delete <index>")
                        );
                    }
                    final int index;
                    try {
                        index = Integer.parseInt(args[2]);
                        if (index < 1) {
                            player.sendMessage(
                                    TranslatableCaption.of("comment.not_valid_inbox_index"),
                                    Template.of("number", index + "")
                            );
                            return false;
                        }
                    } catch (NumberFormatException ignored) {
                        player.sendMessage(
                                TranslatableCaption.of("commandconfig.command_syntax"),
                                Template.of("value", "/plot inbox " + inbox.toString() + " delete <index>")
                        );
                        return false;
                    }

                    if (!inbox.getComments(plot, new RunnableVal<List<PlotComment>>() {
                        @Override public void run(List<PlotComment> value) {
                            if (index > value.size()) {
                                player.sendMessage(
                                        TranslatableCaption.of("comment.not_valid_inbox_index"),
                                        Template.of("number", index + "")
                                );
                                return;
                            }
                            PlotComment comment = value.get(index - 1);
                            inbox.removeComment(plot, comment);
                            boolean success = plot.removeComment(comment);
                            if (success) {
                                player.sendMessage(
                                        TranslatableCaption.of("comment.comment_removed_success"),
                                        Template.of("value", comment.comment)
                                );
                            } else {
                                player.sendMessage(
                                        TranslatableCaption.of("comment.comment_removed_failure"));


                            }
                        }
                    })) {
                        player.sendMessage(TranslatableCaption.of("errors.not_in_plot"));
                        return false;
                    }
                    return true;
                case "clear":
                    if (!inbox.canModify(plot, player)) {
                        player.sendMessage(TranslatableCaption.of("comment.no_perm_inbox_modify"));
                    }
                    inbox.clearInbox(plot);
                    List<PlotComment> comments = plot.getComments(inbox.toString());
                    if (!comments.isEmpty()) {
                        plot.removeComments(comments);
                    }
                    player.sendMessage(
                            TranslatableCaption.of("comment.comment_removed_success"),
                            Template.of("value", "*")
                    );
                    return true;
                default:
                    try {
                        page = Integer.parseInt(args[1]);
                    } catch (NumberFormatException ignored) {
                        sendUsage(player);
                        return false;
                    }
            }
        } else {
            page = 1;
        }
        if (!inbox.canRead(plot, player)) {
            player.sendMessage(TranslatableCaption.of("comment.no_perm_inbox"));
            return false;
        }
        if (!inbox.getComments(plot, new RunnableVal<List<PlotComment>>() {
            @Override public void run(List<PlotComment> value) {
                displayComments(player, value, page);
            }
        })) {
            player.sendMessage(TranslatableCaption.of("info.plot_unowned"));
            return false;
        }
        return true;
    }
}
