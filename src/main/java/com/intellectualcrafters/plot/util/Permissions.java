package com.intellectualcrafters.plot.util;

import com.intellectualcrafters.plot.config.C;
import com.intellectualcrafters.plot.object.PlotPlayer;

public enum Permissions {
    // ADMIN
    ADMIN("plots.admin", "do-not-change"),
    STAR("*", "do-not-change"),
    // BUILD
    BUILD_OTHER("plots.admin.build.other", "build"),
    BUILD_ROAD("plots.admin.build.road", "build"),
    BUILD_UNOWNED("plots.admin.build.unowned", "build"),
    // INTERACT
    INTERACT_OTHER("plots.admin.interact.other", "interact"),
    INTERACT_ROAD("plots.admin.interact.road", "interact"),
    INTERACT_UNOWNED("plots.admin.interact.unowned", "interact"),
    // BREAK
    BREAK_OTHER("plots.admin.break.other", "break"),
    BREAK_ROAD("plots.admin.break.road", "break"),
    BREAK_UNOWNED("plots.admin.break.unowned", "break"),
    // MERGE
    MERGE_OTHER("plots.merge.other", "merge");
    
    public String s;
    public String cat;
    
    Permissions(String perm, String cat) {
        this.s = perm;
        this.cat = cat;
    }
    
    public static boolean hasPermission(final PlotPlayer player, final Permissions perm) {
        return hasPermission(player, perm.s);
    }


    public static boolean hasPermission(final PlotPlayer player, final String perm) {
        if ((player == null) || player.hasPermission(ADMIN.s) || player.hasPermission(STAR.s)) {
            return true;
        }
        if (player.hasPermission(perm)) {
            return true;
        }
        final String[] nodes = perm.split("\\.");
        final StringBuilder n = new StringBuilder();
        for (int i = 0; i < (nodes.length - 1); i++) {
            n.append(nodes[i] + ("."));
            if (player.hasPermission(n + STAR.s)) {
                return true;
            }
        }
        return false;
    }
    
    public static boolean hasPermission(final PlotPlayer player, final String perm, boolean notify) {
        if (!hasPermission(player, perm)) {
            if (notify) {
                MainUtil.sendMessage(player, C.NO_PERMISSION_EVENT, perm);
            }
            return false;
        }
        return true;
    }

    public static int hasPermissionRange(final PlotPlayer player, final String stub, final int range) {
        if ((player == null) || player.hasPermission(ADMIN.s) || player.hasPermission(STAR.s)) {
            return Integer.MAX_VALUE;
        }
        if (player.hasPermission(stub + ".*")) {
            return Integer.MAX_VALUE;
        }
        for (int i = range; i > 0; i--) {
            if (player.hasPermission(stub + "." + i)) {
                return i;
            }
        }
        return 0;
    }
}
