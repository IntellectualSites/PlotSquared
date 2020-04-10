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
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.github.intellectualsites.plotsquared.plot.util;

import com.github.intellectualsites.plotsquared.plot.PlotSquared;
import com.github.intellectualsites.plotsquared.plot.commands.Auto;
import com.github.intellectualsites.plotsquared.plot.config.Captions;
import com.github.intellectualsites.plotsquared.plot.database.DBFunc;
import com.github.intellectualsites.plotsquared.plot.events.PlotMergeEvent;
import com.github.intellectualsites.plotsquared.plot.events.Result;
import com.github.intellectualsites.plotsquared.plot.object.Direction;
import com.github.intellectualsites.plotsquared.plot.object.Plot;
import com.github.intellectualsites.plotsquared.plot.object.PlotArea;
import com.github.intellectualsites.plotsquared.plot.object.PlotPlayer;
import com.github.intellectualsites.plotsquared.plot.object.RunnableVal;
import lombok.RequiredArgsConstructor;

import static com.github.intellectualsites.plotsquared.plot.util.MainUtil.sendMessage;

@RequiredArgsConstructor public final class AutoClaimFinishTask extends RunnableVal<Object> {

    private final PlotPlayer player;
    private final Plot plot;
    private final PlotArea area;
    private final int allowedPlots;
    private final String schematic;

    @Override public void run(Object value) {
        player.deleteMeta(Auto.class.getName());
        if (plot == null) {
            sendMessage(player, Captions.NO_FREE_PLOTS);
            return;
        }

        if (Auto.checkAllowedPlots(player, area, allowedPlots, 1, 1)) {
            plot.claim(player, true, schematic, false);
            if (area.isAutoMerge()) {
                PlotMergeEvent event = PlotSquared.get().getEventDispatcher()
                    .callMerge(plot, Direction.ALL, Integer.MAX_VALUE, player);
                if (event.getEventResult() == Result.DENY) {
                    sendMessage(player, Captions.EVENT_DENIED, "Auto merge");
                } else {
                    plot.autoMerge(event.getDir(), event.getMax(), player.getUUID(),
                        true);
                }
            }
        } else {
            DBFunc.delete(plot);
        }
    }

}
