package com.plotsquared.commands;

import com.plotsquared.config.Captions;
import com.plotsquared.plot.Plot;
import com.plotsquared.player.PlotPlayer;
import com.plotsquared.plot.comment.CommentInbox;
import com.plotsquared.plot.comment.PlotComment;
import com.plotsquared.plot.comment.CommentManager;
import com.plotsquared.util.MainUtil;
import com.plotsquared.util.StringMan;
import com.plotsquared.util.uuid.UUIDHandler;

import java.util.Arrays;
import java.util.Locale;
import java.util.Map.Entry;

@CommandDeclaration(command = "comment",
    aliases = {"msg"},
    description = "Comment on a plot",
    category = CommandCategory.CHAT,
    requiredType = RequiredType.PLAYER,
    permission = "plots.comment")
public class Comment extends SubCommand {

    @Override public boolean onCommand(PlotPlayer player, String[] args) {
        if (args.length < 2) {
            sendMessage(player, Captions.COMMENT_SYNTAX,
                StringMan.join(CommentManager.inboxes.keySet(), "|"));
            return false;
        }

        // Attempt to extract a plot out of the first argument
        Plot plot = null;
        if (!CommentManager.inboxes.containsKey(args[0].toLowerCase(Locale.ENGLISH))) {
            plot = MainUtil.getPlotFromString(player, args[0], false);
        }

        int index;
        if (plot == null) {
            index = 1;
            plot = player.getLocation().getPlotAbs();
        } else {
            if (args.length < 3) {
                sendMessage(player, Captions.COMMENT_SYNTAX,
                    StringMan.join(CommentManager.inboxes.keySet(), "|"));
                return false;
            }
            index = 2;
        }

        CommentInbox inbox = CommentManager.inboxes.get(args[index - 1].toLowerCase());
        if (inbox == null) {
            sendMessage(player, Captions.COMMENT_SYNTAX,
                StringMan.join(CommentManager.inboxes.keySet(), "|"));
            return false;
        }

        if (!inbox.canWrite(plot, player)) {
            sendMessage(player, Captions.NO_PERM_INBOX, "");
            return false;
        }

        String message = StringMan.join(Arrays.copyOfRange(args, index, args.length), " ");
        PlotComment comment =
            new PlotComment(player.getLocation().getWorld(), plot.getId(), message, player.getName(), inbox.toString(),
                System.currentTimeMillis());
        boolean result = inbox.addComment(plot, comment);
        if (!result) {
            sendMessage(player, Captions.NO_PLOT_INBOX, "");
            sendMessage(player, Captions.COMMENT_SYNTAX,
                StringMan.join(CommentManager.inboxes.keySet(), "|"));
            return false;
        }
        for (Entry<String, PlotPlayer> entry : UUIDHandler.getPlayers().entrySet()) {
            PlotPlayer pp = entry.getValue();
            if (pp.getAttribute("chatspy")) {
                MainUtil.sendMessage(pp, "/plot comment " + StringMan.join(args, " "));
            }
        }
        sendMessage(player, Captions.COMMENT_ADDED);
        return true;
    }
}
