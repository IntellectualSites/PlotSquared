package com.intellectualcrafters.plot.object.comment;

import java.util.HashMap;


public class InboxManager {
    public static HashMap<String, CommentInbox> inboxes = new HashMap<>();
    
    public static void addInbox(String name, CommentInbox inbox) {
        inboxes.put(name.toLowerCase(), inbox);
    }
}
