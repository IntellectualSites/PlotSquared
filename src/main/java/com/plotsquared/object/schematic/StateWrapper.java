package com.plotsquared.object.schematic;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.block.BlockState;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import com.intellectualcrafters.jnbt.ByteTag;
import com.intellectualcrafters.jnbt.CompoundTag;
import com.intellectualcrafters.jnbt.ListTag;
import com.intellectualcrafters.jnbt.ShortTag;
import com.intellectualcrafters.jnbt.Tag;
import com.intellectualcrafters.plot.object.schematic.ItemType;
import com.intellectualcrafters.plot.object.schematic.PlotItem;
import com.intellectualcrafters.plot.util.MathMan;
import com.intellectualcrafters.plot.util.SchematicHandler.Schematic;

public class StateWrapper {
    
    public BlockState state = null;
    public CompoundTag tag = null;
    
    public StateWrapper(final BlockState state) {
        this.state = state;
    }
    
    public StateWrapper(final CompoundTag tag) {
        this.tag = tag;
    }
    
    public boolean restoreTag(final short x, final short y, final short z, final Schematic schematic) {
        if (tag == null) {
            return false;
        }
        final List<Tag> itemsTag = tag.getListTag("Items").getValue();
        final int length = itemsTag.size();
        final short[] ids = new short[length];
        final byte[] datas = new byte[length];
        final byte[] amounts = new byte[length];
        for (int i = 0; i < length; i++) {
            final Tag itemTag = itemsTag.get(i);
            final CompoundTag itemComp = (CompoundTag) itemTag;
            short id = itemComp.getShort("id");
            String idStr = itemComp.getString("id");
            if ((idStr != null) && !MathMan.isInteger(idStr)) {
                idStr = idStr.split(":")[1].toLowerCase();
                id = (short) ItemType.getId(idStr);
            }
            ids[i] = id;
            datas[i] = (byte) itemComp.getShort("Damage");
            amounts[i] = itemComp.getByte("Count");
        }
        if (length != 0) {
            schematic.addItem(new PlotItem(x, y, z, ids, datas, amounts));
        }
        return true;
    }
    
    public CompoundTag getTag() {
        if (tag != null) {
            return tag;
        }
        if (state instanceof InventoryHolder) {
            final InventoryHolder inv = (InventoryHolder) state;
            final ItemStack[] contents = inv.getInventory().getContents();
            final Map<String, Tag> values = new HashMap<String, Tag>();
            values.put("Items", new ListTag("Items", CompoundTag.class, serializeInventory(contents)));
            return new CompoundTag(values);
        }
        return null;
    }
    
    public String getId() {
        return "Chest";
    }
    
    public List<CompoundTag> serializeInventory(final ItemStack[] items) {
        final List<CompoundTag> tags = new ArrayList<CompoundTag>();
        for (int i = 0; i < items.length; ++i) {
            if (items[i] != null) {
                final Map<String, Tag> tagData = serializeItem(items[i]);
                tagData.put("Slot", new ByteTag("Slot", (byte) i));
                tags.add(new CompoundTag(tagData));
            }
        }
        return tags;
    }
    
    public Map<String, Tag> serializeItem(final org.spongepowered.api.item.inventory.ItemStack item) {
        final Map<String, Tag> data = new HashMap<String, Tag>();
        
        // FIXME serialize sponge item
        
        return data;
    }
    
    public Map<String, Tag> serializeItem(final ItemStack item) {
        final Map<String, Tag> data = new HashMap<String, Tag>();
        data.put("id", new ShortTag("id", (short) item.getTypeId()));
        data.put("Damage", new ShortTag("Damage", item.getDurability()));
        data.put("Count", new ByteTag("Count", (byte) item.getAmount()));
        if (!item.getEnchantments().isEmpty()) {
            final List<CompoundTag> enchantmentList = new ArrayList<CompoundTag>();
            for (final Entry<Enchantment, Integer> entry : item.getEnchantments().entrySet()) {
                final Map<String, Tag> enchantment = new HashMap<String, Tag>();
                enchantment.put("id", new ShortTag("id", (short) entry.getKey().getId()));
                enchantment.put("lvl", new ShortTag("lvl", entry.getValue().shortValue()));
                enchantmentList.add(new CompoundTag(enchantment));
            }
            final Map<String, Tag> auxData = new HashMap<String, Tag>();
            auxData.put("ench", new ListTag("ench", CompoundTag.class, enchantmentList));
            data.put("tag", new CompoundTag("tag", auxData));
        }
        return data;
    }
}
