package com.github.intellectualsites.plotsquared.bukkit.object.entity;

import com.github.intellectualsites.plotsquared.plot.PlotSquared;
import org.bukkit.*;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.*;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.EulerAngle;
import org.bukkit.util.Vector;

public final class ReplicatingEntityWrapper extends EntityWrapper {

  private final short depth;
  private final int hash;
  private final EntityBaseStats base = new EntityBaseStats();

  public ItemStack[] inventory;
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

  public ReplicatingEntityWrapper(Entity entity, short depth) {
    super(entity);

    this.hash = entity.getEntityId();
    this.depth = depth;

    if (depth == 0) {
      return;
    }
    Entity passenger = entity.getPassenger();
    if (passenger != null) {
      this.base.passenger = new ReplicatingEntityWrapper(passenger, depth);
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
    switch (entity.getType()) {
      case BOAT:
        Boat boat = (Boat) entity;
        this.dataByte = getOrdinal(TreeSpecies.values(), boat.getWoodType());
        return;
      case ARROW:
      case COMPLEX_PART:
      case EGG:
      case ENDER_CRYSTAL:
      case ENDER_PEARL:
      case ENDER_SIGNAL:
      case EXPERIENCE_ORB:
      case FALLING_BLOCK:
      case FIREBALL:
      case FIREWORK:
      case FISHING_HOOK:
      case LEASH_HITCH:
      case LIGHTNING:
      case MINECART:
      case MINECART_COMMAND:
      case MINECART_MOB_SPAWNER:
      case MINECART_TNT:
      case PLAYER:
      case PRIMED_TNT:
      case SLIME:
      case SMALL_FIREBALL:
      case SNOWBALL:
      case MINECART_FURNACE:
      case SPLASH_POTION:
      case THROWN_EXP_BOTTLE:
      case WEATHER:
      case WITHER_SKULL:
      case UNKNOWN:
      case TIPPED_ARROW:
      case SPECTRAL_ARROW:
      case SHULKER_BULLET:
      case DRAGON_FIREBALL:
      case LINGERING_POTION:
      case AREA_EFFECT_CLOUD:
        // Do this stuff later
        return;
      // MISC //
      case DROPPED_ITEM:
        Item item = (Item) entity;
        this.stack = item.getItemStack();
        return;
      case ITEM_FRAME:
        this.x = Math.floor(this.x);
        this.y = Math.floor(this.y);
        this.z = Math.floor(this.z);
        ItemFrame itemFrame = (ItemFrame) entity;
        this.dataByte = getOrdinal(Rotation.values(), itemFrame.getRotation());
        this.stack = itemFrame.getItem().clone();
        return;
      case PAINTING:
        this.x = Math.floor(this.x);
        this.y = Math.floor(this.y);
        this.z = Math.floor(this.z);
        Painting painting = (Painting) entity;
        Art art = painting.getArt();
        this.dataByte = getOrdinal(BlockFace.values(), painting.getFacing());
        int h = art.getBlockHeight();
        if (h % 2 == 0) {
          this.y -= 1;
        }
        this.dataString = art.name();
        return;
      // END MISC //
      // INVENTORY HOLDER //
      case MINECART_CHEST:
      case MINECART_HOPPER:
        storeInventory((InventoryHolder) entity);
        return;
      // START LIVING ENTITY //
      // START AGEABLE //
      // START TAMEABLE //
      case HORSE:
        Horse horse = (Horse) entity;
        this.horse = new HorseStats();
        this.horse.jump = horse.getJumpStrength();
        if (horse instanceof ChestedHorse) {
          this.horse.chest = ((ChestedHorse) horse).isCarryingChest();
        } else {
          this.horse.chest = false;
        }
        this.horse.variant = horse.getVariant();
        this.horse.style = horse.getStyle();
        this.horse.color = horse.getColor();
        storeTameable(horse);
        storeAgeable(horse);
        storeLiving(horse);
        storeInventory(horse);
        return;
      // END INVENTORY HOLDER //
      case WOLF:
      case OCELOT:
        storeTameable((Tameable) entity);
        storeAgeable((Ageable) entity);
        storeLiving((LivingEntity) entity);
        return;
      // END TAMEABLE //
      case SHEEP:
        Sheep sheep = (Sheep) entity;
        this.dataByte = (byte) (sheep.isSheared() ? 1 : 0);
        this.dataByte2 = sheep.getColor().getDyeData();
        storeAgeable(sheep);
        storeLiving(sheep);
        return;
      case VILLAGER:
      case CHICKEN:
      case COW:
      case MUSHROOM_COW:
      case PIG:
      case POLAR_BEAR:
        storeAgeable((Ageable) entity);
        storeLiving((LivingEntity) entity);
        return;
      case RABBIT:
        this.dataByte = getOrdinal(Rabbit.Type.values(), ((Rabbit) entity).getRabbitType());
        storeAgeable((Ageable) entity);
        storeLiving((LivingEntity) entity);
        return;
      // END AGEABLE //
      case GUARDIAN:
        //todo no longer works (possible exception thrown)
        this.dataByte = (byte) (((Guardian) entity).isElder() ? 1 : 0);
        storeLiving((LivingEntity) entity);
        return;
      case SKELETON:
        //todo no longer works (possible exception thrown)
        this.dataByte = getOrdinal(Skeleton.SkeletonType.values(),
            ((Skeleton) entity).getSkeletonType());
        storeLiving((LivingEntity) entity);
        return;
      case ARMOR_STAND:
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
      case ENDERMITE:
        return;
      case BAT:
        if (((Bat) entity).isAwake()) {
          this.dataByte = (byte) 1;
        } else {
          this.dataByte = (byte) 0;
        }
        return;
      case ENDER_DRAGON:
        EnderDragon entity1 = (EnderDragon) entity;
        this.dataByte = (byte) entity1.getPhase().ordinal();
        return;
      case GHAST:
      case MAGMA_CUBE:
      case SQUID:
      case PIG_ZOMBIE:
      case ZOMBIE:
      case WITHER:
      case WITCH:
      case SPIDER:
      case CAVE_SPIDER:
      case SILVERFISH:
      case GIANT:
      case ENDERMAN:
      case CREEPER:
      case BLAZE:
      case SHULKER:
      case SNOWMAN:
        storeLiving((LivingEntity) entity);
        return;
      case IRON_GOLEM:
        if (((IronGolem) entity).isPlayerCreated()) {
          this.dataByte = (byte) 1;
        } else {
          this.dataByte = (byte) 0;
        }
        storeLiving((LivingEntity) entity);
        // END LIVING //
      default:
        PlotSquared.debug("&cCOULD NOT IDENTIFY ENTITY: " + entity.getType());
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
      //            Entity leash = world.spawnEntity(new Location(world, Math.floor(x) + lived.leashX, Math.floor(y) + lived.leashY, Math
      // .floor(z) + lived.leashZ), EntityType.LEASH_HITCH);
      //            entity.setLeashHolder(leash);
    }
  }

  void restoreEquipment(LivingEntity entity) {
    EntityEquipment equipment = entity.getEquipment();
    equipment.setItemInMainHand(this.lived.mainHand);
    equipment.setItemInOffHand(this.lived.offHand);
    equipment.setHelmet(this.lived.helmet);
    equipment.setChestplate(this.lived.chestplate);
    equipment.setLeggings(this.lived.leggings);
    equipment.setBoots(this.lived.boots);
  }

  private void restoreInventory(InventoryHolder entity) {
    try {
      entity.getInventory().setContents(this.inventory);
    } catch (IllegalArgumentException e) {
      PlotSquared.debug("&c[WARN] Failed to restore inventory.\n Reason: " + e.getMessage());
    }
  }

  public void storeLiving(LivingEntity lived) {
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
      this.lived.leashX = (short) (this.x - location.getBlockX());
      this.lived.leashY = (short) (this.y - location.getBlockY());
      this.lived.leashZ = (short) (this.z - location.getBlockZ());
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

  private void restoreAgeable(Ageable entity) {
    if (!this.aged.adult) {
      entity.setBaby();
    }
    entity.setAgeLock(this.aged.locked);
    if (this.aged.age > 0) {
      entity.setAge(this.aged.age);
    }
  }

  public void storeAgeable(Ageable aged) {
    this.aged = new AgeableStats();
    this.aged.age = aged.getAge();
    this.aged.locked = aged.getAgeLock();
    this.aged.adult = aged.isAdult();
  }

  public void storeTameable(Tameable tamed) {
    this.tamed = new TameableStats();
    this.tamed.owner = tamed.getOwner();
    this.tamed.tamed = tamed.isTamed();
  }

  @Override
  public Entity spawn(World world, int xOffset, int zOffset) {
    Location location = new Location(world, this.x + xOffset, this.y, this.z + zOffset);
    location.setYaw(this.yaw);
    location.setPitch(this.pitch);
    if (!this.getType().isSpawnable()) {
      return null;
    }
    Entity entity;
    switch (this.getType()) {
      case DROPPED_ITEM:
        return world.dropItem(location, this.stack);
      case PLAYER:
      case LEASH_HITCH:
        return null;
      case ITEM_FRAME:
        entity = world.spawn(location, ItemFrame.class);
        break;
      case PAINTING:
        entity = world.spawn(location, Painting.class);
        break;
      default:
        entity = world.spawnEntity(location, this.getType());
        break;
    }
    if (this.depth == 0) {
      return entity;
    }
    if (this.base.passenger != null) {
      entity.addPassenger(this.base.passenger.spawn(world, xOffset, zOffset));
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
    switch (entity.getType()) {
      case BOAT:
        Boat boat = (Boat) entity;
        boat.setWoodType(TreeSpecies.values()[dataByte]);
        return entity;
      case SLIME:
        ((Slime) entity).setSize(this.dataByte);
        return entity;
      case ARROW:
      case COMPLEX_PART:
      case EGG:
      case ENDER_CRYSTAL:
      case ENDER_PEARL:
      case ENDER_SIGNAL:
      case DROPPED_ITEM:
      case EXPERIENCE_ORB:
      case FALLING_BLOCK:
      case FIREBALL:
      case FIREWORK:
      case FISHING_HOOK:
      case LEASH_HITCH:
      case LIGHTNING:
      case MINECART:
      case MINECART_COMMAND:
      case MINECART_MOB_SPAWNER:
      case MINECART_TNT:
      case PLAYER:
      case PRIMED_TNT:
      case SMALL_FIREBALL:
      case SNOWBALL:
      case SPLASH_POTION:
      case THROWN_EXP_BOTTLE:
      case WEATHER:
      case TIPPED_ARROW:
      case SPECTRAL_ARROW:
      case SHULKER_BULLET:
      case LINGERING_POTION:
      case AREA_EFFECT_CLOUD:
      case DRAGON_FIREBALL:
      case WITHER_SKULL:
      case MINECART_FURNACE:
      case UNKNOWN:
        // Do this stuff later
        return entity;
      // MISC //
      case ITEM_FRAME:
        ItemFrame itemframe = (ItemFrame) entity;
        itemframe.setRotation(Rotation.values()[this.dataByte]);
        itemframe.setItem(this.stack);
        return entity;
      case PAINTING:
        Painting painting = (Painting) entity;
        painting.setFacingDirection(BlockFace.values()[this.dataByte], true);
        painting.setArt(Art.getByName(this.dataString), true);
        return entity;
      // END MISC //
      // INVENTORY HOLDER //
      case MINECART_CHEST:
      case MINECART_HOPPER:
        restoreInventory((InventoryHolder) entity);
        return entity;
      // START LIVING ENTITY //
      // START AGEABLE //
      // START TAMEABLE //
      case HORSE:
        Horse horse = (Horse) entity;
        horse.setJumpStrength(this.horse.jump);
        if (horse instanceof ChestedHorse && this.horse.chest) {
          ((ChestedHorse) horse).setCarryingChest(true);
        }
        //todo broken in 1.13 possible exception thrown
        horse.setVariant(this.horse.variant);
        horse.setStyle(this.horse.style);
        horse.setColor(this.horse.color);
        restoreTameable(horse);
        restoreAgeable(horse);
        restoreLiving(horse);
        restoreInventory(horse);
        return entity;
      // END INVENTORY HOLDER //
      case WOLF:
      case OCELOT:
        restoreTameable((Tameable) entity);
        restoreAgeable((Ageable) entity);
        restoreLiving((LivingEntity) entity);
        return entity;
      // END AGEABLE //
      case SHEEP:
        Sheep sheep = (Sheep) entity;
        if (this.dataByte == 1) {
          sheep.setSheared(true);
        }
        if (this.dataByte2 != 0) {
          sheep.setColor(DyeColor.getByDyeData(this.dataByte2));
        }
        restoreAgeable(sheep);
        restoreLiving(sheep);
        return sheep;
      case VILLAGER:
      case CHICKEN:
      case COW:
      case POLAR_BEAR:
      case MUSHROOM_COW:
      case PIG:
        restoreAgeable((Ageable) entity);
        restoreLiving((LivingEntity) entity);
        return entity;
      // END AGEABLE //
      case RABBIT:
        if (this.dataByte != 0) {
          ((Rabbit) entity).setRabbitType(Rabbit.Type.values()[this.dataByte]);
        }
        restoreAgeable((Ageable) entity);
        restoreLiving((LivingEntity) entity);
        return entity;
      case GUARDIAN:
        if (this.dataByte != 0) {
          //todo broken in 1.13 possible exception thrown

          ((Guardian) entity).setElder(true);
        }
        restoreLiving((LivingEntity) entity);
        return entity;
      case SKELETON:
        if (this.dataByte != 0) {
          //todo broken in 1.13 possible exception thrown
          ((Skeleton) entity)
              .setSkeletonType(Skeleton.SkeletonType.values()[this.dataByte]);
        }
        storeLiving((LivingEntity) entity);
        return entity;
      case ARMOR_STAND:
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
              this.stand.leftLeg[2]);
          stand.setLeftLegPose(pose);
        }
        if (this.stand.rightLeg[0] != 0 || this.stand.rightLeg[1] != 0
            || this.stand.rightLeg[2] != 0) {
          EulerAngle pose = new EulerAngle(this.stand.rightLeg[0], this.stand.rightLeg[1],
              this.stand.rightLeg[2]);
          stand.setRightLegPose(pose);
        }
        if (this.stand.leftArm[0] != 0 || this.stand.leftArm[1] != 0
            || this.stand.leftArm[2] != 0) {
          EulerAngle pose = new EulerAngle(this.stand.leftArm[0], this.stand.leftArm[1],
              this.stand.leftArm[2]);
          stand.setLeftArmPose(pose);
        }
        if (this.stand.rightArm[0] != 0 || this.stand.rightArm[1] != 0
            || this.stand.rightArm[2] != 0) {
          EulerAngle pose = new EulerAngle(this.stand.rightArm[0], this.stand.rightArm[1],
              this.stand.rightArm[2]);
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
      case BAT:
        if (this.dataByte != 0) {
          ((Bat) entity).setAwake(true);
        }
        restoreLiving((LivingEntity) entity);
        return entity;
      case ENDER_DRAGON:
        if (this.dataByte != 0) {
          ((EnderDragon) entity).setPhase(EnderDragon.Phase.values()[this.dataByte]);
        }
        restoreLiving((LivingEntity) entity);
        return entity;
      case ENDERMITE:
      case GHAST:
      case MAGMA_CUBE:
      case SQUID:
      case PIG_ZOMBIE:
      case ZOMBIE:
      case WITHER:
      case WITCH:
      case SPIDER:
      case CAVE_SPIDER:
      case SILVERFISH:
      case GIANT:
      case ENDERMAN:
      case CREEPER:
      case BLAZE:
      case SNOWMAN:
      case SHULKER:
        restoreLiving((LivingEntity) entity);
        return entity;
      case IRON_GOLEM:
        if (this.dataByte != 0) {
          ((IronGolem) entity).setPlayerCreated(true);
        }
        restoreLiving((LivingEntity) entity);
        return entity;
      default:
        PlotSquared.debug("&cCOULD NOT IDENTIFY ENTITY: " + entity.getType());
        return entity;
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
