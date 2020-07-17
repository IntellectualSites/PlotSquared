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

import com.plotsquared.core.PlotSquared;
import com.plotsquared.core.player.PlotPlayer;
import com.plotsquared.core.util.MainUtil;
import com.plotsquared.core.util.PremiumVerification;
import com.plotsquared.core.util.task.TaskManager;

@CommandDeclaration(command = "plugin",
    permission = "plots.use",
    description = "Show plugin information",
    usage = "/plot plugin",
    aliases = "version",
    category = CommandCategory.INFO)
public class PluginCmd extends SubCommand {

    @Override public boolean onCommand(final PlotPlayer<?> player, String[] args) {
        TaskManager.getPlatformImplementation().taskAsync(() -> {
            MainUtil.sendMessage(player, String.format(
                "$2>> $1&l" + PlotSquared.platform().getPluginName() + " $2($1Version$2: $1%s$2)",
                PlotSquared.get().getVersion()));
            MainUtil.sendMessage(player,
                "$2>> $1&lAuthors$2: $1Citymonstret $2& $1Empire92 $2& $1MattBDev $2& $1dordsor21 $2& $1NotMyFault $2& $1SirYwell");
            MainUtil.sendMessage(player,
                "$2>> $1&lWiki$2: $1https://wiki.intellectualsites.com/plotsquared/home");
            MainUtil
                .sendMessage(player, "$2>> $1&lPremium$2: $1" + PremiumVerification.isPremium());
        });
        return true;
    }
}
