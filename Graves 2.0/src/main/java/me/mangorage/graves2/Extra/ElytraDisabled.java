package me.mangorage.graves2.Extra;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public class ElytraDisabled implements Listener {

    private void stop(Player plr) {
        plr.sendMessage("Elytra propulsion is disabled due to it causing lag and crashes.");
    }

    @EventHandler
    private void onElytraUse(PlayerInteractEvent event) {
        ItemStack Hand = event.getPlayer().getInventory().getItemInMainHand();
        ItemStack OffHand = event.getPlayer().getInventory().getItemInOffHand();

        if (Hand.getType() == Material.FIREWORK_ROCKET) {
            event.setCancelled(true);
            stop(event.getPlayer());
        } else if (OffHand.getType() == Material.FIREWORK_ROCKET) {
            event.setCancelled(true);
            stop(event.getPlayer());
        }
    }

}
