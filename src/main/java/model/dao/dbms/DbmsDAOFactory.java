package model.dao.dbms;

import exception.DataAccessException;
import model.dao.*;
import model.dao.memory.NotificationDAOMemory;

import java.sql.Connection;
import java.sql.SQLException;

public class DbmsDAOFactory implements DAOFactory {
    @Override
    public UserDAO getUserDAO() {
        try {
            Connection connection = ConnectionFactory.getConnection();
            return new UserDAODBMS(connection);
        } catch (SQLException e) {
            throw new DataAccessException("Error creating UserDAO: " + e.getMessage(), e);
        }
    }

    @Override
    public MatchDAO getMatchDAO() {
        try {
            Connection connection = ConnectionFactory.getConnection();
            return new MatchDAODBMS(connection);
        } catch (SQLException e) {
            throw new DataAccessException("Error creating MatchDAO: " + e.getMessage(), e);
        }
    }

    @Override
    public FieldDAO getFieldDAO() {
        try {
            Connection connection = ConnectionFactory.getConnection();
            return new FieldDAODBMS(connection);
        } catch (SQLException e) {
            throw new DataAccessException("Error creating FieldDAO: " + e.getMessage(), e);
        }
    }

    @Override
    public NotificationDAO getNotificationDAO() {
        return new NotificationDAOMemory();
    }
}