package com.github.intellectualsites.plotsquared.plot.commands;

import com.github.intellectualsites.plotsquared.commands.CommandDeclaration;
import com.github.intellectualsites.plotsquared.plot.config.Captions;
import com.github.intellectualsites.plotsquared.plot.config.Settings;
import com.github.intellectualsites.plotsquared.plot.object.Plot;
import com.github.intellectualsites.plotsquared.plot.object.PlotPlayer;
import com.github.intellectualsites.plotsquared.plot.util.CmdConfirm;
import com.github.intellectualsites.plotsquared.plot.util.MainUtil;
import com.github.intellectualsites.plotsquared.plot.util.Permissions;
import com.github.intellectualsites.plotsquared.plot.util.TaskManager;
import com.github.intellectualsites.plotsquared.plot.util.UUIDHandler;

import java.util.Set;
import java.util.UUID;

@CommandDeclaration(command = "setowner",
    permission = "plots.set.owner",
    description = "Set the plot owner",
    usage = "/plot setowner <player>",
    aliases = {"owner", "so", "seto"},
    category = CommandCategory.CLAIMING,
    requiredType = RequiredType.NONE,
    confirmation = true)
public class Owner extends SetCommand {

    @Override public boolean set(final PlotPlayer player, final Plot plot, String value) {
        if (value == null || value.isEmpty()) {
            Captions.SET_OWNER_MISSING_PLAYER.send(player);
            return false;
        }
        Set<Plot> plots = plot.getConnectedPlots();
        UUID uuid = null;
        String name = null;
        if (value.length() == 36) {
            try {
                uuid = UUID.fromString(value);
                name = MainUtil.getName(uuid);
            } catch (Exception ignored) {
            }
        } else {
            uuid = UUIDHandler.getUUID(value, null);
            name = uuid == null ? value : UUIDHandler.getName(uuid);
            name = name == null ? value : name;
        }
        if (uuid == null || value.equalsIgnoreCase("-")) {
            if (value.equalsIgnoreCase("none") || value.equalsIgnoreCase("null") || value
                .equalsIgnoreCase("-")) {
                if (!Permissions.hasPermission(player,
                    Captions.PERMISSION_ADMIN_COMMAND_SET_OWNER.getTranslated(), true)) {
                    return false;
                }
                Set<Plot> connected = plot.getConnectedPlots();
                plot.unlinkPlot(false, false);
                for (Plot current : connected) {
                    current.unclaim();
                    current.removeSign();
                }
                MainUtil.sendMessage(player, Captions.SET_OWNER);
                return true;
            }
            Captions.INVALID_PLAYER.send(player, value);
            return false;
        }
        final PlotPlayer other = UUIDHandler.getPlayer(uuid);
        if (plot.isOwner(uuid)) {
            Captions.ALREADY_OWNER.send(player, MainUtil.getName(uuid));
            return false;
        }
        if (!Permissions.hasPermission(player, Captions.PERMISSION_ADMIN_COMMAND_SET_OWNER)) {
            if (other == null) {
                Captions.INVALID_PLAYER_OFFLINE.send(player, value);
                return false;
            }
            int size = plots.size();
            int currentPlots = (Settings.Limit.GLOBAL ?
                other.getPlotCount() :
                other.getPlotCount(plot.getWorldName())) + size;
            if (currentPlots > other.getAllowedPlots()) {
                sendMessage(player, Captions.CANT_TRANSFER_MORE_PLOTS);
                return false;
            }
        }
        final String finalName = name;
        final UUID finalUUID = uuid;
        final boolean removeDenied = plot.isDenied(finalUUID);
        Runnable run = () -> {
            if (plot.setOwner(finalUUID, player)) {
                if (removeDenied)
                    plot.removeDenied(finalUUID);
                plot.setSign(finalName);
                MainUtil.sendMessage(player, Captions.SET_OWNER);
                if (other != null) {
                    MainUtil.sendMessage(other, Captions.NOW_OWNER,
                        plot.getArea() + ";" + plot.getId());
                }
            } else {
                MainUtil.sendMessage(player, Captions.SET_OWNER_CANCELLED);
            }
        };
        if (hasConfirmation(player)) {
            CmdConfirm.addPending(player, "/plot set owner " + value, run);
        } else {
            TaskManager.runTask(run);
        }
        return true;
    }
}
