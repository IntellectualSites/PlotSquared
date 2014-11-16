////////////////////////////////////////////////////////////////////////////////////////////////////
// PlotSquared - A plot manager and world generator for the Bukkit API                             /
// Copyright (c) 2014 IntellectualSites/IntellectualCrafters                                       /
//                                                                                                 /
// This program is free software; you can redistribute it and/or modify                            /
// it under the terms of the GNU General Public License as published by                            /
// the Free Software Foundation; either version 3 of the License, or                               /
// (at your option) any later version.                                                             /
//                                                                                                 /
// This program is distributed in the hope that it will be useful,                                 /
// but WITHOUT ANY WARRANTY; without even the implied warranty of                                  /
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the                                   /
// GNU General Public License for more details.                                                    /
//                                                                                                 /
// You should have received a copy of the GNU General Public License                               /
// along with this program; if not, write to the Free Software Foundation,                         /
// Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301  USA                               /
//                                                                                                 /
// You can contact us via: support@intellectualsites.com                                           /
////////////////////////////////////////////////////////////////////////////////////////////////////

package com.intellectualcrafters.plot.uuid;

import com.google.common.collect.BiMap;
import com.intellectualcrafters.plot.object.StringWrapper;

import java.util.UUID;

/**
 * Created by Citymonstret on 2014-10-13.
 */
public abstract class UUIDSaver {
    public abstract void globalPopulate();

    public abstract void globalSave(final BiMap<StringWrapper, UUID> biMap);

    public abstract void save(final UUIDSet set);

    public abstract UUIDSet get(final String name);

    public abstract UUIDSet get(final UUID uuid);

    public abstract UUID mojangUUID(final String name) throws Exception;

    public abstract String mojangName(final UUID uuid) throws Exception;
}
