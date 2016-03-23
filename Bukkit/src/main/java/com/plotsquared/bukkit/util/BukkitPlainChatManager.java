package com.plotsquared.bukkit.util;

import com.intellectualcrafters.plot.object.PlotMessage;
import com.intellectualcrafters.plot.object.PlotPlayer;
import com.intellectualcrafters.plot.util.ChatManager;
import org.bukkit.ChatColor;

import java.util.ArrayList;
import java.util.List;

public class BukkitPlainChatManager extends ChatManager<List<StringBuilder>> {

    @Override
    public List<StringBuilder> builder() {
        return new ArrayList<>();
    }

    @Override
    public void color(PlotMessage m, String color) {
        List<StringBuilder> parts = m.$(this);
        parts.get(parts.size() - 1).insert(0, color);
    }

    @Override
    public void tooltip(PlotMessage m, PlotMessage... tooltips) {
    }

    @Override
    public void command(PlotMessage m, String command) {
    }

    @Override
    public void text(PlotMessage m, String text) {
        m.$(this).add(new StringBuilder(ChatColor.stripColor(text)));
    }

    @Override
    public void send(PlotMessage m, PlotPlayer player) {
        StringBuilder built = new StringBuilder();
        for (StringBuilder sb : m.$(this)) {
            built.append(sb);
        }
        player.sendMessage(built.toString());
    }

    @Override
    public void suggest(PlotMessage m, String command) {
    }

}
