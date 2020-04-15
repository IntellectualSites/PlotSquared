package com.plotsquared.core.command;

import com.plotsquared.core.player.PlotPlayer;
import com.plotsquared.core.config.Captions;
import com.plotsquared.core.events.PlotFlagAddEvent;
import com.plotsquared.core.events.PlotFlagRemoveEvent;
import com.plotsquared.core.events.Result;
import com.plotsquared.core.plot.flag.PlotFlag;
import com.plotsquared.core.plot.flag.implementations.MusicFlag;
import com.plotsquared.core.location.Location;
import com.plotsquared.core.plot.Plot;
import com.plotsquared.core.plot.PlotInventory;
import com.plotsquared.core.plot.PlotItemStack;
import com.sk89q.worldedit.world.item.ItemTypes;

import java.util.Arrays;
import java.util.Collection;
import java.util.Locale;

@CommandDeclaration(command = "music",
    permission = "plots.music",
    description = "Play music in your plot",
    usage = "/plot music",
    category = CommandCategory.APPEARANCE,
    requiredType = RequiredType.PLAYER)
public class Music extends SubCommand {

    private static final Collection<String> DISCS = Arrays
        .asList("music_disc_13", "music_disc_cat", "music_disc_blocks", "music_disc_chirp",
            "music_disc_far", "music_disc_mall", "music_disc_mellohi", "music_disc_stal",
            "music_disc_strad", "music_disc_ward", "music_disc_11", "music_disc_wait");

    @Override public boolean onCommand(PlotPlayer player, String[] args) {
        Location location = player.getLocation();
        final Plot plot = location.getPlotAbs();
        if (plot == null) {
            return !sendMessage(player, Captions.NOT_IN_PLOT);
        }
        if (!plot.isAdded(player.getUUID())) {
            sendMessage(player, Captions.NO_PLOT_PERMS);
            return true;
        }
        PlotInventory inv = new PlotInventory(player, 2, "Plot Jukebox") {
            @Override public boolean onClick(int index) {
                PlotItemStack item = getItem(index);
                if (item == null) {
                    return true;
                }
                if (item.getType() == ItemTypes.BEDROCK) {
                    PlotFlag<?, ?> plotFlag = plot.getFlagContainer().getFlag(MusicFlag.class)
                        .createFlagInstance(item.getType());
                    PlotFlagRemoveEvent event = new PlotFlagRemoveEvent(plotFlag, plot);
                    if (event.getEventResult() == Result.DENY) {
                        sendMessage(player, Captions.EVENT_DENIED, "Music removal");
                        return true;
                    }
                    plot.removeFlag(event.getFlag());
                    Captions.FLAG_REMOVED.send(player);
                } else if (item.name.toLowerCase(Locale.ENGLISH).contains("disc")) {
                    PlotFlag<?, ?> plotFlag = plot.getFlagContainer().getFlag(MusicFlag.class)
                        .createFlagInstance(item.getType());
                    PlotFlagAddEvent event = new PlotFlagAddEvent(plotFlag, plot);
                    if (event.getEventResult() == Result.DENY) {
                        sendMessage(player, Captions.EVENT_DENIED, "Music addition");
                        return true;
                    }
                    plot.setFlag(event.getFlag());
                    Captions.FLAG_ADDED.send(player);
                } else {
                    Captions.FLAG_NOT_ADDED.send(player);
                }
                return false;
            }
        };
        int index = 0;

        for (final String disc : DISCS) {
            final String name = String.format("&r&6%s", disc);
            final String[] lore = {"&r&aClick to play!"};
            final PlotItemStack item = new PlotItemStack(disc, 1, name, lore);
            inv.setItem(index++, item);
        }

        // Always add the cancel button
        // if (player.getMeta("music") != null) {
        String name = "&r&6Cancel music";
        String[] lore = {"&r&cClick to cancel!"};
        inv.setItem(index, new PlotItemStack("bedrock", 1, name, lore));
        // }

        inv.openInventory();
        return true;
    }
}
