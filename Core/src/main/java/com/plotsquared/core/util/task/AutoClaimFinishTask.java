/*
 *       _____  _       _    _____                                _
 *      |  __ \| |     | |  / ____|                              | |
 *      | |__) | | ___ | |_| (___   __ _ _   _  __ _ _ __ ___  __| |
 *      |  ___/| |/ _ \| __|\___ \ / _` | | | |/ _` | '__/ _ \/ _` |
 *      | |    | | (_) | |_ ____) | (_| | |_| | (_| | | |  __/ (_| |
 *      |_|    |_|\___/ \__|_____/ \__, |\__,_|\__,_|_|  \___|\__,_|
 *                                    | |
 *                                    |_|
 *            PlotSquared plot management system for Minecraft
 *                  Copyright (C) 2020 IntellectualSites
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.plotsquared.core.util.task;

import com.plotsquared.core.configuration.caption.Templates;
import com.plotsquared.core.configuration.caption.TranslatableCaption;
import com.plotsquared.core.events.PlotMergeEvent;
import com.plotsquared.core.events.Result;
import com.plotsquared.core.location.Direction;
import com.plotsquared.core.player.MetaDataAccess;
import com.plotsquared.core.player.PlayerMetaDataKeys;
import com.plotsquared.core.player.PlotPlayer;
import com.plotsquared.core.plot.Plot;
import com.plotsquared.core.plot.PlotArea;
import com.plotsquared.core.util.EventDispatcher;

import java.util.concurrent.Callable;

public final class AutoClaimFinishTask implements Callable<Boolean> {

    private final PlotPlayer player;
    private final Plot plot;
    private final PlotArea area;
    private final String schematic;
    private final EventDispatcher eventDispatcher;

    public AutoClaimFinishTask(final PlotPlayer player, final Plot plot, final PlotArea area,
        final String schematic, final EventDispatcher eventDispatcher) {
        this.player = player;
        this.plot = plot;
        this.area = area;
        this.schematic = schematic;
        this.eventDispatcher = eventDispatcher;
    }

    @Override public Boolean call() {
        try (final MetaDataAccess<Boolean> autoAccess
            = player.accessTemporaryMetaData(PlayerMetaDataKeys.TEMPORARY_AUTO)) {
            autoAccess.remove();
        }
        if (plot == null) {
            player.sendMessage(TranslatableCaption.of("errors.no_free_plots"));
            return false;
        }
        plot.claim(player, true, schematic, false);
        if (area.isAutoMerge()) {
            PlotMergeEvent event = this.eventDispatcher.callMerge(plot, Direction.ALL, Integer.MAX_VALUE, player);
            if (event.getEventResult() == Result.DENY) {
                player.sendMessage(TranslatableCaption.of("events.event_denied"),
                                   Templates.of("value", "Auto Merge"));
            } else {
                plot.getPlotModificationManager().autoMerge(event.getDir(), event.getMax(), player.getUUID(), true);
            }
        }
        return true;
    }

}
