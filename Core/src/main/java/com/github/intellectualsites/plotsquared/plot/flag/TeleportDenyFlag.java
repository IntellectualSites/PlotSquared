package com.github.intellectualsites.plotsquared.plot.flag;

import com.github.intellectualsites.plotsquared.plot.object.Plot;
import com.github.intellectualsites.plotsquared.plot.object.PlotPlayer;

public class TeleportDenyFlag extends EnumFlag {
    public TeleportDenyFlag(String name) {
        super(name, "trusted", "members", "nonmembers", "nontrusted", "nonowners");
    }

    public boolean allowsTeleport(PlotPlayer player, Plot plot) {
        String value = plot.getFlag(this, null);
        if (value == null || !plot.hasOwner()) {
            return true;
        }
        boolean result;
        switch (plot.getFlag(this, null)) {
            case "trusted":
                result = !plot.getTrusted().contains(player.getUUID());
                break;
            case "members":
                result = !plot.getMembers().contains(player.getUUID());
                break;
            case "nonmembers":
                result = plot.isAdded(player.getUUID());
                break;
            case "nontrusted":
                result = plot.getTrusted().contains(player.getUUID()) || plot
                    .isOwner(player.getUUID());
                break;
            case "nonowners":
                result = plot.isOwner(player.getUUID());
                break;
            default:
                return true;
        }
        return result || player.hasPermission("plots.admin.entry.denied");
    }
}
