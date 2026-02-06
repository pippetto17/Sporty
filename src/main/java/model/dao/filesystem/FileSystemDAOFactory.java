package model.dao.filesystem;

import model.dao.*;

public class FileSystemDAOFactory implements DAOFactory {
    private final model.dao.dbms.DbmsDAOFactory dbmsDelegate;

    public FileSystemDAOFactory() {
        this.dbmsDelegate = new model.dao.dbms.DbmsDAOFactory();
    }

    @Override
    public UserDAO getUserDAO() {
        return dbmsDelegate.getUserDAO();
    }

    @Override
    public MatchDAO getMatchDAO() {
        return new MatchDAOFileSystem();
    }

    @Override
    public FieldDAO getFieldDAO() {
        return dbmsDelegate.getFieldDAO();
    }

    @Override
    public NotificationDAO getNotificationDAO() {
        return new model.dao.memory.NotificationDAOMemory();
    }
}