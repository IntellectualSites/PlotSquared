package com.github.intellectualsites.plotsquared.plot.comment;

import com.github.intellectualsites.plotsquared.plot.config.Captions;
import com.github.intellectualsites.plotsquared.plot.config.Settings;
import com.github.intellectualsites.plotsquared.plot.object.Plot;
import com.github.intellectualsites.plotsquared.player.PlotPlayer;
import com.github.intellectualsites.plotsquared.util.tasks.RunnableVal;
import com.github.intellectualsites.plotsquared.util.tasks.TaskManager;
import com.google.common.annotations.Beta;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Beta
public class CommentManager {

    public static final HashMap<String, CommentInbox> inboxes = new HashMap<>();

    public static void sendTitle(final PlotPlayer player, final Plot plot) {
        if (!Settings.Enabled_Components.COMMENT_NOTIFIER || !plot.isOwner(player.getUUID())) {
            return;
        }
        TaskManager.runTaskLaterAsync(() -> {
            Collection<CommentInbox> boxes = CommentManager.inboxes.values();
            final AtomicInteger count = new AtomicInteger(0);
            final AtomicInteger size = new AtomicInteger(boxes.size());
            for (final CommentInbox inbox : inboxes.values()) {
                inbox.getComments(plot, new RunnableVal<List<PlotComment>>() {
                    @Override public void run(List<PlotComment> value) {
                        int total;
                        if (value != null) {
                            int num = 0;
                            for (PlotComment comment : value) {
                                if (comment.timestamp > getTimestamp(player, inbox.toString())) {
                                    num++;
                                }
                            }
                            total = count.addAndGet(num);
                        } else {
                            total = count.get();
                        }
                        if ((size.decrementAndGet() == 0) && (total > 0)) {
                            player.sendTitle("", Captions.INBOX_NOTIFICATION.getTranslated()
                                .replaceAll("%s", "" + total));
                        }
                    }
                });
            }
        }, 20);
    }

    public static long getTimestamp(PlotPlayer player, String inbox) {
        return player.getMeta("inbox:" + inbox, player.getLastPlayed());
    }

    public static void addInbox(CommentInbox inbox) {
        inboxes.put(inbox.toString().toLowerCase(), inbox);
    }

    public static void registerDefaultInboxes() {
        addInbox(new InboxReport());
        addInbox(new InboxPublic());
        addInbox(new InboxOwner());
    }
}
