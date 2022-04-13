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
 *               Copyright (C) 2014 - 2022 IntellectualSites
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

import com.plotsquared.core.configuration.caption.TranslatableCaption;
import com.plotsquared.core.player.PlotPlayer;
import com.plotsquared.core.plot.PlotArea;
import net.kyori.adventure.text.minimessage.Template;

/**
 * @deprecated In favor of "/plot toggle chat" and
 *         scheduled for removal within the next major release.
 */
@Deprecated(forRemoval = true, since = "6.0.0")
@CommandDeclaration(command = "chat",
        usage = "/plot chat",
        permission = "plots.chat",
        category = CommandCategory.CHAT,
        requiredType = RequiredType.PLAYER)
public class Chat extends SubCommand {

    @Override
    public boolean onCommand(PlotPlayer<?> player, String[] args) {
        PlotArea area = player.getPlotAreaAbs();
        check(area, TranslatableCaption.of("errors.not_in_plot_world"));
        player.sendMessage(
                TranslatableCaption.of("errors.deprecated_commands"),
                Template.of("replacement", "/plot toggle chat")
        );
        if (player.getPlotAreaAbs().isForcingPlotChat()) {
            player.sendMessage(TranslatableCaption.of("chat.plot_chat_forced"));
            return true;
        }
        MainCommand.getInstance().toggle.chat(this, player, args, null, null);
        return true;
    }

}
