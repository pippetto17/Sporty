package model.dao;

public interface DAOFactory {

    UserDAO getUserDAO();

    MatchDAO getMatchDAO();

    FieldDAO getFieldDAO();

    NotificationDAO getNotificationDAO();
}
