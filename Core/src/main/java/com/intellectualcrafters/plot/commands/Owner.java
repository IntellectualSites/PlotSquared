package com.intellectualcrafters.plot.commands;

import com.intellectualcrafters.plot.config.C;
import com.intellectualcrafters.plot.config.Settings;
import com.intellectualcrafters.plot.object.Plot;
import com.intellectualcrafters.plot.object.PlotPlayer;
import com.intellectualcrafters.plot.util.CmdConfirm;
import com.intellectualcrafters.plot.util.MainUtil;
import com.intellectualcrafters.plot.util.Permissions;
import com.intellectualcrafters.plot.util.TaskManager;
import com.intellectualcrafters.plot.util.UUIDHandler;
import com.plotsquared.general.commands.CommandDeclaration;

import java.util.HashSet;
import java.util.UUID;

@CommandDeclaration(
        command = "setowner",
        permission = "plots.set.owner",
        description = "Set the plot owner",
        usage = "/plot setowner <player>",
        aliases = {"owner", "so", "seto"},
        category = CommandCategory.CLAIMING,
        requiredType = RequiredType.NONE,
        confirmation = true)
public class Owner extends SetCommand {

    @Override
    public boolean set(final PlotPlayer plr, final Plot plot, String value) {
        HashSet<Plot> plots = plot.getConnectedPlots();
        UUID uuid = null;
        String name = null;
        if (value.length() == 36) {
            try {
                uuid = UUID.fromString(value);
                name = MainUtil.getName(uuid);
            } catch (Exception e) {
            }
        } else {
            uuid = UUIDHandler.getUUID(value, null);
            name = UUIDHandler.getName(uuid);
            name = name == null ? value : name;
        }
        if (uuid == null) {
            if (value.equalsIgnoreCase("none")) {
                HashSet<Plot> connected = plot.getConnectedPlots();
                plot.unlinkPlot(false, false);
                for (Plot current : connected) {
                    current.unclaim();
                    current.removeSign();
                }
                MainUtil.sendMessage(plr, C.SET_OWNER);
                return true;
            }
            C.INVALID_PLAYER.send(plr, value);
            return false;
        }
        if (plot.isOwner(uuid)) {
            C.ALREADY_OWNER.send(plr);
            return false;
        }
        final PlotPlayer other = UUIDHandler.getPlayer(uuid);
        if (!Permissions.hasPermission(plr, "plots.admin.command.setowner")) {
            if (other == null) {
                C.INVALID_PLAYER_OFFLINE.send(plr, value);
                return false;
            }
            int size = plots.size();
            int currentPlots = (Settings.GLOBAL_LIMIT ? other.getPlotCount() : other.getPlotCount(plot.getArea().worldname)) + size;
            if (currentPlots > other.getAllowedPlots()) {
                sendMessage(plr, C.CANT_TRANSFER_MORE_PLOTS);
                return false;
            }
        }
        final String finalName = name;
        final UUID finalUUID = uuid;
        Runnable run = new Runnable() {
            @Override
            public void run() {
                plot.setOwner(finalUUID);
                plot.setSign(finalName);
                MainUtil.sendMessage(plr, C.SET_OWNER);
                if (other != null) {
                    MainUtil.sendMessage(other, C.NOW_OWNER, plot.getArea() + ";" + plot.getId());
                }
            }
        };
        if (hasConfirmation(plr)) {
            CmdConfirm.addPending(plr, "/plot set owner " + value, run);
        } else {
            TaskManager.runTask(run);
        }
        return true;
    }
}
