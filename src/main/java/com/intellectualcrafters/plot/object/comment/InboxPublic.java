package com.intellectualcrafters.plot.object.comment;

import java.util.ArrayList;
import java.util.List;

import com.intellectualcrafters.plot.database.DBFunc;
import com.intellectualcrafters.plot.object.Plot;
import com.intellectualcrafters.plot.object.PlotHandler;
import com.intellectualcrafters.plot.object.PlotPlayer;
import com.intellectualcrafters.plot.object.RunnableVal;
import com.intellectualcrafters.plot.util.Permissions;
import com.intellectualcrafters.plot.util.TaskManager;

public class InboxPublic extends CommentInbox {
    
    @Override
    public boolean canRead(final Plot plot, final PlotPlayer player) {
        if (plot == null) {
            return Permissions.hasPermission(player, "plots.inbox.read." + toString());
        }
        return (Permissions.hasPermission(player, "plots.inbox.read." + toString()) && (PlotHandler.isOwner(plot, player.getUUID()) || Permissions.hasPermission(player, "plots.inbox.read."
        + toString()
        + ".other")));
    }
    
    @Override
    public boolean canWrite(final Plot plot, final PlotPlayer player) {
        if (plot == null) {
            return Permissions.hasPermission(player, "plots.inbox.write." + toString());
        }
        return (Permissions.hasPermission(player, "plots.inbox.write." + toString()) && (PlotHandler.isOwner(plot, player.getUUID()) || Permissions.hasPermission(player, "plots.inbox.write."
        + toString()
        + ".other")));
    }
    
    @Override
    public boolean canModify(final Plot plot, final PlotPlayer player) {
        if (plot == null) {
            return Permissions.hasPermission(player, "plots.inbox.modify." + toString());
        }
        return (Permissions.hasPermission(player, "plots.inbox.modify." + toString()) && (PlotHandler.isOwner(plot, player.getUUID()) || Permissions.hasPermission(player, "plots.inbox.modify."
        + toString()
        + ".other")));
    }
    
    @Override
    public boolean getComments(final Plot plot, final RunnableVal whenDone) {
        if ((plot == null) || (plot.owner == null)) {
            return false;
        }
        final ArrayList<PlotComment> comments = plot.getSettings().getComments(toString());
        if (comments != null) {
            whenDone.value = comments;
            TaskManager.runTask(whenDone);
            return true;
        }
        DBFunc.getComments(plot, toString(), new RunnableVal<List<PlotComment>>() {
            @Override
            public void run(List<PlotComment> value) {
                whenDone.value = value;
                if (value != null) {
                    for (final PlotComment comment : (ArrayList<PlotComment>) value) {
                        plot.getSettings().addComment(comment);
                    }
                }
                TaskManager.runTask(whenDone);
            }
        });
        return true;
    }
    
    @Override
    public boolean addComment(final Plot plot, final PlotComment comment) {
        if ((plot == null) || (plot.owner == null)) {
            return false;
        }
        plot.getSettings().addComment(comment);
        DBFunc.setComment(plot, comment);
        return true;
    }
    
    @Override
    public String toString() {
        return "public";
    }
    
    @Override
    public boolean removeComment(final Plot plot, final PlotComment comment) {
        if ((plot == null) || (plot.owner == null)) {
            return false;
        }
        DBFunc.removeComment(plot, comment);
        return false;
    }
    
    @Override
    public boolean clearInbox(final Plot plot) {
        if ((plot == null) || (plot.owner == null)) {
            return false;
        }
        DBFunc.clearInbox(plot, toString());
        return false;
    }
}
