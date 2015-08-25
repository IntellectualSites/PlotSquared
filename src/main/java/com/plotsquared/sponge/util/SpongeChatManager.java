package com.plotsquared.sponge.util;


import org.spongepowered.api.text.TextBuilder;
import org.spongepowered.api.text.Texts;
import org.spongepowered.api.text.action.TextActions;

import com.intellectualcrafters.plot.object.ConsolePlayer;
import com.intellectualcrafters.plot.object.PlotMessage;
import com.intellectualcrafters.plot.object.PlotPlayer;
import com.intellectualcrafters.plot.util.ChatManager;
import com.plotsquared.sponge.object.SpongePlayer;

public class SpongeChatManager extends ChatManager<TextBuilder> {
    
    @Override
    public TextBuilder builder() {
        return Texts.builder();
    }
    
    @Override
    public void color(PlotMessage m, String color) {
        m.$(this).color(Texts.of(color).getColor());
    }
    
    @Override
    public void tooltip(PlotMessage m, PlotMessage... tooltips) {
        TextBuilder builder = Texts.builder();
        boolean lb = false;
        for (PlotMessage tooltip : tooltips) {
            if (lb) {
                builder.append(Texts.of("\n"));
            }
            builder.append(tooltip.$(this).build());
            lb = true;
        }
//        AchievementBuilder builder = SpongeMain.THIS.getGame().getRegistry().createAchievementBuilder();
        m.$(this).onHover(TextActions.showText(builder.toText()));
    }
    
    @Override
    public void command(PlotMessage m, String command) {
        m.$(this).onClick(TextActions.runCommand(command));
    }
    
    @Override
    public void text(PlotMessage m, String text) {
        m.$(this).append(Texts.of(text));
    }
    
    @Override
    public void send(PlotMessage m, PlotPlayer player) {
        if (ConsolePlayer.isConsole(player)) {
            player.sendMessage(Texts.legacy().to(m.$(this).build()));
        }
        else {
            ((SpongePlayer) player).player.sendMessage(m.$(this).build());
        }
    }
    
    @Override
    public void suggest(PlotMessage m, String command) {
        m.$(this).onClick(TextActions.suggestCommand(command));
    }
    
}
