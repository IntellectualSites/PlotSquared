package com.plotsquared.core.plot.comment;

import com.plotsquared.core.database.DBFunc;
import com.plotsquared.core.plot.Plot;
import com.plotsquared.core.util.task.RunnableVal;
import com.plotsquared.core.util.task.TaskManager;

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
