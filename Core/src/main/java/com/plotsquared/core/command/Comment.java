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

import com.plotsquared.core.PlotSquared;
import com.plotsquared.core.configuration.Captions;
import com.plotsquared.core.player.PlotPlayer;
import com.plotsquared.core.plot.Plot;
import com.plotsquared.core.plot.comment.CommentInbox;
import com.plotsquared.core.plot.comment.CommentManager;
import com.plotsquared.core.plot.comment.PlotComment;
import com.plotsquared.core.util.MainUtil;
import com.plotsquared.core.util.StringMan;

import java.util.Arrays;
import java.util.Locale;

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
            new PlotComment(player.getLocation().getWorld(), plot.getId(), message,
                player.getName(), inbox.toString(), System.currentTimeMillis());
        boolean result = inbox.addComment(plot, comment);
        if (!result) {
            sendMessage(player, Captions.NO_PLOT_INBOX, "");
            sendMessage(player, Captions.COMMENT_SYNTAX,
                StringMan.join(CommentManager.inboxes.keySet(), "|"));
            return false;
        }

        for (final PlotPlayer pp : PlotSquared.imp().getPlayerManager().getPlayers()) {
            if (pp.getAttribute("chatspy")) {
                MainUtil.sendMessage(pp, "/plot comment " + StringMan.join(args, " "));
            }
        }

        sendMessage(player, Captions.COMMENT_ADDED);
        return true;
    }
}
