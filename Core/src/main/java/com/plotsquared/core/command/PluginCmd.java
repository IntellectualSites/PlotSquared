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

package com.plotsquared.core.command;

import com.plotsquared.core.PlotSquared;
import com.plotsquared.core.configuration.caption.StaticCaption;
import com.plotsquared.core.player.PlotPlayer;
import com.plotsquared.core.util.PremiumVerification;
import com.plotsquared.core.util.task.TaskManager;
import net.kyori.adventure.text.minimessage.Template;

@CommandDeclaration(command = "plugin",
    permission = "plots.use",
    usage = "/plot plugin",
    aliases = "version",
    category = CommandCategory.INFO)
public class PluginCmd extends SubCommand {

    @Override public boolean onCommand(final PlotPlayer<?> player, String[] args) {
        TaskManager.getPlatformImplementation().taskAsync(() -> {
            player.sendMessage(
                    StaticCaption.of("<gray>>> </gray><gold><bold>" + PlotSquared.platform().getPluginName() + " <reset><gray>(<gold>Version</gold><gray>: </gray><gold><version></gold><gray>)</gray>"),
                    Template.of("version", String.valueOf(PlotSquared.get().getVersion()))
            );
            player.sendMessage(StaticCaption.of("<gray> >> </gray><gold><bold>Authors<reset><gray>: </gray><gold>Citymonstret </gold><gray>& </gray><gold>Empire92 </gold><gray>& </gray><gold>MattBDev </gold><gray>& </gray><gold>dordsor21 </gold><gray>& </gray><gold>NotMyFault </gold><gray>& </gray><gold>SirYwell</gold>"));
            player.sendMessage(StaticCaption.of("<gray> >> </gray><gold><bold>Wiki<reset><gray>: </gray><gold><click:open_url>https://wiki.intellectualsites.com/plotsquared/home</gold>"));
            player.sendMessage(
                    StaticCaption.of("<gray> >> </gray><gold><bold>Premium<reset><gray>: <gold><value></gold>"),
                    Template.of("value", String.valueOf(PremiumVerification.isPremium()))
            );
        });
        return true;
    }
}
