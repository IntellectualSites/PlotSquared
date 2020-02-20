package com.github.intellectualsites.plotsquared.plot.config;

import com.github.intellectualsites.plotsquared.plot.object.PlotPlayer;

public class CaptionUtility {

    public static String formatRaw(PlotPlayer recipient, String message, Object... args) {
        final ChatFormatter.ChatContext chatContext = new ChatFormatter.ChatContext(recipient, message, args, true);
        for (final ChatFormatter chatFormatter : ChatFormatter.formatters) {
            chatFormatter.format(chatContext);
        }
        return chatContext.getMessage();
    }

    public static String format(PlotPlayer recipient, String message, Object... args) {
        final ChatFormatter.ChatContext chatContext = new ChatFormatter.ChatContext(recipient, message, args, false);
        for (final ChatFormatter chatFormatter : ChatFormatter.formatters) {
            chatFormatter.format(chatContext);
        }
        return chatContext.getMessage();
    }

    public static String format(PlotPlayer recipient, Caption caption, Object... args) {
        if (caption.usePrefix() && caption.getTranslated().length() > 0) {
            return Captions.PREFIX.getTranslated() + format(recipient, caption.getTranslated(), args);
        } else {
            return format(recipient, caption.getTranslated(), args);
        }
    }

}
