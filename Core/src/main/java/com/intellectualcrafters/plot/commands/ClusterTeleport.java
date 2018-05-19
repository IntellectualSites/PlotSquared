package com.intellectualcrafters.plot.commands;

import com.intellectualcrafters.plot.config.C;
import com.intellectualcrafters.plot.object.PlotArea;
import com.intellectualcrafters.plot.object.PlotCluster;
import com.intellectualcrafters.plot.object.PlotPlayer;
import com.intellectualcrafters.plot.util.MainUtil;
import com.intellectualcrafters.plot.util.Permissions;
import com.plotsquared.general.commands.Command;
import com.plotsquared.general.commands.CommandDeclaration;
import java.util.UUID;

@CommandDeclaration(command = "tp",
        aliases = {"home","spawn"},
        category = CommandCategory.ADMINISTRATION,
        requiredType = RequiredType.NONE,
        permission = "plots.cluster.tp",
        usage = "/plot cluster tp <name>",
        description = "Teleport to a cluster")
public class ClusterTeleport extends SubCommand {

    public ClusterTeleport(Command parent, boolean isStatic) { super(parent, isStatic); }

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
        PlotCluster cluster = area.getCluster(args[0]);
        if (cluster == null) {
            MainUtil.sendMessage(player, C.INVALID_CLUSTER, args[0]);
            return false;
        }
        UUID uuid = player.getUUID();
        if (!cluster.isAdded(uuid)) {
            if (!Permissions.hasPermission(player, C.PERMISSION_CLUSTER_TP_OTHER)) {
                MainUtil.sendMessage(player, C.NO_PERMISSION, C.PERMISSION_CLUSTER_TP_OTHER);
                return false;
            }
        }
        player.teleport(cluster.getHome());
        return MainUtil.sendMessage(player, C.CLUSTER_TELEPORTING);
    }
}
