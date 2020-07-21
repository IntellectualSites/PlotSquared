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

import com.google.inject.Inject;
import com.plotsquared.core.configuration.Captions;
import com.plotsquared.core.configuration.Settings;
import com.plotsquared.core.configuration.caption.StaticCaption;
import com.plotsquared.core.configuration.caption.TranslatableCaption;
import com.plotsquared.core.player.PlotPlayer;
import com.plotsquared.core.plot.Plot;
import com.plotsquared.core.plot.flag.implementations.DoneFlag;
import com.plotsquared.core.plot.world.PlotAreaManager;
import com.plotsquared.core.util.Permissions;
import com.plotsquared.core.util.SchematicHandler;
import com.plotsquared.core.util.StringMan;
import com.plotsquared.core.util.WorldUtil;
import com.plotsquared.core.util.task.RunnableVal;
import com.sk89q.jnbt.CompoundTag;
import net.kyori.adventure.text.minimessage.Template;

import javax.annotation.Nonnull;
import java.net.URL;

@CommandDeclaration(usage = "/plot download [schematic|world]",
    command = "download",
    aliases = {"dl"},
    category = CommandCategory.SCHEMATIC,
    requiredType = RequiredType.NONE,
    description = "Download your plot",
    permission = "plots.download")
public class Download extends SubCommand {

    private final PlotAreaManager plotAreaManager;
    private final SchematicHandler schematicHandler;
    private final WorldUtil worldUtil;

    @Inject public Download(@Nonnull final PlotAreaManager plotAreaManager,
                            @Nonnull final SchematicHandler schematicHandler,
                            @Nonnull final WorldUtil worldUtil) {
        this.plotAreaManager = plotAreaManager;
        this.schematicHandler = schematicHandler;
        this.worldUtil = worldUtil;
    }

    @Override public boolean onCommand(final PlotPlayer<?> player, String[] args) {
        String world = player.getLocation().getWorldName();
        if (!this.plotAreaManager.hasPlotArea(world)) {
            return !sendMessage(player, Captions.NOT_IN_PLOT_WORLD);
        }
        final Plot plot = player.getCurrentPlot();
        if (plot == null) {
            player.sendMessage(TranslatableCaption.of("errors.not_in_plot"));
            return false;
        }
        if (!plot.hasOwner()) {
            player.sendMessage(TranslatableCaption.of("info.plot_unowned"));
            return false;
        }
        if ((Settings.Done.REQUIRED_FOR_DOWNLOAD && (!DoneFlag.isDone(plot))) && !Permissions
            .hasPermission(player, Captions.PERMISSION_ADMIN_COMMAND_DOWNLOAD)) {
            player.sendMessage(TranslatableCaption.of("done.done_not_done"));
            return false;
        }
        if ((!plot.isOwner(player.getUUID())) && !Permissions
            .hasPermission(player, Captions.PERMISSION_ADMIN.getTranslated())) {
            player.sendMessage(TranslatableCaption.of("permission.no_plot_perms"));
            return false;
        }
        if (plot.getRunning() > 0) {
            player.sendMessage(TranslatableCaption.of("errors.wait_for_timer"));
            return false;
        }
        if (args.length == 0 || (args.length == 1 && StringMan
            .isEqualIgnoreCaseToAny(args[0], "sch", "schem", "schematic"))) {
            if (plot.getVolume() > Integer.MAX_VALUE) {
            player.sendMessage(TranslatableCaption.of("schematics.schematic_too_large"));
                return false;
            }
            plot.addRunning();
            this.schematicHandler.getCompoundTag(plot, new RunnableVal<CompoundTag>() {
                @Override public void run(CompoundTag value) {
                    plot.removeRunning();
                    schematicHandler.upload(value, null, null, new RunnableVal<URL>() {
                        @Override public void run(URL url) {
                            if (url == null) {
                                player.sendMessage(TranslatableCaption.of("web.generating_link_failed"));
                                return;
                            }
                            player.sendMessage(StaticCaption.of(url.toString())
                            );
                        }
                    });
                }
            });
        } else if (args.length == 1 && StringMan
            .isEqualIgnoreCaseToAny(args[0], "mcr", "world", "mca")) {
            if (!Permissions.hasPermission(player, Captions.PERMISSION_DOWNLOAD_WORLD)) {
                player.sendMessage(
                        TranslatableCaption.of("permission.no_permission"),
                        Template.of("node", Captions.PERMISSION_DOWNLOAD_WORLD.getTranslated())
                );
                return false;
            }
            player.sendMessage(TranslatableCaption.of("schematics.mca_file_size"));
            plot.addRunning();
            this.worldUtil.saveWorld(world);
            this.worldUtil.upload(plot, null, null, new RunnableVal<URL>() {
                @Override public void run(URL url) {
                    plot.removeRunning();
                    if (url == null) {
                        player.sendMessage(TranslatableCaption.of("web.generating_link_failed"));
                        return;
                    }
                    player.sendMessage(StaticCaption.of(url.toString())
                }
            });
        } else {
            sendUsage(player);
            return false;
        }
        player.sendMessage(TranslatableCaption.of("web.generating_link"));
        return true;
    }
}
