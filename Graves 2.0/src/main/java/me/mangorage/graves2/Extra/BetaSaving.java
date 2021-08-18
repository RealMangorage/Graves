package me.mangorage.graves2.Extra;

import me.mangorage.graves2.API.DBMS;
import me.mangorage.graves2.API.SQLite;
import me.mangorage.graves2.API.Serializer;
import me.mangorage.graves2.Graves2;
import me.mangorage.graves2.PlayerGrave;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

public class BetaSaving {

    Graves2 pl;


    public SQLite DB;


    public BetaSaving(Graves2 Plug) throws SQLException {
        pl = Plug;
        DB = new SQLite(pl.getLogger(), "CacheBeta", pl.getDataFolder().getAbsolutePath(), "GravesData");

        try {
            DB.open();
        } catch (Exception e) {
            pl.getLogger().info(e.getMessage());
            pl.getPluginLoader().disablePlugin(pl);
        }
        sqlTableCheck();

    }

    public void sqlTableCheck() throws SQLException {
        if(DB.checkTable("GravesData")){
            return;
        }else{
            try {
                DB.query("CREATE TABLE IF NOT EXISTS GravesData ('grave' varchar NOT NULL PRIMARY KEY,`playerID` varchar,`PlayerName` varchar,`x` int,`y` int,`z` int,`world` varchar,`Ticksleft` bigint,`created` bigint, 'goespublic' bigint, 'SearchID' varchar, 'isPublic' boolean, 'Inventory' varchar );");

            } catch (SQLException e) {
                e.printStackTrace();
            }

            pl.getLogger().info("GravesData has been created");
        }
    }

    private void CleanUp() throws SQLException {
        PreparedStatement ps;
        ps = DB.getConnection().prepareStatement("VACUUM main INTO '" + pl.getDataFolder().getAbsolutePath() + "/GravesData_Backup" + "';");
        ps.executeUpdate();
    }

    public void onClose() {
        DB.close(); // So we can reload the plugin!
    }

    public void saveGrave(PlayerGrave grave) throws SQLException { // Should be done!
        PreparedStatement ps;
        String uuid = grave.SearchUUID.replace("-", "");
        ps = DB.getConnection().prepareStatement("INSERT OR REPLACE INTO GravesData (grave, playerID, PlayerName, x, y, z, world, Ticksleft, created, goespublic, SearchID, isPublic, Inventory) VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?)");
        ps.setString(1, grave.SearchUUID); // Grave
        ps.setString(2, grave.PlayerID); // playerID
        ps.setString(3, grave.PlayerName); // PlayerName
        ps.setInt(4, grave.Deathloca.getBlockX()); // x
        ps.setInt(5, grave.Deathloca.getBlockY()); // y
        ps.setInt(6, grave.Deathloca.getBlockZ()); // z
        ps.setString(7, grave.Deathloca.getWorld().getName()); // World
        ps.setLong(8, ((grave.goespublicat - System.currentTimeMillis())/1000)*20); // TicksLeft
        ps.setLong(9, grave.createdAt); // created
        ps.setLong(10, grave.goespublicat); // goespublic
        ps.setString(11, grave.SearchUUID); // SearchID
        ps.setBoolean(12, grave.Public); // isPublic
        ps.setString(13, Serializer.toBase64(grave.Grave)); // Inventory
        ps.executeUpdate();
    }

    public void deleteGrave(PlayerGrave grave) throws SQLException {
        PreparedStatement ps;
        String uuid = grave.SearchUUID.replace("-", "");
        ps = DB.getConnection().prepareStatement("DELETE FROM GravesData WHERE grave='" + grave.SearchUUID + "';");
        ps.executeUpdate();
    }

    public ArrayList<String> getGraves() throws SQLException {
        ArrayList<String> graves = new ArrayList<>();
        Connection conn;
        PreparedStatement ps;
        ResultSet rs;
        conn = DB.getConnection();
        ps = conn.prepareStatement("SELECT grave FROM GravesData");
        rs = ps.executeQuery();
        while (rs.next()) {
            graves.add(rs.getString(1));
        }
        return graves;
    }
}


