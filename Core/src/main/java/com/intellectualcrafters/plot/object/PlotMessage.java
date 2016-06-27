package com.intellectualcrafters.plot.object;

import com.intellectualcrafters.plot.PS;
import com.intellectualcrafters.plot.config.C;
import com.intellectualcrafters.plot.object.chat.PlainChatManager;
import com.intellectualcrafters.plot.util.ChatManager;

public class PlotMessage {
    
    private Object builder;
    
    public PlotMessage() {
        try {
            reset(ChatManager.manager);
        } catch (Throwable e) {
            PS.debug("PlotSquared doesn't support fancy chat for " + PS.get().IMP.getServerVersion());
            ChatManager.manager = new PlainChatManager();
        reset(ChatManager.manager);
    }
    }

    public PlotMessage(String text) {
        this();
        text(text);
    }

    public <T> T $(ChatManager<T> manager) {
        return (T) this.builder;
    }

    public <T> T reset(ChatManager<T> manager) {
        return (T) (this.builder = manager.builder());
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
