package me.mangorage.graves2.Menu;

import fr.minuskube.inv.ClickableItem;
import fr.minuskube.inv.InventoryManager;
import fr.minuskube.inv.SmartInventory;
import fr.minuskube.inv.content.InventoryContents;
import fr.minuskube.inv.content.InventoryProvider;
import fr.minuskube.inv.content.SlotPos;
import me.mangorage.graves2.Items.DeathScroll;
import me.mangorage.graves2.PlayerGrave;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.List;

public class UpgradeMenu implements InventoryProvider {
    Inventory UpgradeMenu = Bukkit.createInventory(null, 27, "Death Scroll Upgrade");
    ItemStack Glass = new ItemStack(Material.GRAY_STAINED_GLASS_PANE, 1);
    SmartInventory INVENTORY;
    Economy Eco;

    private Player plr;
    private ItemStack item;
    private PlayerGrave grave;

    public void updateSettings(Player plr, String Setting) {

    }

    public UpgradeMenu (InventoryManager YM, Economy Eco, Player plr, PlayerGrave grave, ItemStack item) {
        this.plr = plr;
        this.item = item;
        this.Eco = Eco;
        this.grave = grave;


        ItemMeta GlassMeta = Glass.getItemMeta();
        GlassMeta.setDisplayName("");
        Glass.setItemMeta(GlassMeta);
        INVENTORY = SmartInventory.builder().manager(YM)
                .id("UpgradeDeathNote!")
                .provider(this)
                .size(3, 9)
                .title(ChatColor.BLUE + "Upgrade Death Note!")
                .build();

    }

    public void open() {
        INVENTORY.open(plr);
    }

    @Override
    public void init(Player player, InventoryContents contents) {
        contents.fill(ClickableItem.empty(Glass));

        contents.set(SlotPos.of(1, 2), ClickableItem.empty(item));
        contents.set(SlotPos.of(1, 4), ClickableItem.of(Icons.getConfirm(Material.EMERALD), (e) -> {
            double Bal = this.Eco.getBalance(plr);
            if (Bal > 1000000.0D) {
                this.Eco.withdrawPlayer(plr, 1000000.0D);
                plr.getInventory().setItemInMainHand(DeathScroll.create(grave, item));
            } else {
                plr.sendMessage("Dont have enough to Upgrade!");
            }
            INVENTORY.close(plr);
        }));
        contents.set(SlotPos.of(1, 6), ClickableItem.empty(Icons.getInfo(Material.BOOK)));
    }

    @Override
    public void update(Player player, InventoryContents contents) {

    }


    static class Icons {

        public static ItemStack getInfo(Material Mat) {
            ItemStack item = new ItemStack(Mat, 1);
            ItemMeta meta = item.getItemMeta();
            meta.setLore(getInfoLore());
            item.setItemMeta(meta);
            return item;
        }

        public static ItemStack getConfirm(Material Mat) {
            ItemStack item = new ItemStack(Mat, 1);
            ItemMeta meta = item.getItemMeta();
            meta.setLore(getConfirmLore());
            item.setItemMeta(meta);
            return item;
        }


        public static List<String> getInfoLore() {
            List<String> lore = new ArrayList<>();
            lore.add("Upgrading this allows");
            lore.add("you to open your Grave!");
            return lore;
        }

        public static List<String> getConfirmLore() {
            List<String> lore = new ArrayList<>();
            lore.add("This Upgrade Costs");
            lore.add("1 million balance!");
            return lore;
        }
    }
}
