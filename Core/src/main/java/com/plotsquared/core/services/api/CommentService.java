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

import com.plotsquared.core.plot.Plot;
import com.plotsquared.core.plot.comment.PlotComment;

import java.util.List;
import java.util.function.Consumer;

public interface CommentService {

    void removeComment(Plot plot, PlotComment comment);

    void setComment(Plot plot, PlotComment comment);

    void clearInbox(Plot plot, String inbox);

    void getComments(Plot plot, String inbox, Consumer<List<PlotComment>> whenDone);
}
