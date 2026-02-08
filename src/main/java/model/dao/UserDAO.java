package model.dao;

import model.domain.User;

/**
 * Data Access Object interface for User entities.
 * Provides methods for user authentication and persistence operations.
 */
public interface UserDAO {
    /**
     * Authenticates a user with the provided credentials.
     *
     * @param username the username
     * @param password the password
     * @return the authenticated User if credentials are valid, null otherwise
     */
    User authenticate(String username, String password);

    /**
     * Finds a user by their unique ID.
     *
     * @param id the user ID
     * @return the User if found, null otherwise
     */
    User findById(int id);

    /**
     * Finds a user by their username.
     *
     * @param username the username
     * @return the User if found, null otherwise
     */
    User findByUsername(String username);

    /**
     * Saves a user entity (creates new or updates existing).
     *
     * @param user the user to save
     */
    void save(User user);
}