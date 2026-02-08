package model.dao.memory;

import model.dao.*;

public class MemoryDAOFactory implements DAOFactory {
    private final UserDAO userDAO = new UserDAOMemory();
    private final MatchDAO matchDAO = new MatchDAOMemory();
    private final FieldDAO fieldDAO = new FieldDAOMemory();
    private final NotificationDAO notificationDAO = new NotificationDAOMemory();

    @Override
    public UserDAO getUserDAO() {
        return userDAO;
    }

    @Override
    public MatchDAO getMatchDAO() {
        return matchDAO;
    }

    @Override
    public FieldDAO getFieldDAO() {
        return fieldDAO;
    }

    @Override
    public NotificationDAO getNotificationDAO() {
        return notificationDAO;
    }
}