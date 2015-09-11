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
package com.intellectualcrafters.plot.object.comment;

import com.intellectualcrafters.plot.object.PlotId;

/**
 */
public class PlotComment
{
    public final String comment;
    public final String inbox;
    public final String senderName;
    public final PlotId id;
    public final String world;
    public final long timestamp;

    public PlotComment(final String world, final PlotId id, final String comment, final String senderName, final String inbox, final long timestamp)
    {
        this.world = world;
        this.id = id;
        this.comment = comment;
        this.senderName = senderName;
        this.inbox = inbox;
        this.timestamp = timestamp;
    }
}
