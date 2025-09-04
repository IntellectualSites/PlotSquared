package com.plotsquared.core.persistence.entity;

import java.io.Serializable;
import java.util.Objects;

public class ClusterUserId implements Serializable {
    private Long clusterId;
    private String userUuid;

    public ClusterUserId() {}

    public ClusterUserId(Long clusterId, String userUuid) {
        this.clusterId = clusterId;
        this.userUuid = userUuid;
        }

    public Long getClusterId() { return clusterId; }
    public void setClusterId(Long clusterId) { this.clusterId = clusterId; }
    public String getUserUuid() { return userUuid; }
    public void setUserUuid(String userUuid) { this.userUuid = userUuid; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ClusterUserId that = (ClusterUserId) o;
        return Objects.equals(clusterId, that.clusterId) && Objects.equals(userUuid, that.userUuid);
    }

    @Override
    public int hashCode() {
        return Objects.hash(clusterId, userUuid);
    }
}
