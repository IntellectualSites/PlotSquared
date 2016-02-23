package com.intellectualcrafters.plot.util;

import com.intellectualcrafters.plot.object.PlotMessage;
import com.intellectualcrafters.plot.object.PlotPlayer;

public abstract class ChatManager<T> {
    public static ChatManager<?> manager;
    
    public abstract T builder();
    
    public abstract void color(final PlotMessage message, final String color);
    
    public abstract void tooltip(final PlotMessage message, final PlotMessage... tooltip);
    
    public abstract void command(final PlotMessage message, final String command);
    
    public abstract void text(final PlotMessage message, final String text);
    
    public abstract void send(final PlotMessage plotMessage, final PlotPlayer player);
    
    public abstract void suggest(final PlotMessage plotMessage, final String command);
}
