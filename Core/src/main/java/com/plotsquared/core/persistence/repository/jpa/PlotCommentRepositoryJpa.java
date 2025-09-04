package com.plotsquared.core.persistence.repository.jpa;

import com.google.inject.Inject;
import com.plotsquared.core.persistence.entity.PlotCommentEntity;
import com.plotsquared.core.persistence.repository.api.PlotCommentRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;

public class PlotCommentRepositoryJpa implements PlotCommentRepository {

    private static final Logger LOGGER = LogManager.getLogger(PlotCommentRepositoryJpa.class);

    private final EntityManagerFactory emf;

    @Inject
    public PlotCommentRepositoryJpa(EntityManagerFactory emf) {
        this.emf = emf;
    }

    @Override
    public List<PlotCommentEntity> findByWorldAndInbox(String world, String inbox) {
        EntityManager em = emf.createEntityManager();
        try {
            return em.createNamedQuery("PlotComment.findByWorldAndInbox", PlotCommentEntity.class)
                    .setParameter("world", world)
                    .setParameter("inbox", inbox)
                    .getResultList();
        } finally {
            em.close();
        }
    }

    @Override
    public List<PlotCommentEntity> findByWorldHashAndInbox(String world, int hashcode, String inbox) {
        EntityManager em = emf.createEntityManager();
        try {
            return em.createNamedQuery("PlotComment.findByWorldHashAndInbox", PlotCommentEntity.class)
                    .setParameter("world", world)
                    .setParameter("hash", hashcode)
                    .setParameter("inbox", inbox)
                    .getResultList();
        } finally {
            em.close();
        }
    }

    @Override
    public void save(PlotCommentEntity entity) {
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            em.persist(entity);
            tx.commit();
        } catch (RuntimeException e) {
            if (tx.isActive()) tx.rollback();
            LOGGER.error("Failed to save plot comment (world={}, inbox={}, hashcode={})", entity.getWorld(), entity.getInbox(), entity.getHashcode(), e);
            throw e;
        } finally {
            em.close();
        }
    }

    @Override
    public void deleteOne(String world, int hashcode, String inbox, String sender, String comment) {
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            em.createNamedQuery("PlotComment.deleteOne")
                    .setParameter("world", world)
                    .setParameter("hash", hashcode)
                    .setParameter("inbox", inbox)
                    .setParameter("sender", sender)
                    .setParameter("comment", comment)
                    .executeUpdate();
            tx.commit();
        } catch (RuntimeException e) {
            if (tx.isActive()) tx.rollback();
            LOGGER.error("Failed to delete one plot comment (world={}, inbox={}, hashcode={}, sender={}, comment={})", world, inbox, hashcode, sender, comment, e);
            throw e;
        } finally {
            em.close();
        }
    }

    @Override
    public void clearInbox(String world, String inbox) {
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            em.createNamedQuery("PlotComment.clearInbox")
                    .setParameter("world", world)
                    .setParameter("inbox", inbox)
                    .executeUpdate();
            tx.commit();
        } catch (RuntimeException e) {
            if (tx.isActive()) tx.rollback();
            LOGGER.error("Failed to clear inbox (world={}, inbox={})", world, inbox, e);
            throw e;
        } finally {
            em.close();
        }
    }

    @Override
    public void clearInbox(String world, int hashcode, String inbox) {
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            em.createNamedQuery("PlotComment.clearInboxByWorldHash")
                    .setParameter("world", world)
                    .setParameter("hash", hashcode)
                    .setParameter("inbox", inbox)
                    .executeUpdate();
            tx.commit();
        } catch (RuntimeException e) {
            if (tx.isActive()) tx.rollback();
            LOGGER.error("Failed to clear inbox by world+hash (world={}, hashcode={}, inbox={})", world, hashcode, inbox, e);
            throw e;
        } finally {
            em.close();
        }
    }

    @Override
    public void deleteByWorldAndHash(String world, int hashcode) {
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            em.createNamedQuery("PlotComment.deleteByWorldAndHash")
                    .setParameter("world", world)
                    .setParameter("hash", hashcode)
                    .executeUpdate();
            tx.commit();
        } catch (RuntimeException e) {
            if (tx.isActive()) tx.rollback();
            LOGGER.error("Failed to delete comments by world and hash (world={}, hashcode={})", world, hashcode, e);
            throw e;
        } finally {
            em.close();
        }
    }
}
