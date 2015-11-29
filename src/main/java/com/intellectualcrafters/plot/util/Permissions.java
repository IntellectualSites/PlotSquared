package com.intellectualcrafters.plot.util;

import java.util.HashMap;

import com.intellectualcrafters.plot.config.C;
import com.intellectualcrafters.plot.config.Settings;
import com.intellectualcrafters.plot.object.PlotPlayer;
import com.plotsquared.general.commands.CommandCaller;

public class Permissions {
    public static boolean hasPermission(final PlotPlayer player, final C c) {
        return hasPermission(player, c.s());
    }
    
    public static boolean hasPermission(final PlotPlayer player, final String perm) {
        if (!Settings.PERMISSION_CACHING) {
            return hasPermission((CommandCaller) player, perm);
        }
        HashMap<String, Boolean> map = (HashMap<String, Boolean>) player.getMeta("perm");
        if (map != null) {
            Boolean result = map.get(perm);
            if (result != null) {
                return result;
            }
        } else {
            map = new HashMap<>();
            player.setMeta("perm", map);
        }
        boolean result = hasPermission((CommandCaller) player, perm);
        map.put(perm, result);
        return result;
    }
    
    public static boolean hasPermission(final CommandCaller player, String perm) {
        if (player.hasPermission(perm) || player.hasPermission(C.PERMISSION_ADMIN.s())) {
            return true;
        }
        perm = perm.toLowerCase().replaceAll("^[^a-z|0-9|\\.|_|-]", "");
        final String[] nodes = perm.split("\\.");
        final StringBuilder n = new StringBuilder();
        for (int i = 0; i < (nodes.length - 1); i++) {
            n.append(nodes[i] + ("."));
            if (!perm.equals(n + C.PERMISSION_STAR.s())) {
                if (player.hasPermission(n + C.PERMISSION_STAR.s())) {
                    return true;
                }
            }
        }
        return false;
    }
    
    public static boolean hasPermission(final PlotPlayer player, final String perm, final boolean notify) {
        if (!hasPermission(player, perm)) {
            if (notify) {
                MainUtil.sendMessage(player, C.NO_PERMISSION_EVENT, perm);
            }
            return false;
        }
        return true;
    }
    
    public static int hasPermissionRange(final PlotPlayer player, final String stub, final int range) {
        if (player.hasPermission(C.PERMISSION_ADMIN.s())) {
            return Integer.MAX_VALUE;
        }
        final String[] nodes = stub.split("\\.");
        final StringBuilder n = new StringBuilder();
        for (int i = 0; i < (nodes.length - 1); i++) {
            n.append(nodes[i] + ("."));
            if (!stub.equals(n + C.PERMISSION_STAR.s())) {
                if (player.hasPermission(n + C.PERMISSION_STAR.s())) {
                    return Integer.MAX_VALUE;
                }
            }
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
