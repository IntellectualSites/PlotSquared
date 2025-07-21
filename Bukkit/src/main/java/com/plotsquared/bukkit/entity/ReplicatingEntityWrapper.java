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
package com.plotsquared.bukkit.entity;

import com.plotsquared.core.configuration.Settings;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.Art;
import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.Rotation;
import org.bukkit.World;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.AbstractHorse;
import org.bukkit.entity.Ageable;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Bat;
import org.bukkit.entity.Boat;
import org.bukkit.entity.Breedable;
import org.bukkit.entity.ChestBoat;
import org.bukkit.entity.ChestedHorse;
import org.bukkit.entity.EnderDragon;
import org.bukkit.entity.Entity;
import org.bukkit.entity.IronGolem;
import org.bukkit.entity.Item;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Painting;
import org.bukkit.entity.Rabbit;
import org.bukkit.entity.Sheep;
import org.bukkit.entity.Tameable;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.EulerAngle;
import org.bukkit.util.Vector;

import java.util.List;

public final class ReplicatingEntityWrapper extends EntityWrapper {

    private static final Logger LOGGER = LogManager.getLogger("PlotSquared/" + ReplicatingEntityWrapper.class.getSimpleName());

    private final short depth;
    private final int hash;
    private final EntityBaseStats base = new EntityBaseStats();

    private ItemStack[] inventory;
    // Extended
    private ItemStack stack;
    private byte dataByte;
    private byte dataByte2;
    private String dataString;
    private LivingEntityStats lived;
    private AgeableStats aged;
    private TameableStats tamed;
    private ArmorStandStats stand;
    private HorseStats horse;
    private boolean noGravity;

