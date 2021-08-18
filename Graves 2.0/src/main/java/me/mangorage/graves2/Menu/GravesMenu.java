package me.mangorage.graves2.Menu;

import de.leonhard.storage.Json;
import de.leonhard.storage.Yaml;
import fr.minuskube.inv.ClickableItem;
import fr.minuskube.inv.InventoryManager;
import fr.minuskube.inv.SmartInventory;
import fr.minuskube.inv.content.*;
import me.mangorage.graves2.Graves2;
import me.mangorage.graves2.Utils.BasicUtils;
import me.mangorage.graves2.Items.DeathNote;
import me.mangorage.graves2.PlayerGrave;
import org.apache.commons.lang.StringUtils;
import org.bukkit.*;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.plugin.Plugin;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.logging.Filter;

public class GravesMenu implements InventoryProvider, Listener {
    private ArrayList<PlayerGrave> GravesList = new ArrayList<>();
    private HashMap<String, PlayerGrave> GravesMap = new HashMap<>();
    private HashMap<String, Integer> PlayerData = new HashMap<>();
    private HashMap<Player, PlayerFilterData> FilterData = new HashMap<>();
    private HashMap<Player, List<PlayerGrave>> PlayerGraveDataFilter = new HashMap<>();

    private HashMap<Player, Integer> PlayerGraveTicks = new HashMap<>();

    private ItemStack Glass = new ItemStack(Material.BLACK_STAINED_GLASS_PANE, 1);
    private HashMap<Player, String> SearchSettings = new HashMap<>();
    private HashMap<Player, Integer> SortSettings = new HashMap<>();
    private List<String> SortingOptions = new ArrayList<>();
    private List<Player> Searching = new ArrayList<>();
    private Yaml file;
    private Plugin pl;

    public SmartInventory INVENTORY = null;

    int Pages = 100;
    int limit = (5*9);

    public GravesMenu(Graves2 pl, InventoryManager YM) {
        this.pl = pl;
        file = new Yaml("sortingConfig", pl.getDataFolder().getPath());

        List<String> details = new ArrayList<>();
        details.add("BlacklistedWorlds add worlds that you dont want to be shown in the Sorting Options");

        if (!file.contains("config-version")) {
            file.setHeader();
            file.set("config-version", 1.0);
            file.set("BlacklistedWorlds", new ArrayList<>());
        }
        /**
         * Sorting:
         *
         * ALL
         * Unlocked
         * Private
         * Personal
         * Worlds
         */
        SortingOptions.add("ALL");
        SortingOptions.add("Unlocked");
        SortingOptions.add("Private");
        SortingOptions.add("Personal");

        Bukkit.getWorlds().forEach((world -> {
            List<String> worlds_blacklisted = file.getStringList("BlacklistedWorlds");
            if (worlds_blacklisted == null) {
                worlds_blacklisted = new ArrayList<>();
                pl.getConfig().set("SortingBlacklistedWorlds", worlds_blacklisted);
            }

            String worldname = world.getName();

            if (!worlds_blacklisted.contains(worldname)) {
                SortingOptions.add(worldname);
            }
        }));

        ItemMeta GlassMeta = Glass.getItemMeta();
        GlassMeta.setDisplayName("");
        Glass.setItemMeta(GlassMeta);
        INVENTORY = SmartInventory.builder().manager(YM)
                .id("GravesMenu!")
                .provider(this)
                .size(6, 9)
                .title(ChatColor.BLUE + "Graves Menu!")
                .build();
        Bukkit.getPluginManager().registerEvents(this, pl);
    }


    @EventHandler(priority = EventPriority.LOWEST)
    private void onChat(AsyncPlayerChatEvent event) {
        if (Searching.contains(event.getPlayer())) {
            event.setCancelled(true);
            SearchSettings.remove(event.getPlayer());
            SearchSettings.put(event.getPlayer(), event.getMessage());
            Searching.remove(event.getPlayer());
            Bukkit.getScheduler().runTaskLater(pl, new Runnable() {
                @Override
                public void run() {
                    openInv(event.getPlayer());
                }
            }, 0);
        }
    }

    public void update(ArrayList<PlayerGrave> GravesA, HashMap<String, PlayerGrave> GravesB) {
        GravesList = GravesA;
        GravesMap = GravesB;
    }


   private String calculateTime(long seconds) {

        long minute = TimeUnit.SECONDS.toMinutes(seconds) - (TimeUnit.SECONDS.toHours(seconds) * 60);
        long second = TimeUnit.SECONDS.toSeconds(seconds) - (TimeUnit.SECONDS.toMinutes(seconds) * 60);

        String data = "%MIN%m %SEC%s";
        data = data.replaceAll("%MIN%", minute + "");
        data = data.replaceAll("%SEC%", second + "");
        return data;

    }

