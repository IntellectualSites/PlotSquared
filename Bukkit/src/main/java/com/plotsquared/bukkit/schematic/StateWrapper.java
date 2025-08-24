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
import com.plotsquared.core.PlotSquared;
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
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.World;
import org.bukkit.block.Banner;
import org.bukkit.block.Block;
import org.bukkit.block.Container;
import org.bukkit.block.Sign;
import org.bukkit.block.Skull;
import org.bukkit.block.banner.Pattern;
import org.bukkit.block.banner.PatternType;
import org.bukkit.block.sign.Side;
import org.bukkit.block.sign.SignSide;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.jetbrains.annotations.ApiStatus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.UUID;
import java.util.function.BiConsumer;

// TODO: somehow unbreak this class so it doesn't fuck up the whole schematic population system due to MC updates
@ApiStatus.Internal
public class StateWrapper {

    private static final Logger LOGGER = LogManager.getLogger("PlotSquared/" + StateWrapper.class.getSimpleName());
    private static final boolean MODERN_SIGNS = PlotSquared.platform().serverVersion()[1] > 19;
    private final Registry<PatternType> PATTERN_TYPE_REGISTRY = Objects.requireNonNull(Bukkit.getRegistry(PatternType.class));

    private static boolean paperErrorTextureSent = false;

