package com.github.intellectualsites.plotsquared.plot.util;

import com.github.intellectualsites.plotsquared.commands.CommandCaller;
import com.github.intellectualsites.plotsquared.plot.config.Captions;
import com.github.intellectualsites.plotsquared.plot.config.Settings;
import com.github.intellectualsites.plotsquared.plot.object.PlotPlayer;

import java.util.HashMap;

/**
 * The Permissions class handles checking user permissions.<br>
 * - This will respect * nodes and plots.admin and can be used to check permission ranges (e.g. plots.plot.5)<br>
 * - Checking the PlotPlayer class directly will not take the above into account<br>
 */
public class Permissions {

    public static boolean hasPermission(PlotPlayer player, Captions caption, boolean notify) {
        return hasPermission(player, caption.s(), notify);
    }

    /**
     * Check if a player has a permission (Captions class helps keep track of permissions).
     *
     * @param player
     * @param caption
     * @return
     */
    public static boolean hasPermission(PlotPlayer player, Captions caption) {
        return hasPermission(player, caption.s());
    }

    /**
     * Check if a {@link PlotPlayer} has a permission.
     *
     * @param player
     * @param permission
     * @return
     */
    public static boolean hasPermission(PlotPlayer player, String permission) {
        if (!Settings.Enabled_Components.PERMISSION_CACHE) {
            return hasPermission((CommandCaller) player, permission);
        }
        HashMap<String, Boolean> map = player.getMeta("perm");
        if (map != null) {
            Boolean result = map.get(permission);
            if (result != null) {
                return result;
            }
        } else {
            map = new HashMap<>();
            player.setMeta("perm", map);
        }
        boolean result = hasPermission((CommandCaller) player, permission);
        map.put(permission, result);
        return result;
    }

    /**
     * Check if a {@code CommandCaller} has a permission.
     *
     * @param caller
     * @param permission
     * @return
     */
    public static boolean hasPermission(CommandCaller caller, String permission) {
        if (caller.hasPermission(permission)) {
            return true;
        } else if (caller.isPermissionSet(permission)) {
            return false;
        }
        if (caller.hasPermission(Captions.PERMISSION_ADMIN.s())) {
            return true;
        }
        permission = permission.toLowerCase().replaceAll("^[^a-z|0-9|\\.|_|-]", "");
        String[] nodes = permission.split("\\.");
        StringBuilder n = new StringBuilder();
        for (int i = 0; i <= (nodes.length - 1); i++) {
            n.append(nodes[i] + ".");
            String combined = n + Captions.PERMISSION_STAR.s();
            if (!permission.equals(combined)) {
                if (caller.hasPermission(combined)) {
                    return true;
                } else if (caller.isPermissionSet(combined)) {
                    return false;
                }
            }
        }
        return false;
    }

    /**
     * Check if a PlotPlayer has a permission, and optionally send the no permission message if applicable.
     *
     * @param player
     * @param permission
     * @param notify
     * @return
     */
    public static boolean hasPermission(PlotPlayer player, String permission, boolean notify) {
        if (!hasPermission(player, permission)) {
            if (notify) {
                MainUtil.sendMessage(player, Captions.NO_PERMISSION_EVENT, permission);
            }
            return false;
        }
        return true;
    }

    public static int hasPermissionRange(PlotPlayer player, Captions perm, int range) {
        return hasPermissionRange(player, perm.s(), range);
    }

    /**
     * Check the the highest permission a PlotPlayer has within a specified range.<br>
     * - Excessively high values will lag<br>
     * - The default range that is checked is {@link Settings.Limit#MAX_PLOTS}<br>
     *
     * @param player
     * @param stub   The permission stub to check e.g. for `plots.plot.#` the stub is `plots.plot`
     * @param range  The range to check
     * @return The highest permission they have within that range
     */
    public static int hasPermissionRange(PlotPlayer player, String stub, int range) {
        return player.hasPermissionRange(stub, range);
    }
}
