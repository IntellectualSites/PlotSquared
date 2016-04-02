package com.intellectualcrafters.plot.object.comment;

import com.google.common.base.Optional;
import com.intellectualcrafters.plot.database.DBFunc;
import com.intellectualcrafters.plot.object.Plot;
import com.intellectualcrafters.plot.object.PlotPlayer;
import com.intellectualcrafters.plot.object.RunnableVal;
import com.intellectualcrafters.plot.util.Permissions;
import com.intellectualcrafters.plot.util.TaskManager;

import java.util.ArrayList;
import java.util.List;

public class InboxPublic extends CommentInbox {
    
    @Override
    public boolean canRead(Plot plot, PlotPlayer player) {
        if (Permissions.hasPermission(player, "plots.inbox.read." + toString())) {
            if (plot.isOwner(player.getUUID()) || Permissions.hasPermission(player, "plots.inbox.read." + toString() + ".other")) {
                return true;
            }
        }
        return false;
    }
    
    @Override
    public boolean canWrite(Plot plot, PlotPlayer player) {
        if (plot == null) {
            return Permissions.hasPermission(player, "plots.inbox.write." + toString());
        }
        return Permissions.hasPermission(player, "plots.inbox.write." + toString()) && (plot.isOwner(player.getUUID()) || Permissions
                .hasPermission(player, "plots.inbox.write." + toString() + ".other"));
    }
    
    @Override
    public boolean canModify(Plot plot, PlotPlayer player) {
        if (Permissions.hasPermission(player, "plots.inbox.modify." + toString())) {
            if (plot.isOwner(player.getUUID()) || Permissions.hasPermission(player, "plots.inbox.modify." + toString() + ".other")) {
                return true;
            }
        }
        return false;
    }
    
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
                }
                TaskManager.runTask(whenDone);
            }
        });
        return true;
    }
    
    @Override
    public boolean addComment(Plot plot, PlotComment comment) {
        plot.getSettings().addComment(comment);
        DBFunc.setComment(plot, comment);
        return true;
    }
    
    @Override
    public String toString() {
        return "public";
    }


}
