/*
 * PlotSquared, a land and world management plugin for Minecraft.
 * Copyright (C) IntellectualSites <https://intellectualsites.com>
 * Copyright (C) IntellectualSites team and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.plotsquared.core.plot.comment;

import com.plotsquared.core.database.DBFunc;
import com.plotsquared.core.plot.Plot;
import com.plotsquared.core.util.task.RunnableVal;
import com.plotsquared.core.util.task.TaskManager;

import java.util.List;

public class InboxPublic extends CommentInbox {

    @Override
    public boolean getComments(final Plot plot, final RunnableVal<List<PlotComment>> whenDone) {
        List<PlotComment> comments = plot.getPlotCommentContainer().getComments(toString());
        if (!comments.isEmpty()) {
            whenDone.value = comments;
            TaskManager.runTask(whenDone);
            return true;
        }
        DBFunc.getComments(plot, toString(), new RunnableVal<>() {
            @Override
            public void run(List<PlotComment> value) {
                whenDone.value = value;
                if (value != null) {
                    for (PlotComment comment : value) {
                        plot.getPlotCommentContainer().addComment(comment);
                    }
                }
                TaskManager.runTask(whenDone);
            }
        });
        return true;
    }

    @Override
    public boolean addComment(Plot plot, PlotComment comment) {
        plot.getPlotCommentContainer().addComment(comment);
        DBFunc.setComment(plot, comment);
        return true;
    }

    @Override
    public String toString() {
        return "public";
    }


}
