package com.plotsquared.util;

import com.plotsquared.plot.message.PlotMessage;
import com.plotsquared.player.PlotPlayer;

public abstract class ChatManager<T> {
    public static ChatManager<?> manager;

    public abstract T builder();

    public abstract void color(PlotMessage message, String color);

    public abstract void tooltip(PlotMessage message, PlotMessage... tooltip);

    public abstract void command(PlotMessage message, String command);

    public abstract void text(PlotMessage message, String text);

    public abstract void send(PlotMessage plotMessage, PlotPlayer player);

    public abstract void suggest(PlotMessage plotMessage, String command);
}
