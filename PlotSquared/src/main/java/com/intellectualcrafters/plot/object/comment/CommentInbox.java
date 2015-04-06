package com.intellectualcrafters.plot.object.comment;

import com.intellectualcrafters.plot.object.Plot;
import com.intellectualcrafters.plot.object.PlotPlayer;
import com.intellectualcrafters.plot.object.RunnableVal;

public abstract class CommentInbox {
    
    @Override
    public abstract String toString();
    
    public abstract boolean canRead(Plot plot, PlotPlayer player);
    
    public abstract boolean canWrite(Plot plot, PlotPlayer player);
    
    public abstract boolean canModify(Plot plot, PlotPlayer player);
    
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
    public abstract boolean getComments(Plot plot, RunnableVal whenDone);
    
    public abstract boolean addComment(Plot plot, PlotComment comment);
    
    public abstract boolean removeComment(Plot plot, PlotComment comment);
    
    public abstract boolean clearInbox(Plot plot);
}
