package com.intellectualcrafters.plot.object.comment;

import java.util.HashMap;


public class CommentManager {
    public static HashMap<String, CommentInbox> inboxes = new HashMap<>();
    
    public static void addInbox(CommentInbox inbox) {
        inboxes.put(inbox.toString().toLowerCase(), inbox);
    }
    
    public static void registerDefaultInboxes() {
        addInbox(new InboxReport());
        addInbox(new InboxPublic());
        addInbox(new InboxOwner());
    }
}
