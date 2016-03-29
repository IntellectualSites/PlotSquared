package com.intellectualcrafters.plot.commands;

import com.intellectualcrafters.plot.PS;
import com.intellectualcrafters.plot.config.C;
import com.intellectualcrafters.plot.generator.HybridPlotManager;
import com.intellectualcrafters.plot.generator.HybridUtils;
import com.intellectualcrafters.plot.object.ChunkLoc;
import com.intellectualcrafters.plot.object.PlotArea;
import com.intellectualcrafters.plot.object.PlotManager;
import com.intellectualcrafters.plot.object.PlotPlayer;
import com.intellectualcrafters.plot.util.ChunkManager;
import com.intellectualcrafters.plot.util.MainUtil;
import com.intellectualcrafters.plot.util.WorldUtil;
import com.plotsquared.general.commands.CommandDeclaration;

import java.util.Set;

@CommandDeclaration(
        command = "regenallroads",
        description = "Regenerate all roads in the map using the set road schematic",
        aliases = {"rgar"},
        usage = "/plot regenallroads <world> [height]",
        category = CommandCategory.ADMINISTRATION,
        requiredType = RequiredType.CONSOLE,
        permission = "plots.regenallroads")
public class RegenAllRoads extends SubCommand {

    @Override
    public boolean onCommand(PlotPlayer plr, String[] args) {
        int height = 0;
        if (args.length == 2) {
            try {
                height = Integer.parseInt(args[1]);
            } catch (NumberFormatException e) {
                MainUtil.sendMessage(plr, C.NOT_VALID_NUMBER, "(0, 256)");
                MainUtil.sendMessage(plr, C.COMMAND_SYNTAX, "/plot regenallroads <world> [height]");
                return false;
            }
        } else if (args.length != 1) {
            MainUtil.sendMessage(plr, C.COMMAND_SYNTAX, "/plot regenallroads <world> [height]");
            return false;
        }
        PlotArea area = PS.get().getPlotAreaByString(args[0]);
        if (area == null || !WorldUtil.IMP.isWorld(area.worldname)) {
            C.NOT_VALID_PLOT_WORLD.send(plr, args[0]);
            return false;
        }
        String name = args[0];
        PlotManager manager = area.getPlotManager();
        if ((manager == null) || !(manager instanceof HybridPlotManager)) {
            MainUtil.sendMessage(plr, C.NOT_VALID_PLOT_WORLD);
            return false;
        }
        Set<ChunkLoc> chunks = ChunkManager.manager.getChunkChunks(name);
        MainUtil.sendMessage(plr, "&cIf no schematic is set, the following will not do anything");
        MainUtil.sendMessage(plr, "&7 - To set a schematic, stand in a plot and use &c/plot createroadschematic");
        MainUtil.sendMessage(plr, "&6Potential chunks to update: &7" + (chunks.size() * 1024));
        MainUtil.sendMessage(plr, "&6Estimated time: &7" + chunks.size() + " seconds");
        boolean result = HybridUtils.manager.scheduleRoadUpdate(area, height);
        if (!result) {
            MainUtil.sendMessage(plr, "&cCannot schedule mass schematic update! (Is one already in progress?)");
            return false;
        }
        return true;
    }
}
