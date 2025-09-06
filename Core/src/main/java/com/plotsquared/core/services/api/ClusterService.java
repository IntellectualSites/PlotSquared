package com.plotsquared.core.services.api;

import com.plotsquared.core.plot.PlotCluster;
import com.plotsquared.core.plot.PlotId;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Set;
import java.util.UUID;

public interface ClusterService {

    void createCluster(PlotCluster cluster);

    void resizeCluster(PlotCluster cluster, PlotId min, PlotId max);

    void removeHelper(PlotCluster cluster, UUID uuid);

    void delete(PlotCluster cluster);

    HashMap<String, Set<PlotCluster>> getClusters();

    void setPosition(PlotCluster cluster, String position);

    void setInvited(PlotCluster cluster, UUID uuid);

    void removeInvited(PlotCluster cluster, UUID uuid);

    void setHelper(PlotCluster cluster, UUID uuid);

    void replaceWorld(String oldWorld, String newWorld, @Nullable  PlotId min, PlotId max);
}
