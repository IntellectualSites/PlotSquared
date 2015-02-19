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

import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.bukkit.entity.Player;

import com.intellectualcrafters.plot.BukkitMain;
import com.intellectualcrafters.plot.config.C;
import com.intellectualcrafters.plot.database.DBFunc;
import com.intellectualcrafters.plot.object.Plot;
import com.intellectualcrafters.plot.object.PlotComment;
import com.intellectualcrafters.plot.util.PlayerFunctions;

public class Comment extends SubCommand {

    public Comment() {
        super(Command.COMMENT, "Comment on a plot", "comment", CommandCategory.ACTIONS, true);
    }

    @Override
    public boolean execute(final Player plr, final String... args) {
        if (!PlayerFunctions.isInPlot(plr)) {
            return sendMessage(plr, C.NOT_IN_PLOT);
        }
        final Plot plot = PlayerFunctions.getCurrentPlot(plr);
        if (!plot.hasOwner()) {
            return sendMessage(plr, C.NOT_IN_PLOT);
        }

        final List<String> recipients = Arrays.asList("admin", "owner", "helper", "trusted", "everyone");

        if ((args.length > 1) && recipients.contains(args[0].toLowerCase())) {

            if (BukkitMain.hasPermission(plr, "plots.comment." + args[0].toLowerCase())) {
                final String text = StringUtils.join(Arrays.copyOfRange(args, 1, args.length), " ");
                final PlotComment comment = new PlotComment(text, plr.getName(), recipients.indexOf(args[0].toLowerCase()));
                plot.settings.addComment(comment);
                DBFunc.setComment(plr.getWorld().getName(), plot, comment);
                return sendMessage(plr, C.COMMENT_ADDED);
            } else {
                return sendMessage(plr, C.NO_PERMISSION, "plots.comment." + args[0].toLowerCase());
            }
        }
        return sendMessage(plr, C.COMMENT_SYNTAX);
    }
}
