package com.plotsquared.bukkit.util;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.ChatColor;

import com.intellectualcrafters.plot.config.C;
import com.intellectualcrafters.plot.object.ConsolePlayer;
import com.intellectualcrafters.plot.object.PlotMessage;
import com.intellectualcrafters.plot.object.PlotPlayer;
import com.intellectualcrafters.plot.util.ChatManager;
import com.plotsquared.bukkit.chat.FancyMessage;
import com.plotsquared.bukkit.object.BukkitPlayer;

public class BukkitChatManager extends ChatManager<FancyMessage> {
    
    @Override
    public FancyMessage builder() {
        return new FancyMessage("");
    }
    
    @Override
    public void color(final PlotMessage m, final String color) {
        m.$(this).color(ChatColor.getByChar(C.color(color).substring(1)));
    }
    
    @Override
    public void tooltip(final PlotMessage m, final PlotMessage... tooltips) {
        final List<FancyMessage> lines = new ArrayList<>();
        for (final PlotMessage tooltip : tooltips) {
            lines.add(tooltip.$(this));
        }
        m.$(this).formattedTooltip(lines);
    }
    
    @Override
    public void command(final PlotMessage m, final String command) {
        m.$(this).command(command);
    }
    
    @Override
    public void text(final PlotMessage m, final String text) {
        m.$(this).then(ChatColor.stripColor(text));
    }
    
    @Override
    public void send(final PlotMessage m, final PlotPlayer player) {
        if (ConsolePlayer.isConsole(player)) {
            player.sendMessage(m.$(this).toOldMessageFormat());
        } else {
            m.$(this).send(((BukkitPlayer) player).player);
        }
    }
    
    @Override
    public void suggest(final PlotMessage m, final String command) {
        m.$(this).suggest(command);
    }
    
}
