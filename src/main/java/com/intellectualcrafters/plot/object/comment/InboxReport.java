package com.intellectualcrafters.plot.object.comment;

import com.intellectualcrafters.plot.database.DBFunc;
import com.intellectualcrafters.plot.object.Plot;
import com.intellectualcrafters.plot.object.PlotHandler;
import com.intellectualcrafters.plot.object.PlotPlayer;
import com.intellectualcrafters.plot.object.RunnableVal;
import com.intellectualcrafters.plot.util.Permissions;
import com.intellectualcrafters.plot.util.TaskManager;

public class InboxReport extends CommentInbox {

    @Override
    public boolean canRead(Plot plot, PlotPlayer player) {
        if (plot == null) {
            return Permissions.hasPermission(player, "plots.inbox.read." + toString());
        }
        return (Permissions.hasPermission(player, "plots.inbox.read." + toString()) && (PlotHandler.isOwner(plot, player.getUUID()) || Permissions.hasPermission(player, "plots.inbox.read." + toString() + ".other")));
    }

    @Override
    public boolean canWrite(Plot plot, PlotPlayer player) {
        if (plot == null) {
            return Permissions.hasPermission(player, "plots.inbox.write." + toString());
        }
        return (Permissions.hasPermission(player, "plots.inbox.write." + toString()) && (PlotHandler.isOwner(plot, player.getUUID()) || Permissions.hasPermission(player, "plots.inbox.write." + toString() + ".other")));
    }

    @Override
    public boolean canModify(Plot plot, PlotPlayer player) {
        if (plot == null) {
            return Permissions.hasPermission(player, "plots.inbox.modify." + toString());
        }
        return (Permissions.hasPermission(player, "plots.inbox.modify." + toString()) && (PlotHandler.isOwner(plot, player.getUUID()) || Permissions.hasPermission(player, "plots.inbox.modify." + toString() + ".other")));
    }

    @Override
    public boolean getComments(final Plot plot, final RunnableVal whenDone) {
        DBFunc.getComments(null, toString(), new RunnableVal() {
            @Override
            public void run() {
                whenDone.value = value;
                TaskManager.runTask(whenDone);
            }
        });
        return true;
    }

    @Override
    public boolean addComment(Plot plot, PlotComment comment) {
        if (plot == null || plot.owner == null) {
            return false;
        }
        DBFunc.setComment(plot, comment);
        return true;
    }

    @Override
    public String toString() {
        return "report";
    }

    @Override
    public boolean removeComment(Plot plot, PlotComment comment) {
        if (plot == null || plot.owner == null) {
            return false;
        }
        DBFunc.removeComment(plot, comment);
        return false;
    }

    @Override
    public boolean clearInbox(Plot plot) {
        if (plot == null || plot.owner == null) {
            return false;
        }
        DBFunc.clearInbox(plot, toString());
        return false;
    }
}
