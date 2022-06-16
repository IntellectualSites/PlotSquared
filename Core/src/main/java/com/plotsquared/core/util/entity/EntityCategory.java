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
package com.plotsquared.core.util.entity;

import com.plotsquared.core.util.WorldUtil;
import com.sk89q.worldedit.registry.Category;
import com.sk89q.worldedit.registry.Keyed;
import com.sk89q.worldedit.registry.NamespacedRegistry;
import com.sk89q.worldedit.world.entity.EntityType;

import java.util.Set;

/**
 * Categories to which an {@link com.sk89q.worldedit.entity.Entity} may belong
 */
public class EntityCategory extends Category<EntityType> implements Keyed {

    public static final NamespacedRegistry<EntityCategory> REGISTRY =
            new NamespacedRegistry<>("entity type");

    private final WorldUtil worldUtil;
    private final String key;

    protected EntityCategory(final WorldUtil worldUtil, final String id) {
        super("plotsquared:" + id);
        this.key = id;
        this.worldUtil = worldUtil;
    }

    @Override
    protected Set<EntityType> load() {
        return this.worldUtil.getTypesInCategory(this.key);
    }

}
