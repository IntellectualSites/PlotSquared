package com.github.intellectualsites.plotsquared.plot.commands;

import com.github.intellectualsites.plotsquared.commands.CommandDeclaration;
import com.github.intellectualsites.plotsquared.plot.config.C;
import com.github.intellectualsites.plotsquared.plot.object.Plot;
import com.github.intellectualsites.plotsquared.plot.object.PlotPlayer;
import com.github.intellectualsites.plotsquared.plot.object.RunnableVal;
import com.github.intellectualsites.plotsquared.plot.object.comment.CommentInbox;
import com.github.intellectualsites.plotsquared.plot.object.comment.PlotComment;
import com.github.intellectualsites.plotsquared.plot.util.CommentManager;
import com.github.intellectualsites.plotsquared.plot.util.MainUtil;
import com.github.intellectualsites.plotsquared.plot.util.StringMan;

import java.util.List;

@CommandDeclaration(command = "inbox", description = "Review the comments for a plot", usage = "/plot inbox [inbox] [delete <index>|clear|page]", permission = "plots.inbox", category = CommandCategory.CHAT, requiredType = RequiredType.NONE)
public class Inbox extends SubCommand {

    public void displayComments(PlotPlayer player, List<PlotComment> oldComments, int page) {
        if (oldComments == null || oldComments.isEmpty()) {
            MainUtil.sendMessage(player, C.INBOX_EMPTY);
            return;
        }
        PlotComment[] comments = oldComments.toArray(new PlotComment[oldComments.size()]);
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
            .replaceAll(C.COMMENT_LIST_HEADER_PAGED.s(), "%amount%", comments.length, "%cur",
                page + 1, "%max", totalPages + 1, "%word", "all") + '\n');

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
        MainUtil.sendMessage(player, string.toString());
    }

    @Override public boolean onCommand(final PlotPlayer player, String[] args) {
        final Plot plot = player.getCurrentPlot();
        if (plot == null) {
            sendMessage(player, C.NOT_IN_PLOT);
            return false;
        }
        if (!plot.hasOwner()) {
            sendMessage(player, C.PLOT_UNOWNED);
            return false;
        }
        if (args.length == 0) {
            sendMessage(player, C.COMMAND_SYNTAX,
                "/plot inbox [inbox] [delete <index>|clear|page]");
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
                                    sendMessage(player, C.INBOX_ITEM,
                                        color + inbox.toString() + " (" + total + '/' + unread
                                            + ')');
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
            sendMessage(player, C.INVALID_INBOX,
                StringMan.join(CommentManager.inboxes.keySet(), ", "));
            return false;
        }
        player.setMeta("inbox:" + inbox.toString(), System.currentTimeMillis());
        final int page;
        if (args.length > 1) {
            switch (args[1].toLowerCase()) {
                case "delete":
                    if (!inbox.canModify(plot, player)) {
                        sendMessage(player, C.NO_PERM_INBOX_MODIFY);
                        return false;
                    }
                    if (args.length != 3) {
                        sendMessage(player, C.COMMAND_SYNTAX,
                            "/plot inbox " + inbox.toString() + " delete <index>");
                    }
                    final int index;
                    try {
                        index = Integer.parseInt(args[2]);
                        if (index < 1) {
                            sendMessage(player, C.NOT_VALID_INBOX_INDEX, index + "");
                            return false;
                        }
                    } catch (NumberFormatException ignored) {
                        sendMessage(player, C.COMMAND_SYNTAX,
                            "/plot inbox " + inbox.toString() + " delete <index>");
                        return false;
                    }

                    if (!inbox.getComments(plot, new RunnableVal<List<PlotComment>>() {
                        @Override public void run(List<PlotComment> value) {
                            if (index > value.size()) {
                                sendMessage(player, C.NOT_VALID_INBOX_INDEX, index + "");
                                return;
                            }
                            PlotComment comment = value.get(index - 1);
                            inbox.removeComment(plot, comment);
                            plot.removeComment(comment);
                            MainUtil.sendMessage(player, C.COMMENT_REMOVED, comment.comment);
                        }
                    })) {
                        sendMessage(player, C.NOT_IN_PLOT);
                        return false;
                    }
                    return true;
                case "clear":
                    if (!inbox.canModify(plot, player)) {
                        sendMessage(player, C.NO_PERM_INBOX_MODIFY);
                    }
                    inbox.clearInbox(plot);
                    List<PlotComment> comments = plot.getComments(inbox.toString());
                    if (!comments.isEmpty()) {
                        plot.removeComments(comments);
                    }
                    MainUtil.sendMessage(player, C.COMMENT_REMOVED, "*");
                    return true;
                default:
                    try {
                        page = Integer.parseInt(args[1]);
                    } catch (NumberFormatException ignored) {
                        sendMessage(player, C.COMMAND_SYNTAX,
                            "/plot inbox [inbox] [delete <index>|clear|page]");
                        return false;
                    }
            }
        } else {
            page = 1;
        }
        if (!inbox.canRead(plot, player)) {
            sendMessage(player, C.NO_PERM_INBOX);
            return false;
        }
        if (!inbox.getComments(plot, new RunnableVal<List<PlotComment>>() {
            @Override public void run(List<PlotComment> value) {
                displayComments(player, value, page);
            }
        })) {
            sendMessage(player, C.PLOT_UNOWNED);
            return false;
        }
        return true;
    }
}
