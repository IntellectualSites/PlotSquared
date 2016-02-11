package com.plotsquared.bukkit.object.entity;

import com.intellectualcrafters.plot.PS;
import org.bukkit.Art;
import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.Rotation;
import org.bukkit.World;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Ageable;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Guardian;
import org.bukkit.entity.Horse;
import org.bukkit.entity.Horse.Color;
import org.bukkit.entity.Horse.Style;
import org.bukkit.entity.Horse.Variant;
import org.bukkit.entity.Item;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Painting;
import org.bukkit.entity.Rabbit;
import org.bukkit.entity.Rabbit.Type;
import org.bukkit.entity.Sheep;
import org.bukkit.entity.Skeleton;
import org.bukkit.entity.Skeleton.SkeletonType;
import org.bukkit.entity.Tameable;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.EulerAngle;
import org.bukkit.util.Vector;

public class EntityWrapper {
    public short id;
    public float yaw;
    public float pitch;
    public double x;
    public double y;
    public double z;
    public short depth;
    public EntityBaseStats base = null;
    // Extended
    public ItemStack stack;
    public ItemStack[] inventory;
    public byte dataByte;
    public byte dataByte2;
    public String dataString;
    public LivingEntityStats lived;
    public AgeableStats aged;
    public TameableStats tamed;
    private HorseStats horse;
    private ArmorStandStats stand;
    
    private int hash;
    
    @Override
    public boolean equals(final Object obj) {
        return hash == obj.hashCode();
    }
    
    @Override
    public int hashCode() {
        return hash;
    }
    
    public void storeInventory(final InventoryHolder held) {
        inventory = held.getInventory().getContents().clone();
    }
    
    private void restoreLiving(final LivingEntity entity) {
        entity.setCanPickupItems(lived.loot);
        if (lived.name != null) {
            entity.setCustomName(lived.name);
            entity.setCustomNameVisible(lived.visible);
        }
        if ((lived.potions != null) && (lived.potions.size() > 0)) {
            entity.addPotionEffects(lived.potions);
        }
        entity.setRemainingAir(lived.air);
        entity.setRemoveWhenFarAway(lived.persistent);
        if (lived.equipped) {
            final EntityEquipment equipment = entity.getEquipment();
            equipment.setItemInHand(lived.hands);
            equipment.setHelmet(lived.helmet);
            equipment.setChestplate(lived.chestplate);
            equipment.setLeggings(lived.leggings);
            equipment.setBoots(lived.boots);
        }
        if (lived.leashed) {
            // TODO leashes
            //            World world = entity.getWorld();
            //            Entity leash = world.spawnEntity(new Location(world, Math.floor(x) + lived.leash_x, Math.floor(y) + lived.leash_y, Math.floor(z) + lived.leash_z), EntityType.LEASH_HITCH);
            //            entity.setLeashHolder(leash);
        }
    }
    
    private void restoreInventory(final InventoryHolder entity) {
        entity.getInventory().setContents(inventory);
    }
    
