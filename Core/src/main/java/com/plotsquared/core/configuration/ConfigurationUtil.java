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
package com.plotsquared.core.configuration;

import com.plotsquared.core.plot.BlockBucket;
import com.plotsquared.core.plot.PlotAreaTerrainType;
import com.plotsquared.core.plot.PlotAreaType;
import com.plotsquared.core.util.MathMan;
import com.sk89q.worldedit.function.pattern.Pattern;
import com.sk89q.worldedit.world.biome.BiomeType;
import com.sk89q.worldedit.world.biome.BiomeTypes;
import com.sk89q.worldedit.world.block.BlockState;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.Optional;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.Supplier;

/**
 * Main Configuration Utility
 */
public class ConfigurationUtil {

    public static final SettingValue<Integer> INTEGER = new SettingValue<>("INTEGER") {
        @Override
        public boolean validateValue(String string) {
            try {
                Integer.parseInt(string);
                return true;
            } catch (NumberFormatException ignored) {
                return false;
            }
        }

        @Override
        public Integer parseString(String string) {
            return Integer.parseInt(string);
        }
    };
    public static final SettingValue<Boolean> BOOLEAN = new SettingValue<>("BOOLEAN") {
        @Override
        public boolean validateValue(String string) {
            //noinspection ResultOfMethodCallIgnored
            Boolean.parseBoolean(string);
            return true;
        }

        @Override
        public Boolean parseString(String string) {
            return Boolean.parseBoolean(string);
        }
    };
    public static final SettingValue<BiomeType> BIOME = new SettingValue<>("BIOME") {
        @Override
        public boolean validateValue(String string) {
            try {
                return BiomeTypes.get(string) != null;
            } catch (Exception ignored) {
                return false;
            }
        }

        @Override
        public BiomeType parseString(String string) {
            if (validateValue(string)) {
                return BiomeTypes.get(string.toLowerCase());
            }
            return BiomeTypes.FOREST;
        }
    };

    public static final SettingValue<BlockBucket> BLOCK_BUCKET =
            new SettingValue<>("BLOCK_BUCKET") {

                @Override
                public BlockBucket parseString(final String string) {
                    BlockBucket bucket = new BlockBucket(string);
                    bucket.compile();
                    Pattern pattern = bucket.toPattern();
                    return pattern != null ? bucket : null;
                }

                @Override
                public boolean validateValue(final String string) {
                    try {
                        return parseString(string) != null;
                    } catch (Exception e) {
                        return false;
                    }
                }
            };

    private static <T> T getValueFromConfig(
            ConfigurationSection config, String path,
            IntFunction<Optional<T>> intParser, Function<String, Optional<T>> textualParser,
            Supplier<T> defaultValue
    ) {
        String value = config.getString(path);
        if (value == null) {
            return defaultValue.get();
        }
        if (MathMan.isInteger(value)) {
            return intParser.apply(Integer.parseInt(value)).orElseGet(defaultValue);
        }
        return textualParser.apply(value).orElseGet(defaultValue);
    }

    public static PlotAreaType getType(ConfigurationSection config) {
        return getValueFromConfig(config, "generator.type", PlotAreaType::fromLegacyInt,
                PlotAreaType::fromString, () -> PlotAreaType.NORMAL
        );
    }

    public static PlotAreaTerrainType getTerrain(ConfigurationSection config) {
        return getValueFromConfig(config, "generator.terrain", PlotAreaTerrainType::fromLegacyInt,
                PlotAreaTerrainType::fromString, () -> PlotAreaTerrainType.NONE
        );
    }


    public static final class UnknownBlockException extends IllegalArgumentException {

        private final String unknownValue;

        UnknownBlockException(final @NonNull String unknownValue) {
            super(String.format("\"%s\" is not a valid block", unknownValue));
            this.unknownValue = unknownValue;
        }

        public String getUnknownValue() {
            return this.unknownValue;
        }

    }


    /**
     * Create your own SettingValue object to make the management of plotworld configuration easier
     */
    public abstract static class SettingValue<T> {

        private final String type;

        SettingValue(String type) {
            this.type = type;
        }

        public String getType() {
            return this.type;
        }

        public abstract T parseString(String string);

        public abstract boolean validateValue(String string);

    }


    public static final class UnsafeBlockException extends IllegalArgumentException {

        private final BlockState unsafeBlock;

        UnsafeBlockException(final @NonNull BlockState unsafeBlock) {
            super(String.format("%s is not a valid block", unsafeBlock));
            this.unsafeBlock = unsafeBlock;
        }

        public BlockState getUnsafeBlock() {
            return this.unsafeBlock;
        }

    }

}