    private String getFormattedTime(long value) {
        long nData = value - System.currentTimeMillis();

        return calculateTime(nData/1000);
    }

    private String getPosData(Location data) {
        return "";
    }


    private ItemStack getHead(PlayerGrave grave) {
        ItemStack nHead = new ItemStack(Material.PLAYER_HEAD, 1, (short) SkullType.PLAYER.ordinal());
        SkullMeta meta = (SkullMeta) nHead.getItemMeta();
        meta.setOwningPlayer(Bukkit.getOfflinePlayer(UUID.fromString(grave.PlayerID)));
        nHead.setItemMeta(meta);
        ItemMeta metaB = nHead.getItemMeta();
        metaB.setDisplayName(grave.PlayerName);

        long t = System.currentTimeMillis() - grave.goespublicat;
        List<String> lore = new ArrayList<>();



        lore.add(BasicUtils.colorify(getPosData(grave.Deathloca)));
        lore.add(BasicUtils.colorify("World: " + grave.Deathloca.getWorld().getName()));
        lore.add(BasicUtils.colorify(""));
        lore.add(BasicUtils.colorify("Owner: " + grave.PlayerName));

        if (grave.Public) {
            lore.add(BasicUtils.colorify("Grave Unlocked"));
        } else {
            lore.add(BasicUtils.colorify("Grave locked"));
            lore.add(BasicUtils.colorify(getFormattedTime(grave.goespublicat)));
        }

        metaB.setLore(lore);
        nHead.setItemMeta(metaB);
        return nHead;
    }

    private ItemStack getArrow(int Direction, Pagination pagination) {
        ItemStack Arrow = new ItemStack(Material.ARROW, 1);
        ItemMeta ArrowMeta = Arrow.getItemMeta();
        List<String> Lore = new ArrayList<>();

        if (Direction == 1) {
            // Next
            ArrowMeta.setDisplayName(BasicUtils.colorify("&aNext Page"));
            int a = pagination.getPage() + 1;
            Lore.add(BasicUtils.colorify("&eNext Page: " + a));
            if (pagination.isLast()) {
                return Glass;
            }
        } else if (Direction == 2) {
            ArrowMeta.setDisplayName(BasicUtils.colorify("&aPrevious Page"));
            int a = (pagination.getPage() - 1);
            Lore.add(BasicUtils.colorify("&ePrevious Page: " + a));
            if (pagination.isFirst()) {
                return Glass;
            }
        }

        ArrowMeta.setLore(Lore);
        Arrow.setItemMeta(ArrowMeta);


        return Arrow;
    }

    public void openInv(Player plr) {
        INVENTORY.open(plr, 0);
    }

    public void ClickGrave(PlayerGrave grave, Player Plr, ClickType clickType) {
        if (clickType.isRightClick()) {
            if (Plr.hasPermission("Graves.menu.open")) {
                Plr.openInventory(grave.Grave);
            }
        } else if (clickType == ClickType.MIDDLE) {
            if (Plr.hasPermission("Graves.menu.getdeathnote")) {
                Plr.getInventory().addItem(DeathNote.create(grave));
            }
        } else if (clickType.isLeftClick()) {
            if (Plr.hasPermission("Graves.menu.teleport")) {
                Plr.teleport(grave.Deathloca.add(0, 1,1));
            }
        }
    }

    private List<PlayerGrave> getGravesRawFiltered(Player plr) {
        // Raw
        // Filtered

        List<PlayerGrave> gravesRaw = new ArrayList<>();

        if (SortSettings.containsKey(plr)) {
            int SortSetting = SortSettings.get(plr);
            if (SortSetting == 0) {
                return getGravesFiltered(GravesList, SearchSettings.get(plr));
            } else {
                for (PlayerGrave gave : GravesList) {
                    if (SortSetting == 1) {
                        if (gave.Public == false) {
                            gravesRaw.add(gave);
                        }
                    }
                }
            }
        } else {
            SortSettings.put(plr, 0);
            return getGravesRawFiltered(plr);
        }

        return getGravesFiltered(gravesRaw, SearchSettings.get(plr));
    }




    private List<PlayerGrave> getGravesFiltered(List<PlayerGrave> Data, String Filter) {
        if (Filter == null) return Data;

        List<PlayerGrave> graves = new ArrayList<>();

        for (PlayerGrave playerGrave : Data) {
            if (playerGrave.PlayerName.contains(Filter)) {
                graves.add(playerGrave);
            }
        }
        return graves;
    }

    private String getSearchData(Player plr) {
        if (SearchSettings.containsKey(plr)) {
            return SearchSettings.get(plr);
        }
        return "NONE";
    }

