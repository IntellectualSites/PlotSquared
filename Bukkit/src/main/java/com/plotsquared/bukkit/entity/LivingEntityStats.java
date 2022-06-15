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
