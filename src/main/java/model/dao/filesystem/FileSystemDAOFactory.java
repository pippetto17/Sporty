package model.dao.filesystem;

import model.dao.*;

public class FileSystemDAOFactory implements DAOFactory {
    private final DAOFactory dbmsDelegate;

    public FileSystemDAOFactory(DAOFactory dbmsDelegate) {
        this.dbmsDelegate = dbmsDelegate;
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
        return dbmsDelegate.getNotificationDAO();
    }
}