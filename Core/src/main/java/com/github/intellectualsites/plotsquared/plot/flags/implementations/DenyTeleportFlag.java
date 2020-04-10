package com.github.intellectualsites.plotsquared.plot.flags.implementations;

import com.github.intellectualsites.plotsquared.plot.config.Captions;
import com.github.intellectualsites.plotsquared.plot.flags.FlagParseException;
import com.github.intellectualsites.plotsquared.plot.flags.PlotFlag;
import com.github.intellectualsites.plotsquared.plot.object.Plot;
import com.github.intellectualsites.plotsquared.player.PlotPlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
    protected DenyTeleportFlag(@NotNull DeniedGroup value) {
        super(value, Captions.FLAG_CATEGORY_ENUM, Captions.FLAG_DESCRIPTION_DENY_TELEPORT);
    }

    public static boolean allowsTeleport(PlotPlayer player, Plot plot) {
        final DeniedGroup value = plot.getFlag(DenyTeleportFlag.class);
        if (value == DeniedGroup.NONE) {
            return true;
        }
        final boolean result;
        switch (value) {
            case TRUSTED:
                result = !plot.getTrusted().contains(player.getUUID());
                break;
            case MEMBERS:
                result = !plot.getMembers().contains(player.getUUID());
                break;
            case NONMEMBERS:
                result = plot.isAdded(player.getUUID());
                break;
            case NONTRUSTED:
                result =
                    plot.getTrusted().contains(player.getUUID()) || plot.isOwner(player.getUUID());
                break;
            case NONOWNERS:
                result = plot.isOwner(player.getUUID());
                break;
            default:
                return true;
        }
        return result || player.hasPermission("plots.admin.entry.denied");
    }

    @Override public DenyTeleportFlag parse(@NotNull String input) throws FlagParseException {
        final DeniedGroup group = DeniedGroup.fromString(input);
        if (group == null) {
            throw new FlagParseException(this, input, Captions.FLAG_ERROR_ENUM,
                "members, nonmembers, trusted, nontrusted, nonowners");
        }
        return flagOf(group);
    }

    @Override public DenyTeleportFlag merge(@NotNull DeniedGroup newValue) {
        if (getValue().ordinal() < newValue.ordinal()) {
            return flagOf(newValue);
        }
        return this;
    }

    @Override public String toString() {
        return this.getValue().name();
    }

    @Override public String getExample() {
        return "trusted";
    }

    @Override protected DenyTeleportFlag flagOf(@NotNull DeniedGroup value) {
        return new DenyTeleportFlag(value);
    }

    @Override public Collection<String> getTabCompletions() {
        return Arrays.asList("none", "members", "trusted", "nonmembers", "nontrusted", "nonowners");
    }

    public enum DeniedGroup {
        NONE, MEMBERS, TRUSTED, NONMEMBERS, NONTRUSTED, NONOWNERS;

        @Nullable public static DeniedGroup fromString(@NotNull final String string) {
            for (final DeniedGroup group : values()) {
                if (group.name().equalsIgnoreCase(string)) {
                    return group;
                }
            }
            return null;
        }
    }

}
