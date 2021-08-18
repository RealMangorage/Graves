package me.mangorage.graves2.Menu;

import fr.minuskube.inv.ClickableItem;
import me.mangorage.graves2.PlayerGrave;

import java.sql.Time;
import java.util.ArrayList;
import java.util.List;

public class PlayerFilterData {
    private int Ticks = 0;
    private ClickableItem[] clickableItems = null;

    public void updateItems(ClickableItem[] Data, int Adder) {



    }

    public ClickableItem[] getItems() {
        return clickableItems;
    }

    public int getTicks() {
        return Ticks;
    }

    public void setTicks(int value) {
        Ticks = value;
    }

}