    @SuppressWarnings("deprecation") // Deprecation exists since 1.20, while we support 1.16 onwards
    public ReplicatingEntityWrapper(Entity entity, short depth) {
        super(entity);

        this.hash = entity.getEntityId();
        this.depth = depth;

        if (depth == 0) {
            return;
        }
        List<Entity> passengers = entity.getPassengers();
        if (passengers.size() > 0) {
            this.base.passenger = new ReplicatingEntityWrapper(passengers.get(0), depth);
        }
        this.base.fall = entity.getFallDistance();
        this.base.fire = (short) entity.getFireTicks();
        this.base.age = entity.getTicksLived();
        Vector velocity = entity.getVelocity();
        this.base.vX = velocity.getX();
        this.base.vY = velocity.getY();
        this.base.vZ = velocity.getZ();
        if (depth == 1) {
            return;
        }
        if (!entity.hasGravity()) {
            this.noGravity = true;
        }
        switch (entity.getType().toString()) {
            case "BOAT", "ACACIA_BOAT", "BIRCH_BOAT", "CHERRY_BOAT", "DARK_OAK_BOAT", "JUNGLE_BOAT", "MANGROVE_BOAT",
                 "OAK_BOAT", "PALE_OAK_BOAT", "SPRUCE_BOAT", "BAMBOO_RAFT" -> {
                Boat boat = (Boat) entity;
                this.dataByte = getOrdinal(Boat.Type.values(), boat.getBoatType());
                return;
            }
            case "ACACIA_CHEST_BOAT", "BIRCH_CHEST_BOAT", "CHERRY_CHEST_BOAT", "DARK_OAK_CHEST_BOAT",
                 "JUNGLE_CHEST_BOAT", "MANGROVE_CHEST_BOAT", "OAK_CHEST_BOAT", "PALE_OAK_CHEST_BOAT",
                 "SPRUCE_CHEST_BOAT", "BAMBOO_CHEST_RAFT" -> {
                ChestBoat boat = (ChestBoat) entity;
                this.dataByte = getOrdinal(Boat.Type.values(), boat.getBoatType());
                storeInventory(boat);
            }
            case "ARROW", "EGG", "END_CRYSTAL", "ENDER_CRYSTAL", "ENDER_PEARL", "ENDER_SIGNAL", "EXPERIENCE_ORB", "FALLING_BLOCK", "FIREBALL",
                    "FIREWORK", "FISHING_HOOK", "LEASH_HITCH", "LIGHTNING", "MINECART", "MINECART_COMMAND", "MINECART_MOB_SPAWNER",
                    "MINECART_TNT", "PLAYER", "PRIMED_TNT", "SLIME", "SMALL_FIREBALL", "SNOWBALL", "MINECART_FURNACE", "SPLASH_POTION",
                    "THROWN_EXP_BOTTLE", "WITHER_SKULL", "UNKNOWN", "SPECTRAL_ARROW", "SHULKER_BULLET", "DRAGON_FIREBALL", "AREA_EFFECT_CLOUD",
                    "TRIDENT", "LLAMA_SPIT" -> {
                // Do this stuff later
                return;
            }
            // MISC //
            case "DROPPED_ITEM", "ITEM" -> {
                Item item = (Item) entity;
                this.stack = item.getItemStack();
                return;
            }
            case "ITEM_FRAME" -> {
                this.x = Math.floor(this.getX());
                this.y = Math.floor(this.getY());
                this.z = Math.floor(this.getZ());
                ItemFrame itemFrame = (ItemFrame) entity;
                this.dataByte = getOrdinal(Rotation.values(), itemFrame.getRotation());
                this.stack = itemFrame.getItem().clone();
                return;
            }
            case "PAINTING" -> {
                this.x = Math.floor(this.getX());
                this.y = Math.floor(this.getY());
                this.z = Math.floor(this.getZ());
                Painting painting = (Painting) entity;
                Art art = painting.getArt();
                this.dataByte = getOrdinal(BlockFace.values(), painting.getFacing());
                int h = art.getBlockHeight();
                if (h % 2 == 0) {
                    this.y -= 1;
                }
                this.dataString = art.name();
                return;
            }
            // END MISC //
            // INVENTORY HOLDER //
            case "MINECART_CHEST", "CHEST_MINECART", "MINECART_HOPPER", "HOPPER_MINECART" -> {
                storeInventory((InventoryHolder) entity);
                return;
            }
            // START LIVING ENTITY //
            // START AGEABLE //
            // START TAMEABLE //
            case "CAMEL", "HORSE", "DONKEY", "LLAMA", "TRADER_LLAMA", "MULE", "SKELETON_HORSE", "ZOMBIE_HORSE" -> {
                AbstractHorse horse = (AbstractHorse) entity;
                this.horse = new HorseStats();
                this.horse.jump = horse.getJumpStrength();
                if (horse instanceof ChestedHorse horse1) {
                    this.horse.chest = horse1.isCarryingChest();
                }
                //todo these horse features need fixing
                //this.horse.variant = horse.getVariant();
                //this.horse.style = horse.getStyle();
                //this.horse.color = horse.getColor();
                storeTameable(horse);
                storeBreedable(horse);
                storeLiving(horse);
                storeInventory(horse);
                return;
            }
            // END INVENTORY HOLDER //
            case "WOLF", "OCELOT", "CAT", "PARROT" -> {
                storeTameable((Tameable) entity);
                storeBreedable((Breedable) entity);
                storeLiving((LivingEntity) entity);
                return;
            }
            // END TAMEABLE //
            case "SHEEP" -> {
                Sheep sheep = (Sheep) entity;
                if (sheep.isSheared()) {
                    this.dataByte = (byte) 1;
                } else {
                    this.dataByte = (byte) 0;
                }
                this.dataByte2 = getOrdinal(DyeColor.values(), sheep.getColor());
                storeBreedable(sheep);
                storeLiving(sheep);
                return;
            }
            case "VILLAGER", "CHICKEN", "COW", "MUSHROOM_COW", "PIG", "TURTLE", "POLAR_BEAR" -> {
                storeBreedable((Breedable) entity);
                storeLiving((LivingEntity) entity);
                return;
            }
            case "RABBIT" -> {
                this.dataByte = getOrdinal(Rabbit.Type.values(), ((Rabbit) entity).getRabbitType());
                storeBreedable((Breedable) entity);
                storeLiving((LivingEntity) entity);
                return;
            }
            // END AGEABLE //
            case "ARMOR_STAND" -> {
                ArmorStand stand = (ArmorStand) entity;
                this.inventory =
                        new ItemStack[]{stand.getItemInHand().clone(), stand.getHelmet().clone(),
                                stand.getChestplate().clone(), stand.getLeggings().clone(),
                                stand.getBoots().clone()};
                storeLiving(stand);
                this.stand = new ArmorStandStats();
                EulerAngle head = stand.getHeadPose();
                this.stand.head[0] = (float) head.getX();
                this.stand.head[1] = (float) head.getY();
                this.stand.head[2] = (float) head.getZ();
                EulerAngle body = stand.getBodyPose();
                this.stand.body[0] = (float) body.getX();
                this.stand.body[1] = (float) body.getY();
                this.stand.body[2] = (float) body.getZ();
                EulerAngle leftLeg = stand.getLeftLegPose();
                this.stand.leftLeg[0] = (float) leftLeg.getX();
                this.stand.leftLeg[1] = (float) leftLeg.getY();
                this.stand.leftLeg[2] = (float) leftLeg.getZ();
                EulerAngle rightLeg = stand.getRightLegPose();
                this.stand.rightLeg[0] = (float) rightLeg.getX();
                this.stand.rightLeg[1] = (float) rightLeg.getY();
                this.stand.rightLeg[2] = (float) rightLeg.getZ();
                EulerAngle leftArm = stand.getLeftArmPose();
                this.stand.leftArm[0] = (float) leftArm.getX();
                this.stand.leftArm[1] = (float) leftArm.getY();
                this.stand.leftArm[2] = (float) leftArm.getZ();
                EulerAngle rightArm = stand.getRightArmPose();
                this.stand.rightArm[0] = (float) rightArm.getX();
                this.stand.rightArm[1] = (float) rightArm.getY();
                this.stand.rightArm[2] = (float) rightArm.getZ();
                if (stand.hasArms()) {
                    this.stand.arms = true;
                }
                if (!stand.hasBasePlate()) {
                    this.stand.noPlate = true;
                }
                if (!stand.isVisible()) {
                    this.stand.invisible = true;
                }
                if (stand.isSmall()) {
                    this.stand.small = true;
                }
                return;
            }
            case "ENDERMITE" -> {
                return;
            }
            case "BAT" -> {
                if (((Bat) entity).isAwake()) {
                    this.dataByte = (byte) 1;
                } else {
                    this.dataByte = (byte) 0;
                }
                return;
            }
            case "ENDER_DRAGON" -> {
                EnderDragon entity1 = (EnderDragon) entity;
                this.dataByte = (byte) entity1.getPhase().ordinal();
                return;
            }
            case "SKELETON", "WITHER_SKELETON", "GUARDIAN", "ELDER_GUARDIAN", "GHAST", "HAPPY_GHAST", "GHASTLING", "MAGMA_CUBE", "SQUID", "PIG_ZOMBIE", "HOGLIN",
                    "ZOMBIFIED_PIGLIN", "PIGLIN", "PIGLIN_BRUTE", "ZOMBIE", "WITHER", "WITCH", "SPIDER", "CAVE_SPIDER", "SILVERFISH",
                    "GIANT", "ENDERMAN", "CREEPER", "BLAZE", "SHULKER", "SNOWMAN", "SNOW_GOLEM" -> {
                storeLiving((LivingEntity) entity);
                return;
            }
            case "IRON_GOLEM" -> {
                if (((IronGolem) entity).isPlayerCreated()) {
                    this.dataByte = (byte) 1;
                } else {
                    this.dataByte = (byte) 0;
                }
                storeLiving((LivingEntity) entity);
            }
            // END LIVING //
        }
    }

