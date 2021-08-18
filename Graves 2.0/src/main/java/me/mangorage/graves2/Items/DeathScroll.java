package me.mangorage.graves2.Items;

import de.tr7zw.nbtapi.NBTItem;
import me.mangorage.graves2.Graves2;
import me.mangorage.graves2.PlayerGrave;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;
import java.util.UUID;

public class DeathScroll {
    public static ItemStack create(PlayerGrave grave, ItemStack item) {
        ItemMeta meta = item.getItemMeta();

        List<String> lore = meta.getLore();
        lore.remove(lore.size()-1);
        lore.add("Right Click to use!");
        meta.setLore(lore);

        meta.setDisplayName(grave.PlayerName + "'s Death Scroll");
        meta.addItemFlags(new ItemFlag[] { ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ENCHANTS });
        meta.addEnchant(Enchantment.MENDING, 1, false);
        item.setItemMeta(meta);

        NBTItem nbti = new NBTItem(item);
        nbti.setString("GraveType", "Scroll");


        return nbti.getItem();
    }
}
