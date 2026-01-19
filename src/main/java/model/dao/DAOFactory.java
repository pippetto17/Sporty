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

    public static UserDAO getUserDAO(PersistenceType type) {
        return switch (type) {
            case DBMS -> {
                try {
                    Connection connection = ConnectionFactory.getConnection();
                    yield new UserDAODBMS(connection);
                } catch (SQLException e) {
                    throw new exception.DataAccessException("Error creating UserDAO: " + e.getMessage(), e);
                }
            }
            case FILESYSTEM -> new UserDAOFileSystem();
            case MEMORY -> new UserDAOMemory();
        };
    }

    public static MatchDAO getMatchDAO(PersistenceType type) {
        return switch (type) {
            case DBMS, FILESYSTEM -> {
                try {
                    Connection connection = ConnectionFactory.getConnection();
                    yield new MatchDAODBMS(connection);
                } catch (SQLException e) {
                    throw new exception.DataAccessException("Error creating MatchDAO: " + e.getMessage(), e);
                }
            }
            case MEMORY -> new MatchDAOMemory();
        };
    }

    public static FieldDAO getFieldDAO(PersistenceType type) {
        return switch (type) {
            case DBMS, FILESYSTEM -> {
                try {
                    Connection connection = ConnectionFactory.getConnection();
                    yield new FieldDAODBMS(connection);
                } catch (SQLException e) {
                    throw new exception.DataAccessException("Error creating FieldDAO: " + e.getMessage(), e);
                }
            }
            case MEMORY -> new FieldDAOMemory();
        };
    }

    public static BookingDAO getBookingDAO(PersistenceType type) {
        return switch (type) {
            case DBMS, FILESYSTEM -> new BookingDAODBMS();
            case MEMORY -> new BookingDAOMemory();
        };
    }

    public static TimeSlotDAO getTimeSlotDAO(PersistenceType type) {
        return switch (type) {
            case DBMS, FILESYSTEM -> new TimeSlotDAODBMS();
            case MEMORY -> new TimeSlotDAOMemory();
        };
    }
}