    @Override
    public boolean equals(Object obj) {
        return this.hash == obj.hashCode();
    }

    @Override
    public int hashCode() {
        return this.hash;
    }

    public void storeInventory(InventoryHolder held) {
        this.inventory = held.getInventory().getContents().clone();
    }

    void restoreLiving(LivingEntity entity) {
        entity.setCanPickupItems(this.lived.loot);
        if (this.lived.name != null) {
            entity.setCustomName(this.lived.name);
            entity.setCustomNameVisible(this.lived.visible);
        }
        if (this.lived.potions != null && !this.lived.potions.isEmpty()) {
            entity.addPotionEffects(this.lived.potions);
        }
        entity.setRemainingAir(this.lived.air);
        entity.setRemoveWhenFarAway(this.lived.persistent);
        if (this.lived.equipped) {
            this.restoreEquipment(entity);
        }
        if (this.lived.leashed) {
            // TODO leashes
            //            World world = entity.getWorld();
            //            Entity leash = world.spawnEntity(new Location(world, Math.floor(x) +
            //            lived.leashX, Math.floor(y) + lived.leashY, Math.floor(z) + lived.leashZ),
            //            EntityType.LEASH_HITCH);
            //            entity.setLeashHolder(leash);
        }
    }

    void restoreEquipment(LivingEntity entity) {
        EntityEquipment equipment = entity.getEquipment();
        if (equipment != null) {
            equipment.setItemInMainHand(this.lived.mainHand);
            equipment.setItemInOffHand(this.lived.offHand);
            equipment.setHelmet(this.lived.helmet);
            equipment.setChestplate(this.lived.chestplate);
            equipment.setLeggings(this.lived.leggings);
            equipment.setBoots(this.lived.boots);
        }
    }