    public CompoundTag tag;

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
                    int count = itemComp.containsKey("count") ? itemComp.getInt("count") : itemComp.getByte("Count");
                    int slot = itemComp.getByte("Slot");
                    CompoundTag tag = (CompoundTag) itemComp.getValue().get(itemComp.containsKey("tag") ? "tag" : "components");
                    BaseItemStack baseItemStack = new BaseItemStack(type, tag, count);
                    ItemStack itemStack = BukkitAdapter.adapt(baseItemStack);
                    inv.setItem(slot, itemStack);
                }
                container.update(true, false);
                return true;
            }
            case "sign" -> {
                if (state instanceof Sign sign && this.restoreSign(sign)) {
                    state.update(true, false);
                    return true;
                }
                return false;
            }
            case "skull" -> {
                if (state instanceof Skull skull && this.restoreSkull(skull)) {
                    skull.update(true, false);
                    return true;
                }
                return false;
            }
            case "banner" -> {
                if (state instanceof Banner banner) {
                    List<CompoundTag> patterns;
                    // "old" format
                    if ((patterns = this.tag.getList("Patterns", CompoundTag.class)) != null && !patterns.isEmpty()) {
                        banner.setPatterns(patterns.stream().map(compoundTag -> {
                            DyeColor color = DyeColor.getByWoolData((byte) compoundTag.getInt("Color"));
                            final PatternType patternType = PATTERN_TYPE_REGISTRY.get(Objects.requireNonNull(
                                    NamespacedKey.fromString(compoundTag.getString("Pattern"))
                            ));
                            if (color == null || patternType == null) {
                                return null;
                            }
                            return new Pattern(color, patternType);
                        }).filter(Objects::nonNull).toList());
                        banner.update(true, false);
                        return true;
                    }

                    // "new" format - since 1.21.3-ish
                    if ((patterns = this.tag.getList("patterns", CompoundTag.class)) != null && !patterns.isEmpty()) {
                        for (final CompoundTag patternTag : patterns) {
                            final String color = patternTag.getString("color");
                            if (color.isEmpty()) {
                                continue;
                            }
                            final Tag pattern = patternTag.getValue().get("pattern");
                            if (pattern instanceof StringTag patternString && !patternString.getValue().isEmpty()) {
                                final PatternType patternType = PATTERN_TYPE_REGISTRY.get(Objects.requireNonNull(
                                        NamespacedKey.fromString(patternString.getValue())
                                ));
                                if (patternType == null) {
                                    continue;
                                }
                                banner.addPattern(new Pattern(
                                        DyeColor.legacyValueOf(color.toUpperCase(Locale.ROOT)),
                                        patternType
                                ));
                            }
                            // not supporting banner pattern definitions (no API available)
                        }
                        banner.update(true, false);
                    }
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

    private boolean restoreSkull(Skull skull) {
        boolean updated = false;
        // can't support custom_name - Spigot does not provide any API for that
        if (this.tag.containsKey("note_block_sound")) {
            skull.setNoteBlockSound(NamespacedKey.fromString(this.tag.getString("note_block_sound")));
            updated = true;
        }
        // modern format - MC 1.21.3-ish
        if (this.tag.containsKey("profile")) {
            final Tag profile = this.tag.getValue().get("profile");
            if (profile instanceof StringTag stringTag) {
                final String name = stringTag.getValue();
                if (name != null && !name.isEmpty()) {
                    skull.setOwningPlayer(Bukkit.getOfflinePlayer(name));
                    return true;
                }
                return updated;
            }
            if (profile instanceof CompoundTag compoundTag) {
                final List<Tag> properties = compoundTag.getList("properties");
                if (properties != null && !properties.isEmpty()) {
                    if (!PaperLib.isPaper()) {
                        if (!paperErrorTextureSent) {
                            paperErrorTextureSent = true;
                            LOGGER.error("Failed to populate schematic skull data - this is a Spigot limitation.");
                        }
                        return updated;
                    }
                    for (final Tag propertyTag : properties) {
                        if (!(propertyTag instanceof CompoundTag property)) {
                            continue;
                        }
                        if (!property.getString("name").equals("textures")) {
                            continue;
                        }
                        final String value = property.getString("value");
                        final String signature = property.containsKey("signature") ? property.getString("signature") : null;
                        final PlayerProfile playerProfile = Bukkit.createProfile(UUID.randomUUID());
                        playerProfile.setProperty(new ProfileProperty("textures", value, signature));
                        skull.setPlayerProfile(playerProfile);
                        return true;
                    }
                    return updated;
                }
                final int[] id = compoundTag.getIntArray("id");
                if (id != null && id.length == 4) {
                    skull.setOwningPlayer(Bukkit.getOfflinePlayer(new UUID(
                            (long) id[0] << 32 | (id[1] & 0xFFFFFFFFL),
                            (long) id[2] << 32 | (id[3] & 0xFFFFFFFFL)
                    )));
                    return true;
                }
                final String name = compoundTag.getString("name");
                if (name != null && !name.isEmpty()) {
                    skull.setOwningPlayer(Bukkit.getOfflinePlayer(name));
                    return true;
                }
            }
        }

        // "Old" MC format (idk when it got updated)
        if (this.tag.getValue().get("SkullOwner") instanceof CompoundTag skullOwner) {
            if (skullOwner.getValue().get("Name") instanceof StringTag ownerName && !ownerName.getValue().isEmpty()) {
                skull.setOwningPlayer(Bukkit.getOfflinePlayer(ownerName.getValue()));
                skull.update(true);
                return true;
            }
            if (skullOwner.getValue().get("Properties") instanceof CompoundTag properties) {
                if (!paperErrorTextureSent) {
                    paperErrorTextureSent = true;
                    LOGGER.error("Failed to populate schematic skull data - this is a Spigot limitation.");
                    return updated;
                }
                final List<CompoundTag> textures = properties.getList("textures", CompoundTag.class);
                if (textures.isEmpty()) {
                    return updated;
                }
                final String value = textures.get(0).getString("Value");
                if (!value.isEmpty()) {
                    final PlayerProfile profile = Bukkit.createProfile(UUID.randomUUID());
                    profile.setProperty(new ProfileProperty("textures", value));
                    skull.setPlayerProfile(profile);
                    return true;
                }
            }
        }
        return updated;
    }

    private boolean restoreSign(Sign sign) {
        // "old" format (pre 1.20)
        if (this.tag.containsKey("Text1") || this.tag.containsKey("Text2")
                || this.tag.containsKey("Text3") || this.tag.containsKey("Text4")) {
            if (!MODERN_SIGNS) {
                sign.setLine(0, jsonToColourCode(tag.getString("Text1")));
                sign.setLine(1, jsonToColourCode(tag.getString("Text2")));
                sign.setLine(2, jsonToColourCode(tag.getString("Text3")));
                sign.setLine(3, jsonToColourCode(tag.getString("Text4")));
                sign.setGlowingText(tag.getByte("GlowingText") == 1);
                if (tag.getValue().get("Color") instanceof StringTag colorTag && !colorTag.getValue().isEmpty()) {
                    sign.setColor(DyeColor.legacyValueOf(colorTag.getValue()));
                }
                return true;
            }
            SignSide front = sign.getSide(Side.FRONT);
            front.setLine(0, jsonToColourCode(tag.getString("Text1")));
            front.setLine(1, jsonToColourCode(tag.getString("Text2")));
            front.setLine(2, jsonToColourCode(tag.getString("Text3")));
            front.setLine(3, jsonToColourCode(tag.getString("Text4")));
            front.setGlowingText(tag.getByte("GlowingText") == 1);
            if (tag.getValue().get("Color") instanceof StringTag colorTag && !colorTag.getValue().isEmpty()) {
                front.setColor(DyeColor.legacyValueOf(colorTag.getValue()));
            }
            return true;
        }

        // "modern" format
        if (this.tag.containsKey("front_text") || this.tag.containsKey("back_text") || this.tag.containsKey("is_waxed")) {
            // the new format on older servers shouldn't be possible, I hope?
            sign.setWaxed(this.tag.getByte("is_waxed") == 1);
            BiConsumer<SignSide, CompoundTag> sideSetter = (signSide, compoundTag) -> {
                signSide.setGlowingText(compoundTag.getByte("has_glowing_text") == 1);
                if (tag.getValue().get("color") instanceof StringTag colorTag && !colorTag.getValue().isEmpty()) {
                    signSide.setColor(DyeColor.legacyValueOf(colorTag.getValue()));
                }
                final List<Tag> lines = compoundTag.getList("messages");
                for (int i = 0; i < Math.min(lines.size(), 4); i++) {
                    final Tag line = lines.get(i);
                    if (line instanceof StringTag stringLine) {
                        signSide.setLine(i, jsonToColourCode(stringLine.getValue()));
                        continue;
                    }
                    // TODO: how tf support list of components + components - utilize paper + adventure?
                }
            };
            if (this.tag.getValue().get("front_text") instanceof CompoundTag frontText) {
                sideSetter.accept(sign.getSide(Side.FRONT), frontText);
            }
            if (this.tag.getValue().get("back_text") instanceof CompoundTag backText) {
                sideSetter.accept(sign.getSide(Side.BACK), backText);
            }
            return true;
        }
        return false;
    }

}
