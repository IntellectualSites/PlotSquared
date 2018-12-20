package com.intellectualcrafters.plot.commands;

import com.intellectualcrafters.plot.config.C;
import com.intellectualcrafters.plot.object.Location;
import com.intellectualcrafters.plot.object.Plot;
import com.intellectualcrafters.plot.object.PlotId;
import com.intellectualcrafters.plot.object.PlotPlayer;
import com.intellectualcrafters.plot.object.comment.CommentInbox;
import com.intellectualcrafters.plot.object.comment.PlotComment;
import com.intellectualcrafters.plot.util.CommentManager;
import com.intellectualcrafters.plot.util.MainUtil;
import com.intellectualcrafters.plot.util.StringMan;
import com.intellectualcrafters.plot.util.UUIDHandler;
import com.plotsquared.general.commands.CommandDeclaration;

import java.util.Arrays;
import java.util.Map.Entry;

@CommandDeclaration(command = "comment",
        aliases = {"msg"},
        description = "Comment on a plot",
        category = CommandCategory.CHAT,
        requiredType = RequiredType.NONE,
        permission = "plots.comment")
public class Comment extends SubCommand {

    @Override
    public boolean onCommand(PlotPlayer player, String[] args) {
        if (args.length < 2) {
            sendMessage(player, C.COMMENT_SYNTAX, StringMan.join(CommentManager.inboxes.keySet(), "|"));
            return false;
        }
        CommentInbox inbox = CommentManager.inboxes.get(args[0].toLowerCase());
        if (inbox == null) {
            sendMessage(player, C.COMMENT_SYNTAX, StringMan.join(CommentManager.inboxes.keySet(), "|"));
            return false;
        }
        Location loc = player.getLocation();
        PlotId id = PlotId.fromString(args[1]);
        Plot plot = MainUtil.getPlotFromString(player, args[1], false);
        int index;
        if (plot == null) {
            index = 1;
            plot = loc.getPlotAbs();
        } else {
            if (args.length < 4) {
                sendMessage(player, C.COMMENT_SYNTAX, StringMan.join(CommentManager.inboxes.keySet(), "|"));
                return false;
            }
            index = 2;
        }
        if (!inbox.canWrite(plot, player)) {
            sendMessage(player, C.NO_PERM_INBOX, "");
            return false;
        }
        String message = StringMan.join(Arrays.copyOfRange(args, index, args.length), " ");
        PlotComment comment = new PlotComment(loc.getWorld(), id, message, player.getName(), inbox.toString(), System.currentTimeMillis());
        boolean result = inbox.addComment(plot, comment);
        if (!result) {
            sendMessage(player, C.NO_PLOT_INBOX, "");
            sendMessage(player, C.COMMENT_SYNTAX, StringMan.join(CommentManager.inboxes.keySet(), "|"));
            return false;
        }
        for (Entry<String, PlotPlayer> entry : UUIDHandler.getPlayers().entrySet()) {
            PlotPlayer pp = entry.getValue();
            if (pp.getAttribute("chatspy")) {
                MainUtil.sendMessage(pp, "/plot comment " + StringMan.join(args, " "));
            }
        }
        sendMessage(player, C.COMMENT_ADDED);
        return true;
    }
}
