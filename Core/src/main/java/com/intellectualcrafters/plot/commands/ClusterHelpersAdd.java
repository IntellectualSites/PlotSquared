package com.intellectualcrafters.plot.commands;

import com.intellectualcrafters.plot.config.C;
import com.intellectualcrafters.plot.database.DBFunc;
import com.intellectualcrafters.plot.object.PlotArea;
import com.intellectualcrafters.plot.object.PlotCluster;
import com.intellectualcrafters.plot.object.PlotPlayer;
import com.intellectualcrafters.plot.util.MainUtil;
import com.intellectualcrafters.plot.util.UUIDHandler;
import com.plotsquared.general.commands.Command;
import com.plotsquared.general.commands.CommandDeclaration;
import java.util.UUID;

@CommandDeclaration(command = "add",
        category = CommandCategory.ADMINISTRATION,
        requiredType = RequiredType.NONE,
        permission = "plots.cluster.helpers",
        usage = "/plot cluster helpers add <player>",
        description = "Add helper to plot cluster")
public class ClusterHelpersAdd extends SubCommand {

    public ClusterHelpersAdd(Command parent, boolean isStatic) { super(parent, isStatic); }

    @Override
    public boolean onCommand(final PlotPlayer player, String[] args) {
        if (args.length != 1) {
            MainUtil.sendMessage(player, C.COMMAND_SYNTAX, getUsage());
            return false;
        }
        PlotArea area = player.getApplicablePlotArea();
        if (area == null) {
            C.NOT_IN_PLOT_WORLD.send(player);
            return false;
        }
        PlotCluster cluster = area.getCluster(player.getLocation());
        if (cluster == null) {
            MainUtil.sendMessage(player, C.NOT_IN_CLUSTER);
            return false;
        }
        UUID uuid = UUIDHandler.getUUID(args[0], null);
        if (uuid == null) {
            MainUtil.sendMessage(player, C.INVALID_PLAYER, args[0]);
            return false;
        }

        cluster.helpers.add(uuid);
        DBFunc.setHelper(cluster, uuid);
        return MainUtil.sendMessage(player, C.CLUSTER_ADDED_HELPER);
    }
}