    private ItemStack getSearch(Player plr) {
        ItemStack searchitem = new ItemStack(Material.OAK_SIGN, 1);
        ItemMeta meta = searchitem.getItemMeta();
        meta.setDisplayName(ChatColor.GOLD + "Search Graves");

        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.GREEN + "Searching: " + getSearchData(plr));
        lore.add("");
        lore.add(ChatColor.GREEN + "" + ChatColor.BOLD + "Right click to Clear");
        lore.add(ChatColor.GREEN + "" + ChatColor.BOLD + "Left click to Search");

        meta.setLore(lore);
        searchitem.setItemMeta(meta);
        return searchitem;
    }

    private ItemStack getSorting(Player plr) {
        /**
         * Sorting:
         *
         * ALL
         * Unlocked
         * Private
         * Personal
         * Worlds
         */


        String Setting = "ALL";

        if (SortSettings.containsKey(plr) ) {
            Setting = SortingOptions.get(SortSettings.get(plr));
        }

        ItemStack item = new ItemStack(Material.CLOCK, 1);
        ItemMeta itemMeta = item.getItemMeta();
        itemMeta.setDisplayName(ChatColor.GREEN + "Sort");


        List<String> lore = new ArrayList<>();
        lore.add("");

        String finalSetting = Setting;
        SortingOptions.forEach((Data -> {
            String info = Data.replace("world_", "");

            if (Data.contains("world_")) {
                info = Data.replace("world_", "");
            }

            if (finalSetting.equals(Data)) {
                lore.add(ChatColor.AQUA + "> " + info);
            } else {
                lore.add(ChatColor.GRAY + info);
            }

        }));
        lore.add("");
        lore.add(ChatColor.YELLOW + "Click to switch sort!");

        itemMeta.setLore(lore);
        item.setItemMeta(itemMeta);

        return item;
    }


    private void updateContent(Player plr, InventoryContents contents) {


    }



    @Override
    public void init(Player player, InventoryContents contents) {
        Pagination pagination = contents.pagination();

        List<PlayerGrave> GravesData = GravesList;



        int adder = 0;
        int Size_ = GravesData.size();
        if (pagination.isLast()) {
            int ladder = GravesData.size() - 36 * pagination.getPage();
            if (ladder > 0)
                adder = Math.abs(36 - Math.abs(ladder));
        } else {
            adder = 0;
        }

        if (Size_ == 0) {
            adder = 36;
        }

        ClickableItem[] items = new ClickableItem[GravesData.size() + adder];

        for (int i = 0; i < GravesData.size() + adder; i++) {
            if (i < Size_) {
                String UUID = ((PlayerGrave)GravesData.get(i)).SearchUUID;
                items[i] = ClickableItem.of(getHead(this.GravesMap.get(UUID)), e -> ClickGrave(this.GravesMap.get(UUID), (Player)e.getWhoClicked(), e.getClick()));
            } else {
                items[i] = ClickableItem.empty(this.Glass);
            }
        }

        contents.fill(ClickableItem.empty(this.Glass));

        pagination.setItems(items);
        pagination.setItemsPerPage(36);
        pagination.addToIterator(contents.newIterator(SlotIterator.Type.HORIZONTAL, 1, 0));



    }

    @Override
    public void update(Player player, InventoryContents contents) {
        Pagination pagination = contents.pagination();

        contents.set(5, 0, ClickableItem.of(getArrow(2, pagination), e -> {
            if (!pagination.isFirst())
                this.INVENTORY.open(player, pagination.previous().getPage());
        }));

        contents.set(5, 3, ClickableItem.of(getSearch(player), e -> {
            if (e.getClick() == ClickType.LEFT) {
                this.Searching.add(player);
                this.INVENTORY.close(player);
                player.sendMessage(ChatColor.GREEN + "Type in what you are Searching for!");
            } else if (e.getClick() == ClickType.RIGHT) {
                this.SearchSettings.remove(player);
            }
        }));

        contents.set(5, 5, ClickableItem.of(getSorting(player), e -> {
            player.sendMessage("Set Optidon!");
            int Value = 0;

            if (SortSettings.containsKey(player)) {
                Value = SortSettings.get(player);
            }

            if (Value >= this.SortingOptions.size() - 1) {
                Value = 0;
            } else {
                Value++;
            }

            this.SortSettings.remove(player);
            player.sendMessage("Set Option!");
            this.SortSettings.put(player, Integer.valueOf(Value));
        }));

        contents.set(5, 8, ClickableItem.of(getArrow(1, pagination), e -> {
            if (!pagination.isLast())
                this.INVENTORY.open(player, pagination.next().getPage());
        }));
    }

    static class Icons {
        public static String[] getSearch() {
            return new String[] {"Search: ", "", "", ""};
        }
    }
}
