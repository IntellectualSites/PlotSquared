package com.plotsquared.core.persistence.repository.api;

import com.plotsquared.core.persistence.entity.PlotCommentEntity;

import java.util.List;

/**
 * Repository for managing plot comments, typically grouped by "inbox" names
 * (e.g., inbox types). Methods support querying by world, by plot hash, and
 * by inbox, as well as adding and removing comments.
 */
public interface PlotCommentRepository {
    /**
     * Returns all comments for a given world and inbox across all plots.
     *
     * @param world the world name
     * @param inbox the inbox identifier/category
     * @return list of comments, never null; may be empty
     */
    List<PlotCommentEntity> findByWorldAndInbox(String world, String inbox);

    /**
     * Returns all comments for a specific plot (identified by hash) in a world and inbox.
     *
     * @param world    the world name
     * @param hashcode the plot hash identifier
     * @param inbox    the inbox identifier/category
     * @return list of comments, never null; may be empty
     */
    List<PlotCommentEntity> findByWorldHashAndInbox(String world, int hashcode, String inbox);

    /**
     * Persists a comment entity (insert).
     *
     * @param entity the comment entity to save
     */
    void save(PlotCommentEntity entity);

    /**
     * Deletes a single comment by its identifying fields.
     *
     * @param world   the world name
     * @param hashcode the plot hash identifier
     * @param inbox   the inbox identifier/category
     * @param sender  the comment sender (UUID or name as stored)
     * @param comment the exact comment text
     */
    void deleteOne(String world, int hashcode, String inbox, String sender, String comment);

    /**
     * Clears all comments in the given world for the specified inbox across all plots.
     *
     * @param world the world name
     * @param inbox the inbox identifier/category
     */
    void clearInbox(String world, String inbox);

    /**
     * Clears all comments for a specific plot (by hash) in the given world and inbox.
     *
     * @param world    the world name
     * @param hashcode the plot hash identifier
     * @param inbox    the inbox identifier/category
     */
    void clearInbox(String world, int hashcode, String inbox);

    /**
     * Deletes all comments associated with a specific plot (by hash) in a world,
     * regardless of inbox.
     *
     * @param world    the world name
     * @param hashcode the plot hash identifier
     */
    void deleteByWorldAndHash(String world, int hashcode);
}
