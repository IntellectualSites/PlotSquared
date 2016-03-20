package com.intellectualcrafters.plot.util;

import com.intellectualcrafters.plot.config.C;
import com.intellectualcrafters.plot.config.Settings;
import com.intellectualcrafters.plot.object.PlotPlayer;
import com.plotsquared.general.commands.CommandCaller;

import java.util.HashMap;

/**
 * The Permissions class handles checking user permissions.<br>
 *  - This will respect * nodes and plots.admin and can be used to check permission ranges (e.g. plots.plot.5)<br>
 *  - Checking the PlotPlayer class directly will not take the above into account<br> 
 */
public class Permissions {
    
    /**
     * Check if a player has a permission (C class helps keep track of permissions)
     * @param player
     * @param c
     * @return
     */
    public static boolean hasPermission(final PlotPlayer player, final C c) {
        return hasPermission(player, c.s());
    }
    
    /**
     * Check if a PlotPlayer has a permission
     * @param player
     * @param perm
     * @return
     */
    public static boolean hasPermission(final PlotPlayer player, final String perm) {
        if (!Settings.PERMISSION_CACHING) {
            return hasPermission((CommandCaller) player, perm);
        }
        HashMap<String, Boolean> map = player.getMeta("perm");
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
    
    /**
     * Check if a CommandCaller (PlotPlayer implements CommandCaller) has a permission
     * @param player
     * @param perm
     * @return
     */
    public static boolean hasPermission(final CommandCaller player, String perm) {
        if (player.hasPermission(perm) || player.hasPermission(C.PERMISSION_ADMIN.s())) {
            return true;
        }
        perm = perm.toLowerCase().replaceAll("^[^a-z|0-9|\\.|_|-]", "");
        final String[] nodes = perm.split("\\.");
        final StringBuilder n = new StringBuilder();
        for (int i = 0; i <= (nodes.length - 1); i++) {
            n.append(nodes[i] + ".");
            if (!perm.equals(n + C.PERMISSION_STAR.s())) {
                if (player.hasPermission(n + C.PERMISSION_STAR.s())) {
                    return true;
                }
            }
        }
        return false;
    }
    
    /**
     * Check if a PlotPlayer has a permission, and optionally send the no perm message if applicable.
     * @param player
     * @param perm
     * @param notify
     * @return 
     */
    public static boolean hasPermission(final PlotPlayer player, final String perm, final boolean notify) {
        if (!hasPermission(player, perm)) {
            if (notify) {
                MainUtil.sendMessage(player, C.NO_PERMISSION_EVENT, perm);
            }
            return false;
        }
        return true;
    }
    
    /**
     * Check the the highest permission a PlotPlayer has within a specified range.<br>
     *  - Excessively high values will lag<br>
     *  - The default range that is checked is {@link Settings#MAX_PLOTS}<br>
     * @param player
     * @param stub The permission stub to check e.g. for `plots.plot.#` the stub is `plots.plot`
     * @param range The range to check
     * @return The highest permission they have within that range
     */
    public static int hasPermissionRange(final PlotPlayer player, final String stub, final int range) {
        if (player.hasPermission(C.PERMISSION_ADMIN.s())) {
            return Integer.MAX_VALUE;
        }
        final String[] nodes = stub.split("\\.");
        final StringBuilder n = new StringBuilder();
        for (int i = 0; i < (nodes.length - 1); i++) {
            n.append(nodes[i] + ".");
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
