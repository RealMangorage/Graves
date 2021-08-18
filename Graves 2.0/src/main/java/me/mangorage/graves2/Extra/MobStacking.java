package me.mangorage.graves2.Extra;

import me.mangorage.graves2.Graves2;
import org.bukkit.Bukkit;
import org.bukkit.entity.Sheep;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerShearEntityEvent;
import org.bukkit.inventory.Inventory;

import java.util.ArrayList;

public class MobStacking implements Listener {
    Inventory SheepMenu = Bukkit.createInventory(null, (9*4), "Sheep Menu");
    ArrayList<String> cooldown = new ArrayList<>();
    Graves2 pla;

    public MobStacking(Graves2 pl) {
        pla = pl;
        System.out.println("Loaded Sheeps");
    }

    @EventHandler
    public void Death(PlayerDeathEvent event) {
        String UUID = event.getEntity().getUniqueId().toString();
        if (cooldown.contains(UUID)) {
            event.setDeathMessage(event.getEntity().getPlayer().getName() + " Was Sheared by a Sheep!");
        }
    }


    @EventHandler
    private void onSheepClick(PlayerShearEntityEvent event) {
        double num = Math.random();
        if (Math.round(num*10) == 5) {
            event.getPlayer().sendMessage("[Sheep] OWWWW!");
            event.getPlayer().sendMessage("[Sheep] Ima Shear you!");
            event.getPlayer().damage(0.5);
            String UUID = event.getPlayer().getUniqueId().toString();

            if (!cooldown.contains(UUID)) {
                cooldown.add(UUID);
                Bukkit.getScheduler().runTaskLater(pla, () -> {
                    cooldown.remove(UUID);
                }, 120L);
            }
        }
    }



}
