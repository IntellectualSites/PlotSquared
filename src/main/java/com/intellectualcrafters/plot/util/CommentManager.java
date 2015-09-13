package com.intellectualcrafters.plot.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;

import com.intellectualcrafters.plot.config.C;
import com.intellectualcrafters.plot.config.Settings;
import com.intellectualcrafters.plot.object.Plot;
import com.intellectualcrafters.plot.object.PlotPlayer;
import com.intellectualcrafters.plot.object.RunnableVal;
import com.intellectualcrafters.plot.object.comment.CommentInbox;
import com.intellectualcrafters.plot.object.comment.InboxOwner;
import com.intellectualcrafters.plot.object.comment.InboxPublic;
import com.intellectualcrafters.plot.object.comment.InboxReport;
import com.intellectualcrafters.plot.object.comment.PlotComment;

public class CommentManager {
    public static HashMap<String, CommentInbox> inboxes = new HashMap<>();
    
    public static void sendTitle(final PlotPlayer player, final Plot plot) {
        if (!Settings.COMMENT_NOTIFICATIONS) {
            return;
        }
        if (!plot.isOwner(player.getUUID())) {
            return;
        }
        TaskManager.runTaskLaterAsync(new Runnable() {
            @Override
            public void run() {
                final Collection<CommentInbox> boxes = CommentManager.inboxes.values();
                final AtomicInteger count = new AtomicInteger(0);
                final AtomicInteger size = new AtomicInteger(boxes.size());
                for (final CommentInbox inbox : inboxes.values()) {
                    inbox.getComments(plot, new RunnableVal() {
                        @Override
                        public void run() {
                            int total;
                            if (value != null) {
                                int num = 0;
                                for (final PlotComment comment : (ArrayList<PlotComment>) value) {
                                    if (comment.timestamp > getTimestamp(player, inbox.toString())) {
                                        num++;
                                    }
                                }
                                total = count.addAndGet(num);
                            } else {
                                total = count.get();
                            }
                            if ((size.decrementAndGet() == 0) && (total > 0)) {
                                AbstractTitle.sendTitle(player, "", C.INBOX_NOTIFICATION.s().replaceAll("%s", "" + total));
                            }
                        }
                    });
                }
            }
        }, 20);
    }
    
    public static long getTimestamp(final PlotPlayer player, final String inbox) {
        final Object meta = player.getMeta("inbox:" + inbox);
        if (meta == null) {
            return player.getPreviousLogin();
        }
        return (Long) meta;
    }
    
    public static void addInbox(final CommentInbox inbox) {
        inboxes.put(inbox.toString().toLowerCase(), inbox);
    }
    
    public static void registerDefaultInboxes() {
        addInbox(new InboxReport());
        addInbox(new InboxPublic());
        addInbox(new InboxOwner());
    }
}
