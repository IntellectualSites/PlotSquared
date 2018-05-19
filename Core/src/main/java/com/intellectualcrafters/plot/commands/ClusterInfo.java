package com.intellectualcrafters.plot.commands;

import com.intellectualcrafters.plot.config.C;
import com.intellectualcrafters.plot.object.PlotArea;
import com.intellectualcrafters.plot.object.PlotCluster;
import com.intellectualcrafters.plot.object.PlotPlayer;
import com.intellectualcrafters.plot.util.MainUtil;
import com.intellectualcrafters.plot.util.UUIDHandler;
import com.plotsquared.general.commands.Command;
import com.plotsquared.general.commands.CommandDeclaration;

@CommandDeclaration(command = "info",
        aliases = {"i","show","information"},
        category = CommandCategory.ADMINISTRATION,
        requiredType = RequiredType.NONE,
        permission = "plots.cluster.info",
        usage = "/plot cluster info [name]",
        description = "Get plot cluster information")
public class ClusterInfo extends SubCommand {

    public ClusterInfo(Command parent, boolean isStatic) { super(parent, isStatic); }

    @Override
    public boolean onCommand(final PlotPlayer player, String[] args) {
        if (args.length > 1) {
            MainUtil.sendMessage(player, C.COMMAND_SYNTAX, getUsage());
            return false;
        }
        PlotArea area = player.getApplicablePlotArea();
        if (area == null) {
            C.NOT_IN_PLOT_WORLD.send(player);
            return false;
        }
        PlotCluster cluster;
        if (args.length == 1) {
            cluster = area.getCluster(args[0]);
            if (cluster == null) {
                MainUtil.sendMessage(player, C.INVALID_CLUSTER, args[0]);
                return false;
            }
        } else {
            cluster = area.getCluster(player.getLocation());
            if (cluster == null) {
                MainUtil.sendMessage(player, C.NOT_IN_CLUSTER);
                return false;
            }
        }
        String id = cluster.toString();
        String owner = UUIDHandler.getName(cluster.owner);
        if (owner == null) {
            owner = "unknown";
        }
        String name = cluster.getName();
        String size = (cluster.getP2().x - cluster.getP1().x + 1) + "x" + (cluster.getP2().y - cluster.getP1().y + 1);
        String rights = cluster.isAdded(player.getUUID()) + "";
        String message = C.CLUSTER_INFO.s();
        message = message.replaceAll("%id%", id);
        message = message.replaceAll("%owner%", owner);
        message = message.replaceAll("%name%", name);
        message = message.replaceAll("%size%", size);
        message = message.replaceAll("%rights%", rights);
        MainUtil.sendMessage(player, message);
        return true;
    }
}

