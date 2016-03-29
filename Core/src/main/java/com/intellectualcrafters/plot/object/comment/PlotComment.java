package com.intellectualcrafters.plot.object.comment;

import com.intellectualcrafters.plot.object.PlotId;

public class PlotComment {
    public final String comment;
    public final String inbox;
    public final String senderName;
    public final PlotId id;
    public final String world;
    public final long timestamp;

    public PlotComment(String world, PlotId id, String comment, String senderName, String inbox, long timestamp) {
        this.world = world;
        this.id = id;
        this.comment = comment;
        this.senderName = senderName;
        this.inbox = inbox;
        this.timestamp = timestamp;
    }
}
