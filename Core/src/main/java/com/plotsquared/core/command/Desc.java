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
package com.plotsquared.core.command;

import com.plotsquared.core.configuration.Captions;
import com.plotsquared.core.events.PlotFlagAddEvent;
import com.plotsquared.core.events.PlotFlagRemoveEvent;
import com.plotsquared.core.events.Result;
import com.plotsquared.core.player.PlotPlayer;
import com.plotsquared.core.plot.Plot;
import com.plotsquared.core.plot.flag.implementations.DescriptionFlag;
import com.plotsquared.core.util.EventDispatcher;
import com.plotsquared.core.util.MainUtil;
import org.jetbrains.annotations.NotNull;

@CommandDeclaration(command = "setdescription",
    permission = "plots.set.desc",
    description = "Set the plot description",
    usage = "/plot desc <description>",
    aliases = {"desc", "setdesc", "setd", "description"},
    category = CommandCategory.SETTINGS,
    requiredType = RequiredType.PLAYER)
public class Desc extends SetCommand {

    private final EventDispatcher eventDispatcher;
    
    public Desc(@NotNull final EventDispatcher eventDispatcher) {
        this.eventDispatcher = eventDispatcher;
    }
    
    @Override public boolean set(PlotPlayer player, Plot plot, String desc) {
        if (desc.isEmpty()) {
            PlotFlagRemoveEvent event = this.eventDispatcher.callFlagRemove(plot.getFlagContainer().getFlag(DescriptionFlag.class), plot);
            if (event.getEventResult() == Result.DENY) {
                sendMessage(player, Captions.EVENT_DENIED, "Description removal");
                return false;
            }
            plot.removeFlag(event.getFlag());
            MainUtil.sendMessage(player, Captions.DESC_UNSET);
            return true;
        }
        PlotFlagAddEvent event = this.eventDispatcher.callFlagAdd(plot.getFlagContainer().getFlag(DescriptionFlag.class).createFlagInstance(desc), plot);
        if (event.getEventResult() == Result.DENY) {
            sendMessage(player, Captions.EVENT_DENIED, "Description set");
            return false;
        }
        boolean result = plot.setFlag(event.getFlag());
        if (!result) {
            MainUtil.sendMessage(player, Captions.FLAG_NOT_ADDED);
            return false;
        }
        MainUtil.sendMessage(player, Captions.DESC_SET);
        return true;
    }
}
