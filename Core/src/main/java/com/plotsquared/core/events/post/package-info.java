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
/**
 * Contains events which are fired after specific actions are completed.
 * <br>
 * E.g. {@link com.plotsquared.core.events.PlotDeleteEvent} is called <b>before</b> a plot is actually deleted and useful for
 * cancelling the action itself. {@link com.plotsquared.core.events.post.PostPlotDeleteEvent} will be called after the plot was
 * deleted.
 */
package com.plotsquared.core.events.post;
