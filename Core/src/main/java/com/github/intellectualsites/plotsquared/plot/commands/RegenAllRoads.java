package com.github.intellectualsites.plotsquared.plot.commands;

import com.github.intellectualsites.plotsquared.commands.CommandDeclaration;
import com.github.intellectualsites.plotsquared.plot.PlotSquared;
import com.github.intellectualsites.plotsquared.plot.config.Captions;
import com.github.intellectualsites.plotsquared.plot.generator.HybridPlotManager;
import com.github.intellectualsites.plotsquared.plot.generator.HybridUtils;
import com.github.intellectualsites.plotsquared.plot.object.PlotArea;
import com.github.intellectualsites.plotsquared.plot.object.PlotManager;
import com.github.intellectualsites.plotsquared.plot.object.PlotPlayer;
import com.github.intellectualsites.plotsquared.plot.util.MainUtil;

@CommandDeclaration(command = "regenallroads",
    description = "Regenerate all roads in the map using the set road schematic",
    aliases = {"rgar"}, usage = "/plot regenallroads <world> [height]",
    category = CommandCategory.ADMINISTRATION, requiredType = RequiredType.CONSOLE,
    permission = "plots.regenallroads") public class RegenAllRoads extends SubCommand {

    @Override public boolean onCommand(PlotPlayer player, String[] args) {
        int height = 0;
        if (args.length == 2) {
            try {
                height = Integer.parseInt(args[1]);
            } catch (NumberFormatException ignored) {
                MainUtil.sendMessage(player, Captions.NOT_VALID_NUMBER, "(0, 256)");
                MainUtil.sendMessage(player, Captions.COMMAND_SYNTAX,
                    "/plot regenallroads <world> [height]");
                return false;
            }
        } else if (args.length != 1) {
            MainUtil.sendMessage(player, Captions.COMMAND_SYNTAX,
                "/plot regenallroads <world> [height]");
            return false;
        }
        PlotArea area = PlotSquared.get().getPlotAreaByString(args[0]);
        if (area == null) {
            Captions.NOT_VALID_PLOT_WORLD.send(player, args[0]);
            return false;
        }
        String name = args[0];
        PlotManager manager = area.getPlotManager();
        if (!(manager instanceof HybridPlotManager)) {
            MainUtil.sendMessage(player, Captions.NOT_VALID_PLOT_WORLD);
            return false;
        }
        //Set<BlockVector2> chunks = ChunkManager.manager.getChunkChunks(name);
        MainUtil
            .sendMessage(player, "&cIf no schematic is set, the following will not do anything");
        MainUtil.sendMessage(player,
            "&7 - To set a schematic, stand in a plot and use &c/plot createroadschematic");
        //MainUtil.sendMessage(player, "&6Potential chunks to update: &7" + (chunks.size() * 1024));
        //MainUtil.sendMessage(player, "&6Estimated time: &7" + chunks.size() + " seconds");
        boolean result = HybridUtils.manager.scheduleRoadUpdate(area, height);
        if (!result) {
            MainUtil.sendMessage(player,
                "&cCannot schedule mass schematic update! (Is one already in progress?)");
            return false;
        }
        return true;
    }
}
