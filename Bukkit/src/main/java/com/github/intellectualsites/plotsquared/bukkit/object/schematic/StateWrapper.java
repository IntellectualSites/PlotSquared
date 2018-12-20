package com.github.intellectualsites.plotsquared.bukkit.object.schematic;

import com.github.intellectualsites.plotsquared.bukkit.util.BukkitUtil;
import com.sk89q.jnbt.*;
import org.bukkit.Material;
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
                String[] ids = new String[length];
                byte[] amounts = new byte[length];
                byte[] slots = new byte[length];
                for (int i = 0; i < length; i++) {
                    Tag itemTag = itemsTag.get(i);
                    CompoundTag itemComp = (CompoundTag) itemTag;
                    String id = itemComp.getString("id");
                    ids[i] = id;
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
                        ItemStack item =
                            new ItemStack(Material.getMaterial(ids[i]), (int) amounts[i]);
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
            values.put("Items", new ListTag(CompoundTag.class, serializeInventory(contents)));
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
