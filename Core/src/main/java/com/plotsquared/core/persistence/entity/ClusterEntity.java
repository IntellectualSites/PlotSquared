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
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.Table;

import java.sql.Timestamp;

@Entity
@Table(name = "cluster")
@NamedQueries({
        @NamedQuery(name = "Cluster.findByWorldAndBounds", query = "SELECT c FROM ClusterEntity c WHERE c.world = :world AND c" +
                ".pos1X <= :x AND c.pos2X >= :x AND c.pos1Z <= :z AND c.pos2Z >= :z"),
        @NamedQuery(name = "Cluster.findByWorld", query = "SELECT c FROM ClusterEntity c WHERE c.world = :world"),
        @NamedQuery(name = "Cluster.finaAll", query = "SELECT c FROM ClusterEntity c"),
        @NamedQuery(name = "Cluster.updateWorld", query = "UPDATE ClusterEntity c SET c.world = :newWorld WHERE c.world = :oldWorld"),
        @NamedQuery(name = "Cluster.updateWorldInBounds", query = "UPDATE ClusterEntity c SET c.world = :newWorld WHERE c.world = :oldWorld AND c.pos1X <= :maxX AND c.pos1Z <= :maxZ AND c.pos2X >= :minX AND c.pos2Z >= :minZ")
})
public class ClusterEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "pos1_x") private int pos1X;
    @Column(name = "pos1_z") private int pos1Z;
    @Column(name = "pos2_x") private int pos2X;
    @Column(name = "pos2_z") private int pos2Z;
    @Column(length = 40) private String owner;
    @Column(length = 45) private String world;
    @Column(name = "timestamp", insertable = false, updatable = false)
    private Timestamp timestamp;

    public ClusterEntity() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public int getPos1X() { return pos1X; }
    public void setPos1X(int pos1X) { this.pos1X = pos1X; }
    public int getPos1Z() { return pos1Z; }
    public void setPos1Z(int pos1Z) { this.pos1Z = pos1Z; }
    public int getPos2X() { return pos2X; }
    public void setPos2X(int pos2X) { this.pos2X = pos2X; }
    public int getPos2Z() { return pos2Z; }
    public void setPos2Z(int pos2Z) { this.pos2Z = pos2Z; }
    public String getOwner() { return owner; }
    public void setOwner(String owner) { this.owner = owner; }
    public String getWorld() { return world; }
    public void setWorld(String world) { this.world = world; }
    public java.sql.Timestamp getTimestamp() { return timestamp; }
    public void setTimestamp(Timestamp timestamp) { this.timestamp = timestamp; }
}
