/*
 * PlotSquared, a land and world management plugin for Minecraft.
 * Copyright (C) IntellectualSites <https://intellectualsites.com>
 * Copyright (C) IntellectualSites team and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.plotsquared.bukkit.permissions;

import com.plotsquared.bukkit.player.BukkitPlayer;
import com.plotsquared.core.permissions.RangedPermissionResolver;
import com.plotsquared.core.player.PlotPlayer;
import com.plotsquared.core.util.MathMan;
import org.bukkit.permissions.PermissionAttachmentInfo;
import org.checkerframework.checker.index.qual.NonNegative;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Set;
import java.util.stream.IntStream;

public class BukkitRangedPermissionResolver implements RangedPermissionResolver {

    private static boolean CHECK_EFFECTIVE = true;

    @Override
    public @NonNegative int getPermissionRange(
            final @NonNull PlotPlayer<?> generic,
            final @NonNull String stub,
            final @Nullable String worldContext,
            @NonNegative final int range
    ) {
        if (!(generic instanceof BukkitPlayer player)) {
            throw new IllegalArgumentException("PlotPlayer is not a BukkitPlayer");
        }
        if (hasWildcardRange(player, stub, worldContext)) {
            return INFINITE_RANGE_VALUE;
        }
        int max = 0;
        if (CHECK_EFFECTIVE) {
            boolean hasAny = false;
            String stubPlus = stub + ".";
            final Set<PermissionAttachmentInfo> effective = player.getPlatformPlayer().getEffectivePermissions();
            if (!effective.isEmpty()) {
                for (PermissionAttachmentInfo attach : effective) {
                    // Ignore all "false" permissions
                    if (!attach.getValue()) {
                        continue;
                    }
                    String permStr = attach.getPermission();
                    if (permStr.startsWith(stubPlus)) {
                        hasAny = true;
                        String end = permStr.substring(stubPlus.length());
                        if (MathMan.isInteger(end)) {
                            int val = Integer.parseInt(end);
                            if (val > range) {
                                return val;
                            }
                            if (val > max) {
                                max = val;
                            }
                        }
                    }
                }
                if (hasAny) {
                    return max;
                }
                // Workaround
                for (PermissionAttachmentInfo attach : effective) {
                    String permStr = attach.getPermission();
                    if (permStr.startsWith("plots.") && !permStr.equals("plots.use")) {
                        return max;
                    }
                }
                CHECK_EFFECTIVE = false;
            }
        }
        for (int i = range; i > 0; i--) {
            if (player.hasPermission(worldContext, stub + "." + i)) {
                return i;
            }
        }
        return max;
    }

    @Override
    public @NonNull IntStream streamFullPermissionRange(
            final @NonNull PlotPlayer<?> player,
            final @NonNull String stub,
            final @Nullable String worldContext,
            @NonNegative final int range
    ) {
        return IntStream.of(getPermissionRange(player, stub, worldContext, range));
    }

}
