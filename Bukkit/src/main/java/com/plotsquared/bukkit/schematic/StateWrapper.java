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
package com.plotsquared.bukkit.schematic;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;
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
import io.papermc.lib.PaperLib;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.World;
import org.bukkit.block.Banner;
import org.bukkit.block.Block;
import org.bukkit.block.Container;
import org.bukkit.block.Sign;
import org.bukkit.block.Skull;
import org.bukkit.block.banner.Pattern;
import org.bukkit.block.banner.PatternType;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.UUID;

public class StateWrapper {

    public CompoundTag tag;

    private boolean paperErrorTextureSent = false;
    private static final Logger LOGGER = LogManager.getLogger("PlotSquared/" + StateWrapper.class.getSimpleName());

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
        str = ChatColor.translateAlternateColorCodes('&', str);
        return str;
    }

    /**
     * Restore the TileEntity data to the given world at the given coordinates.
     *
     * @param worldName World name
     * @param x         x position
     * @param y         y position
     * @param z         z position
     * @return true if successful
     */
    public boolean restoreTag(String worldName, int x, int y, int z) {
        World world = BukkitUtil.getWorld(worldName);
        if (world == null) {
            return false;
        }
        return restoreTag(world.getBlockAt(x, y, z));
    }

    /**
     * Restore the TileEntity data to the given block
     *
     * @param block Block to restore to
     * @return true if successful
     */
    @SuppressWarnings("deprecation") // #setLine is needed for Spigot compatibility
    public boolean restoreTag(@NonNull Block block) {
        if (this.tag == null) {
            return false;
        }
        org.bukkit.block.BlockState state = block.getState();
        switch (getId()) {
            case "chest", "beacon", "brewingstand", "dispenser", "dropper", "furnace", "hopper", "shulkerbox" -> {
                if (!(state instanceof Container container)) {
                    return false;
                }
                List<Tag> itemsTag = this.tag.getListTag("Items").getValue();
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
            }
            case "sign" -> {
                if (state instanceof Sign sign) {
                    sign.setLine(0, jsonToColourCode(tag.getString("Text1")));
                    sign.setLine(1, jsonToColourCode(tag.getString("Text2")));
                    sign.setLine(2, jsonToColourCode(tag.getString("Text3")));
                    sign.setLine(3, jsonToColourCode(tag.getString("Text4")));
                    state.update(true);
                    return true;
                }
                return false;
            }
            case "skull" -> {
                if (state instanceof Skull skull) {
                    CompoundTag skullOwner = ((CompoundTag) this.tag.getValue().get("SkullOwner"));
                    if (skullOwner == null) {
                        return true;
                    }
                    String player = skullOwner.getString("Name");

                    if (player != null && !player.isEmpty()) {
                        try {
                            skull.setOwningPlayer(Bukkit.getOfflinePlayer(player));
                            skull.update(true);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        return true;
                    }

                    final CompoundTag properties = (CompoundTag) skullOwner.getValue().get("Properties");
                    if (properties == null) {
                        return false;
                    }
                    final ListTag textures = properties.getListTag("textures");
                    if (textures.getValue().isEmpty()) {
                        return false;
                    }
                    final CompoundTag textureCompound = (CompoundTag) textures.getValue().get(0);
                    if (textureCompound == null) {
                        return false;
                    }
                    String textureValue = textureCompound.getString("Value");
                    if (textureValue == null) {
                        return false;
                    }
                    if (!PaperLib.isPaper()) {
                        if (!paperErrorTextureSent) {
                            paperErrorTextureSent = true;
                            LOGGER.error("Failed to populate skull data in your road schematic - This is a Spigot limitation.");
                        }
                        return false;
                    }
                    final PlayerProfile profile = Bukkit.createProfile(UUID.randomUUID());
                    profile.setProperty(new ProfileProperty("textures", textureValue));
                    skull.setPlayerProfile(profile);
                    skull.update(true);
                    return true;

                }
                return false;
            }
            case "banner" -> {
                if (state instanceof Banner banner) {
                    List<Tag> patterns = this.tag.getListTag("Patterns").getValue();
                    if (patterns == null || patterns.isEmpty()) {
                        return false;
                    }
                    banner.setPatterns(patterns.stream().map(t -> (CompoundTag) t).map(compoundTag -> {
                        DyeColor color = DyeColor.getByWoolData((byte) compoundTag.getInt("Color"));
                        PatternType patternType = PatternType.getByIdentifier(compoundTag.getString("Pattern"));
                        if (color == null || patternType == null) {
                            return null;
                        }
                        return new Pattern(color, patternType);
                    }).filter(Objects::nonNull).toList());
                    banner.update(true);
                    return true;
                }
                return false;
            }
        }
        return false;
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
