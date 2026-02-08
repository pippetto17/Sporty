package model.dao.memory;

import model.dao.*;

public class MemoryDAOFactory implements DAOFactory {
    @Override
    public UserDAO getUserDAO() {
        return new UserDAOMemory();
    }

    @Override
    public MatchDAO getMatchDAO() {
        return new MatchDAOMemory();
    }

    @Override
    public FieldDAO getFieldDAO() {
        return new FieldDAOMemory();
    }

    @Override
    public NotificationDAO getNotificationDAO() {
        return new NotificationDAOMemory();
    }
}