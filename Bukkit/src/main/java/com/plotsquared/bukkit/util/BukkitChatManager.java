package com.plotsquared.bukkit.util;

import com.intellectualcrafters.plot.config.C;
import com.intellectualcrafters.plot.config.Settings;
import com.intellectualcrafters.plot.object.ConsolePlayer;
import com.intellectualcrafters.plot.object.PlotMessage;
import com.intellectualcrafters.plot.object.PlotPlayer;
import com.intellectualcrafters.plot.util.ChatManager;
import com.plotsquared.bukkit.chat.FancyMessage;
import com.plotsquared.bukkit.object.BukkitPlayer;
import org.bukkit.ChatColor;

import java.util.ArrayList;
import java.util.List;

public class BukkitChatManager extends ChatManager<FancyMessage> {

    @Override
    public FancyMessage builder() {
        return new FancyMessage("");
    }

    @Override
    public void color(PlotMessage message, String color) {
        message.$(this).color(ChatColor.getByChar(C.color(color).substring(1)));
    }

    @Override
    public void tooltip(PlotMessage message, PlotMessage... tooltips) {
        List<FancyMessage> lines = new ArrayList<>();
        for (PlotMessage tooltip : tooltips) {
            lines.add(tooltip.$(this));
        }
        message.$(this).formattedTooltip(lines);
    }

    @Override
    public void command(PlotMessage message, String command) {
        message.$(this).command(command);
    }

    @Override
    public void text(PlotMessage message, String text) {
        message.$(this).then(ChatColor.stripColor(text));
    }

    @Override
    public void send(PlotMessage plotMessage, PlotPlayer player) {
        if (player instanceof ConsolePlayer || !Settings.Chat.INTERACTIVE) {
            player.sendMessage(plotMessage.$(this).toOldMessageFormat());
        } else {
            plotMessage.$(this).send(((BukkitPlayer) player).player);
        }
    }

    @Override
    public void suggest(PlotMessage plotMessage, String command) {
        plotMessage.$(this).suggest(command);
    }

}
