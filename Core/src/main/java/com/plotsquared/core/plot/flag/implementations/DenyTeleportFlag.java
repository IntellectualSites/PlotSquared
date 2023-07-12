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
package com.plotsquared.core.plot.flag.implementations;

import com.plotsquared.core.configuration.caption.TranslatableCaption;
import com.plotsquared.core.player.PlotPlayer;
import com.plotsquared.core.plot.Plot;
import com.plotsquared.core.plot.flag.FlagParseException;
import com.plotsquared.core.plot.flag.PlotFlag;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Arrays;
import java.util.Collection;

public class DenyTeleportFlag extends PlotFlag<DenyTeleportFlag.DeniedGroup, DenyTeleportFlag> {

    public static final DenyTeleportFlag DENY_TELEPORT_FLAG_NONE =
            new DenyTeleportFlag(DeniedGroup.NONE);

    /**
     * Construct a new flag instance.
     *
     * @param value Flag value
     */
    protected DenyTeleportFlag(@NonNull DeniedGroup value) {
        super(
                value,
                TranslatableCaption.of("flags.flag_category_enum"),
                TranslatableCaption.of("flags.flag_description_deny_teleport")
        );
    }

    public static boolean allowsTeleport(PlotPlayer<?> player, Plot plot) {
        final DeniedGroup value = plot.getFlag(DenyTeleportFlag.class);
        if (value == DeniedGroup.NONE) {
            return true;
        }
        final boolean result;
        switch (value) {
            case TRUSTED -> result = !plot.getTrusted().contains(player.getUUID());
            case MEMBERS -> result = !plot.getMembers().contains(player.getUUID());
            case NONMEMBERS -> result = plot.isAdded(player.getUUID());
            case NONTRUSTED -> result =
                    plot.getTrusted().contains(player.getUUID()) || plot.isOwner(player.getUUID());
            case NONOWNERS -> result = plot.isOwner(player.getUUID());
            default -> {
                return true;
            }
        }
        return result || player.hasPermission("plots.admin.entry.denied");
    }

    @Override
    public DenyTeleportFlag parse(@NonNull String input) throws FlagParseException {
        final DeniedGroup group = DeniedGroup.fromString(input);
        if (group == null) {
            throw new FlagParseException(this, input, TranslatableCaption.of("flags.flag_error_enum"),
                    TagResolver.resolver(
                            "list",
                            Tag.inserting(Component.text("members, nonmembers, trusted, nontrusted, nonowners"))
                    )
            );
        }
        return flagOf(group);
    }

    @Override
    public DenyTeleportFlag merge(@NonNull DeniedGroup newValue) {
        if (getValue().ordinal() < newValue.ordinal()) {
            return flagOf(newValue);
        }
        return this;
    }

    @Override
    public String toString() {
        return this.getValue().name();
    }

    @Override
    public String getExample() {
        return "trusted";
    }

    @Override
    protected DenyTeleportFlag flagOf(@NonNull DeniedGroup value) {
        return new DenyTeleportFlag(value);
    }

    @Override
    public Collection<String> getTabCompletions() {
        return Arrays.asList("none", "members", "trusted", "nonmembers", "nontrusted", "nonowners");
    }

    public enum DeniedGroup {
        NONE,
        MEMBERS,
        TRUSTED,
        NONMEMBERS,
        NONTRUSTED,
        NONOWNERS;

        public static @Nullable DeniedGroup fromString(final @NonNull String string) {
            for (final DeniedGroup group : values()) {
                if (group.name().equalsIgnoreCase(string)) {
                    return group;
                }
            }
            return null;
        }
    }

}
