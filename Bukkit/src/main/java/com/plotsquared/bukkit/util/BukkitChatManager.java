package com.plotsquared.bukkit.util;

import com.intellectualcrafters.plot.config.C;
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
    public void color(PlotMessage m, String color) {
        m.$(this).color(ChatColor.getByChar(C.color(color).substring(1)));
    }

    @Override
    public void tooltip(PlotMessage m, PlotMessage... tooltips) {
        List<FancyMessage> lines = new ArrayList<>();
        for (PlotMessage tooltip : tooltips) {
            lines.add(tooltip.$(this));
        }
        m.$(this).formattedTooltip(lines);
    }

    @Override
    public void command(PlotMessage m, String command) {
        m.$(this).command(command);
    }

    @Override
    public void text(PlotMessage m, String text) {
        m.$(this).then(ChatColor.stripColor(text));
    }

    @Override
    public void send(PlotMessage m, PlotPlayer player) {
        if (player instanceof ConsolePlayer) {
            player.sendMessage(m.$(this).toOldMessageFormat());
        } else {
            m.$(this).send(((BukkitPlayer) player).player);
        }
    }

    @Override
    public void suggest(PlotMessage m, String command) {
        m.$(this).suggest(command);
    }

}
