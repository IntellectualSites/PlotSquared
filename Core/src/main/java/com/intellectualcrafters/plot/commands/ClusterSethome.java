package com.intellectualcrafters.plot.commands;

import com.intellectualcrafters.plot.config.C;
import com.intellectualcrafters.plot.database.DBFunc;
import com.intellectualcrafters.plot.object.BlockLoc;
import com.intellectualcrafters.plot.object.Location;
import com.intellectualcrafters.plot.object.PlotArea;
import com.intellectualcrafters.plot.object.PlotCluster;
import com.intellectualcrafters.plot.object.PlotPlayer;
import com.intellectualcrafters.plot.util.MainUtil;
import com.intellectualcrafters.plot.util.Permissions;
import com.plotsquared.general.commands.Command;
import com.plotsquared.general.commands.CommandDeclaration;

@CommandDeclaration(command = "sethome",
        permission = "plots.cluster.sethome",
        category = CommandCategory.ADMINISTRATION,
        requiredType = RequiredType.NONE,
        description = "Set plot cluster home",
        aliases = {"setspawn","sh"},
        usage = "/plot cluster sethome",
        confirmation = true)
public class ClusterSethome extends SubCommand {

    public ClusterSethome(Command parent, boolean isStatic) {
        super(parent, isStatic);
    }

    @Override
    public boolean onCommand(final PlotPlayer player, String[] args) {
        PlotArea area = player.getApplicablePlotArea();
        if (area == null) {
            C.NOT_IN_PLOT_WORLD.send(player);
        }
        PlotCluster cluster = area.getCluster(player.getLocation());
        if (cluster == null) {
            MainUtil.sendMessage(player, C.NOT_IN_CLUSTER);
            return false;
        }
        if (!cluster.hasHelperRights(player.getUUID())) {
            if (!Permissions.hasPermission(player, C.PERMISSION_CLUSTER_SETHOME_OTHER)) {
                MainUtil.sendMessage(player, C.NO_PERMISSION, C.PERMISSION_CLUSTER_SETHOME_OTHER);
                return false;
            }
        }
        Location base = cluster.getClusterBottom();
        Location relative = player.getLocation().subtract(base.getX(), 0, base.getZ());
        BlockLoc blockloc = new BlockLoc(relative.getX(), relative.getY(), relative.getZ());
        cluster.settings.setPosition(blockloc);
        DBFunc.setPosition(cluster, relative.getX() + "," + relative.getY() + "," + relative.getZ());
        return MainUtil.sendMessage(player, C.POSITION_SET);
    }
}

