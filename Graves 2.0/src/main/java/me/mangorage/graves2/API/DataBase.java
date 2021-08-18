package me.mangorage.graves2.API;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

abstract class Database {

    protected Logger log;
    protected final String prefix;
    protected DBMS driver;
    protected Connection connection;
    protected Map<PreparedStatement, StatementEnum> preparedStatements = new HashMap<PreparedStatement, StatementEnum>();
    protected int lastUpdate;

    public Database(Logger log, String prefix, DBMS dbms) throws DatabaseException {
        if (log == null)
            throw new DatabaseException("Logger cannot be null.");
        if (prefix == null || prefix.length() == 0)
            throw new DatabaseException("Plugin prefix cannot be null or empty.");

        this.log = log;
        this.prefix = prefix;
        this.driver = dbms;
    }

    protected final String prefix(String message) {
        return this.prefix + this.driver + message;
    }

    @Deprecated
    public final void writeInfo(String toWrite) {
        info(toWrite);
    }


    @Deprecated
    public final void writeError(String toWrite, boolean severe) {
        if (severe) {
            error(toWrite);
        } else {
            warning(toWrite);
        }
    }

    public final void info(String info) {
        if (info != null && !info.isEmpty()) {
            this.log.info(prefix(info));
        }
    }

    public final void warning(String warning) {
        if (warning != null && !warning.isEmpty()) {
            this.log.warning(prefix(warning));
        }
    }

    public final void error(String error) {
        if (error != null && !error.isEmpty()) {
            this.log.severe(prefix(error));
        }
    }

    protected abstract boolean initialize();

    public final DBMS getDriver() {
        return getDBMS();
    }

    public final DBMS getDBMS() {
        return this.driver;
    }

    public abstract boolean open();

    public final boolean close() {
        if (connection != null) {
            try {
                connection.close();
                return true;
            } catch (SQLException e) {
                this.writeError("Could not close connection, SQLException: " + e.getMessage(), true);
                return false;
            }
        } else {
            this.writeError("Could not close connection, it is null.", true);
            return false;
        }
    }

    @Deprecated
    public final boolean isConnected() {
        return isOpen();
    }

    public final Connection getConnection() {
        return this.connection;
    }

    public final boolean isOpen() {
        return isOpen(1);
    }

    public final boolean isOpen(int seconds) {
        if (connection != null)
            try {
                if (connection.isValid(seconds))
                    return true;
            } catch (SQLException e) {}
        return false;
    }

    @Deprecated
    public final boolean checkConnection() {
        return isOpen();
    }

    public final int getLastUpdateCount() {
        return this.lastUpdate;
    }

    protected abstract void queryValidation(StatementEnum statement) throws SQLException;

    public final ResultSet query(String query) throws SQLException {
        queryValidation(this.getStatement(query));
        Statement statement = this.getConnection().createStatement();
        if (statement.execute(query)) {
            return statement.getResultSet();
        } else {
            int uc = statement.getUpdateCount();
            this.lastUpdate = uc;
            return this.getConnection().createStatement().executeQuery("SELECT " + uc);
        }
    }

    protected final ResultSet query(PreparedStatement ps, StatementEnum statement) throws SQLException {
        queryValidation(statement);
        if (ps.execute()) {
            return ps.getResultSet();
        } else {
            int uc = ps.getUpdateCount();
            this.lastUpdate = uc;
            return this.connection.createStatement().executeQuery("SELECT " + uc);
        }
    }


    public final ResultSet query(PreparedStatement ps) throws SQLException {
        ResultSet output = query(ps, preparedStatements.get(ps));
        preparedStatements.remove(ps);
        return output;
    }

    public final PreparedStatement prepare(String query) throws SQLException {
        StatementEnum s = getStatement(query); // Throws an exception and stops creation of the PreparedStatement.
        PreparedStatement ps = connection.prepareStatement(query);
        preparedStatements.put(ps, s);
        return ps;
    }

    public ArrayList<Long> insert(String query) throws SQLException {
        ArrayList<Long> keys = new ArrayList<Long>();

        PreparedStatement ps = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
        lastUpdate = ps.executeUpdate();

        ResultSet key = ps.getGeneratedKeys();
        if (key.next())
            keys.add(key.getLong(1));
        return keys;
    }

    public ArrayList<Long> insert(PreparedStatement ps) throws SQLException {
        lastUpdate = ps.executeUpdate();
        preparedStatements.remove(ps);

        ArrayList<Long> keys = new ArrayList<Long>();
        ResultSet key = ps.getGeneratedKeys();
        if (key.next())
            keys.add(key.getLong(1));
        return keys;
    }

    public final ResultSet query(Calendar.Builder builder) throws SQLException {
        return query(builder.toString());
    }

    public abstract StatementEnum getStatement(String query) throws SQLException;

    @Deprecated
    public boolean checkTable(String table) {
        return isTable(table);
    }

    @Deprecated
    public boolean wipeTable(String table) {
        return truncate(table);
    }

    public abstract boolean isTable(String table);

    public abstract boolean truncate(String table);
}