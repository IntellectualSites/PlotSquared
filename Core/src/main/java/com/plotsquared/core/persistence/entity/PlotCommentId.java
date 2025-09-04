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

import java.io.Serializable;
import java.util.Objects;

public class PlotCommentId implements Serializable {
    private String world;
    private Integer hashcode;
    private String inbox;

    public PlotCommentId() {}

    public PlotCommentId(String world, Integer hashcode, String inbox) {
        this.world = world;
        this.hashcode = hashcode;
        this.inbox = inbox;
    }

    public String getWorld() { return world; }
    public void setWorld(String world) { this.world = world; }
    public Integer getHashcode() { return hashcode; }
    public void setHashcode(Integer hashcode) { this.hashcode = hashcode; }
    public String getInbox() { return inbox; }
    public void setInbox(String inbox) { this.inbox = inbox; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PlotCommentId that = (PlotCommentId) o;
        return Objects.equals(world, that.world) && Objects.equals(hashcode, that.hashcode) && Objects.equals(inbox, that.inbox);
    }

    @Override
    public int hashCode() {
        return Objects.hash(world, hashcode, inbox);
    }
}
