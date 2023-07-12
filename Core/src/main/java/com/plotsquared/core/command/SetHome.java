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
package com.plotsquared.core.command;

import com.plotsquared.core.configuration.caption.TranslatableCaption;
import com.plotsquared.core.location.BlockLoc;
import com.plotsquared.core.location.Location;
import com.plotsquared.core.player.PlotPlayer;
import com.plotsquared.core.plot.Plot;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;

@CommandDeclaration(command = "sethome",
        permission = "plots.set.home",
        usage = "/plot sethome [none]",
        aliases = {"sh", "seth"},
        category = CommandCategory.SETTINGS,
        requiredType = RequiredType.PLAYER)
public class SetHome extends SetCommand {

    @Override
    public boolean set(PlotPlayer<?> player, Plot plot, String value) {
        if (!plot.hasOwner()) {
            player.sendMessage(TranslatableCaption.of("info.plot_unowned"));
            return false;
        }
        switch (value.toLowerCase()) {
            case "unset", "reset", "remove", "none" -> {
                Plot base = plot.getBasePlot(false);
                base.setHome(null);
                player.sendMessage(TranslatableCaption.of("position.position_unset"));
                return true;
            }
            case "" -> {
                Plot base = plot.getBasePlot(false);
                Location bottom = base.getBottomAbs();
                Location location = player.getLocationFull();
                BlockLoc rel = new BlockLoc(
                        location.getX() - bottom.getX(),
                        location.getY(), // y is absolute
                        location.getZ() - bottom.getZ(),
                        location.getYaw(),
                        location.getPitch()
                );
                base.setHome(rel);
                player.sendMessage(TranslatableCaption.of("position.position_set"));
                return true;
            }
            default -> {
                player.sendMessage(
                        TranslatableCaption.of("commandconfig.command_syntax"),
                        TagResolver.resolver("value", Tag.inserting(Component.text("Use /plot set home [none]")))
                );
                return false;
            }
        }
    }

}
