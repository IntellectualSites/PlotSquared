package com.plotsquared.core.util;

import com.plotsquared.core.plot.message.PlotMessage;
import com.plotsquared.core.player.PlotPlayer;

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
