package com.plotsquared.bukkit.util;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.ChatColor;

import com.intellectualcrafters.plot.object.PlotMessage;
import com.intellectualcrafters.plot.object.PlotPlayer;
import com.intellectualcrafters.plot.util.ChatManager;

public class BukkitPlainChatManager extends ChatManager<List<StringBuilder>>
{

    @Override
    public List<StringBuilder> builder()
    {
        return new ArrayList<StringBuilder>();
    }

    @Override
    public void color(final PlotMessage m, final String color)
    {
        final List<StringBuilder> parts = m.$(this);
        parts.get(parts.size() - 1).insert(0, color);
    }

    @Override
    public void tooltip(final PlotMessage m, final PlotMessage... tooltips)
    {}

    @Override
    public void command(final PlotMessage m, final String command)
    {}

    @Override
    public void text(final PlotMessage m, final String text)
    {
        m.$(this).add(new StringBuilder(ChatColor.stripColor(text)));
    }

    @Override
    public void send(final PlotMessage m, final PlotPlayer player)
    {
        final StringBuilder built = new StringBuilder();
        for (final StringBuilder sb : m.$(this))
        {
            built.append(sb);
        }
        player.sendMessage(built.toString());
    }

    @Override
    public void suggest(final PlotMessage m, final String command)
    {}

}
