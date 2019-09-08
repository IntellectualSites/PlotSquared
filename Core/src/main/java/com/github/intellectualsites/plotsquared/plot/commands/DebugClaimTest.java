package com.github.intellectualsites.plotsquared.plot.commands;

import com.github.intellectualsites.plotsquared.commands.CommandDeclaration;
import com.github.intellectualsites.plotsquared.plot.PlotSquared;
import com.github.intellectualsites.plotsquared.plot.config.Captions;
import com.github.intellectualsites.plotsquared.plot.database.DBFunc;
import com.github.intellectualsites.plotsquared.plot.object.*;
import com.github.intellectualsites.plotsquared.plot.util.ChunkManager;
import com.github.intellectualsites.plotsquared.plot.util.MainUtil;
import com.github.intellectualsites.plotsquared.plot.util.UUIDHandler;
import com.github.intellectualsites.plotsquared.plot.util.WorldUtil;
import com.google.common.collect.BiMap;

import java.util.ArrayList;
import java.util.Map;
import java.util.UUID;

@CommandDeclaration(command = "debugclaimtest", description =
    "If you accidentally delete your database, this command will attempt to restore all plots based on the data from plot signs. "
        + "Execution time may vary", category = CommandCategory.DEBUG,
    requiredType = RequiredType.CONSOLE, permission = "plots.debugclaimtest")
public class DebugClaimTest extends SubCommand {

    @Override public boolean onCommand(final PlotPlayer player, String[] args) {
        if (args.length < 3) {
            return !MainUtil.sendMessage(null,
                "If you accidentally delete your database, this command will attempt to restore all plots based on the data from the "
                    + "plot signs. \n\n&cMissing world arg /plot debugclaimtest {world} {PlotId min} {PlotId max}");
        }
        PlotArea area = PlotSquared.get().getPlotAreaByString(args[0]);
        if (area == null || !WorldUtil.IMP.isWorld(area.worldname)) {
            Captions.NOT_VALID_PLOT_WORLD.send(player, args[0]);
            return false;
        }
        PlotId min, max;
        try {
            args[1].split(";");
            args[2].split(";");
            min = PlotId.fromString(args[1]);
            max = PlotId.fromString(args[2]);
        } catch (Exception ignored) {
            return !MainUtil.sendMessage(player,
                "&cInvalid min/max values. &7The values are to Plot IDs in the format &cX;Y &7where X;Y are the plot coords\nThe conversion "
                    + "will only check the plots in the selected area.");
        }
        MainUtil.sendMessage(player,
            "&3Sign Block&8->&3Plot&8: &7Beginning sign to plot conversion. This may take a while...");
        MainUtil.sendMessage(player,
            "&3Sign Block&8->&3Plot&8: Found an excess of 250,000 chunks. Limiting search radius... (~3.8 min)");
        PlotManager manager = area.getPlotManager();
        ArrayList<Plot> plots = new ArrayList<>();
        for (PlotId id : MainUtil.getPlotSelectionIds(min, max)) {
            Plot plot = area.getPlotAbs(id);
            if (plot.hasOwner()) {
                MainUtil.sendMessage(player, " - &cDB Already contains: " + plot.getId());
                continue;
            }
            Location location = manager.getSignLoc(plot);
            ChunkLoc chunk = new ChunkLoc(location.getX() >> 4, location.getZ() >> 4);
            boolean result = ChunkManager.manager.loadChunk(area.worldname, chunk, false);
            if (!result) {
                continue;
            }
            String[] lines = WorldUtil.IMP.getSign(location);
            if (lines != null) {
                String line = lines[2];
                if (line != null && line.length() > 2) {
                    line = line.substring(2);
                    BiMap<StringWrapper, UUID> map = UUIDHandler.getUuidMap();
                    UUID uuid = map.get(new StringWrapper(line));
                    if (uuid == null) {
                        for (Map.Entry<StringWrapper, UUID> stringWrapperUUIDEntry : map
                            .entrySet()) {
                            if (stringWrapperUUIDEntry.getKey().value.toLowerCase()
                                .startsWith(line.toLowerCase())) {
                                uuid = stringWrapperUUIDEntry.getValue();
                                break;
                            }
                        }
                    }
                    if (uuid == null) {
                        uuid = UUIDHandler.getUUID(line, null);
                    }
                    if (uuid != null) {
                        MainUtil
                            .sendMessage(player, " - &aFound plot: " + plot.getId() + " : " + line);
                        plot.setOwner(uuid);
                        plots.add(plot);
                    } else {
                        MainUtil.sendMessage(player,
                            " - &cInvalid PlayerName: " + plot.getId() + " : " + line);
                    }
                }
            }
        }
        if (!plots.isEmpty()) {
            MainUtil.sendMessage(player,
                "&3Sign Block&8->&3Plot&8: &7Updating '" + plots.size() + "' plots!");
            DBFunc.createPlotsAndData(plots, new Runnable() {
                @Override public void run() {
                    MainUtil.sendMessage(player, "&6Database update finished!");
                }
            });
            for (Plot plot : plots) {
                plot.create();
            }
            MainUtil.sendMessage(player, "&3Sign Block&8->&3Plot&8: &7Complete!");
        } else {
            MainUtil.sendMessage(player, "No plots were found for the given search.");
        }
        return true;
    }
}
