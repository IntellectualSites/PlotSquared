package com.github.intellectualsites.plotsquared.plot.object.comment;

import com.github.intellectualsites.plotsquared.plot.util.block.BlockUtil;

import com.github.intellectualsites.plotsquared.plot.database.DBFunc;
import com.github.intellectualsites.plotsquared.plot.object.Plot;
import com.github.intellectualsites.plotsquared.plot.object.RunnableVal;
import com.github.intellectualsites.plotsquared.plot.util.TaskManager;

import java.util.List;

public class InboxReport extends CommentInbox {

    @Override public boolean getComments(Plot plot, final RunnableVal<List<PlotComment>> whenDone) {
        DBFunc.getComments(null, toString(), new RunnableVal<List<PlotComment>>() {
            @Override public void run(List<PlotComment> value) {
                whenDone.value = value;
                TaskManager.runTask(whenDone);
            }
        });
        return true;
    }

    @Override public boolean addComment(Plot plot, PlotComment comment) {
        if (plot.getOwner() == null) {
            return false;
        }
        DBFunc.setComment(plot, comment);
        return true;
    }

    @Override public String toString() {
        return "report";
    }

}
