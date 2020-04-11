package com.plotsquared.util.tasks;

import com.plotsquared.PlotSquared;
import com.plotsquared.commands.Auto;
import com.plotsquared.config.Captions;
import com.plotsquared.database.DBFunc;
import com.plotsquared.events.PlotMergeEvent;
import com.plotsquared.events.Result;
import com.plotsquared.location.Direction;
import com.plotsquared.plot.Plot;
import com.plotsquared.plot.PlotArea;
import com.plotsquared.player.PlotPlayer;
import lombok.RequiredArgsConstructor;

import static com.plotsquared.util.MainUtil.sendMessage;

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
