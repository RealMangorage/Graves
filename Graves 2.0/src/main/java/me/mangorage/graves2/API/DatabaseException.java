package me.mangorage.graves2.API;

public class DatabaseException extends RuntimeException {
    private static final long serialVersionUID = 3063547825200154629L;

    public DatabaseException(String message) {
        super(message);
    }
}