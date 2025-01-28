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

import com.google.inject.Inject;
import com.plotsquared.core.configuration.caption.TranslatableCaption;
import com.plotsquared.core.events.PlotFlagAddEvent;
import com.plotsquared.core.events.PlotFlagRemoveEvent;
import com.plotsquared.core.events.Result;
import com.plotsquared.core.location.Location;
import com.plotsquared.core.permissions.Permission;
import com.plotsquared.core.player.PlotPlayer;
import com.plotsquared.core.plot.Plot;
import com.plotsquared.core.plot.PlotInventory;
import com.plotsquared.core.plot.PlotItemStack;
import com.plotsquared.core.plot.flag.PlotFlag;
import com.plotsquared.core.plot.flag.implementations.MusicFlag;
import com.plotsquared.core.util.EventDispatcher;
import com.plotsquared.core.util.InventoryUtil;
import com.sk89q.worldedit.world.item.ItemType;
import com.sk89q.worldedit.world.item.ItemTypes;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.checkerframework.checker.nullness.qual.NonNull;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Collection;
import java.util.Locale;

@CommandDeclaration(command = "music",
        permission = "plots.music",
        usage = "/plot music",
        category = CommandCategory.APPEARANCE,
        requiredType = RequiredType.PLAYER)
public class Music extends SubCommand {

    private static final Collection<String> DISCS = Arrays
            .asList("music_disc_13", "music_disc_cat", "music_disc_blocks", "music_disc_chirp",
                    "music_disc_far", "music_disc_mall", "music_disc_mellohi", "music_disc_stal",
                    "music_disc_strad", "music_disc_ward", "music_disc_11", "music_disc_wait", "music_disc_otherside",
                    "music_disc_pigstep", "music_disc_5", "music_disc_relic", "music_disc_creator",
                    "music_disc_creator_music_box", "music_disc_precipice"
            );

    // make sure all discs and the bedrock ("cancel") fit into the inventory
    private static final int INVENTORY_ROWS = (int) Math.ceil((DISCS.size() + 1) / 9.0);

    private final InventoryUtil inventoryUtil;
    private final EventDispatcher eventDispatcher;

    @Inject
    public Music(final @Nullable InventoryUtil inventoryUtil, final @NonNull EventDispatcher eventDispatcher) {
        this.inventoryUtil = inventoryUtil;
        this.eventDispatcher = eventDispatcher;
    }

    @Override
    public boolean onCommand(PlotPlayer<?> player, String[] args) {
        Location location = player.getLocation();
        final Plot plot = location.getPlotAbs();
        if (plot == null) {
            player.sendMessage(TranslatableCaption.of("errors.not_in_plot"));
            return false;
        }
        if (!plot.hasOwner()) {
            player.sendMessage(TranslatableCaption.of("info.plot_unowned"));
            return false;
        }
        if (!plot.isAdded(player.getUUID()) && !player.hasPermission(Permission.PERMISSION_ADMIN_MUSIC_OTHER)) {
            player.sendMessage(
                    TranslatableCaption.of("permission.no_permission"),
                    TagResolver.resolver(
                            "node",
                            Tag.inserting(Permission.PERMISSION_ADMIN_MUSIC_OTHER)
                    )
            );
            return true;
        }
        PlotInventory inv = new PlotInventory(
                this.inventoryUtil,
                player,
                INVENTORY_ROWS,
                TranslatableCaption.of("plotjukebox.jukebox_header").getComponent(player)
        ) {
            @Override
            public boolean onClick(int index) {
                PlotItemStack item = getItem(index);
                if (item == null) {
                    return true;
                }
                if (item.getType() == ItemTypes.BEDROCK) {
                    PlotFlag<?, ?> plotFlag = plot.getFlagContainer().getFlag(MusicFlag.class)
                            .createFlagInstance(item.getType());
                    PlotFlagRemoveEvent event = eventDispatcher.callFlagRemove(plotFlag, plot);
                    if (event.getEventResult() == Result.DENY) {
                        getPlayer().sendMessage(
                                TranslatableCaption.of("events.event_denied"),
                                TagResolver.resolver("value", Tag.inserting(Component.text("Music removal")))
                        );
                        return true;
                    }
                    plot.removeFlag(event.getFlag());
                    getPlayer().sendMessage(
                            TranslatableCaption.of("flag.flag_removed"),
                            TagResolver.builder()
                                    .tag("flag", Tag.inserting(Component.text("music")))
                                    .tag("value", Tag.inserting(Component.text("music_disc")))
                                    .build()
                    );
                } else if (item.getName().toLowerCase(Locale.ENGLISH).contains("disc")) {
                    PlotFlag<?, ?> plotFlag = plot.getFlagContainer().getFlag(MusicFlag.class)
                            .createFlagInstance(item.getType());
                    PlotFlagAddEvent event = eventDispatcher.callFlagAdd(plotFlag, plot);
                    if (event.getEventResult() == Result.DENY) {
                        getPlayer().sendMessage(
                                TranslatableCaption.of("events.event_denied"),
                                TagResolver.resolver("value", Tag.inserting(Component.text("Music addition")))
                        );
                        return true;
                    }
                    plot.setFlag(event.getFlag());
                    getPlayer().sendMessage(
                            TranslatableCaption.of("flag.flag_added"),
                            TagResolver.builder()
                                    .tag("flag", Tag.inserting(Component.text("music")))
                                    .tag("value", Tag.inserting(Component.text(event.getFlag().getValue().toString())))
                                    .build()
                    );
                } else {
                    getPlayer().sendMessage(TranslatableCaption.of("flag.flag_not_added"));
                }
                return false;
            }
        };
        int index = 0;

        for (final String disc : DISCS) {
            final String name = String.format("<gold>%s</gold>", disc);
            final String[] lore = {TranslatableCaption.of("plotjukebox.click_to_play").getComponent(player)};
            ItemType type = ItemTypes.get(disc);
            if (type == null) {
                continue;
            }
            final PlotItemStack item = new PlotItemStack(type, 1, name, lore);
            if (inv.setItemChecked(index, item)) {
                index++;
            }
        }

        // Always add the cancel button
        // if (player.getMeta("music") != null) {
        String name = TranslatableCaption.of("plotjukebox.cancel_music").getComponent(player);
        String[] lore = {TranslatableCaption.of("plotjukebox.reset_music").getComponent(player)};
        inv.setItem(index, new PlotItemStack("bedrock", 1, name, lore));
        // }

        inv.openInventory();
        return true;
    }

}
