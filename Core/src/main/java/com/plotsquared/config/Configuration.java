package com.plotsquared.config;

import com.plotsquared.plot.BlockBucket;
import com.sk89q.worldedit.function.pattern.Pattern;
import com.sk89q.worldedit.world.biome.BiomeType;
import com.sk89q.worldedit.world.biome.BiomeTypes;
import com.sk89q.worldedit.world.block.BlockState;
import lombok.Getter;
import lombok.NonNull;

/**
 * Main Configuration Utility
 */
public class Configuration {

    public static final SettingValue<Integer> INTEGER = new SettingValue<Integer>("INTEGER") {
        @Override public boolean validateValue(String string) {
            try {
                Integer.parseInt(string);
                return true;
            } catch (NumberFormatException ignored) {
                return false;
            }
        }

        @Override public Integer parseString(String string) {
            return Integer.parseInt(string);
        }
    };
    public static final SettingValue<Boolean> BOOLEAN = new SettingValue<Boolean>("BOOLEAN") {
        @Override public boolean validateValue(String string) {
            //noinspection ResultOfMethodCallIgnored
            Boolean.parseBoolean(string);
            return true;
        }

        @Override public Boolean parseString(String string) {
            return Boolean.parseBoolean(string);
        }
    };
    public static final SettingValue<BiomeType> BIOME = new SettingValue<BiomeType>("BIOME") {
        @Override public boolean validateValue(String string) {
            try {
                return BiomeTypes.get(string) != null;
            } catch (Exception ignored) {
                return false;
            }
        }

        @Override public BiomeType parseString(String string) {
            if (validateValue(string)) {
                return BiomeTypes.get(string.toLowerCase());
            }
            return BiomeTypes.FOREST;
        }
    };

    public static final SettingValue<BlockBucket> BLOCK_BUCKET =
        new SettingValue<BlockBucket>("BLOCK_BUCKET") {

            @Override public BlockBucket parseString(final String string) {
                BlockBucket bucket = new BlockBucket(string);
                bucket.compile();
                Pattern pattern = bucket.toPattern();
                return pattern != null ? bucket : null;
            }

            @Override public boolean validateValue(final String string) {
                try {
                    return parseString(string) != null;
                } catch (Exception e) {
                    return false;
                }
            }
        };


    public static final class UnknownBlockException extends IllegalArgumentException {

        @Getter private final String unknownValue;

        UnknownBlockException(@NonNull final String unknownValue) {
            super(String.format("\"%s\" is not a valid block", unknownValue));
            this.unknownValue = unknownValue;
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

        @Getter private final BlockState unsafeBlock;

        UnsafeBlockException(@NonNull final BlockState unsafeBlock) {
            super(String.format("%s is not a valid block", unsafeBlock));
            this.unsafeBlock = unsafeBlock;
        }

    }

}
