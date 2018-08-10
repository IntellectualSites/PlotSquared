package com.plotsquared.sponge.util;

import com.intellectualcrafters.plot.config.Settings;
import com.intellectualcrafters.plot.object.ConsolePlayer;
import com.intellectualcrafters.plot.object.PlotMessage;
import com.intellectualcrafters.plot.object.PlotPlayer;
import com.intellectualcrafters.plot.util.ChatManager;
import com.plotsquared.sponge.object.SpongePlayer;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.action.TextActions;
import org.spongepowered.api.text.format.TextColor;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.format.TextStyle;
import org.spongepowered.api.text.format.TextStyles;

import java.util.List;

public class SpongeChatManager extends ChatManager<Text.Builder> {

    @Override public Text.Builder builder() {
        return Text.builder();
    }

    @Override public void color(PlotMessage message, String color) {
        TextColor tc = null;
        TextStyle ts = null;
        switch (color.charAt(1)) {
            case 'a':
                tc = TextColors.GREEN;
                break;
            case 'b':
                tc = TextColors.AQUA;
                break;
            case 'c':
                tc = TextColors.RED;
                break;
            case 'd':
                tc = TextColors.LIGHT_PURPLE;
                break;
            case 'e':
                tc = TextColors.YELLOW;
                break;
            case 'f':
                tc = TextColors.WHITE;
                break;
            case '1':
                tc = TextColors.DARK_BLUE;
                break;
            case '2':
                tc = TextColors.DARK_GREEN;
                break;
            case '3':
                tc = TextColors.DARK_AQUA;
                break;
            case '4':
                tc = TextColors.DARK_RED;
                break;
            case '5':
                tc = TextColors.DARK_PURPLE;
                break;
            case '6':
                tc = TextColors.GOLD;
                break;
            case '7':
                tc = TextColors.GRAY;
                break;
            case '8':
                tc = TextColors.DARK_GRAY;
                break;
            case '9':
                tc = TextColors.BLUE;
                break;
            case '0':
                tc = TextColors.BLACK;
                break;
            case 'k':
                ts = TextStyles.OBFUSCATED;
                break;
            case 'l':
                ts = TextStyles.BOLD;
                break;
            case 'm':
                ts = TextStyles.UNDERLINE;
                break;
            case 'n':
                ts = TextStyles.STRIKETHROUGH;
                break;
            case 'o':
                ts = TextStyles.ITALIC;
                break;
            case 'r':
                tc = TextColors.RESET;
                break;
        }
        if (tc != null) {
            apply(message, getChild(message).color(tc));
        }
        if (ts != null) {
            apply(message, getChild(message).style(ts));
        }
    }

    public Text.Builder getChild(PlotMessage m) {
        Text.Builder builder = m.$(this);
        List<Text> children = builder.getChildren();
        Text last = children.get(children.size() - 1);
        builder.remove(last);
        return Text.builder().append(last);
    }

    public void apply(PlotMessage m, Text.Builder builder) {
        m.$(this).append(builder.build());
    }

    @Override public void tooltip(PlotMessage message, PlotMessage... tooltips) {
        Text.Builder builder = Text.builder();
        boolean lb = false;
        for (PlotMessage tooltip : tooltips) {
            if (lb) {
                builder.append(Text.of("\n"));
            }
            builder.append(tooltip.$(this).build());
            lb = true;
        }
        apply(message, getChild(message).onHover(TextActions.showText(builder.toText())));
    }

    @Override public void command(PlotMessage message, String command) {
        apply(message, getChild(message).onClick(TextActions.runCommand(command)));
    }

    @Override public void text(PlotMessage message, String text) {
        message.$(this).append(SpongeUtil.getText(text));
    }

    @Override public void send(PlotMessage plotMessage, PlotPlayer player) {
        if (player instanceof ConsolePlayer || !Settings.Chat.INTERACTIVE) {
            player.sendMessage(plotMessage.$(this).build().toPlain());
        } else {
            ((SpongePlayer) player).player.sendMessage(plotMessage.$(this).build());
        }
    }

    @Override public void suggest(PlotMessage plotMessage, String command) {
        apply(plotMessage, getChild(plotMessage).onClick(TextActions.suggestCommand(command)));
    }
}
