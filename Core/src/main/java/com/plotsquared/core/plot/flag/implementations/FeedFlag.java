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
import com.plotsquared.core.plot.flag.types.TimedFlag;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.checkerframework.checker.nullness.qual.NonNull;

public class FeedFlag extends TimedFlag<Integer, FeedFlag> {

    public static final FeedFlag FEED_NOTHING = new FeedFlag(new Timed<>(0, 0));

    public FeedFlag(@NonNull Timed<Integer> value) {
        super(value, 1, TranslatableCaption.of("flags.flag_description_feed"));
    }

    @Override
    protected Integer parseValue(String input) throws FlagParseException {
        int parsed;
        try {
            parsed = Integer.parseInt(input);
        } catch (Throwable throwable) {
            throw new FlagParseException(
                    this,
                    input,
                    TranslatableCaption.of("invalid.not_a_number"),
                    TagResolver.resolver("value", Tag.inserting(Component.text(input)))
            );
        }
        if (parsed < 1) {
            throw new FlagParseException(
                    this,
                    input,
                    TranslatableCaption.of("invalid.number_not_positive"),
                    TagResolver.resolver("value", Tag.inserting(Component.text(parsed)))
            );
        }
        return parsed;
    }

    @Override
    protected Integer mergeValue(Integer other) {
        return this.getValue().value() + other;
    }

    @Override
    public String getExample() {
        return "10 5";
    }

    @Override
    protected FeedFlag flagOf(@NonNull Timed<Integer> value) {
        return new FeedFlag(value);
    }

}