    private void restoreInventory(InventoryHolder entity) {
        try {
            entity.getInventory().setContents(this.inventory);
        } catch (IllegalArgumentException e) {
            LOGGER.error("Failed to restore inventory", e);
        }
    }

    private void storeLiving(LivingEntity lived) {
        this.lived = new LivingEntityStats();
        this.lived.potions = lived.getActivePotionEffects();
        this.lived.loot = lived.getCanPickupItems();
        this.lived.name = lived.getCustomName();
        this.lived.visible = lived.isCustomNameVisible();
        this.lived.health = (float) lived.getHealth();
        this.lived.air = (short) lived.getRemainingAir();
        this.lived.persistent = lived.getRemoveWhenFarAway();
        this.lived.leashed = lived.isLeashed();
        if (this.lived.leashed) {
            Location location = lived.getLeashHolder().getLocation();
            this.lived.leashX = (short) (this.getX() - location.getBlockX());
            this.lived.leashY = (short) (this.getY() - location.getBlockY());
            this.lived.leashZ = (short) (this.getZ() - location.getBlockZ());
        }
        EntityEquipment equipment = lived.getEquipment();
        this.lived.equipped = equipment != null;
        if (this.lived.equipped) {
            storeEquipment(equipment);
        }
    }

    void storeEquipment(EntityEquipment equipment) {
        this.lived.mainHand = equipment.getItemInMainHand().clone();
        this.lived.offHand = equipment.getItemInOffHand().clone();
        this.lived.boots = equipment.getBoots().clone();
        this.lived.leggings = equipment.getLeggings().clone();
        this.lived.chestplate = equipment.getChestplate().clone();
        this.lived.helmet = equipment.getHelmet().clone();
    }

    private void restoreTameable(Tameable entity) {
        if (this.tamed.tamed) {
            if (this.tamed.owner != null) {
                entity.setTamed(true);
                entity.setOwner(this.tamed.owner);
            }
        }
    }

    /**
     * @deprecated Use {@link #restoreBreedable(Breedable)} instead
     * @since 7.1.0
     */
    @Deprecated(forRemoval = true, since = "7.1.0")
    private void restoreAgeable(Ageable entity) {
        if (!this.aged.adult) {
            entity.setBaby();
        }
        entity.setAgeLock(this.aged.locked);
        if (this.aged.age > 0) {
            entity.setAge(this.aged.age);
        }
    }

    /**
     * @deprecated Use {@link #storeBreedable(Breedable)} instead
     * @since 7.1.0
     */
    @Deprecated(forRemoval = true, since = "7.1.0")
    public void storeAgeable(Ageable aged) {
        this.aged = new AgeableStats();
        this.aged.age = aged.getAge();
        this.aged.locked = aged.getAgeLock();
        this.aged.adult = aged.isAdult();
    }

