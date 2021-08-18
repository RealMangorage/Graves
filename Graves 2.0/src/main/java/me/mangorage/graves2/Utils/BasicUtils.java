package me.mangorage.graves2.Utils;

import me.mangorage.graves2.API.Serializer;
import me.mangorage.graves2.Extra.BetaSaving;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BasicUtils {
    public static BetaSaving DB;


    public static void load(BetaSaving DBa) {
        DB = DBa;
    }



    public static boolean isInvEmpty(List<ItemStack> Drops) {
        for(ItemStack item : Drops)
        {
            if(item != null)
                return false;
        }
        return true;
    }

    public static int OutOfBounds(int y) {
        if (y >= 255) {
            return 1; // Over Y 255
        } else if (y <= 0) {
            return -1; // Under Y 0
        } else {
            return 0; // No out of Bounds
        }
    }

    public static boolean isInvEmpty(Inventory Inv) {
        for(ItemStack item : Inv)
        {
            if(item != null)
                return false;
        }
        return true;
    }

    public static String turnPosintoString(Location loc) {
        return loc.getBlockX() + " " + loc.getBlockY() + " " + loc.getBlockZ();
    }

    public static void sendColoredMessage(Player Plr, String Message) {
        String Char = "&";
        Plr.sendMessage(ChatColor.translateAlternateColorCodes(Char.charAt(0), Message));
    }

    public static String colorify(String Message) {
        String Char = "&";
        return ChatColor.translateAlternateColorCodes(Char.charAt(0), Message);
    }

    public static Map<String, Integer> time(long milliseconds) {
        Map<String, Integer> Map = new HashMap<>();
        int seconds =(int)(milliseconds /1000)%60;
        int minutes =(int)((milliseconds /(1000*60))%60);
        int hours =(int)((milliseconds /(1000*60*60))%24);
        Map.put("seconds", seconds);
        Map.put("Minutes", minutes);
        Map.put("Hours", hours);
        return Map;
    }
}
