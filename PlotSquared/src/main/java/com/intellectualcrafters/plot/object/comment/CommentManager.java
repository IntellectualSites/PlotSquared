package com.intellectualcrafters.plot.object.comment;

import java.util.HashMap;

import com.intellectualcrafters.plot.object.PlotPlayer;


public class CommentManager {
    public static HashMap<String, CommentInbox> inboxes = new HashMap<>();
    
    private static HashMap<String, Long> timestamps = new HashMap<>();
    
    public static void runTask() {
//        TaskManager.runTaskRepeat(new Runnable() {
//            
//            @Override
//            public void run() {
//                
//            }
//        }, Settings.COMMENT_NOTIFICATION_INTERVAL * 1200);
    }
    
    public static long getTimestamp(PlotPlayer player) {
        Long time = timestamps.get(player.getName());
        if (time == null) {
            time = player.getPreviousLogin();
            timestamps.put(player.getName(), time);
        }
        return time;
    }
    
    public static void setTime(PlotPlayer player) {
        timestamps.put(player.getName(), System.currentTimeMillis());
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