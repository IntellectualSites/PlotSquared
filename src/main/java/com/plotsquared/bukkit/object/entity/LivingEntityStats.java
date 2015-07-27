package com.plotsquared.bukkit.object.entity;

import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;

import java.util.Collection;

public class LivingEntityStats {
    public boolean loot;
    public String name;
    public boolean visible;
    public float health;
    public short air;
    public boolean persistent;
    public boolean leashed;
    public short leash_x;
    public short leash_y;
    public short leash_z;
    public boolean equipped;
    public ItemStack hands;
    public ItemStack helmet;
    public ItemStack boots;
    public ItemStack leggings;
    public ItemStack chestplate;
    public Collection<PotionEffect> potions;
}
