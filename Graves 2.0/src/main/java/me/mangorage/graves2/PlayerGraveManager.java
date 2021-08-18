package me.mangorage.graves2;

import de.tr7zw.nbtapi.NBTItem;
import fr.minuskube.inv.InventoryManager;
import me.mangorage.graves2.Commands.GravesCommand;
import me.mangorage.graves2.Items.DeathNote;
import me.mangorage.graves2.Items.DeathScroll;
import me.mangorage.graves2.Menu.GravesMenu;
import me.mangorage.graves2.Menu.UpgradeMenu;
import me.mangorage.graves2.Utils.BasicUtils;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.List;
import java.util.UUID;

public class PlayerGraveManager implements Listener {

    Graves2 pl;
    Economy Eco;
    InventoryManager InvManager;

    GravesMenu GravesMenu;
    UpgradeMenu UpgradeMenu;

    public PlayerGraveManager(Graves2 pl, Economy Eco) {
        this.pl = pl;
        this.Eco = Eco;

        InvManager = new InventoryManager(pl);
        InvManager.init();

        GravesMenu = new GravesMenu(pl, InvManager);
        // UpgradeMenu = new UpgradeMenu(InvManager);

        Bukkit.getPluginManager().registerEvents(this, pl);
        pl.getCommand("graves").setExecutor(new GravesCommand(this));



        Bukkit.getScheduler().scheduleSyncRepeatingTask(pl, new Runnable() {
            public void run() {
                try {
                    for (PlayerGrave cGrave : pl.Graves) {
                        if (cGrave.Deleted || cGrave.Empty) {
                            pl.AdminGraves.remove(cGrave.SearchUUID);
                            pl.Graves.remove(cGrave);
                        }
                    }
                } catch (ConcurrentModificationException concurrentModificationException) {}
                GravesMenu.update(pl.Graves, pl.AdminGraves);
            }
        }, 0L, 20L);
    }

    public void openMenu(Player plr) {
        GravesMenu.openInv(plr);
    }


    // Handles Death Note
    @EventHandler
    private void DeathNoteInteract(PlayerInteractEvent event) {
        Player plr = event.getPlayer();
        ItemStack item = event.getItem();

        if (item == null) return;

        if (item.getItemMeta().getDisplayName().contains("Death Note")) {
            NBTItem nbtItem = new NBTItem(item);

            if (nbtItem.getString("GraveID") != null) {
                String ID = nbtItem.getString("GraveID");

                if (pl.AdminGraves.containsKey(ID) ) {
                    PlayerGrave grave = pl.AdminGraves.get(ID);
                    UpgradeMenu UM = new UpgradeMenu(InvManager, Eco, plr, grave, item);
                    UM.open();
                } else {
                    plr.sendMessage("Grave Doesnt Exist!"); // Cooldown?
                }
            }

        }
    }

    // Handles Death Scroll
    @EventHandler
    private void DeathScrollInteract(PlayerInteractEvent event) {
        Player plr = event.getPlayer();
        ItemStack item = event.getItem();

        if (item == null) return;

        if (item.getItemMeta().getDisplayName().contains("Death Scroll")) {
            NBTItem nbtItem = new NBTItem(item);

            if (nbtItem.getString("GraveID") != null) {
                String ID = nbtItem.getString("GraveID");

                if (pl.AdminGraves.containsKey(ID) ) {
                    PlayerGrave grave = pl.AdminGraves.get(ID);

                    if (!nbtItem.getString("GraveType").equalsIgnoreCase("Scroll")) {
                        // Fix
                        plr.getInventory().setItemInMainHand(DeathNote.create(grave));
                        return;
                    }

                    if (plr.getUniqueId().toString().equals(grave.PlayerID)) {
                        plr.openInventory(grave.Grave);
                    }
                } else {
                    if (ID != null) {
                        plr.sendMessage("Grave Doesnt Exist!"); // Cooldown?
                    }
                }
            }

        }
    }

    // Handles Converting Old Scrolls -> New Scrolls
    @EventHandler
    private void ConvertDeathNoteScroll(PlayerInteractEvent event) {
        Player plr = event.getPlayer();
        ItemStack item = event.getItem();

        List<String> lore = null;
        boolean isScroll = false;

        if (item == null) return;

        if (item.getItemMeta().getDisplayName().contains("Death Note")) {
            lore = item.getItemMeta().getLore();
        } else if (item.getItemMeta().getDisplayName().contains("Death Scroll")) {
            lore = item.getItemMeta().getLore();
            isScroll = true;
        }

        if (lore != null) {
            String ID = lore.get(0);
            if (pl.AdminGraves.containsKey(ID)) {
                PlayerGrave grave = pl.AdminGraves.get(ID);
                ItemStack nitem = DeathNote.create(grave);
                if (isScroll) {
                    plr.getInventory().setItemInMainHand(DeathScroll.create(grave, nitem));
                } else {
                    plr.getInventory().setItemInMainHand(nitem);
                }
            }
        }

        // All Good!
    }

    // Handles Grave
    @EventHandler
    public void OnDeath(PlayerDeathEvent event) throws IOException, SQLException {

        if (!BasicUtils.isInvEmpty(event.getDrops()) && event.getEntity().hasPermission("Graves.use")) {

            PlayerGrave newGrave = new PlayerGrave(pl, event.getEntity(), event.getEntity().getLocation(), new ArrayList<ItemStack>(event.getDrops()));
            pl.Graves.add(newGrave);
            newGrave.SearchUUID = UUID.randomUUID().toString();
            pl.AdminGraves.put(newGrave.SearchUUID, newGrave);

            // Final bit!
            event.getDrops().clear();
            event.getEntity().getInventory().clear();

            BasicUtils.DB.saveGrave(newGrave);

            // Death Note
            ItemStack DeathNoteItem = DeathNote.create(newGrave);
            Bukkit.getScheduler().runTaskLater(pl, new Runnable() {
                @Override
                public void run() {
                    event.getEntity().getInventory().addItem(DeathNoteItem);
                }
            }, 10L);
        }

    }


}
