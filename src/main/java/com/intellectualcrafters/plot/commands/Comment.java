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

import com.intellectualcrafters.plot.config.C;
import com.intellectualcrafters.plot.object.Location;
import com.intellectualcrafters.plot.object.Plot;
import com.intellectualcrafters.plot.object.PlotId;
import com.intellectualcrafters.plot.object.PlotPlayer;
import com.intellectualcrafters.plot.object.comment.CommentInbox;
import com.intellectualcrafters.plot.object.comment.PlotComment;
import com.intellectualcrafters.plot.util.MainUtil;
import com.plotsquared.bukkit.object.comment.CommentManager;
import com.plotsquared.general.commands.CommandDeclaration;
import org.apache.commons.lang.StringUtils;

import java.util.Arrays;

@CommandDeclaration(
        command = "comment",
        aliases = {"msg"},
        description = "Comment on a plot",
        category = CommandCategory.ACTIONS,
        requiredType = RequiredType.PLAYER,
        permission = "plot.comment"
)
public class Comment extends SubCommand {

    @Override
    public boolean onCommand(PlotPlayer player, String[] args) {
        if (args.length < 2) {
            sendMessage(player, C.COMMENT_SYNTAX, StringUtils.join(CommentManager.inboxes.keySet(),"|"));
            return false;
        }
        CommentInbox inbox = CommentManager.inboxes.get(args[0].toLowerCase());
        if (inbox == null) {
            sendMessage(player, C.COMMENT_SYNTAX, StringUtils.join(CommentManager.inboxes.keySet(),"|"));
            return false;
        }
        Plot plot;
        Location loc = player.getLocation();
        PlotId id = PlotId.fromString(args[1]);
        int index;
        if (id != null) {
            if (args.length < 4) {
                sendMessage(player, C.COMMENT_SYNTAX, StringUtils.join(CommentManager.inboxes.keySet(),"|"));
                return false;
            }
            index = 2;
            plot = MainUtil.getPlot(loc.getWorld(), id);
        }
        else {
            index = 1;
            plot = MainUtil.getPlot(loc);
        }
        if (!inbox.canWrite(plot, player)) {
            sendMessage(player, C.NO_PERM_INBOX, "");
            return false;
        }
        String message = StringUtils.join(Arrays.copyOfRange(args,index, args.length), " ");
        PlotComment comment = new PlotComment(loc.getWorld(), id, message, player.getName(), inbox.toString(), System.currentTimeMillis());
        boolean result = inbox.addComment(plot, comment);
        if (!result) {
            sendMessage(player, C.NO_PLOT_INBOX, "");
            sendMessage(player, C.COMMENT_SYNTAX, StringUtils.join(CommentManager.inboxes.keySet(),"|"));
            return false;
        }
        sendMessage(player, C.COMMENT_ADDED);
        return true;
    }
}
