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
import com.intellectualcrafters.plot.object.PlotMessage;
import com.intellectualcrafters.plot.object.PlotPlayer;
import com.intellectualcrafters.plot.object.RunnableVal3;
import com.intellectualcrafters.plot.util.MainUtil;

import java.util.List;

/**
 * SubCommand class.
 */
public abstract class SubCommand extends com.plotsquared.general.commands.Command<PlotPlayer> {

    /**
     * The category.
     */
    public CommandCategory category;

    /**
     * Send a message.
     *
     * @param plr  Player who will receive the message
     * @param c    Caption
     * @param args Arguments (%s's)
     *
     * @see MainUtil#sendMessage(PlotPlayer, C, String...)
     */
    public boolean sendMessage(PlotPlayer plr, C c, String... args) {
        c.send(plr, args);
        return true;
    }

    public <T> void paginate(PlotPlayer player, List<T> c, int size, int page, RunnableVal3<Integer, T, PlotMessage> add, String baseCommand,
            String header) {
        // Calculate pages & index
        if (page < 0) {
            page = 0;
        }
        int totalPages = (int) Math.ceil(c.size() / size);
        if (page > totalPages) {
            page = totalPages;
        }
        int max = page * size + size;
        if (max > c.size()) {
            max = c.size();
        }
        // Send the header
        header = header.replaceAll("%cur", page + 1 + "").replaceAll("%max", totalPages + 1 + "").replaceAll("%amount%", c.size() + "")
                .replaceAll("%word%", "all");
        MainUtil.sendMessage(player, header);
        // Send the page content
        List<T> subList = c.subList(page * size, max);
        int i = page * size;
        for (T obj : subList) {
            i++;
            PlotMessage msg = new PlotMessage();
            add.run(i, obj, msg);
            msg.send(player);
        }
        // Send the footer
        if (page < totalPages && page > 0) { // Back | Next
            new PlotMessage().text("<-").color("$1").command(baseCommand + " " + page).text(" | ").color("$3").text("->").color("$1")
                    .command(baseCommand + " " + (page + 2))
                    .text(C.CLICKABLE.s()).color("$2").send(player);
            return;
        }
        if (page == 0 && totalPages != 0) { // Next
            new PlotMessage().text("<-").color("$3").text(" | ").color("$3").text("->").color("$1").command(baseCommand + " " + (page + 2))
                    .text(C.CLICKABLE.s()).color("$2").send(player);
            return;
        }
        if (page == totalPages && totalPages != 0) { // Back
            new PlotMessage().text("<-").color("$1").command(baseCommand + " " + page).text(" | ").color("$3").text("->").color("$3")
                    .text(C.CLICKABLE.s()).color("$2").send(player);

        }
    }
}
