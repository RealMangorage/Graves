package me.mangorage.graves2.Extra;

import me.mangorage.graves2.Utils.BasicUtils;
import me.mangorage.graves2.Graves2;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ExtraFeatures implements Listener {

    Graves2 pl;
    List<UUID> BlockCache = new ArrayList<>();
    boolean CustomChat = false;

    public ExtraFeatures(Graves2 pla) {
        System.out.println("[Graves] Loading Extra Features!");
        pl = pla;
    }

    private boolean Check(ItemMeta a) {
        if (a.getLore() != null) {
            for (String s : a.getLore()) {
                System.out.println(s);
                if (s.contains("Special")) {
                    System.out.println("aa!!!");
                    return true;
                 }
             }
         }
        return false;
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        ItemMeta inHand = event.getItemInHand().getItemMeta();
        if ((inHand.getLore() != null && inHand.getLore().contains("Special")) || Check(inHand)) {

            event.setCancelled(true);
            if (!this.BlockCache.contains(event.getPlayer().getUniqueId())) {
                this.BlockCache.add(event.getPlayer().getUniqueId());
                Player plr = event.getPlayer();
                final UUID UID = plr.getUniqueId();

                plr.sendMessage("Cant place Block, you cant place Special Blocks!");
                BlockCache.add(UID);
                Bukkit.getScheduler().runTaskLater(pl, new Runnable() {
                      public void run() {
                          BlockCache.remove(UID);
                      }
                  },  1200L);
              }
         }
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
        Player plr = event.getPlayer();

        if (plr.isOp() && CustomChat) {
            // [Owner] Username >> Message
            String Message = "&f&l[&4&lAdmin&f&l]&f " + event.getPlayer().getDisplayName() + " &b»&f " + event.getMessage();
            event.setFormat(BasicUtils.colorify(Message));
        }
    }
}
