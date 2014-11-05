package com.intellectualcrafters.plot;

public class PlotComment {
    public final String comment;
    public final int    tier;
    public final String senderName;

    public PlotComment(final String comment, final String senderName, final int tier) {
        this.comment = comment;
        this.tier = tier;
        this.senderName = senderName;
    }
}
