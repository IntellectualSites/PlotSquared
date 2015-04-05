package com.intellectualcrafters.plot.object.comment;

import com.intellectualcrafters.plot.object.Plot;
import com.intellectualcrafters.plot.object.PlotPlayer;
import com.intellectualcrafters.plot.object.RunnableVal;

public class ReportInbox extends CommentInbox {

    @Override
    public boolean canRead(Plot plot, PlotPlayer player) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean canWrite(Plot plot, PlotPlayer player) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean canModify(Plot plot, PlotPlayer player) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean getComments(Plot plot, RunnableVal whenDone) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean addComment(Plot plot, PlotComment comment) {
        // TODO Auto-generated method stub
        return false;
    }
}
