package com.github.intellectualsites.plotsquared.plot.object.comment;

import com.github.intellectualsites.plotsquared.plot.util.block.BlockUtil;

import com.github.intellectualsites.plotsquared.plot.database.DBFunc;
import com.github.intellectualsites.plotsquared.plot.object.Plot;
import com.github.intellectualsites.plotsquared.plot.object.RunnableVal;
import com.github.intellectualsites.plotsquared.plot.util.TaskManager;

import java.util.List;

public class InboxPublic extends CommentInbox {

    @Override
    public boolean getComments(final Plot plot, final RunnableVal<List<PlotComment>> whenDone) {
        List<PlotComment> comments = plot.getComments(toString());
        if (!comments.isEmpty()) {
            whenDone.value = comments;
            TaskManager.runTask(whenDone);
            return true;
        }
        DBFunc.getComments(plot, toString(), new RunnableVal<List<PlotComment>>() {
            @Override public void run(List<PlotComment> value) {
                whenDone.value = value;
                if (value != null) {
                    for (PlotComment comment : value) {
                        plot.addComment(comment);
                    }
                }
                TaskManager.runTask(whenDone);
            }
        });
        return true;
    }

    @Override public boolean addComment(Plot plot, PlotComment comment) {
        plot.addComment(comment);
        DBFunc.setComment(plot, comment);
        return true;
    }

    @Override public String toString() {
        return "public";
    }


}
