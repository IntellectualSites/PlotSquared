package com.plotsquared.bukkit.object.schematic;

import com.intellectualcrafters.jnbt.*;
import com.intellectualcrafters.plot.object.schematic.ItemType;
import com.intellectualcrafters.plot.util.MathMan;
import com.plotsquared.bukkit.util.BukkitUtil;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
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

    public BlockState state = null;
    public CompoundTag tag = null;

    public StateWrapper(BlockState state) {
        this.state = state;
    }

    public StateWrapper(CompoundTag tag) {
        this.tag = tag;
    }

    public boolean restoreTag(String worldName, int x, int y, int z) {
        if (this.tag == null) {
            return false;
        }
        switch (this.tag.getString("id").toLowerCase()) {
            case "chest":
                List<Tag> itemsTag = this.tag.getListTag("Items").getValue();
                int length = itemsTag.size();
                short[] ids = new short[length];
                byte[] datas = new byte[length];
                byte[] amounts = new byte[length];
                byte[] slots = new byte[length];
                for (int i = 0; i < length; i++) {
                    Tag itemTag = itemsTag.get(i);
                    CompoundTag itemComp = (CompoundTag) itemTag;
                    short id = itemComp.getShort("id");
                    String idStr = itemComp.getString("id");
                    if (idStr != null && !MathMan.isInteger(idStr)) {
                        idStr = idStr.split(":")[1].toLowerCase();
                        id = (short) ItemType.getId(idStr);
                    }
                    ids[i] = id;
                    datas[i] = (byte) itemComp.getShort("Damage");
                    amounts[i] = itemComp.getByte("Count");
                    slots[i] = itemComp.getByte("Slot");
                }
                World world = BukkitUtil.getWorld(worldName);
                Block block = world.getBlockAt(x, y, z);
                if (block == null) {
                    return false;
                }
                BlockState state = block.getState();
                if (state instanceof InventoryHolder) {
                    InventoryHolder holder = (InventoryHolder) state;
                    Inventory inv = holder.getInventory();
                    for (int i = 0; i < ids.length; i++) {
                        ItemStack item = new ItemStack(ids[i], amounts[i], datas[i]);
                        inv.addItem(item);
                    }
                    state.update(true);
                    return true;
                }
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
            values.put("Items",
                new ListTag("Items", CompoundTag.class, serializeInventory(contents)));
            return new CompoundTag(values);
        }
        return null;
    }

    public String getId() {
        return "Chest";
    }

    public List<CompoundTag> serializeInventory(ItemStack[] items) {
        List<CompoundTag> tags = new ArrayList<>();
        for (int i = 0; i < items.length; ++i) {
            if (items[i] != null) {
                Map<String, Tag> tagData = serializeItem(items[i]);
                tagData.put("Slot", new ByteTag("Slot", (byte) i));
                tags.add(new CompoundTag(tagData));
            }
        }
        return tags;
    }

    /*
     * TODO: Move this into the sponge module!
     *
    public Map<String, Tag> serializeItem(final org.spongepowered.api.item.inventory.ItemStack item) {
        final Map<String, Tag> data = new HashMap<String, Tag>();
        
        // FIXME serialize sponge item
        
        return data;
    }
    */

    public Map<String, Tag> serializeItem(ItemStack item) {
        Map<String, Tag> data = new HashMap<>();
        data.put("id", new ShortTag("id", (short) item.getTypeId()));
        data.put("Damage", new ShortTag("Damage", item.getDurability()));
        data.put("Count", new ByteTag("Count", (byte) item.getAmount()));
        if (!item.getEnchantments().isEmpty()) {
            List<CompoundTag> enchantmentList = new ArrayList<>();
            for (Entry<Enchantment, Integer> entry : item.getEnchantments().entrySet()) {
                Map<String, Tag> enchantment = new HashMap<>();
                enchantment.put("id", new ShortTag("id", (short) entry.getKey().getId()));
                enchantment.put("lvl", new ShortTag("lvl", entry.getValue().shortValue()));
                enchantmentList.add(new CompoundTag(enchantment));
            }
            Map<String, Tag> auxData = new HashMap<>();
            auxData.put("ench", new ListTag("ench", CompoundTag.class, enchantmentList));
            data.put("tag", new CompoundTag("tag", auxData));
        }
        return data;
    }
}
