package model.dao;

import model.dao.dbms.ConnectionFactory;
import model.dao.dbms.BookingDAODBMS;
import model.dao.dbms.FieldDAODBMS;
import model.dao.dbms.MatchDAODBMS;
import model.dao.dbms.TimeSlotDAODBMS;
import model.dao.dbms.UserDAODBMS;
import model.dao.filesystem.UserDAOFileSystem;
import model.dao.memory.BookingDAOMemory;
import model.dao.memory.FieldDAOMemory;
import model.dao.memory.MatchDAOMemory;
import model.dao.memory.TimeSlotDAOMemory;
import model.dao.memory.UserDAOMemory;

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

    public static BookingDAO getBookingDAO(PersistenceType type) throws SQLException {
        return switch (type) {
            case DBMS, FILESYSTEM -> {
                // Filesystem usa DBMS per i bookings
                yield new BookingDAODBMS();
            }
            case MEMORY -> new BookingDAOMemory();
        };
    }

    public static TimeSlotDAO getTimeSlotDAO(PersistenceType type) throws SQLException {
        return switch (type) {
            case DBMS, FILESYSTEM -> {
                // Filesystem usa DBMS per i time slots
                yield new TimeSlotDAODBMS();
            }
            case MEMORY -> new TimeSlotDAOMemory();
        };
    }
}
