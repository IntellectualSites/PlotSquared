package com.plotsquared.core.persistence.repository.jpa;

import com.google.inject.Inject;
import com.plotsquared.core.persistence.entity.ClusterEntity;
import com.plotsquared.core.persistence.repository.api.ClusterRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.Optional;

public class ClusterRepositoryJpa implements ClusterRepository {

    private static final Logger LOGGER = LogManager.getLogger(ClusterRepositoryJpa.class);

    private final EntityManagerFactory emf;

    @Inject
    public ClusterRepositoryJpa(EntityManagerFactory emf) {
        this.emf = emf;
    }

    @Override
    public Optional<ClusterEntity> findById(long id) {
        EntityManager em = emf.createEntityManager();
        try {
            return Optional.ofNullable(em.find(ClusterEntity.class, id));
        } finally { em.close(); }
    }

    @Override
    public Optional<ClusterEntity> findByWorldAndBounds(String world, int x, int z) {
        EntityManager em = emf.createEntityManager();
        try {
            return em.createNamedQuery("Cluster.findByWorldAndBounds", ClusterEntity.class)
                    .setParameter("world", world)
                    .setParameter("x", x)
                    .setParameter("z", z)
                    .getResultStream().findFirst();
        } finally { em.close(); }
    }

    @Override
    public List<ClusterEntity> findByWorld(String world) {
        EntityManager em = emf.createEntityManager();
        try {
            return em.createQuery("SELECT c FROM ClusterEntity c WHERE c.world = :world", ClusterEntity.class)
                    .setParameter("world", world)
                    .getResultList();
        } finally { em.close(); }
    }

    @Override
    public void save(ClusterEntity cluster) {
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            if (cluster.getId() == null) {
                em.persist(cluster);
            } else {
                em.merge(cluster);
            }
            tx.commit();
        } catch (RuntimeException ex) {
            if (tx.isActive()) tx.rollback();
            LOGGER.error("Failed to save cluster (id={}, world={})", cluster.getId(), cluster.getWorld(), ex);
            throw ex;
        } finally { em.close(); }
    }

    @Override
    public void deleteById(long id) {
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            ClusterEntity e = em.find(ClusterEntity.class, id);
            if (e != null) {
                em.remove(e);
            }
            tx.commit();
        } catch (RuntimeException ex) {
            if (tx.isActive()) tx.rollback();
            LOGGER.error("Failed to delete cluster by id (id={})", id, ex);
            throw ex;
        } finally { em.close(); }
    }
}
