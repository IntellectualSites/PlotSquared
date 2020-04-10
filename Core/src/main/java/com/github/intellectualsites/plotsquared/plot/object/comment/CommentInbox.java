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
package com.github.intellectualsites.plotsquared.plot.object.comment;

import com.github.intellectualsites.plotsquared.plot.database.DBFunc;
import com.github.intellectualsites.plotsquared.plot.object.Plot;
import com.github.intellectualsites.plotsquared.plot.object.PlotPlayer;
import com.github.intellectualsites.plotsquared.plot.object.RunnableVal;
import com.github.intellectualsites.plotsquared.plot.util.Permissions;

import java.util.List;

public abstract class CommentInbox {

    @Override public abstract String toString();

    public boolean canRead(Plot plot, PlotPlayer player) {
        if (Permissions.hasPermission(player, "plots.inbox.read." + toString(), true)) {
            return plot.isOwner(player.getUUID()) || Permissions
                .hasPermission(player, "plots.inbox.read." + toString() + ".other", true);
        }
        return false;
    }

    public boolean canWrite(Plot plot, PlotPlayer player) {
        if (plot == null) {
            return Permissions.hasPermission(player, "plots.inbox.write." + toString(), true);
        }
        return Permissions.hasPermission(player, "plots.inbox.write." + toString(), true) && (
            plot.isOwner(player.getUUID()) || Permissions
                .hasPermission(player, "plots.inbox.write." + toString() + ".other", true));
    }

    public boolean canModify(Plot plot, PlotPlayer player) {
        if (Permissions.hasPermission(player, "plots.inbox.modify." + toString(), true)) {
            return plot.isOwner(player.getUUID()) || Permissions
                .hasPermission(player, "plots.inbox.modify." + toString() + ".other", true);
        }
        return false;
    }

    /**
     * <br>
     * The `whenDone` parameter should be executed when it's done fetching the comments.
     * The value should be set to List of comments
     *
     * @param plot
     * @param whenDone
     * @return
     */
    public abstract boolean getComments(Plot plot, RunnableVal<List<PlotComment>> whenDone);

    public abstract boolean addComment(Plot plot, PlotComment comment);

    public void removeComment(Plot plot, PlotComment comment) {
        DBFunc.removeComment(plot, comment);
    }

    public void clearInbox(Plot plot) {
        DBFunc.clearInbox(plot, toString());
    }
}
