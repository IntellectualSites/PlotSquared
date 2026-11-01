package com.plotsquared.bukkit.entity;

import com.sk89q.worldedit.util.Enums;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.EnumMap;

/**
 * Wrapper Class for Bukkit {@link org.bukkit.entity.EntityType EntityTypes}.
 * <br>
 * Intended for a backwards compatible way to interact with entity types. Enum Constants should always be named after the
 * constants of bukkits EntityType enum in the latest supported versions. Backwards compatible enum constants identifiers
 * should have a comment assigned to them, so they can be removed in future versions.
 */
@ApiStatus.Internal
public enum EntityType {

    ACACIA_BOAT,
    ACACIA_CHEST_BOAT,
    ALLAY,
    AREA_EFFECT_CLOUD,
    ARMADILLO,
    ARMOR_STAND,
    ARROW,
    AXOLOTL,
    BAMBOO_CHEST_RAFT,
    BAMBOO_RAFT,
    BAT,
    BEE,
    BIRCH_BOAT,
    BIRCH_CHEST_BOAT,
    BLAZE,
    BLOCK_DISPLAY,
    BOGGED,
    BREEZE,
    BREEZE_WIND_CHARGE,
    CAMEL,
    CAT,
    CAVE_SPIDER,
    CHERRY_BOAT,
    CHERRY_CHEST_BOAT,
    // 1.20.5: MINECART_CHEST -> CHEST_MINECART
    CHEST_MINECART("MINECART_CHEST"),
    CHICKEN,
    COD,
    // 1.20.5: MINECART_COMMAND -> COMMAND_BLOCK_MINECART
    COMMAND_BLOCK_MINECART("MINECART_COMMAND"),
    COPPER_GOLEM,
    COW,
    CREAKING,
    CREEPER,
    DARK_OAK_BOAT,
    DARK_OAK_CHEST_BOAT,
    DOLPHIN,
    DONKEY,
    DRAGON_FIREBALL,
    DROWNED,
    EGG,
    ELDER_GUARDIAN,
    // 1.20.5: ENDER_CRYSTAL -> END_CRYSTAL
    END_CRYSTAL("ENDER_CRYSTAL"),
    ENDER_DRAGON,
    ENDER_PEARL,
    ENDERMAN,
    ENDERMITE,
    EVOKER,
    EVOKER_FANGS,
    // 1.20.5: THROWN_EXP_BOTTLE -> EXPERIENCE_BOTTLE
    EXPERIENCE_BOTTLE("THROWN_EXP_BOTTLE"),
    EXPERIENCE_ORB,
    // 1.20.5: ENDER_SIGNAL -> EYE_OF_ENDER
    EYE_OF_ENDER("ENDER_SIGNAL"),
    FALLING_BLOCK,
    FIREBALL,
    // 1.20.5: FIREWORK -> FIREWORK_ROCKET
    FIREWORK_ROCKET("FIREWORK"),
    // 1.20.5: FISHING_HOOK -> FISHING_BOBBER
    FISHING_BOBBER("FISHING_HOOK"),
    FOX,
    FROG,
    // 1.20.5: MINECART_FURNACE -> FURNACE_MINECART
    FURNACE_MINECART("MINECART_FURNACE"),
    GHAST,
    GIANT,
    GLOW_ITEM_FRAME,
    GLOW_SQUID,
    GOAT,
    GUARDIAN,
    HAPPY_GHAST,
    HOGLIN,
    // 1.20.5: MINECART_HOPPER -> HOPPER_MINECART
    HOPPER_MINECART("MINECART_HOPPER"),
    HORSE,
    HUSK,
    ILLUSIONER,
    INTERACTION,
    IRON_GOLEM,
    // 1.20.5: DROPPED_ITEM -> ITEM
    ITEM("DROPPED_ITEM"),
    ITEM_DISPLAY,
    ITEM_FRAME,
    JUNGLE_BOAT,
    JUNGLE_CHEST_BOAT,
    // 1.20.5: LEASH_HITCH -> LEASH_KNOT
    LEASH_KNOT("LEASH_HITCH"),
    // 1.20.5: LIGHTNING -> LIGHTNING_BOLT
    LIGHTNING_BOLT("LIGHTNING"),
    LINGERING_POTION,
    LLAMA,
    LLAMA_SPIT,
    MAGMA_CUBE,
    MANGROVE_BOAT,
    MANGROVE_CHEST_BOAT,
    MANNEQUIN,
    MARKER,
    MINECART,
    // 1.20.5: MUSHROOM_COW -> MOOSHROOM
    MOOSHROOM("MUSHROOM_COW"),
    MULE,
    // 1.21.3: BOAT -> boat subvariants (oak, dark_oak, ...)
    OAK_BOAT("BOAT"),
    // 1.21.3: CHEST_BOAT -> chest boat subvariants (oak, dark_oak, ...)
    OAK_CHEST_BOAT("CHEST_BOAT"),
    OCELOT,
    OMINOUS_ITEM_SPAWNER,
    PAINTING,
    PALE_OAK_BOAT,
    PALE_OAK_CHEST_BOAT,
    PANDA,
    PARROT,
    PHANTOM,
    PIG,
    PIGLIN,
    PIGLIN_BRUTE,
    PILLAGER,
    PLAYER,
    POLAR_BEAR,
    PUFFERFISH,
    RABBIT,
    RAVAGER,
    SALMON,
    SHEEP,
    SHULKER,
    SHULKER_BULLET,
    SILVERFISH,
    SKELETON,
    SKELETON_HORSE,
    SLIME,
    SMALL_FIREBALL,
    SNIFFER,
    // 1.20.5: SNOWMAN -> SNOW_GOLEM
    SNOW_GOLEM("SNOWMAN"),
    SNOWBALL,
    // 1.20.5: MINECART_MOB_SPAWNER -> SPAWNER_MINECART
    SPAWNER_MINECART("MINECART_MOB_SPAWNER"),
    SPECTRAL_ARROW,
    SPIDER,
    // 1.21.5: POTION -> SPLASH_POTION + LINGERING_POTION (use SPLASH_POTION FOR compatibility)
    SPLASH_POTION("POTION"),
    SPRUCE_BOAT,
    SPRUCE_CHEST_BOAT,
    SQUID,
    STRAY,
    STRIDER,
    TADPOLE,
    TEXT_DISPLAY,
    // 1.20.5: PRIMED_TNT -> TNT
    TNT("PRIMED_TNT"),
    // 1.20.5: MINECART_TNT -> TNT_MINECART
    TNT_MINECART("MINECART_TNT"),
    TRADER_LLAMA,
    TRIDENT,
    TROPICAL_FISH,
    TURTLE,
    UNKNOWN,
    VEX,
    VILLAGER,
    VINDICATOR,
    WANDERING_TRADER,
    WARDEN,
    WIND_CHARGE,
    WITCH,
    WITHER,
    WITHER_SKELETON,
    WITHER_SKULL,
    WOLF,
    ZOGLIN,
    ZOMBIE,
    ZOMBIE_HORSE,
    ZOMBIE_VILLAGER,
    ZOMBIFIED_PIGLIN;

