package me.mangorage.graves2.API;

import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Logger;

public class SQLite extends FilenameDatabase {
    private enum Statements implements StatementEnum {
        SELECT("SELECT"),
        INSERT("INSERT"),
        UPDATE("UPDATE"),
        DELETE("DELETE"),
        REPLACE("REPLACE"),
        CREATE("CREATE"),
        ALTER("ALTER"),
        DROP("DROP"),
        ANALYZE("ANALYZE"),
        ATTACH("ATTACH"),
        BEGIN("BEGIN"),
        DETACH("DETACH"),
        END("END"),
        EXPLAIN("EXPLAIN"),
        INDEXED("INDEXED"),
        PRAGMA("PRAGMA"),
        REINDEX("REINDEX"),
        RELEASE("RELEASE"),
        SAVEPOINT("SAVEPOINT"),
        VACUUM("VACUUM"),
        LINE_COMMENT("--"),
        BLOCK_COMMENT("/*");

        private String string;

        private Statements(String string) {
            this.string = string;
        }

        public String toString() {
            return string;
        }
    }

    public SQLite(Logger log, String prefix, String directory, String filename) {
        super(log, prefix, DBMS.SQLite, directory, filename);
    }

    public SQLite(Logger log, String prefix, String directory, String filename, String extension) {
        super(log, prefix, DBMS.SQLite, directory, filename, extension);
    }


    public SQLite(Logger log, String prefix) {
        super(log, prefix, DBMS.SQLite);
    }

    protected boolean initialize() {
        try {
            Class.forName("org.sqlite.JDBC");
            return true;
        } catch (ClassNotFoundException e) {
            error("Class not found in initialize(): " + e);
            return false;
        }
    }

    @Override
    public boolean open() {
        if (initialize()) {
            try {
                connection = DriverManager.getConnection("jdbc:sqlite:" + (getFile() == null ? ":memory:" : getFile().getAbsolutePath()));
                return true;
            } catch (SQLException e) {
                error("Could not establish an SQLite connection, SQLException: " + e.getMessage());
                return false;
            }
        } else {
            return false;
        }
    }

    @Override
    protected void queryValidation(StatementEnum statement) throws SQLException { }

    @Override
    public Statements getStatement(String query) throws SQLException {
        String[] statement = query.trim().split(" ", 2);
        try {
            Statements converted = Statements.valueOf(statement[0].toUpperCase());
            return converted;
        } catch (IllegalArgumentException e) {
            throw new SQLException("Unknown statement: \"" + statement[0] + "\".");
        }
    }

    @Deprecated
    public boolean createTable(String query) {
        Statement statement = null;
        try {
            if (query == null || query.equals("")) {
                this.writeError("Could not create table: query is empty or null.", true);
                return false;
            }

            statement = connection.createStatement();
            statement.execute(query);
            statement.close();
            return true;
        } catch (SQLException e) {
            this.writeError("Could not create table, SQLException: " + e.getMessage(), true);
            return false;
        }
    }

    @Override
    public boolean isTable(String table) {
        DatabaseMetaData md = null;
        try {
            md = this.connection.getMetaData();
            ResultSet tables = md.getTables(null, null, table, null);
            if (tables.next()) {
                tables.close();
                return true;
            } else {
                tables.close();
                return false;
            }
        } catch (SQLException e) {
            error("Could not check if table \"" + table + "\" exists, SQLException: " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean truncate(String table) {
        Statement statement = null;
        String query = null;
        try {
            if (!this.isTable(table)) {
                error("Table \"" + table + "\" does not exist.");
                return false;
            }
            statement = connection.createStatement();
            query = "DELETE FROM " + table + ";";
            statement.executeQuery(query);
            statement.close();
            return true;
        } catch (SQLException e) {
            if (!(e.getMessage().toLowerCase().contains("locking") || e.getMessage().toLowerCase().contains("locked")) &&
                    !e.toString().contains("not return ResultSet"))
                error("Error in wipeTable() query: " + e);
            return false;
        }
    }

    @Deprecated
    public ResultSet retry(String query) {
        try {
            return this.getConnection().createStatement().executeQuery(query);
        } catch (SQLException e) {
            if (e.getMessage().toLowerCase().contains("locking") || e.getMessage().toLowerCase().contains("locked")) {
                this.writeError("Please close your previous ResultSet to run the query: \n\t" + query, false);
            } else {
                this.writeError("SQLException in retry(): " + e.getMessage(), false);
            }
        }
        return null;
    }
}