package com.plotsquared.core.persistence.repository.api;

import java.util.List;

public interface PlotTrustedRepository {
    List<String> findUsers(long plotId);
    void add(long plotId, String userUuid);
    void remove(long plotId, String userUuid);
}
