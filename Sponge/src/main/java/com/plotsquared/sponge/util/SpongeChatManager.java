package com.plotsquared.sponge.util;

import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.action.TextActions;
import org.spongepowered.api.text.serializer.TextSerializers;

import com.intellectualcrafters.plot.object.ConsolePlayer;
import com.intellectualcrafters.plot.object.PlotMessage;
import com.intellectualcrafters.plot.object.PlotPlayer;
import com.intellectualcrafters.plot.util.ChatManager;
import com.plotsquared.sponge.object.SpongePlayer;

public class SpongeChatManager extends ChatManager<Text.Builder> {
    
    @Override
    public Text.Builder builder() {
        return Text.builder();
    }
    
    @Override
    public void color(final PlotMessage m, final String color) {
        m.$(this).color(TextSerializers.LEGACY_FORMATTING_CODE.deserialize(color).getColor());
    }
    
    @Override
    public void tooltip(final PlotMessage m, final PlotMessage... tooltips) {
        final Text.Builder builder = Text.builder();
        boolean lb = false;
        for (final PlotMessage tooltip : tooltips) {
            if (lb) {
                builder.append(Text.of("\n"));
            }
            builder.append(tooltip.$(this).build());
            lb = true;
        }
        //        AchievementBuilder builder = SpongeMain.THIS.getGame().getRegistry().createAchievementBuilder();
        m.$(this).onHover(TextActions.showText(builder.toText()));
    }
    
    @Override
    public void command(final PlotMessage m, final String command) {
        m.$(this).onClick(TextActions.runCommand(command));
    }
    
    @Override
    public void text(final PlotMessage m, final String text) {
        m.$(this).append(Text.of(text));
    }
    
    @Override
    public void send(final PlotMessage m, final PlotPlayer player) {
        if (ConsolePlayer.isConsole(player)) {
            player.sendMessage(m.$(this).build().toPlain());
        } else {
            ((SpongePlayer) player).player.sendMessage(m.$(this).build());
        }
    }
    
    @Override
    public void suggest(final PlotMessage m, final String command) {
        m.$(this).onClick(TextActions.suggestCommand(command));
    }
    
}
