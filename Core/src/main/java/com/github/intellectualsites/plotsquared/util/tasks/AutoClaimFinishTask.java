package com.github.intellectualsites.plotsquared.util.tasks;

import com.github.intellectualsites.plotsquared.PlotSquared;
import com.github.intellectualsites.plotsquared.commands.Auto;
import com.github.intellectualsites.plotsquared.plot.config.Captions;
import com.github.intellectualsites.plotsquared.database.DBFunc;
import com.github.intellectualsites.plotsquared.events.PlotMergeEvent;
import com.github.intellectualsites.plotsquared.events.Result;
import com.github.intellectualsites.plotsquared.plot.object.Direction;
import com.github.intellectualsites.plotsquared.plot.object.Plot;
import com.github.intellectualsites.plotsquared.plot.object.PlotArea;
import com.github.intellectualsites.plotsquared.player.PlotPlayer;
import lombok.RequiredArgsConstructor;

import static com.github.intellectualsites.plotsquared.util.MainUtil.sendMessage;

@RequiredArgsConstructor public final class AutoClaimFinishTask extends RunnableVal<Object> {

    private final PlotPlayer player;
    private final Plot plot;
    private final PlotArea area;
    private final int allowedPlots;
    private final String schematic;

    @Override public void run(Object value) {
        player.deleteMeta(Auto.class.getName());
        if (plot == null) {
            sendMessage(player, Captions.NO_FREE_PLOTS);
            return;
        }

        if (Auto.checkAllowedPlots(player, area, allowedPlots, 1, 1)) {
            plot.claim(player, true, schematic, false);
            if (area.isAutoMerge()) {
                PlotMergeEvent event = PlotSquared.get().getEventDispatcher()
                    .callMerge(plot, Direction.ALL, Integer.MAX_VALUE, player);
                if (event.getEventResult() == Result.DENY) {
                    sendMessage(player, Captions.EVENT_DENIED, "Auto merge");
                } else {
                    plot.autoMerge(event.getDir(), event.getMax(), player.getUUID(),
                        true);
                }
            }
        } else {
            DBFunc.delete(plot);
        }
    }

}
