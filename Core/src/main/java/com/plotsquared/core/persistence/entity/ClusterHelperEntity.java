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
package com.plotsquared.core.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.Table;

@Entity
@Table(name = "cluster_helpers")
@IdClass(ClusterUserId.class)
@NamedQueries({
        @NamedQuery(name = "ClusterHelper.delete", query = "DELETE FROM ClusterHelperEntity e WHERE e.clusterId = :clusterId AND e.userUuid = :uuid"),
        @NamedQuery(name = "ClusterHelper.findUsers", query = "SELECT e.userUuid FROM ClusterHelperEntity e WHERE e.clusterId = :clusterId")
})
public class ClusterHelperEntity {
    @Id
    @Column(name = "cluster_id")
    private Long clusterId;
    @Id
    @Column(name = "user_uuid", length = 40)
    private String userUuid;

    public ClusterHelperEntity() {}

    public Long getClusterId() { return clusterId; }
    public void setClusterId(Long clusterId) { this.clusterId = clusterId; }
    public String getUserUuid() { return userUuid; }
    public void setUserUuid(String userUuid) { this.userUuid = userUuid; }
}
