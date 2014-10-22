package com.intellectualcrafters.plot;

public class PlotComment {
    public final String comment;
    public final int tier;
    public final String senderName;
    
    public PlotComment(String comment, String senderName, int tier) {
        this.comment = comment;
        this.tier = tier;
        this.senderName = senderName;
    }
}
