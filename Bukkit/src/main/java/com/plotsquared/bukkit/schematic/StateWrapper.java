/*
 *       _____  _       _    _____                                _
 *      |  __ \| |     | |  / ____|                              | |
 *      | |__) | | ___ | |_| (___   __ _ _   _  __ _ _ __ ___  __| |
 *      |  ___/| |/ _ \| __|\___ \ / _` | | | |/ _` | '__/ _ \/ _` |
 *      | |    | | (_) | |_ ____) | (_| | |_| | (_| | | |  __/ (_| |
 *      |_|    |_|\___/ \__|_____/ \__, |\__,_|\__,_|_|  \___|\__,_|
 *                                    | |
 *                                    |_|
 *            PlotSquared plot management system for Minecraft
 *                  Copyright (C) 2020 IntellectualSites
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.plotsquared.bukkit.schematic;

import com.plotsquared.bukkit.util.BukkitUtil;
import com.sk89q.jnbt.ByteTag;
import com.sk89q.jnbt.CompoundTag;
import com.sk89q.jnbt.ListTag;
import com.sk89q.jnbt.ShortTag;
import com.sk89q.jnbt.StringTag;
import com.sk89q.jnbt.Tag;
import com.sk89q.worldedit.blocks.BaseItemStack;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.world.item.ItemType;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Container;
import org.bukkit.block.Sign;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class StateWrapper {

    public org.bukkit.block.BlockState state = null;
    public CompoundTag tag = null;

    public StateWrapper(org.bukkit.block.BlockState state) {
        this.state = state;
    }

    public StateWrapper(CompoundTag tag) {
        this.tag = tag;
    }

    public static String jsonToColourCode(String str) {
        str = str.replace("{\"extra\":", "").replace("],\"text\":\"\"}", "]")
            .replace("[{\"color\":\"black\",\"text\":\"", "&0")
            .replace("[{\"color\":\"dark_blue\",\"text\":\"", "&1")
            .replace("[{\"color\":\"dark_green\",\"text\":\"", "&2")
            .replace("[{\"color\":\"dark_aqua\",\"text\":\"", "&3")
            .replace("[{\"color\":\"dark_red\",\"text\":\"", "&4")
            .replace("[{\"color\":\"dark_purple\",\"text\":\"", "&5")
            .replace("[{\"color\":\"gold\",\"text\":\"", "&6")
            .replace("[{\"color\":\"gray\",\"text\":\"", "&7")
            .replace("[{\"color\":\"dark_gray\",\"text\":\"", "&8")
            .replace("[{\"color\":\"blue\",\"text\":\"", "&9")
            .replace("[{\"color\":\"green\",\"text\":\"", "&a")
            .replace("[{\"color\":\"aqua\",\"text\":\"", "&b")
            .replace("[{\"color\":\"red\",\"text\":\"", "&c")
            .replace("[{\"color\":\"light_purple\",\"text\":\"", "&d")
            .replace("[{\"color\":\"yellow\",\"text\":\"", "&e")
            .replace("[{\"color\":\"white\",\"text\":\"", "&f")
            .replace("[{\"obfuscated\":true,\"text\":\"", "&k")
            .replace("[{\"bold\":true,\"text\":\"", "&l")
            .replace("[{\"strikethrough\":true,\"text\":\"", "&m")
            .replace("[{\"underlined\":true,\"text\":\"", "&n")
            .replace("[{\"italic\":true,\"text\":\"", "&o").replace("[{\"color\":\"black\",", "&0")
            .replace("[{\"color\":\"dark_blue\",", "&1")
            .replace("[{\"color\":\"dark_green\",", "&2")
            .replace("[{\"color\":\"dark_aqua\",", "&3").replace("[{\"color\":\"dark_red\",", "&4")
            .replace("[{\"color\":\"dark_purple\",", "&5").replace("[{\"color\":\"gold\",", "&6")
            .replace("[{\"color\":\"gray\",", "&7").replace("[{\"color\":\"dark_gray\",", "&8")
            .replace("[{\"color\":\"blue\",", "&9").replace("[{\"color\":\"green\",", "&a")
            .replace("[{\"color\":\"aqua\",", "&b").replace("[{\"color\":\"red\",", "&c")
            .replace("[{\"color\":\"light_purple\",", "&d").replace("[{\"color\":\"yellow\",", "&e")
            .replace("[{\"color\":\"white\",", "&f").replace("[{\"obfuscated\":true,", "&k")
            .replace("[{\"bold\":true,", "&l").replace("[{\"strikethrough\":true,", "&m")
            .replace("[{\"underlined\":true,", "&n").replace("[{\"italic\":true,", "&o")
            .replace("{\"color\":\"black\",\"text\":\"", "&0")
            .replace("{\"color\":\"dark_blue\",\"text\":\"", "&1")
            .replace("{\"color\":\"dark_green\",\"text\":\"", "&2")
            .replace("{\"color\":\"dark_aqua\",\"text\":\"", "&3")
            .replace("{\"color\":\"dark_red\",\"text\":\"", "&4")
            .replace("{\"color\":\"dark_purple\",\"text\":\"", "&5")
            .replace("{\"color\":\"gold\",\"text\":\"", "&6")
            .replace("{\"color\":\"gray\",\"text\":\"", "&7")
            .replace("{\"color\":\"dark_gray\",\"text\":\"", "&8")
            .replace("{\"color\":\"blue\",\"text\":\"", "&9")
            .replace("{\"color\":\"green\",\"text\":\"", "&a")
            .replace("{\"color\":\"aqua\",\"text\":\"", "&b")
            .replace("{\"color\":\"red\",\"text\":\"", "&c")
            .replace("{\"color\":\"light_purple\",\"text\":\"", "&d")
            .replace("{\"color\":\"yellow\",\"text\":\"", "&e")
            .replace("{\"color\":\"white\",\"text\":\"", "&f")
            .replace("{\"obfuscated\":true,\"text\":\"", "&k")
            .replace("{\"bold\":true,\"text\":\"", "&l")
            .replace("{\"strikethrough\":true,\"text\":\"", "&m")
            .replace("{\"underlined\":true,\"text\":\"", "&n")
            .replace("{\"italic\":true,\"text\":\"", "&o").replace("{\"color\":\"black\",", "&0")
            .replace("{\"color\":\"dark_blue\",", "&1").replace("{\"color\":\"dark_green\",", "&2")
            .replace("{\"color\":\"dark_aqua\",", "&3").replace("{\"color\":\"dark_red\",", "&4")
            .replace("{\"color\":\"dark_purple\",", "&5").replace("{\"color\":\"gold\",", "&6")
            .replace("{\"color\":\"gray\",", "&7").replace("{\"color\":\"dark_gray\",", "&8")
            .replace("{\"color\":\"blue\",", "&9").replace("{\"color\":\"green\",", "&a")
            .replace("{\"color\":\"aqua\",", "&b").replace("{\"color\":\"red\",", "&c")
            .replace("{\"color\":\"light_purple\",", "&d").replace("{\"color\":\"yellow\",", "&e")
            .replace("{\"color\":\"white\",", "&f").replace("{\"obfuscated\":true,", "&k")
            .replace("{\"bold\":true,", "&l").replace("{\"strikethrough\":true,", "&m")
            .replace("{\"underlined\":true,", "&n").replace("{\"italic\":true,", "&o")
            .replace("\"color\":\"black\",\"text\":\"", "&0")
            .replace("\"color\":\"dark_blue\",\"text\":\"", "&1")
            .replace("\"color\":\"dark_green\",\"text\":\"", "&2")
            .replace("\"color\":\"dark_aqua\",\"text\":\"", "&3")
            .replace("\"color\":\"dark_red\",\"text\":\"", "&4")
            .replace("\"color\":\"dark_purple\",\"text\":\"", "&5")
            .replace("\"color\":\"gold\",\"text\":\"", "&6")
            .replace("\"color\":\"gray\",\"text\":\"", "&7")
            .replace("\"color\":\"dark_gray\",\"text\":\"", "&8")
            .replace("\"color\":\"blue\",\"text\":\"", "&9")
            .replace("\"color\":\"green\",\"text\":\"", "&a")
            .replace("\"color\":\"aqua\",\"text\":\"", "&b")
            .replace("\"color\":\"red\",\"text\":\"", "&c")
            .replace("\"color\":\"light_purple\",\"text\":\"", "&d")
            .replace("\"color\":\"yellow\",\"text\":\"", "&e")
            .replace("\"color\":\"white\",\"text\":\"", "&f")
            .replace("\"obfuscated\":true,\"text\":\"", "&k")
            .replace("\"bold\":true,\"text\":\"", "&l")
            .replace("\"strikethrough\":true,\"text\":\"", "&m")
            .replace("\"underlined\":true,\"text\":\"", "&n")
            .replace("\"italic\":true,\"text\":\"", "&o").replace("\"color\":\"black\",", "&0")
            .replace("\"color\":\"dark_blue\",", "&1").replace("\"color\":\"dark_green\",", "&2")
            .replace("\"color\":\"dark_aqua\",", "&3").replace("\"color\":\"dark_red\",", "&4")
            .replace("\"color\":\"dark_purple\",", "&5").replace("\"color\":\"gold\",", "&6")
            .replace("\"color\":\"gray\",", "&7").replace("\"color\":\"dark_gray\",", "&8")
            .replace("\"color\":\"blue\",", "&9").replace("\"color\":\"green\",", "&a")
            .replace("\"color\":\"aqua\",", "&b").replace("\"color\":\"red\",", "&c")
            .replace("\"color\":\"light_purple\",", "&d").replace("\"color\":\"yellow\",", "&e")
            .replace("\"color\":\"white\",", "&f").replace("\"obfuscated\":true,", "&k")
            .replace("\"bold\":true,", "&l").replace("\"strikethrough\":true,", "&m")
            .replace("\"underlined\":true,", "&n").replace("\"italic\":true,", "&o")
            .replace("[{\"text\":\"", "&0").replace("{\"text\":\"", "&0").replace("\"},", "")
            .replace("\"}]", "").replace("\"}", "");
        for (Entry<String, String> entry : Captions.replacements.entrySet()) {
            str = str.replace(entry.getKey(), entry.getValue());
        }
        return str;
    }

    public boolean restoreTag(String worldName, int x, int y, int z) {
        if (this.tag == null) {
            return false;
        }
        World world = BukkitUtil.getWorld(worldName);
        Block block = world.getBlockAt(x, y, z);
        if (block == null) {
            return false;
        }
        org.bukkit.block.BlockState state = block.getState();
        switch (getId()) {
            case "chest":
            case "beacon":
            case "brewingstand":
            case "dispenser":
            case "dropper":
            case "furnace":
            case "hopper":
            case "shulkerbox":
                if (!(state instanceof Container)) {
                    return false;
                }
                List<Tag> itemsTag = this.tag.getListTag("Items").getValue();
                Container container = (Container) state;
                Inventory inv = container.getSnapshotInventory();
                for (Tag itemTag : itemsTag) {
                    CompoundTag itemComp = (CompoundTag) itemTag;
                    ItemType type = ItemType.REGISTRY.get(itemComp.getString("id").toLowerCase());
                    if (type == null) {
                        continue;
                    }
                    int count = itemComp.getByte("Count");
                    int slot = itemComp.getByte("Slot");
                    CompoundTag tag = (CompoundTag) itemComp.getValue().get("tag");
                    BaseItemStack baseItemStack = new BaseItemStack(type, tag, count);
                    ItemStack itemStack = BukkitAdapter.adapt(baseItemStack);
                    inv.setItem(slot, itemStack);
                }
                container.update(true, false);
                return true;
            case "sign":
                if (state instanceof Sign) {
                    Sign sign = (Sign) state;
                    sign.setLine(0, jsonToColourCode(tag.getString("Text1")));
                    sign.setLine(1, jsonToColourCode(tag.getString("Text2")));
                    sign.setLine(2, jsonToColourCode(tag.getString("Text3")));
                    sign.setLine(3, jsonToColourCode(tag.getString("Text4")));
                    state.update(true);
                    return true;
                }
                return false;
        }
        return false;
    }

    public CompoundTag getTag() {
        if (this.tag != null) {
            return this.tag;
        }
        if (this.state instanceof InventoryHolder) {
            InventoryHolder inv = (InventoryHolder) this.state;
            ItemStack[] contents = inv.getInventory().getContents();
            Map<String, Tag> values = new HashMap<>();
            values.put("Items", new ListTag(CompoundTag.class, serializeInventory(contents)));
            return new CompoundTag(values);
        }
        return null;
    }

    public String getId() {
        String tileid = this.tag.getString("id").toLowerCase();
        if (tileid.startsWith("minecraft:")) {
            tileid = tileid.replace("minecraft:", "");
        }
        return tileid;
    }

    public List<CompoundTag> serializeInventory(ItemStack[] items) {
        List<CompoundTag> tags = new ArrayList<>();
        for (int i = 0; i < items.length; ++i) {
            if (items[i] != null) {
                Map<String, Tag> tagData = serializeItem(items[i]);
                tagData.put("Slot", new ByteTag((byte) i));
                tags.add(new CompoundTag(tagData));
            }
        }
        return tags;
    }

    public Map<String, Tag> serializeItem(ItemStack item) {
        Map<String, Tag> data = new HashMap<>();
        data.put("id", new StringTag(item.getType().name()));
        data.put("Damage", new ShortTag(item.getDurability()));
        data.put("Count", new ByteTag((byte) item.getAmount()));
        if (!item.getEnchantments().isEmpty()) {
            List<CompoundTag> enchantmentList = new ArrayList<>();
            for (Entry<Enchantment, Integer> entry : item.getEnchantments().entrySet()) {
                Map<String, Tag> enchantment = new HashMap<>();
                enchantment.put("id", new StringTag(entry.getKey().toString()));
                enchantment.put("lvl", new ShortTag(entry.getValue().shortValue()));
                enchantmentList.add(new CompoundTag(enchantment));
            }
            Map<String, Tag> auxData = new HashMap<>();
            auxData.put("ench", new ListTag(CompoundTag.class, enchantmentList));
            data.put("tag", new CompoundTag(auxData));
        }
        return data;
    }

}
