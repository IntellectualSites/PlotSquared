package com.plotsquared.core.services.impl;

import com.plotsquared.core.PlotSquared;
import com.plotsquared.core.persistence.entity.ClusterEntity;
import com.plotsquared.core.persistence.repository.api.ClusterHelperRepository;
import com.plotsquared.core.persistence.repository.api.ClusterInvitedRepository;
import com.plotsquared.core.persistence.repository.api.ClusterRepository;
import com.plotsquared.core.persistence.repository.api.ClusterSettingsRepository;
import com.plotsquared.core.persistence.repository.api.PlotRepository;
import com.plotsquared.core.plot.PlotCluster;
import com.plotsquared.core.plot.PlotId;
import com.plotsquared.core.services.api.ClusterService;
import jakarta.inject.Inject;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public class ClusterDefaultService implements ClusterService {

    private final ClusterRepository repository;
    private final ClusterHelperRepository helperRepository;
    private final ClusterInvitedRepository invitedRepo;
    private final ClusterSettingsRepository settingsRepo;
    private final PlotRepository plotRepository;

    @Inject
    public ClusterDefaultService(final ClusterRepository repository, final ClusterHelperRepository helperRepository,
                                 final ClusterInvitedRepository repo, final ClusterSettingsRepository settingsRepo,
                                 final PlotRepository plotRepository
    ) {
        this.repository = repository;
        this.helperRepository = helperRepository;
        this.invitedRepo = repo;
        this.settingsRepo = settingsRepo;
        this.plotRepository = plotRepository;
    }

    @Override
    public void createCluster(final PlotCluster cluster) {
        ClusterEntity e = new ClusterEntity();
        e.setWorld(cluster.area != null ? cluster.area.toString() : null);
        e.setOwner(cluster.owner != null ? cluster.owner.toString() : null);
        if (cluster.getP1() != null) {
            e.setPos1X(cluster.getP1().getX());
            e.setPos1Z(cluster.getP1().getY());
        }
        if (cluster.getP2() != null) {
            e.setPos2X(cluster.getP2().getX());
            e.setPos2Z(cluster.getP2().getY());
        }
        this.repository.save(e);
    }

    @Override
    public void resizeCluster(final PlotCluster cluster, final PlotId min, final PlotId max) {
        String world = cluster.area != null ? cluster.area.toString() : null;
        PlotId center = cluster.getCenterPlotId();
        if (world != null) {
            Optional<ClusterEntity> ent = this.repository.findByWorldAndBounds(world, center.getX(), center.getY());
            if (ent.isPresent()) {
                ClusterEntity e = ent.get();
                e.setPos1X(min.getX());
                e.setPos1Z(min.getY());
                e.setPos2X(max.getX());
                e.setPos2Z(max.getY());
                this.repository.save(e);
            }
        }
    }

    @Override
    public void removeHelper(final PlotCluster cluster, final @NotNull UUID uuid) {
        String world = cluster.area != null ? cluster.area.toString() : null;
        PlotId center = cluster.getCenterPlotId();
        if (world != null) {
            Optional<ClusterEntity> ent = this.repository.findByWorldAndBounds(world, center.getX(), center.getY());
            if (ent.isPresent() && ent.get().getId() != null) {
                this.helperRepository.remove(ent.get().getId(), uuid.toString());
            }
        }
    }

    @Override
    public void delete(final PlotCluster cluster) {
        String world = cluster.area != null ? cluster.area.getWorldName() : null;
        if (world == null) {
            return;
        }
        PlotId center = cluster.getCenterPlotId();
        Optional<ClusterEntity> ce = this.repository.findByWorldAndBounds(world, center.getX(), center.getY());
        ce.ifPresent(entity -> this.repository.deleteById(entity.getId()));
    }

    @Override
    public HashMap<String, Set<PlotCluster>> getClusters() {
        HashMap<String, Set<PlotCluster>> result = new HashMap<>();
        List<ClusterEntity> clusters = this.repository.findAll();
        Map<Long, PlotCluster> built = new HashMap<>();
        for (ClusterEntity ce : clusters) {
            UUID owner = Optional.ofNullable(ce.getOwner()).map(UUID::fromString).orElse(null);
            PlotCluster cluster = new PlotCluster(null, PlotId.of(ce.getPos1X(), ce.getPos1Z()), PlotId.of(ce.getPos2X(), ce.getPos2Z()), owner);
            built.put(ce.getId(), cluster);
            result.computeIfAbsent(ce.getWorld(), k -> new HashSet<>()).add(cluster);
        }
        // Populate helpers and invited
        for (Map.Entry<Long, PlotCluster> e : built.entrySet()) {
            long id = e.getKey();
            PlotCluster cluster = e.getValue();
            for (String u : this.helperRepository.findUsers(id)) {
                cluster.helpers.add(UUID.fromString(u));
            }
            for (String u : this.invitedRepo.findUsers(id)) {
                cluster.invited.add(UUID.fromString(u));
            }
            // Apply settings (alias, merged). Avoid setting temp variable.
            this.settingsRepo.findById(id).ifPresent(se -> {
                if (se.getAlias() != null) {
                    cluster.settings.setAlias(se.getAlias());
                }
                Integer m = se.getMerged();
                if (m != null) {
                    boolean[] merged = new boolean[4];
                    for (int i = 0; i < 4; i++) {
                        merged[3 - i] = (m & 1 << i) != 0;
                    }
                    cluster.settings.setMerged(merged);
                }
            });
        }
        return result;
    }

    @Override
    public void setPosition(final PlotCluster cluster, final String position) {
        String world = cluster.area != null ? cluster.area.getWorldName() : null;
        if (world == null) {
            return;
        }
        PlotId center = cluster.getCenterPlotId();
        Optional<ClusterEntity> ce = this.repository.findByWorldAndBounds(world, center.getX(), center.getY());
        ce.ifPresent(entity -> this.settingsRepo.updatePosition(entity.getId(), position));
    }

    @Override
    public void setInvited(final PlotCluster cluster, final UUID uuid) {
        String world = cluster.area != null ? cluster.area.getWorldName() : null;
        if (world == null) {
            return;
        }
        PlotId center = cluster.getCenterPlotId();
        Optional<ClusterEntity> ce = this.repository.findByWorldAndBounds(world, center.getX(), center.getY());
        ce.ifPresent(entity -> this.invitedRepo.add(entity.getId(), uuid.toString()));
    }

    @Override
    public void removeInvited(final PlotCluster cluster, final UUID uuid) {
        String world = cluster.area != null ? cluster.area.getWorldName() : null;
        if (world == null) {
            return;
        }
        PlotId center = cluster.getCenterPlotId();
        Optional<ClusterEntity> ce = this.repository.findByWorldAndBounds(world, center.getX(), center.getY());
        ce.ifPresent(entity -> this.invitedRepo.remove(entity.getId(), uuid.toString()));
    }

    @Override
    public void setHelper(final PlotCluster cluster, final UUID uuid) {
        String world = cluster.area != null ? cluster.area.getWorldName() : null;
        if (world == null) {
            return;
        }
        PlotId center = cluster.getCenterPlotId();
        Optional<ClusterEntity> ce = this.repository.findByWorldAndBounds(world, center.getX(), center.getY());
        ce.ifPresent(entity -> this.helperRepository.add(entity.getId(), uuid.toString()));
    }

    @Override
    public void replaceWorld(final @NotNull String oldWorld, final @NotNull String newWorld, final PlotId min, final PlotId max) {
        if (min == null) {
            this.plotRepository.replaceWorld(oldWorld, newWorld);
            this.repository.replaceWorld(oldWorld, newWorld);
        } else {
            this.plotRepository.replaceWorldInBounds(oldWorld, newWorld, min, max);
            this.repository.replaceWorldInBounds(oldWorld, newWorld, min, max);
        }
    }

}
