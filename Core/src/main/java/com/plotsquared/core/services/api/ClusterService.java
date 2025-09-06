/*
 * PlotSquared, a land and world management plugin for Minecraft.
 * Copyright (C) IntellectualSites <https://intellectualsites.com>
 * Copyright (C) IntellectualSites team and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
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
