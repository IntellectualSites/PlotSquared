package com.github.intellectualsites.plotsquared.plot.config;

import com.github.intellectualsites.plotsquared.plot.object.BlockBucket;
import com.github.intellectualsites.plotsquared.plot.object.PlotBlock;
import com.github.intellectualsites.plotsquared.plot.util.StringComparison;
import com.github.intellectualsites.plotsquared.plot.util.WorldUtil;
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
    public static final SettingValue<String> BIOME = new SettingValue<String>("BIOME") {
        @Override public boolean validateValue(String string) {
            try {
                int biome = WorldUtil.IMP.getBiomeFromString(string.toUpperCase());
                return biome != -1;
            } catch (Exception ignored) {
                return false;
            }
        }

        @Override public String parseString(String string) {
            if (validateValue(string)) {
                return string.toUpperCase();
            }
            return "FOREST";
        }
    };

    public static final SettingValue<BlockBucket> BLOCK_BUCKET =
        new SettingValue<BlockBucket>("BLOCK_BUCKET") {
            @Override public BlockBucket parseString(final String string) {
                if (string == null || string.isEmpty()) {
                    return new BlockBucket();
                }
                final BlockBucket blockBucket = new BlockBucket();
                final String[] parts = string.split(",");
                for (final String part : parts) {
                    String block;
                    int chance = -1;

                    if (part.contains(":")) {
                        final String[] innerParts = part.split(":");
                        if (innerParts.length > 1) {
                            chance = Integer.parseInt(innerParts[1]);
                        }
                        block = innerParts[0];
                    } else {
                        block = part;
                    }
                    final StringComparison<PlotBlock>.ComparisonResult value =
                        WorldUtil.IMP.getClosestBlock(block);
                    if (value == null) {
                        throw new UnknownBlockException(block);
                    } else if (Settings.Enabled_Components.PREVENT_UNSAFE && !value.best.isAir() &&
                        !WorldUtil.IMP.isBlockSolid(value.best)) {
                        throw new UnsafeBlockException(value.best);
                    }
                    blockBucket.addBlock(value.best, chance);
                }
                blockBucket.compile(); // Pre-compile :D
                return blockBucket;
            }

            @Override public boolean validateValue(final String string) {
                try {
                    if (string == null || string.isEmpty()) {
                        return false;
                    }
                    final String[] parts = string.split(",");
                    for (final String part : parts) {
                        String block;
                        if (part.contains(":")) {
                            final String[] innerParts = part.split(":");
                            if (innerParts.length > 1) {
                                final int chance = Integer.parseInt(innerParts[1]);
                                if (chance < 1 || chance > 100) {
                                    return false;
                                }
                            }
                            block = innerParts[0];
                        } else {
                            block = part;
                        }
                        StringComparison<PlotBlock>.ComparisonResult value =
                            WorldUtil.IMP.getClosestBlock(block);
                        if (value == null || value.match > 1) {
                            return false;
                        } else if (Settings.Enabled_Components.PREVENT_UNSAFE && !value.best.isAir() && !WorldUtil.IMP.isBlockSolid(value.best)) {
                            throw new UnsafeBlockException(value.best);
                        }
                    }
                } catch (final Throwable exception) {
                    return false;
                }
                return true;
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

        @Getter private final PlotBlock unsafeBlock;

        UnsafeBlockException(@NonNull final PlotBlock unsafeBlock) {
            super(String.format("%s is not a valid block", unsafeBlock));
            this.unsafeBlock = unsafeBlock;
        }

    }

}
