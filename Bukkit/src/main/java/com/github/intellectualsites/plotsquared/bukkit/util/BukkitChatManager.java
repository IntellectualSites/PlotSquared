package com.github.intellectualsites.plotsquared.bukkit.util;

import com.github.intellectualsites.plotsquared.plot.util.block.BlockUtil;

import com.github.intellectualsites.plotsquared.bukkit.chat.FancyMessage;
import com.github.intellectualsites.plotsquared.bukkit.object.BukkitPlayer;
import com.github.intellectualsites.plotsquared.plot.config.Captions;
import com.github.intellectualsites.plotsquared.plot.config.Settings;
import com.github.intellectualsites.plotsquared.plot.object.ConsolePlayer;
import com.github.intellectualsites.plotsquared.plot.object.PlotMessage;
import com.github.intellectualsites.plotsquared.plot.object.PlotPlayer;
import com.github.intellectualsites.plotsquared.plot.util.ChatManager;
import org.bukkit.ChatColor;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class BukkitChatManager extends ChatManager<FancyMessage> {

    @Override public FancyMessage builder() {
        return new FancyMessage("");
    }

    @Override public void color(PlotMessage message, String color) {
        message.$(this).color(ChatColor.getByChar(Captions.color(color).substring(1)));
    }

    @Override public void tooltip(PlotMessage message, PlotMessage... tooltips) {
        List<FancyMessage> lines =
            Arrays.stream(tooltips).map(tooltip -> tooltip.$(this)).collect(Collectors.toList());
        message.$(this).formattedTooltip(lines);
    }

    @Override public void command(PlotMessage message, String command) {
        message.$(this).command(command);
    }

    @Override public void text(PlotMessage message, String text) {
        message.$(this).then(ChatColor.stripColor(text));
    }

    @Override public void send(PlotMessage plotMessage, PlotPlayer player) {
        if (player instanceof ConsolePlayer || !Settings.Chat.INTERACTIVE) {
            player.sendMessage(plotMessage.$(this).toOldMessageFormat());
        } else {
            plotMessage.$(this).send(((BukkitPlayer) player).player);
        }
    }

    @Override public void suggest(PlotMessage plotMessage, String command) {
        plotMessage.$(this).suggest(command);
    }

}
