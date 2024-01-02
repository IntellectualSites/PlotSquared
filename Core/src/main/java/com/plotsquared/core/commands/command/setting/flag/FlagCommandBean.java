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
package com.plotsquared.core.commands.command.setting.flag;

import cloud.commandframework.Command;
import com.plotsquared.core.PlotSquared;
import com.plotsquared.core.command.CommandCategory;
import com.plotsquared.core.commands.CommandRequirement;
import com.plotsquared.core.commands.CommonCommandRequirement;
import com.plotsquared.core.commands.PlotSquaredCommandBean;
import com.plotsquared.core.configuration.Settings;
import com.plotsquared.core.configuration.caption.TranslatableCaption;
import com.plotsquared.core.permissions.Permission;
import com.plotsquared.core.player.PlotPlayer;
import com.plotsquared.core.plot.flag.FlagParseException;
import com.plotsquared.core.plot.flag.PlotFlag;
import com.plotsquared.core.plot.flag.types.IntegerFlag;
import com.plotsquared.core.plot.flag.types.ListFlag;
import com.plotsquared.core.util.MathMan;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.List;

public abstract class FlagCommandBean extends PlotSquaredCommandBean {

    protected static boolean checkPermValue(
            final @NonNull PlotPlayer<?> player,
            final @NonNull PlotFlag<?, ?> flag, @NonNull String key, @NonNull String value
    ) {
        key = key.toLowerCase();
        value = value.toLowerCase();
        String perm = Permission.PERMISSION_SET_FLAG_KEY_VALUE.format(key.toLowerCase(), value.toLowerCase());
        if (flag instanceof IntegerFlag && MathMan.isInteger(value)) {
            try {
                int numeric = Integer.parseInt(value);
                // Getting full permission without ".<amount>" at the end
                perm = perm.substring(0, perm.length() - value.length() - 1);
                boolean result = false;
                if (numeric >= 0) {
                    int checkRange = PlotSquared.get().getPlatform().equalsIgnoreCase("bukkit") ?
                            numeric :
                            Settings.Limit.MAX_PLOTS;
                    result = player.hasPermissionRange(perm, checkRange) >= numeric;
                }
                if (!result) {
                    player.sendMessage(
                            TranslatableCaption.of("permission.no_permission"),
                            TagResolver.resolver(
                                    "node",
                                    Tag.inserting(Component.text(perm + "." + numeric))
                            )
                    );
                }
                return result;
            } catch (NumberFormatException ignore) {
            }
        } else if (flag instanceof final ListFlag<?, ?> listFlag) {
            try {
                PlotFlag<? extends List<?>, ?> parsedFlag = listFlag.parse(value);
                for (final Object entry : parsedFlag.getValue()) {
                    final String permission = Permission.PERMISSION_SET_FLAG_KEY_VALUE.format(
                            key.toLowerCase(),
                            entry.toString().toLowerCase()
                    );
                    final boolean result = player.hasPermission(permission);
                    if (!result) {
                        player.sendMessage(
                                TranslatableCaption.of("permission.no_permission"),
                                TagResolver.resolver("node", Tag.inserting(Component.text(permission)))
                        );
                        return false;
                    }
                }
            } catch (final FlagParseException e) {
                player.sendMessage(
                        TranslatableCaption.of("flag.flag_parse_error"),
                        TagResolver.builder()
                                .tag("flag_name", Tag.inserting(Component.text(flag.getName())))
                                .tag("flag_value", Tag.inserting(Component.text(e.getValue())))
                                .tag("error", Tag.inserting(e.getErrorMessage().toComponent(player)))
                                .build()
                );
                return false;
            } catch (final Exception e) {
                return false;
            }
            return true;
        }
        boolean result;
        String basePerm = Permission.PERMISSION_SET_FLAG_KEY.format(key.toLowerCase());
        if (flag.isValuedPermission()) {
            result = player.hasKeyedPermission(basePerm, value);
        } else {
            result = player.hasPermission(basePerm);
            perm = basePerm;
        }
        if (!result) {
            player.sendMessage(
                    TranslatableCaption.of("permission.no_permission"),
                    TagResolver.resolver("node", Tag.inserting(Component.text(perm)))
            );
        }
        return result;
    }

    @Override
    public final @NonNull CommandCategory category() {
        return CommandCategory.SETTINGS;
    }

    @Override
    public @NonNull List<@NonNull CommandRequirement> requirements() {
        return List.of(CommonCommandRequirement.IS_OWNER.withPermissionOverride(Permission.PERMISSION_SET_FLAG_OTHER));
    }

    @Override
    protected final Command.@NonNull Builder<PlotPlayer<?>> prepare(final Command.@NonNull Builder<PlotPlayer<?>> builder) {
        return builder.literal("flag");
    }
}
