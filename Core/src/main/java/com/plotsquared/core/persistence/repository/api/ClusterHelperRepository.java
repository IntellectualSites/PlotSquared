package com.plotsquared.core.persistence.repository.api;

import java.util.List;

public interface ClusterHelperRepository {
    List<String> findUsers(long clusterId);
    void add(long clusterId, String userUuid);
    void remove(long clusterId, String userUuid);
}
