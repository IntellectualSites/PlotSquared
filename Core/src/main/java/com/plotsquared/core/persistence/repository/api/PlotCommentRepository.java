package com.plotsquared.core.persistence.repository.api;

import com.plotsquared.core.persistence.entity.PlotCommentEntity;

import java.util.List;

public interface PlotCommentRepository {
    List<PlotCommentEntity> findByWorldAndInbox(String world, String inbox);
    void save(PlotCommentEntity entity);
    void deleteOne(String world, int hashcode, String inbox, String sender);
    void clearInbox(String world, String inbox);
}
