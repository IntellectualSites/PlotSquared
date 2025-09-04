package com.plotsquared.core.persistence.repository.api;

import com.plotsquared.core.persistence.entity.PlotCommentEntity;

import java.util.List;

public interface PlotCommentRepository {
    List<PlotCommentEntity> findByWorldAndInbox(String world, String inbox);
    List<PlotCommentEntity> findByWorldHashAndInbox(String world, int hashcode, String inbox);
    void save(PlotCommentEntity entity);
    void deleteOne(String world, int hashcode, String inbox, String sender, String comment);
    void clearInbox(String world, String inbox);
    void clearInbox(String world, int hashcode, String inbox);
    void deleteByWorldAndHash(String world, int hashcode);
}