    public void storeLiving(final LivingEntity lived) {
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
            final Location loc = lived.getLeashHolder().getLocation();
            this.lived.leash_x = (short) (x - loc.getBlockX());
            this.lived.leash_y = (short) (y - loc.getBlockY());
            this.lived.leash_z = (short) (z - loc.getBlockZ());
        }
        final EntityEquipment equipment = lived.getEquipment();
        this.lived.equipped = equipment != null;
        if (this.lived.equipped) {
            this.lived.hands = equipment.getItemInHand().clone();
            this.lived.boots = equipment.getBoots().clone();
            this.lived.leggings = equipment.getLeggings().clone();
            this.lived.chestplate = equipment.getChestplate().clone();
            this.lived.helmet = equipment.getHelmet().clone();
        }
    }
    
    private void restoreTameable(final Tameable entity) {
        if (tamed.tamed) {
            if (tamed.owner != null) {
                entity.setTamed(true);
                entity.setOwner(tamed.owner);
            }
        }
    }
    
    private void restoreAgeable(final Ageable entity) {
        if (!aged.adult) {
            entity.setBaby();
        }
        entity.setAgeLock(aged.locked);
        if (aged.age > 0) {
            entity.setAge(aged.age);
        }
    }
    
    public void storeAgeable(final Ageable aged) {
        this.aged = new AgeableStats();
        this.aged.age = aged.getAge();
        this.aged.locked = aged.getAgeLock();
        this.aged.adult = aged.isAdult();
    }
    
    public void storeTameable(final Tameable tamed) {
        this.tamed = new TameableStats();
        this.tamed.owner = tamed.getOwner();
        this.tamed.tamed = tamed.isTamed();
    }
    
    @SuppressWarnings("deprecation")
    public EntityWrapper(final org.bukkit.entity.Entity entity, final short depth) {
        hash = entity.getEntityId();
        this.depth = depth;
        final Location loc = entity.getLocation();
        yaw = loc.getYaw();
        pitch = loc.getPitch();
        x = loc.getX();
        y = loc.getY();
        z = loc.getZ();
        id = entity.getType().getTypeId();
        if (depth == 0) {
            return;
        }
        base = new EntityBaseStats();
        final Entity p = entity.getPassenger();
        if (p != null) {
            base.passenger = new EntityWrapper(p, depth);
        }
        base.fall = entity.getFallDistance();
        base.fire = (short) entity.getFireTicks();
        base.age = entity.getTicksLived();
        final Vector velocity = entity.getVelocity();
        base.v_x = velocity.getX();
        base.v_y = velocity.getY();
        base.v_z = velocity.getZ();
        if (depth == 1) {
            return;
        }
        switch (entity.getType()) {
            case ARROW:
            case BOAT:
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
            case UNKNOWN: {
                // Do this stuff later
                return;
            }
            default: {
                PS.debug("&cCOULD NOT IDENTIFY ENTITY: " + entity.getType());
                return;
            }
            // MISC //
            case DROPPED_ITEM: {
                final Item item = (Item) entity;
                stack = item.getItemStack();
                return;
            }
            case ITEM_FRAME: {
                final ItemFrame itemframe = (ItemFrame) entity;
                x = Math.floor(x);
                y = Math.floor(y);
                z = Math.floor(z);
                dataByte = getOrdinal(Rotation.values(), itemframe.getRotation());
                stack = itemframe.getItem().clone();
                return;
            }
            case PAINTING: {
                final Painting painting = (Painting) entity;
                x = Math.floor(x);
                y = Math.floor(y);
                z = Math.floor(z);
                final Art a = painting.getArt();
                dataByte = getOrdinal(BlockFace.values(), painting.getFacing());
                final int h = a.getBlockHeight();
                if ((h % 2) == 0) {
                    y -= 1;
                }
                dataString = a.name();
                return;
            }
            // END MISC //
            // INVENTORY HOLDER //
            case MINECART_CHEST: {
                storeInventory((InventoryHolder) entity);
                return;
            }
            case MINECART_HOPPER: {
                storeInventory((InventoryHolder) entity);
                return;
            }
            // START LIVING ENTITY //
            // START AGEABLE //
            // START TAMEABLE //
            case HORSE: {
                final Horse horse = (Horse) entity;
                this.horse = new HorseStats();
                this.horse.jump = horse.getJumpStrength();
                this.horse.chest = horse.isCarryingChest();
                this.horse.variant = getOrdinal(Variant.values(), horse.getVariant());
                this.horse.style = getOrdinal(Style.values(), horse.getStyle());
                this.horse.color = getOrdinal(Color.values(), horse.getColor());
                storeTameable((Tameable) entity);
                storeAgeable((Ageable) entity);
                storeLiving((LivingEntity) entity);
                storeInventory((InventoryHolder) entity);
                return;
            }
            // END INVENTORY HOLDER //
            case WOLF:
            case OCELOT: {
                storeTameable((Tameable) entity);
                storeAgeable((Ageable) entity);
                storeLiving((LivingEntity) entity);
                return;
            }
            // END AMEABLE //
            case SHEEP: {
                final Sheep sheep = (Sheep) entity;
                dataByte = (byte) ((sheep).isSheared() ? 1 : 0);
                dataByte2 = sheep.getColor().getDyeData();
                storeAgeable((Ageable) entity);
                storeLiving((LivingEntity) entity);
                return;
            }
            case VILLAGER:
            case CHICKEN:
            case COW:
            case MUSHROOM_COW:
            case PIG: {
                storeAgeable((Ageable) entity);
                storeLiving((LivingEntity) entity);
                return;
            }
            // END AGEABLE //
            case RABBIT: { // NEW
                dataByte = getOrdinal(Type.values(), ((Rabbit) entity).getRabbitType());
                storeAgeable((Ageable) entity);
                storeLiving((LivingEntity) entity);
                return;
            }
            case GUARDIAN: { // NEW
                dataByte = (byte) (((Guardian) entity).isElder() ? 1 : 0);
                storeLiving((LivingEntity) entity);
                return;
            }
            case SKELETON: { // NEW
                dataByte = (byte) ((Skeleton) entity).getSkeletonType().getId();
                storeLiving((LivingEntity) entity);
                return;
            }
            case ARMOR_STAND: { // NEW
                                // CHECK positions
                final ArmorStand stand = (ArmorStand) entity;
                inventory = new ItemStack[] { stand.getItemInHand().clone(), stand.getHelmet().clone(), stand.getChestplate().clone(), stand.getLeggings().clone(), stand.getBoots().clone() };
                storeLiving((LivingEntity) entity);
                this.stand = new ArmorStandStats();
                
                final EulerAngle head = stand.getHeadPose();
                this.stand.head[0] = (float) head.getX();
                this.stand.head[1] = (float) head.getY();
                this.stand.head[2] = (float) head.getZ();
                
                final EulerAngle body = stand.getBodyPose();
                this.stand.body[0] = (float) body.getX();
                this.stand.body[1] = (float) body.getY();
                this.stand.body[2] = (float) body.getZ();
                
                final EulerAngle leftLeg = stand.getLeftLegPose();
                this.stand.leftLeg[0] = (float) leftLeg.getX();
                this.stand.leftLeg[1] = (float) leftLeg.getY();
                this.stand.leftLeg[2] = (float) leftLeg.getZ();
                
                final EulerAngle rightLeg = stand.getRightLegPose();
                this.stand.rightLeg[0] = (float) rightLeg.getX();
                this.stand.rightLeg[1] = (float) rightLeg.getY();
                this.stand.rightLeg[2] = (float) rightLeg.getZ();
                
                final EulerAngle leftArm = stand.getLeftArmPose();
                this.stand.leftArm[0] = (float) leftArm.getX();
                this.stand.leftArm[1] = (float) leftArm.getY();
                this.stand.leftArm[2] = (float) leftArm.getZ();
                
                final EulerAngle rightArm = stand.getRightArmPose();
                this.stand.rightArm[0] = (float) rightArm.getX();
                this.stand.rightArm[1] = (float) rightArm.getY();
                this.stand.rightArm[2] = (float) rightArm.getZ();
                
                if (stand.hasArms()) {
                    this.stand.arms = true;
                }
                if (!stand.hasBasePlate()) {
                    this.stand.noplate = true;
                }
                if (!stand.hasGravity()) {
                    this.stand.nogravity = true;
                }
                if (!stand.isVisible()) {
                    this.stand.invisible = true;
                }
                if (stand.isSmall()) {
                    this.stand.small = true;
                }
                return;
            }
            case ENDERMITE: // NEW
            case BAT:
            case ENDER_DRAGON:
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
            case IRON_GOLEM: {
                storeLiving((LivingEntity) entity);
                return;
            }
            // END LIVING //
        }
    }
    
    @SuppressWarnings("deprecation")
    public Entity spawn(final World world, final int x_offset, final int z_offset) {
        final Location loc = new Location(world, x + x_offset, y, z + z_offset);
        loc.setYaw(yaw);
        loc.setPitch(pitch);
        if (id == -1) {
            return null;
        }
        final EntityType type = EntityType.fromId(id);
        Entity entity;
        switch (type) {
            case DROPPED_ITEM: {
                return world.dropItem(loc, stack);
            }
            case PLAYER:
            case LEASH_HITCH: {
                return null;
            }
            case ITEM_FRAME: {
                entity = world.spawn(loc, ItemFrame.class);
                break;
            }
            case PAINTING: {
                entity = world.spawn(loc, Painting.class);
                break;
            }
            default:
                entity = world.spawnEntity(loc, type);
                break;
        }
        if (depth == 0) {
            return entity;
        }
        if (base.passenger != null) {
            try {
                entity.setPassenger(base.passenger.spawn(world, x_offset, z_offset));
            } catch (final Exception e) {}
        }
        if (base.fall != 0) {
            entity.setFallDistance(base.fall);
        }
        if (base.fire != 0) {
            entity.setFireTicks(base.fire);
        }
        if (base.age != 0) {
            entity.setTicksLived(base.age);
        }
        entity.setVelocity(new Vector(base.v_x, base.v_y, base.v_z));
        if (depth == 1) {
            return entity;
        }
        switch (entity.getType()) {
            case ARROW:
            case BOAT:
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
            case SPLASH_POTION:
            case THROWN_EXP_BOTTLE:
            case WEATHER:
            case WITHER_SKULL:
            case MINECART_FURNACE:
            case UNKNOWN: {
                // Do this stuff later
                return entity;
            }
            default: {
                PS.debug("&cCOULD NOT IDENTIFY ENTITY: " + entity.getType());
                return entity;
            }
            // MISC //
            case ITEM_FRAME: {
                final ItemFrame itemframe = (ItemFrame) entity;
                itemframe.setRotation(Rotation.values()[dataByte]);
                itemframe.setItem(stack);
                return entity;
            }
            case PAINTING: {
                final Painting painting = (Painting) entity;
                painting.setFacingDirection(BlockFace.values()[dataByte], true);
                painting.setArt(Art.getByName(dataString), true);
                return entity;
            }
            // END MISC //
            // INVENTORY HOLDER //
            case MINECART_CHEST: {
                restoreInventory((InventoryHolder) entity);
                return entity;
            }
            case MINECART_HOPPER: {
                restoreInventory((InventoryHolder) entity);
                return entity;
            }
            // START LIVING ENTITY //
            // START AGEABLE //
            // START TAMEABLE //
            case HORSE: {
                final Horse horse = (Horse) entity;
                horse.setJumpStrength(this.horse.jump);
                horse.setCarryingChest(this.horse.chest);
                horse.setVariant(Variant.values()[this.horse.variant]);
                horse.setStyle(Style.values()[this.horse.style]);
                horse.setColor(Color.values()[this.horse.color]);
                restoreTameable((Tameable) entity);
                restoreAgeable((Ageable) entity);
                restoreLiving((LivingEntity) entity);
                restoreInventory((InventoryHolder) entity);
                return entity;
            }
            // END INVENTORY HOLDER //
            case WOLF:
            case OCELOT: {
                restoreTameable((Tameable) entity);
                restoreAgeable((Ageable) entity);
                restoreLiving((LivingEntity) entity);
                return entity;
            }
            // END AGEABLE //
            case SHEEP: {
                final Sheep sheep = (Sheep) entity;
                if (dataByte == 1) {
                    sheep.setSheared(true);
                }
                if (dataByte2 != 0) {
                    sheep.setColor(DyeColor.getByDyeData(dataByte2));
                }
                restoreAgeable((Ageable) entity);
                restoreLiving((LivingEntity) entity);
                return entity;
            }
            case VILLAGER:
            case CHICKEN:
            case COW:
            case MUSHROOM_COW:
            case PIG: {
                restoreAgeable((Ageable) entity);
                restoreLiving((LivingEntity) entity);
                return entity;
            }
            // END AGEABLE //
            case RABBIT: { // NEW
                if (dataByte != 0) {
                    ((Rabbit) entity).setRabbitType(Type.values()[dataByte]);
                }
                restoreAgeable((Ageable) entity);
                restoreLiving((LivingEntity) entity);
                return entity;
            }
            case GUARDIAN: { // NEW
                if (dataByte != 0) {
                    ((Guardian) entity).setElder(true);
                }
                restoreLiving((LivingEntity) entity);
                return entity;
            }
            case SKELETON: { // NEW
                if (dataByte != 0) {
                    ((Skeleton) entity).setSkeletonType(SkeletonType.values()[dataByte]);
                }
                storeLiving((LivingEntity) entity);
                return entity;
            }
            case ARMOR_STAND: { // NEW
                                // CHECK positions
                final ArmorStand stand = (ArmorStand) entity;
                if (inventory[0] != null) {
                    stand.setItemInHand(inventory[0]);
                }
                if (inventory[1] != null) {
                    stand.setHelmet(inventory[1]);
                }
                if (inventory[2] != null) {
                    stand.setChestplate(inventory[2]);
                }
                if (inventory[3] != null) {
                    stand.setLeggings(inventory[3]);
                }
                if (inventory[4] != null) {
                    stand.setBoots(inventory[4]);
                }
                if ((this.stand.head[0] != 0) || (this.stand.head[1] != 0) || (this.stand.head[2] != 0)) {
                    final EulerAngle pose = new EulerAngle(this.stand.head[0], this.stand.head[1], this.stand.head[2]);
                    stand.setHeadPose(pose);
                }
                if ((this.stand.body[0] != 0) || (this.stand.body[1] != 0) || (this.stand.body[2] != 0)) {
                    final EulerAngle pose = new EulerAngle(this.stand.body[0], this.stand.body[1], this.stand.body[2]);
                    stand.setBodyPose(pose);
                }
                if ((this.stand.leftLeg[0] != 0) || (this.stand.leftLeg[1] != 0) || (this.stand.leftLeg[2] != 0)) {
                    final EulerAngle pose = new EulerAngle(this.stand.leftLeg[0], this.stand.leftLeg[1], this.stand.leftLeg[2]);
                    stand.setLeftLegPose(pose);
                }
                if ((this.stand.rightLeg[0] != 0) || (this.stand.rightLeg[1] != 0) || (this.stand.rightLeg[2] != 0)) {
                    final EulerAngle pose = new EulerAngle(this.stand.rightLeg[0], this.stand.rightLeg[1], this.stand.rightLeg[2]);
                    stand.setRightLegPose(pose);
                }
                if ((this.stand.leftArm[0] != 0) || (this.stand.leftArm[1] != 0) || (this.stand.leftArm[2] != 0)) {
                    final EulerAngle pose = new EulerAngle(this.stand.leftArm[0], this.stand.leftArm[1], this.stand.leftArm[2]);
                    stand.setLeftArmPose(pose);
                }
                if ((this.stand.rightArm[0] != 0) || (this.stand.rightArm[1] != 0) || (this.stand.rightArm[2] != 0)) {
                    final EulerAngle pose = new EulerAngle(this.stand.rightArm[0], this.stand.rightArm[1], this.stand.rightArm[2]);
                    stand.setRightArmPose(pose);
                }
                if (this.stand.invisible) {
                    stand.setVisible(false);
                }
                if (this.stand.arms) {
                    stand.setArms(true);
                }
                if (this.stand.nogravity) {
                    stand.setGravity(false);
                }
                if (this.stand.noplate) {
                    stand.setBasePlate(false);
                }
                if (this.stand.small) {
                    stand.setSmall(true);
                }
                restoreLiving((LivingEntity) entity);
                return entity;
            }
            case ENDERMITE: // NEW
            case BAT:
            case ENDER_DRAGON:
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
            case IRON_GOLEM: {
                restoreLiving((LivingEntity) entity);
                return entity;
            }
            // END LIVING //
        }
    }
    
    private byte getOrdinal(final Object[] list, final Object value) {
        for (byte i = 0; i < list.length; i++) {
            if (list[i].equals(value)) {
                return i;
            }
        }
        return 0;
    }
}
