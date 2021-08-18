package me.mangorage.graves2.Items;

import de.tr7zw.nbtapi.NBTItem;
import me.mangorage.graves2.PlayerGrave;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class DeathNote {
    public static ItemStack create(PlayerGrave Grave) {
        int X = Grave.Deathloca.getBlockX();
        int Y = Grave.Deathloca.getBlockY();
        int Z = Grave.Deathloca.getBlockZ();
        String World = Grave.Deathloca.getWorld().getName();

        List<String> lore = new ArrayList<>();
        lore.add("X: " + X + " Y: " + Y + " Z: " + Z);
        lore.add("World: " + World);
        lore.add("Owner: " + Grave.PlayerName);
        lore.add("Right click to Upgrade");

        ItemStack DeathNote = new ItemStack(Material.PAPER);
        ItemMeta Meta = DeathNote.getItemMeta();
        Meta.setLore(lore);
        Meta.setDisplayName(Grave.PlayerName + "'s Death Note");
        DeathNote.setItemMeta(Meta);

        NBTItem nbti = new NBTItem(DeathNote);
        nbti.setString("GraveID", Grave.SearchUUID);
        nbti.setString(UUID.randomUUID().toString(), "Idiot Hacker! Shouldnt be seeing this!!!");
        nbti.setString("GraveType", "Note");
        DeathNote = nbti.getItem();


        return DeathNote;
    }
}
