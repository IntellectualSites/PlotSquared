package com.intellectualcrafters.plot.object.comment;

import com.google.common.base.Optional;
import com.intellectualcrafters.plot.database.DBFunc;
import com.intellectualcrafters.plot.object.Plot;
import com.intellectualcrafters.plot.object.RunnableVal;
import com.intellectualcrafters.plot.util.TaskManager;

import java.util.ArrayList;
import java.util.List;

public class InboxOwner extends CommentInbox {

    @Override
    public boolean getComments(final Plot plot, final RunnableVal<List<PlotComment>> whenDone) {
        Optional<ArrayList<PlotComment>> comments = plot.getSettings().getComments(toString());
        if (comments.isPresent()) {
            whenDone.value = comments.get();
            TaskManager.runTask(whenDone);
            return true;
        }
        DBFunc.getComments(plot, toString(), new RunnableVal<List<PlotComment>>() {
            @Override
            public void run(List<PlotComment> value) {
                whenDone.value = value;
                if (value != null) {
                    for (PlotComment comment : value) {
                        plot.getSettings().addComment(comment);
                    }
                } else {
                    plot.getSettings().setComments(new ArrayList<PlotComment>());
                }
                TaskManager.runTask(whenDone);
            }
        });
        return true;
    }

    @Override
    public boolean addComment(Plot plot, PlotComment comment) {
        if (plot.owner == null) {
            return false;
        }
        plot.getSettings().addComment(comment);
        DBFunc.setComment(plot, comment);
        return true;
    }

    @Override
    public String toString() {
        return "owner";
    }

}
