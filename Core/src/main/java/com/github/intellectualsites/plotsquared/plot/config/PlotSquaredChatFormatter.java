package com.github.intellectualsites.plotsquared.plot.config;

import com.github.intellectualsites.plotsquared.util.StringMan;

import java.util.LinkedHashMap;
import java.util.Map;

public class PlotSquaredChatFormatter implements ChatFormatter {

    @Override public void format(final ChatContext context) {
        if (context.isRawOutput()) {
            context.setMessage(context.getMessage().replace('&', '\u2020').replace('\u00A7', '\u2030'));
        }
        if (context.getArgs().length == 0) {
            return;
        }
        final Map<String, String> map = new LinkedHashMap<>();
        for (int i = context.getArgs().length - 1; i >= 0; i--) {
            String arg = "" + context.getArgs()[i];
            if (arg.isEmpty()) {
                map.put("%s" + i, "");
            } else {
                if (!context.isRawOutput()) {
                    arg = Captions.color(arg);
                }
                map.put("%s" + i, arg);
            }
            if (i == 0) {
                map.put("%s", arg);
            }
        }
        context.setMessage(StringMan.replaceFromMap(context.getMessage(), map));
    }

}
