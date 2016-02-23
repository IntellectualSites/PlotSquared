package com.intellectualcrafters.plot.object.comment;

import java.util.List;

import com.intellectualcrafters.plot.object.Plot;
import com.intellectualcrafters.plot.object.PlotPlayer;
import com.intellectualcrafters.plot.object.RunnableVal;

public abstract class CommentInbox {
    
    @Override
    public abstract String toString();
    
    public abstract boolean canRead(final Plot plot, final PlotPlayer player);
    
    public abstract boolean canWrite(final Plot plot, final PlotPlayer player);
    
    public abstract boolean canModify(final Plot plot, final PlotPlayer player);
    
    /**
     * The plot may be null if the user is not standing in a plot. Return false if this is not a plot-less inbox.
     * <br>
     * The `whenDone` parameter should be executed when it's done fetching the comments.
     * The value should be set to List of comments
     *
     * @param plot
     * @param whenDone
     * @return
     */
    public abstract boolean getComments(final Plot plot, final RunnableVal<List<PlotComment>> whenDone);
    
    public abstract boolean addComment(final Plot plot, final PlotComment comment);
    
    public abstract boolean removeComment(final Plot plot, final PlotComment comment);
    
    public abstract boolean clearInbox(final Plot plot);
}
