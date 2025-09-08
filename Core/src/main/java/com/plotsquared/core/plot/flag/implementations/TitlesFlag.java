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
import com.plotsquared.core.plot.flag.FlagParseException;
import com.plotsquared.core.plot.flag.PlotFlag;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Arrays;
import java.util.Collection;
import java.util.Locale;

public class TitlesFlag extends PlotFlag<TitlesFlag.TitlesFlagValue, TitlesFlag> {

    public static final TitlesFlag TITLES_NONE = new TitlesFlag(TitlesFlagValue.NONE);
    public static final TitlesFlag TITLES_TRUE = new TitlesFlag(TitlesFlagValue.TRUE);
    public static final TitlesFlag TITLES_FALSE = new TitlesFlag(TitlesFlagValue.FALSE);

    private TitlesFlag(final TitlesFlagValue value) {
        super(value, TranslatableCaption.of("flags.flag_category_enum"), TranslatableCaption.of("flags.flag_description_titles"));
    }

    @Override
    public TitlesFlag parse(final @NonNull String input) throws FlagParseException {
        final TitlesFlagValue titlesFlagValue = TitlesFlagValue.fromString(input);
        if (titlesFlagValue == null) {
            throw new FlagParseException(
                    this,
                    input,
                    TranslatableCaption.of("flags.flag_error_enum"),
                    TagResolver.resolver("list", Tag.inserting(Component.text("none, true, false")))
            );
        }
        return flagOf(titlesFlagValue);
    }

    @Override
    public TitlesFlag merge(@NonNull TitlesFlagValue newValue) {
        if (newValue == TitlesFlagValue.TRUE || newValue == TitlesFlagValue.FALSE) {
            return flagOf(newValue);
        }
        return this;
    }

    @Override
    public String toString() {
        return getValue().name().toLowerCase(Locale.ENGLISH);
    }

    @Override
    public String getExample() {
        return "true";
    }

    @Override
    protected TitlesFlag flagOf(@NonNull TitlesFlagValue value) {
        if (value == TitlesFlagValue.TRUE) {
            return TITLES_TRUE;
        } else if (value == TitlesFlagValue.FALSE) {
            return TITLES_FALSE;
        }
        return TITLES_NONE;
    }

    @Override
    public Collection<String> getTabCompletions() {
        return Arrays.asList("none", "true", "false");
    }

    public enum TitlesFlagValue {
        NONE,
        TRUE,
        FALSE;

        public static @Nullable TitlesFlagValue fromString(final String value) {
            if (value.equalsIgnoreCase("true")) {
                return TRUE;
            } else if (value.equalsIgnoreCase("false")) {
                return FALSE;
            } else if (value.equalsIgnoreCase("none")) {
                return NONE;
            }
            return null;
        }
    }

}
