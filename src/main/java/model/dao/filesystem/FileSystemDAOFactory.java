package model.dao.filesystem;

import model.dao.*;

public class FileSystemDAOFactory implements DAOFactory {

    @Override
    public UserDAO getUserDAO() {
        return new UserDAOFileSystem();
    }

    @Override
    public MatchDAO getMatchDAO() {
        return new MatchDAOFileSystem();
    }

    @Override
    public FieldDAO getFieldDAO() {
        return new FieldDAOFileSystem();
    }

    @Override
    public NotificationDAO getNotificationDAO() {
        return new model.dao.memory.NotificationDAOMemory();
    }
}
