package com.plotsquared.core.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.Table;

@Entity
@Table(name = "cluster_invited")
@IdClass(ClusterUserId.class)
@NamedQueries({
        @NamedQuery(name = "ClusterInvited.delete", query = "DELETE FROM ClusterInvitedEntity e WHERE e.clusterId = :clusterId AND e.userUuid = :uuid"),
        @NamedQuery(name = "ClusterInvited.findUsers", query = "SELECT e.userUuid FROM ClusterInvitedEntity e WHERE e.clusterId = :clusterId")
})
public class ClusterInvitedEntity {
    @Id
    @Column(name = "cluster_id")
    private Long clusterId;
    @Id
    @Column(name = "user_uuid", length = 40)
    private String userUuid;

    public ClusterInvitedEntity() {}

    public Long getClusterId() { return clusterId; }
    public void setClusterId(Long clusterId) { this.clusterId = clusterId; }
    public String getUserUuid() { return userUuid; }
    public void setUserUuid(String userUuid) { this.userUuid = userUuid; }
}
