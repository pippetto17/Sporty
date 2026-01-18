package model.dao;

import model.dao.dbms.*;
import model.dao.filesystem.UserDAOFileSystem;
import model.dao.memory.*;

import java.sql.Connection;
import java.sql.SQLException;

public class DAOFactory {

    public enum PersistenceType {
        DBMS,
        FILESYSTEM,
        MEMORY
    }

    public static UserDAO getUserDAO(PersistenceType type) throws SQLException {
        return switch (type) {
            case DBMS -> {
                Connection connection = ConnectionFactory.getConnection();
                yield new UserDAODBMS(connection);
            }
            case FILESYSTEM -> new UserDAOFileSystem();
            case MEMORY -> new UserDAOMemory();
        };
    }

    public static MatchDAO getMatchDAO(PersistenceType type) throws SQLException {
        return switch (type) {
            case DBMS, FILESYSTEM -> {
                // Filesystem usa DBMS per i match (come da specifica)
                Connection connection = ConnectionFactory.getConnection();
                yield new MatchDAODBMS(connection);
            }
            case MEMORY -> new MatchDAOMemory();
        };
    }

    public static FieldDAO getFieldDAO(PersistenceType type) throws SQLException {
        return switch (type) {
            case DBMS, FILESYSTEM -> {
                // Filesystem usa DBMS per i field (come da specifica)
                Connection connection = ConnectionFactory.getConnection();
                yield new FieldDAODBMS(connection);
            }
            case MEMORY -> new FieldDAOMemory();
        };
    }

    public static BookingDAO getBookingDAO(PersistenceType type) {
        return switch (type) {
            case DBMS, FILESYSTEM -> new BookingDAODBMS(); // Filesystem usa DBMS per i bookings
            case MEMORY -> new BookingDAOMemory();
        };
    }

    public static TimeSlotDAO getTimeSlotDAO(PersistenceType type) {
        return switch (type) {
            case DBMS, FILESYSTEM -> new TimeSlotDAODBMS(); // Filesystem usa DBMS per i time slots
            case MEMORY -> new TimeSlotDAOMemory();
        };
    }
}
