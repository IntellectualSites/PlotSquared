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
package com.plotsquared.core.plot.comment;

import com.plotsquared.core.plot.PlotId;

public class PlotComment {

    /**
     * @deprecated {@link PlotComment} class will be replaced by a record in the next major update. This variable will be
     *         accessible using {@code PlotComment#comment()}
     */
    @Deprecated(forRemoval = true, since = "TODO")
    public final String comment;

    /**
     * @deprecated {@link PlotComment} class will be replaced by a record in the next major update. This variable will be
     *         accessible using {@code PlotComment#inbox()}
     */
    @Deprecated(forRemoval = true, since = "TODO")
    public final String inbox;

    /**
     * @deprecated {@link PlotComment} class will be replaced by a record in the next major update. This variable will be
     *         accessible using {@code PlotComment#senderName()}
     */
    @Deprecated(forRemoval = true, since = "TODO")
    public final String senderName;

    /**
     * @deprecated {@link PlotComment} class will be replaced by a record in the next major update. This variable will be
     *         accessible using {@code PlotComment#id()}
     */
    @Deprecated(forRemoval = true, since = "TODO")
    public final PlotId id;

    /**
     * @deprecated {@link PlotComment} class will be replaced by a record in the next major update. This variable will be
     *         accessible using {@code PlotComment#world()}
     */
    @Deprecated(forRemoval = true, since = "TODO")
    public final String world;
    /**
     * @deprecated {@link PlotComment} class will be replaced by a record in the next major update. This variable will be
     *         accessible using {@code PlotComment#timestamp()}
     */
    @Deprecated(forRemoval = true, since = "TODO")
    public final long timestamp;

    public PlotComment(
            String world, PlotId id, String comment, String senderName, String inbox,
            long timestamp
    ) {
        this.world = world;
        this.id = id;
        this.comment = comment;
        this.senderName = senderName;
        this.inbox = inbox;
        this.timestamp = timestamp;
    }

}
