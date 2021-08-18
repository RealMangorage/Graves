package me.mangorage.graves2;

import de.leonhard.storage.Json;
import de.leonhard.storage.Yaml;
import me.mangorage.graves2.API.Serializer;
import me.mangorage.graves2.Extra.*;
import me.mangorage.graves2.Menu.GravesMenu;
import me.mangorage.graves2.Utils.BasicUtils;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.inventory.Inventory;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.sql.*;
import java.util.*;
import java.util.logging.Logger;

public final class Graves2 extends JavaPlugin {
    private static final Logger log = Logger.getLogger("Minecraft");
    HashMap<String, PlayerGrave> AdminGraves = new HashMap<>();
    ArrayList<PlayerGrave> Graves = new ArrayList<>();
    BetaSaving BS;
    GravesMenu GM;
    me.mangorage.graves2.Menu.UpgradeMenu UpgradeMenu;
    public static Economy Eco;




    /**
     * Save:
     *
     * X,Y,Z
     * World
     * GraveInventory
     * PlayerName and ID
     * goespublicat
     *
     */

    /**
     * Permissions:
     * Graves.comamnd.open
     * Graves.command.tp
     * Graves.command.menu
     * Graves.command.help
     * Graves.use
     */

    public void loadGravesData() throws SQLException {
        ArrayList<String> graves = BasicUtils.DB.getGraves();

        System.out.println("Loading " + graves.size() + " graves!");
        int loaded = 0;

        for (String GraveUUID : graves) {
            Connection conn;
            PreparedStatement ps;
            ResultSet rs;
            conn = BasicUtils.DB.DB.getConnection();
            ps = conn.prepareStatement("SELECT * FROM GravesData WHERE grave = '" + GraveUUID + "';");
            rs = ps.executeQuery();

            String Inv = rs.getString("Inventory");
            String PlayerID = rs.getString("PlayerID");
            String PlayerName = rs.getString("PlayerName");
            int LocationX = rs.getInt("x");
            int LocationY = rs.getInt("y");
            int LocationZ = rs.getInt("z");
            String World = rs.getString("world");
            long TicksLeft = rs.getLong("Ticksleft");
            long Created = rs.getLong("created");
            long GoesPublicAt = rs.getLong("goespublic");
            String SearchID = rs.getString("SearchID");
            boolean isPub = rs.getBoolean("isPublic");
            Inventory InvData = null;

            Location loca = new Location(Bukkit.getWorld(World), LocationX, LocationY, LocationZ);

            try {
                InvData = Serializer.fromBase64(Inv);
            } catch (IOException e) {
                e.printStackTrace();
            }

            if (InvData != null) {
                PlayerGrave newGrave = new PlayerGrave(this, InvData, PlayerID, PlayerName, loca, TicksLeft, Created, GoesPublicAt, isPub);
                Graves.add(newGrave);
                newGrave.SearchUUID = SearchID;
                AdminGraves.put(newGrave.SearchUUID, newGrave);
                loaded++;
            }

        }

        System.out.println("loaded: " + loaded + " graves");
    }


    private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        Eco = rsp.getProvider();
        return Eco != null;
    }

    public static Economy getEconomy() {
        return Eco;
    }


    @Override
    public void onEnable() {
        // Configure File Structure!

        if (!setupEconomy() ) {
            log.severe(String.format("[%s] - Disabled due to no Vault dependency found!", getDescription().getName()));
            getServer().getPluginManager().disablePlugin(this);
            return;
        }


        if (!getDataFolder().exists()) {
            getDataFolder().mkdir();
        }

        try {
            BS = new BetaSaving(this);
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }

        BasicUtils.load(BS);

        try {
            loadGravesData();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }

        Yaml secretfile = new Yaml("extra", getDataFolder().getPath());
        if (secretfile.getBoolean("Use-Extra")) {
            Bukkit.getPluginManager().registerEvents(new ExtraFeatures(this), this);
            Bukkit.getPluginManager().registerEvents(new MobStacking(this), this);
        } else {
            secretfile.getFile().delete(); // No need to keep it, for My Personal use!
        }


        new PlayerGraveManager(this, Eco);


    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic

        System.out.println("[Graves] Saving Graves!");
        int Success = 0;
        for (PlayerGrave Shut : Graves) {
            try {
                Shut.Shutdown();
                Success++;
            } catch (SQLException e) {
                e.printStackTrace();
                Success--;
            }
        }

        System.out.println("[Graves] Successfully Saved " + Success + " out of " + Graves.size() + " Graves!");

        if (BS != null) {
            BasicUtils.DB.onClose();
        }
    }



}
