package com.plotsquared.core.events;

import com.plotsquared.core.player.PlotPlayer;
import com.plotsquared.core.plot.Plot;

import java.util.Set;

/**
 * @since TODO
 */
public class PlayerPlotChatEvent extends PlotEvent {

    private final PlotPlayer<?> player;
    private final String message;
    private final Set<PlotPlayer<?>> recipients;
    private final Set<PlotPlayer<?>> spies;

    public PlayerPlotChatEvent(
            PlotPlayer<?> player,
            Plot plot,
            String message,
            Set<PlotPlayer<?>> recipients,
            Set<PlotPlayer<?>> spies
    ) {
        super(plot);
        this.player = player;
        this.message = message;
        this.recipients = recipients;
        this.spies = spies;
    }

    /**
     * The player that sent the message.
     *
     * @return PlotPlayer
     */
    public PlotPlayer<?> getPlayer() {
        return this.player;
    }

    /**
     * The message that was sent.
     *
     * @return String
     */
    public String getMessage() {
        return this.message;
    }

    /**
     * The message recipients.
     *
     * @return Set of PlotPlayer
     */
    public Set<PlotPlayer<?>> getRecipients() {
        return this.recipients;
    }

    /**
     * The message spies.
     *
     * @return Set of PlotPlayer
     */
    public Set<PlotPlayer<?>> getSpies() {
        return this.spies;
    }
}
