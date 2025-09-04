package com.plotsquared.core.persistence.entity;

import java.io.Serializable;
import java.util.Objects;

public class PlotCommentId implements Serializable {
    private String world;
    private Integer hashcode;
    private String inbox;

    public PlotCommentId() {}

    public PlotCommentId(String world, Integer hashcode, String inbox) {
        this.world = world;
        this.hashcode = hashcode;
        this.inbox = inbox;
    }

    public String getWorld() { return world; }
    public void setWorld(String world) { this.world = world; }
    public Integer getHashcode() { return hashcode; }
    public void setHashcode(Integer hashcode) { this.hashcode = hashcode; }
    public String getInbox() { return inbox; }
    public void setInbox(String inbox) { this.inbox = inbox; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PlotCommentId that = (PlotCommentId) o;
        return Objects.equals(world, that.world) && Objects.equals(hashcode, that.hashcode) && Objects.equals(inbox, that.inbox);
    }

    @Override
    public int hashCode() {
        return Objects.hash(world, hashcode, inbox);
    }
}
