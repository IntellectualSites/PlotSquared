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
package com.plotsquared.core.gui;

import com.plotsquared.core.configuration.caption.TranslatableCaption;
import com.plotsquared.core.events.PlotFlagAddEvent;
import com.plotsquared.core.events.PlotFlagRemoveEvent;
import com.plotsquared.core.events.Result;
import com.plotsquared.core.player.PlotPlayer;
import com.plotsquared.core.plot.Plot;
import com.plotsquared.core.plot.PlotItemStack;
import com.plotsquared.core.plot.flag.PlotFlag;
import com.plotsquared.core.plot.flag.implementations.MusicFlag;
import com.plotsquared.core.util.EventDispatcher;
import com.plotsquared.core.util.gui.ExtendablePlotInventory;
import com.plotsquared.core.util.gui.PlotInventoryProvider;
import com.sk89q.worldedit.world.item.ItemTypes;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;

import java.util.List;
import java.util.stream.Stream;

public class PlotMusicInventory<P, I> extends ExtendablePlotInventory<P, I> {

    private static final List<String> DISCS = Stream.of(
            "music_disc_13",
            "music_disc_cat",
            "music_disc_blocks",
            "music_disc_chirp",
            "music_disc_far",
            "music_disc_mall",
            "music_disc_mellohi",
            "music_disc_stal",
            "music_disc_strad",
            "music_disc_ward",
            "music_disc_11",
            "music_disc_wait",
            "music_disc_otherside",
            "music_disc_pigstep"
    ).filter(s -> ItemTypes.get(s) != null).toList();
    private final Plot plot;
    private final EventDispatcher eventDispatcher;

    public PlotMusicInventory(
            final PlotInventoryProvider<P, I> provider,
            PlotPlayer<?> player,
            Plot plot,
            EventDispatcher eventDispatcher
    ) {
        super(
                provider, player, 2 * 9,
                TranslatableCaption.of("plotjukebox.jukebox_header")
        );
        this.plot = plot;
        this.eventDispatcher = eventDispatcher;
        setDiscs();
        setCancelButton();
    }

    private void setDiscs() {
        for (final String disc : DISCS) {
            PlotItemStack itemStack = new PlotItemStack(
                    disc, 1, String.format("<gold>%s</gold>", disc),
                    TranslatableCaption.of("plotjukebox.click_to_play").getComponent(player())
            );
            addItem(itemStack, (item, type) -> {
                close();
                PlotFlag<?, ?> plotFlag = plot.getFlagContainer().getFlag(MusicFlag.class)
                        .createFlagInstance(item.getType());
                PlotFlagAddEvent event = eventDispatcher.callFlagAdd(plotFlag, plot);
                if (event.getEventResult() == Result.DENY) {
                    player().sendMessage(
                            TranslatableCaption.of("events.event_denied"),
                            TagResolver.resolver("value", Tag.inserting(Component.text("Music addition")))
                    );
                    return;
                }
                plot.setFlag(event.getFlag());
                player().sendMessage(
                        TranslatableCaption.of("flag.flag_added"),
                        TagResolver.builder()
                                .tag("flag", Tag.inserting(Component.text("music")))
                                .tag("value", Tag.inserting(Component.text(event.getFlag().getValue().toString())))
                                .build()
                );
            });
        }
    }


    private void setCancelButton() {
        PlotItemStack cancelItem = new PlotItemStack(
                ItemTypes.BEDROCK, 1,
                TranslatableCaption.of("plotjukebox.cancel_music").getComponent(player()),
                TranslatableCaption.of("plotjukebox.reset_music").getComponent(player())
        );
        setItem(size() - 1, cancelItem, (item, type) -> {
            close();
            PlotFlag<?, ?> plotFlag = plot.getFlagContainer().getFlag(MusicFlag.class)
                    .createFlagInstance(item.getType());
            PlotFlagRemoveEvent event = eventDispatcher.callFlagRemove(plotFlag, plot);
            if (event.getEventResult() == Result.DENY) {
                player().sendMessage(
                        TranslatableCaption.of("events.event_denied"),
                        TagResolver.resolver("value", Tag.inserting(Component.text("Music removal")))
                );
                return;
            }
            plot.removeFlag(event.getFlag());
            player().sendMessage(
                    TranslatableCaption.of("flag.flag_removed"),
                    TagResolver.builder()
                            .tag("flag", Tag.inserting(Component.text("music")))
                            .tag("value", Tag.inserting(Component.text("music_disc")))
                            .build()
            );
        });
    }

}
