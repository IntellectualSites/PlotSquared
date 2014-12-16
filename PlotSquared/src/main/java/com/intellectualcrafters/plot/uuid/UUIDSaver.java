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

import java.util.UUID;

import com.google.common.collect.BiMap;
import com.intellectualcrafters.plot.object.StringWrapper;

/**
 * @author Citymonstret
 */
public interface UUIDSaver {

    /**
     * Populate the default list
     */
    public void globalPopulate();

    /**
     * Save the UUIDs
     *
     * @param biMap
     *            Map containing names and UUIDs
     */
    public void globalSave(final BiMap<StringWrapper, UUID> biMap);

    /**
     * Save a single UUIDSet
     *
     * @param set
     *            Set to save
     */
    public void save(final UUIDSet set);

    /**
     * Get a single UUIDSet
     *
     * @param name
     *            Username
     * @return UUID Set
     */
    public UUIDSet get(final String name);

    /**
     * Get a single UUIDSet
     *
     * @param uuid
     *            UUID
     * @return UUID Set
     */
    public UUIDSet get(final UUID uuid);

    /**
     * Fetch uuid from mojang servers
     *
     * @param name
     *            Username
     * @return uuid
     * @throws Exception
     */
    public UUID mojangUUID(final String name) throws Exception;

    /**
     * Fetch username from mojang servers
     *
     * @param uuid
     *            UUID
     * @return username
     * @throws Exception
     */
    public String mojangName(final UUID uuid) throws Exception;
}