    /**
     * @since 7.1.0
     */
    private void restoreBreedable(Breedable entity) {
        if (!this.aged.adult) {
            entity.setBaby();
        }
        entity.setAgeLock(this.aged.locked);
        if (this.aged.age > 0) {
            entity.setAge(this.aged.age);
        }
    }

    /**
     * @since 7.1.0
     */
    private void storeBreedable(Breedable breedable) {
        this.aged = new AgeableStats();
        this.aged.age = breedable.getAge();
        this.aged.locked = breedable.getAgeLock();
        this.aged.adult = breedable.isAdult();
    }

    public void storeTameable(Tameable tamed) {
        this.tamed = new TameableStats();
        this.tamed.owner = tamed.getOwner();
        this.tamed.tamed = tamed.isTamed();
    }

    @SuppressWarnings("deprecation") // Paper deprecation
    @Override
    public Entity spawn(World world, int xOffset, int zOffset) {
        Location location = new Location(world, this.getX() + xOffset, this.getY(), this.z + zOffset);
        location.setYaw(this.yaw);
        location.setPitch(this.pitch);
        if (!this.getType().isSpawnable()) {
            return null;
        }
        Entity entity;
        switch (this.getType().toString()) {
            case "DROPPED_ITEM", "ITEM" -> {
                return world.dropItem(location, this.stack);
            }
            case "PLAYER", "LEASH_HITCH" -> {
                return null;
            }
            case "ITEM_FRAME" -> entity = world.spawn(location, ItemFrame.class);
            case "PAINTING" -> entity = world.spawn(location, Painting.class);
            default -> entity = world.spawnEntity(location, this.getType());
        }
        if (this.depth == 0) {
            return entity;
        }
        if (this.base.passenger != null) {
            try {
                entity.addPassenger(this.base.passenger.spawn(world, xOffset, zOffset));
            } catch (Exception ignored) {
            }
        }
        if (this.base.fall != 0) {
            entity.setFallDistance(this.base.fall);
        }
        if (this.base.fire != 0) {
            entity.setFireTicks(this.base.fire);
        }
        if (this.base.age != 0) {
            entity.setTicksLived(this.base.age);
        }
        entity.setVelocity(new Vector(this.base.vX, this.base.vY, this.base.vZ));
        if (this.depth == 1) {
            return entity;
        }
        if (this.noGravity) {
            entity.setGravity(false);
        }
        switch (entity.getType().toString()) {
            case "BOAT", "ACACIA_BOAT", "BIRCH_BOAT", "CHERRY_BOAT", "DARK_OAK_BOAT", "JUNGLE_BOAT", "MANGROVE_BOAT",
                 "OAK_BOAT", "PALE_OAK_BOAT", "SPRUCE_BOAT", "BAMBOO_RAFT" -> {
                Boat boat = (Boat) entity;
                boat.setBoatType(Boat.Type.values()[dataByte]);
                return entity;
            }
            case "ACACIA_CHEST_BOAT", "BIRCH_CHEST_BOAT", "CHERRY_CHEST_BOAT", "DARK_OAK_CHEST_BOAT",
                 "JUNGLE_CHEST_BOAT", "MANGROVE_CHEST_BOAT", "OAK_CHEST_BOAT", "PALE_OAK_CHEST_BOAT",
                 "SPRUCE_CHEST_BOAT", "BAMBOO_CHEST_RAFT" -> {
                ChestBoat boat = (ChestBoat) entity;
                boat.setBoatType(Boat.Type.values()[dataByte]);
                restoreInventory(boat);
                return entity;
            }
            // SLIME is not even stored
            /* case "SLIME" -> {
                ((Slime) entity).setSize(this.dataByte);
                return entity;
            } */
            case "ARROW", "EGG", "END_CRYSTAL", "ENDER_CRYSTAL", "ENDER_PEARL", "ENDER_SIGNAL", "DROPPED_ITEM", "EXPERIENCE_ORB", "FALLING_BLOCK",
                    "FIREBALL", "FIREWORK", "FISHING_HOOK", "LEASH_HITCH", "LIGHTNING", "MINECART", "MINECART_COMMAND",
                    "MINECART_MOB_SPAWNER", "MINECART_TNT", "PLAYER", "PRIMED_TNT", "SMALL_FIREBALL", "SNOWBALL",
                    "SPLASH_POTION", "THROWN_EXP_BOTTLE", "SPECTRAL_ARROW", "SHULKER_BULLET", "AREA_EFFECT_CLOUD",
                    "DRAGON_FIREBALL", "WITHER_SKULL", "MINECART_FURNACE", "LLAMA_SPIT", "TRIDENT", "UNKNOWN" -> {
                // Do this stuff later
                return entity;
            }
            // MISC //
            case "ITEM_FRAME" -> {
                ItemFrame itemframe = (ItemFrame) entity;
                itemframe.setRotation(Rotation.values()[this.dataByte]);
                itemframe.setItem(this.stack);
                return entity;
            }
            case "PAINTING" -> {
                Painting painting = (Painting) entity;
                painting.setFacingDirection(BlockFace.values()[this.dataByte], true);
                painting.setArt(Art.getByName(this.dataString), true);
                return entity;
            }
            // END MISC //
            // INVENTORY HOLDER //
            case "MINECART_CHEST", "CHEST_MINECART", "MINECART_HOPPER", "HOPPER_MINECART" -> {
                restoreInventory((InventoryHolder) entity);
                return entity;
            }
            // START LIVING ENTITY //
            // START AGEABLE //
            // START TAMEABLE //
            case "CAMEL", "HORSE", "DONKEY", "LLAMA", "TRADER_LLAMA", "MULE", "SKELETON_HORSE", "ZOMBIE_HORSE" -> {
                AbstractHorse horse = (AbstractHorse) entity;
                horse.setJumpStrength(this.horse.jump);
                if (horse instanceof ChestedHorse) {
                    ((ChestedHorse) horse).setCarryingChest(this.horse.chest);
                }
                //todo broken as of 1.13
                //horse.setVariant(this.horse.variant);
                //horse.setStyle(this.horse.style);
                //horse.setColor(this.horse.color);
                restoreTameable(horse);
                restoreBreedable(horse);
                restoreLiving(horse);
                restoreInventory(horse);
                return entity;
            }
            // END INVENTORY HOLDER //
            case "WOLF", "OCELOT", "CAT", "PARROT" -> {
                restoreTameable((Tameable) entity);
                restoreBreedable((Breedable) entity);
                restoreLiving((LivingEntity) entity);
                return entity;
            }
            // END AGEABLE //
            case "SHEEP" -> {
                Sheep sheep = (Sheep) entity;
                if (this.dataByte == 1) {
                    sheep.setSheared(true);
                }
                if (this.dataByte2 != 0) {
                    sheep.setColor(DyeColor.values()[this.dataByte2]);
                }
                restoreBreedable(sheep);
                restoreLiving(sheep);
                return sheep;
            }
            case "VILLAGER", "CHICKEN", "COW", "TURTLE", "POLAR_BEAR", "MUSHROOM_COW", "PIG" -> {
                restoreBreedable((Breedable) entity);
                restoreLiving((LivingEntity) entity);
                return entity;
            }
            // END AGEABLE //
            case "RABBIT" -> {
                if (this.dataByte != 0) {
                    ((Rabbit) entity).setRabbitType(Rabbit.Type.values()[this.dataByte]);
                }
                restoreBreedable((Breedable) entity);
                restoreLiving((LivingEntity) entity);
                return entity;
            }
            case "ARMOR_STAND" -> {
                // CHECK positions
                ArmorStand stand = (ArmorStand) entity;
                if (this.inventory[0] != null) {
                    stand.setItemInHand(this.inventory[0]);
                }
                if (this.inventory[1] != null) {
                    stand.setHelmet(this.inventory[1]);
                }
                if (this.inventory[2] != null) {
                    stand.setChestplate(this.inventory[2]);
                }
                if (this.inventory[3] != null) {
                    stand.setLeggings(this.inventory[3]);
                }
                if (this.inventory[4] != null) {
                    stand.setBoots(this.inventory[4]);
                }
                if (this.stand.head[0] != 0 || this.stand.head[1] != 0 || this.stand.head[2] != 0) {
                    EulerAngle pose =
                            new EulerAngle(this.stand.head[0], this.stand.head[1], this.stand.head[2]);
                    stand.setHeadPose(pose);
                }
                if (this.stand.body[0] != 0 || this.stand.body[1] != 0 || this.stand.body[2] != 0) {
                    EulerAngle pose =
                            new EulerAngle(this.stand.body[0], this.stand.body[1], this.stand.body[2]);
                    stand.setBodyPose(pose);
                }
                if (this.stand.leftLeg[0] != 0 || this.stand.leftLeg[1] != 0
                        || this.stand.leftLeg[2] != 0) {
                    EulerAngle pose = new EulerAngle(this.stand.leftLeg[0], this.stand.leftLeg[1],
                            this.stand.leftLeg[2]
                    );
                    stand.setLeftLegPose(pose);
                }
                if (this.stand.rightLeg[0] != 0 || this.stand.rightLeg[1] != 0
                        || this.stand.rightLeg[2] != 0) {
                    EulerAngle pose = new EulerAngle(this.stand.rightLeg[0], this.stand.rightLeg[1],
                            this.stand.rightLeg[2]
                    );
                    stand.setRightLegPose(pose);
                }
                if (this.stand.leftArm[0] != 0 || this.stand.leftArm[1] != 0
                        || this.stand.leftArm[2] != 0) {
                    EulerAngle pose = new EulerAngle(this.stand.leftArm[0], this.stand.leftArm[1],
                            this.stand.leftArm[2]
                    );
                    stand.setLeftArmPose(pose);
                }
                if (this.stand.rightArm[0] != 0 || this.stand.rightArm[1] != 0
                        || this.stand.rightArm[2] != 0) {
                    EulerAngle pose = new EulerAngle(this.stand.rightArm[0], this.stand.rightArm[1],
                            this.stand.rightArm[2]
                    );
                    stand.setRightArmPose(pose);
                }
                if (this.stand.invisible) {
                    stand.setVisible(false);
                }
                if (this.stand.arms) {
                    stand.setArms(true);
                }
                if (this.stand.noPlate) {
                    stand.setBasePlate(false);
                }
                if (this.stand.small) {
                    stand.setSmall(true);
                }
                restoreLiving(stand);
                return stand;
            }
            case "BAT" -> {
                if (this.dataByte != 0) {
                    ((Bat) entity).setAwake(true);
                }
                restoreLiving((LivingEntity) entity);
                return entity;
            }
            case "ENDER_DRAGON" -> {
                if (this.dataByte != 0) {
                    ((EnderDragon) entity).setPhase(EnderDragon.Phase.values()[this.dataByte]);
                }
                restoreLiving((LivingEntity) entity);
                return entity;
            }
            case "ENDERMITE", "GHAST", "HAPPY_GHAST", "GHASTLING", "MAGMA_CUBE", "SQUID", "PIG_ZOMBIE", "HOGLIN", "PIGLIN", "ZOMBIFIED_PIGLIN", "PIGLIN_BRUTE", "ZOMBIE", "WITHER", "WITCH", "SPIDER", "CAVE_SPIDER", "SILVERFISH", "GIANT", "ENDERMAN", "CREEPER", "BLAZE", "SNOWMAN", "SHULKER", "GUARDIAN", "ELDER_GUARDIAN", "SKELETON", "WITHER_SKELETON" -> {
                restoreLiving((LivingEntity) entity);
                return entity;
            }
            case "IRON_GOLEM" -> {
                if (this.dataByte != 0) {
                    ((IronGolem) entity).setPlayerCreated(true);
                }
                restoreLiving((LivingEntity) entity);
                return entity;
            }
            default -> {
                if (Settings.DEBUG) {
                    LOGGER.info("Could not identify entity: {}", entity.getType());
                }
                return entity;
            }
            // END LIVING
        }
    }

    public void saveEntity() {
    }

    private byte getOrdinal(Object[] list, Object value) {
        for (byte i = 0; i < list.length; i++) {
            if (list[i].equals(value)) {
                return i;
            }
        }
        return 0;
    }


}
