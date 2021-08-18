package me.mangorage.graves2.API;

import java.util.HashMap;
import java.util.Map;

public enum DBMS {
    SQLite("[SQLite] ");

    private String prefix;

    private DBMS(String prefix) {
        this.prefix = prefix;
    }

    public String toString() {
        return prefix;
    }

    private static Map<String, DBMS> prefixes;

    static {
        prefixes = new HashMap<String, DBMS>();
        for (DBMS dbms : prefixes.values())
            prefixes.put(dbms.toString(), dbms);
    }

    public static DBMS getDBMS(String prefix) {
        return prefixes.get(prefix);
    }
}

