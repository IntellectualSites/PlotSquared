package com.intellectualcrafters.plot.object;

import com.intellectualcrafters.plot.config.C;
import com.intellectualcrafters.plot.util.ChatManager;

public class PlotMessage {
    
    private Object builder;

    public PlotMessage() {
        this.builder = ChatManager.manager.builder();
    }
    
    public <T> T $(ChatManager<T> manager) {
        return (T) builder;
    }
    
    public PlotMessage(String text) {
        this();
        text(text);
    }
    
    public PlotMessage text(String text) {
        ChatManager.manager.text(this, text);
        return this;
    }
    
    public PlotMessage tooltip(PlotMessage... tooltip) {
        ChatManager.manager.tooltip(this, tooltip);
        return this;
    }
    
    public PlotMessage tooltip(String tooltip) {
        return tooltip(new PlotMessage(tooltip));
    }
    
    public PlotMessage command(String command) {
        ChatManager.manager.command(this, command);
        return this;
    }
    
    public PlotMessage suggest(String command) {
        ChatManager.manager.suggest(this, command);
        return this;
    }
    
    public PlotMessage color(String color) {
        ChatManager.manager.color(this, C.color(color));
        return this;
    }
    
    public void send(PlotPlayer player) {
        ChatManager.manager.send(this, player);
    }
}
