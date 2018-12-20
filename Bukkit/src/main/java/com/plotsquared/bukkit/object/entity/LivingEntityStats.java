package com.plotsquared.bukkit.object.entity;

import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;

import java.util.Collection;

class LivingEntityStats {

    boolean loot;
    String name;
    boolean visible;
    float health;
    short air;
    boolean persistent;
    boolean leashed;
    short leashX;
    short leashY;
    short leashZ;
    boolean equipped;
    ItemStack mainHand;
    ItemStack helmet;
    ItemStack boots;
    ItemStack leggings;
    ItemStack chestplate;
    Collection<PotionEffect> potions;
    ItemStack offHand;
}
