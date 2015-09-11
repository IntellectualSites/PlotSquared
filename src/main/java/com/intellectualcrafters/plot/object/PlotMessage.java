package com.intellectualcrafters.plot.object;

import com.intellectualcrafters.plot.config.C;
import com.intellectualcrafters.plot.util.ChatManager;

public class PlotMessage
{

    private final Object builder;

    public PlotMessage()
    {
        builder = ChatManager.manager.builder();
    }

    public <T> T $(final ChatManager<T> manager)
    {
        return (T) builder;
    }

    public PlotMessage(final String text)
    {
        this();
        text(text);
    }

    public PlotMessage text(final String text)
    {
        ChatManager.manager.text(this, text);
        return this;
    }

    public PlotMessage tooltip(final PlotMessage... tooltip)
    {
        ChatManager.manager.tooltip(this, tooltip);
        return this;
    }

    public PlotMessage tooltip(final String tooltip)
    {
        return tooltip(new PlotMessage(tooltip));
    }

    public PlotMessage command(final String command)
    {
        ChatManager.manager.command(this, command);
        return this;
    }

    public PlotMessage suggest(final String command)
    {
        ChatManager.manager.suggest(this, command);
        return this;
    }

    public PlotMessage color(final String color)
    {
        ChatManager.manager.color(this, C.color(color));
        return this;
    }

    public void send(final PlotPlayer player)
    {
        ChatManager.manager.send(this, player);
    }
}
