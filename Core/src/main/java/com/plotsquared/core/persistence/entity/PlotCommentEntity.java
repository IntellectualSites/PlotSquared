package com.plotsquared.core.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.Table;

@Entity
@Table(name = "plot_comments")
@IdClass(PlotCommentId.class)
@NamedQueries({
        @NamedQuery(name = "PlotComment.findByWorldAndInbox", query = "SELECT c FROM PlotCommentEntity c WHERE c.world = :world AND c.inbox = :inbox ORDER BY c.timestamp DESC"),
        @NamedQuery(name = "PlotComment.findByWorldHashAndInbox", query = "SELECT c FROM PlotCommentEntity c WHERE c.world = :world AND c.hashcode = :hash AND c.inbox = :inbox ORDER BY c.timestamp DESC"),
        @NamedQuery(name = "PlotComment.deleteOne", query = "DELETE FROM PlotCommentEntity c WHERE c.world = :world AND c.hashcode = :hash AND c.inbox = :inbox AND c.sender = :sender AND c.comment = :comment"),
        @NamedQuery(name = "PlotComment.clearInbox", query = "DELETE FROM PlotCommentEntity c WHERE c.world = :world AND c.inbox = :inbox"),
        @NamedQuery(name = "PlotComment.clearInboxByWorldHash", query = "DELETE FROM PlotCommentEntity c WHERE c.world = :world AND c.hashcode = :hash AND c.inbox = :inbox"),
        @NamedQuery(name = "PlotComment.deleteByWorldAndHash", query = "DELETE FROM PlotCommentEntity c WHERE c.world = :world AND c.hashcode = :hash")
})
public class PlotCommentEntity {
    @Id
    @Column(length = 40)
    private String world;
    @Id @Column(name = "hashcode")
    private Integer hashcode;
    @Id @Column(length = 40)
    private String inbox;

    @Column(length = 40, nullable = false)
    private String comment;
    @Column(name = "timestamp", nullable = false)
    private Integer timestamp;
    @Column(length = 40, nullable = false)
    private String sender;

    public PlotCommentEntity() {}

    public String getWorld() { return world; }
    public void setWorld(String world) { this.world = world; }
    public Integer getHashcode() { return hashcode; }
    public void setHashcode(Integer hashcode) { this.hashcode = hashcode; }
    public String getInbox() { return inbox; }
    public void setInbox(String inbox) { this.inbox = inbox; }
    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }
    public Integer getTimestamp() { return timestamp; }
    public void setTimestamp(Integer timestamp) { this.timestamp = timestamp; }
    public String getSender() { return sender; }
    public void setSender(String sender) { this.sender = sender; }
}
