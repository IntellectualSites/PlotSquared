package com.github.intellectualsites.plotsquared.plot.config;

import com.github.intellectualsites.plotsquared.plot.object.PlotPlayer;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

@FunctionalInterface public interface ChatFormatter {

    Collection<ChatFormatter> formatters = new ArrayList<>(Collections.singletonList(new PlotSquaredChatFormatter()));

    void format(ChatContext context);

    @AllArgsConstructor final class ChatContext {

        @Getter private final PlotPlayer recipient;
        @Getter @Setter private String message;
        @Getter private final Object[] args;
        @Getter private final boolean rawOutput;

    }

}
