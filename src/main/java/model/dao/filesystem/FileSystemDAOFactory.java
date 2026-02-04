package model.dao.filesystem;

import model.dao.*;

/**
 * FileSystem DAO Factory implementing hybrid persistence strategy.
 * Strategy: Match uses FileSystem with embedded objects (true dual
 * persistence),
 * while User and Field delegate to DBMS for consistency and foreign key
 * integrity.
 * This satisfies the requirement of "at least 1 DAO with dual persistence".
 */
public class FileSystemDAOFactory implements DAOFactory {
    private final model.dao.dbms.DbmsDAOFactory dbmsDelegate;

    public FileSystemDAOFactory() {
        this.dbmsDelegate = new model.dao.dbms.DbmsDAOFactory();
    }

    @Override
    public UserDAO getUserDAO() {
        // Delegate to DBMS for consistency
        return dbmsDelegate.getUserDAO();
    }

    @Override
    public MatchDAO getMatchDAO() {
        // TRUE dual persistence: Match stored in filesystem with embedded User/Field
        // objects
        return new MatchDAOFileSystem();
    }

    @Override
    public FieldDAO getFieldDAO() {
        // Delegate to DBMS for consistency
        return dbmsDelegate.getFieldDAO();
    }

    @Override
    public NotificationDAO getNotificationDAO() {
        return new model.dao.memory.NotificationDAOMemory();
    }
}