    private static final Logger LOGGER = LoggerFactory.getLogger("PlotSquared/" + EntityType.class.getSimpleName());
    private static final EnumMap<org.bukkit.entity.EntityType, EntityType> BUKKIT_TO_INTERNAL =
            new EnumMap<>(org.bukkit.entity.EntityType.class);

    private final org.bukkit.entity.EntityType bukkitType;

    EntityType(final String... additionalBukkitEnumFields) {
        org.bukkit.entity.EntityType temp = null;
        try {
            temp = org.bukkit.entity.EntityType.valueOf(name());
        } catch (IllegalArgumentException ignored) {
            if (additionalBukkitEnumFields.length > 0) {
                temp = Enums.findByValue(org.bukkit.entity.EntityType.class, additionalBukkitEnumFields);
            }
        }
        bukkitType = temp;
    }

    public @Nullable org.bukkit.entity.EntityType bukkitType() {
        return bukkitType;
    }

    public boolean isAlive() {
        return this.bukkitType != null && this.bukkitType.isAlive();
    }

    public boolean isSpawnable() {
        return this.bukkitType != null && this.bukkitType.isSpawnable();
    }

    @Override
    public String toString() {
        return "EntityType{name=" + name() + "}";
    }

    public static EntityType of(org.bukkit.entity.EntityType bukkitType) {
        return BUKKIT_TO_INTERNAL.get(bukkitType);
    }

    public static EntityType of(Entity entity) {
        return of(entity.getType());
    }

    static {
        // Register all entity types with their aliases
        for (final EntityType value : values()) {
            org.bukkit.entity.EntityType bukkitType = value.bukkitType();
            if (bukkitType != null) {
                BUKKIT_TO_INTERNAL.put(bukkitType, value);
            }
        }
        // Make sure this wrapper contains EVERY possible entity type available by the server
        for (final org.bukkit.entity.EntityType bukkitType : org.bukkit.entity.EntityType.values()) {
            if (!BUKKIT_TO_INTERNAL.containsKey(bukkitType)) {
                LOGGER.error(
                        "EntityType {} provided by server has no wrapper equivalent. Is this server version supported? " +
                                "Falling back to 'UNKNOWN' type.",
                        bukkitType
                );
                BUKKIT_TO_INTERNAL.put(bukkitType, EntityType.UNKNOWN);
            }
        }
    }

}
