package com.plotsquared.plot.comment;

import com.plotsquared.database.DBFunc;
import com.plotsquared.plot.Plot;
import com.plotsquared.util.tasks.RunnableVal;
import com.plotsquared.util.tasks.TaskManager;

import java.util.ArrayList;
import java.util.List;

public class InboxOwner extends CommentInbox {

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
                } else {
                    plot.setComments(new ArrayList<>());
                }
                TaskManager.runTask(whenDone);
            }
        });
        return true;
    }

    @Override public boolean addComment(Plot plot, PlotComment comment) {
        if (plot.getOwner() == null) {
            return false;
        }
        plot.addComment(comment);
        DBFunc.setComment(plot, comment);
        return true;
    }

    @Override public String toString() {
        return "owner";
    }

}
