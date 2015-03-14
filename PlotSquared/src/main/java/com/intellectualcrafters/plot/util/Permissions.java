package com.intellectualcrafters.plot.util;

import com.intellectualcrafters.plot.object.PlotPlayer;

public class Permissions {
    // ADMIN
    public static String ADMIN = "plots.admin";
    // BUILD
    public static String BUILD_OTHER = "plots.admin.build.other";
    public static String BUILD_ROAD = "plots.admin.build.road";
    public static String BUILD_UNOWNED = "plots.admin.build.unowned";
    // INTERACT
    public static String INTERACT_OTHER = "plots.admin.interact.other";
    public static String INTERACT_ROAD = "plots.admin.interact.road";
    public static String INTERACT_UNOWNED = "plots.admin.interact.unowned";
    // BREAK
    public static String BREAK_OTHER = "plots.admin.break.other";
    public static String BREAK_ROAD = "plots.admin.break.road";
    public static String BREAK_UNOWNED = "plots.admin.break.unowned";

    public static boolean hasPermission(final PlotPlayer player, final String perm) {
        if ((player == null) || player.isOp() || player.hasPermission(ADMIN)) {
            return true;
        }
        if (player.hasPermission(perm)) {
            return true;
        }
        final String[] nodes = perm.split("\\.");
        final StringBuilder n = new StringBuilder();
        for (int i = 0; i < (nodes.length - 1); i++) {
            n.append(nodes[i] + ("."));
            if (player.hasPermission(n + "*")) {
                return true;
            }
        }
        return false;
    }

    public static int hasPermissionRange(final PlotPlayer player, final String stub, final int range) {
        if ((player == null) || player.isOp() || player.hasPermission(ADMIN)) {
            return Byte.MAX_VALUE;
        }
        if (player.hasPermission(stub + ".*")) {
            return Byte.MAX_VALUE;
        }
        for (int i = range; i > 0; i--) {
            if (player.hasPermission(stub + "." + i)) {
                return i;
            }
        }
        return 0;
    }
}
