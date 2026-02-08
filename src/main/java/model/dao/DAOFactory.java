package model.dao;

/**
 * Abstract factory interface for creating Data Access Objects (DAOs).
 * Implementations provide different persistence strategies (DBMS, file system,
 * in-memory).
 */
public interface DAOFactory {
    /**
     * Creates a UserDAO instance.
     *
     * @return a UserDAO implementation
     */
    UserDAO getUserDAO();

    /**
     * Creates a MatchDAO instance.
     *
     * @return a MatchDAO implementation
     */
    MatchDAO getMatchDAO();

    /**
     * Creates a FieldDAO instance.
     *
     * @return a FieldDAO implementation
     */
    FieldDAO getFieldDAO();

    /**
     * Creates a NotificationDAO instance.
     *
     * @return a NotificationDAO implementation
     */
    NotificationDAO getNotificationDAO();
}