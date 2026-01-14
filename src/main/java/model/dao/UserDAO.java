package model.dao;

import model.domain.User;

public interface UserDAO {
    /**
     * Authenticate user with username and password
     * @return User object if authentication successful, null otherwise
     */
    User authenticate(String username, String password);

    /**
     * Find user by username
     * @return User object if found, null otherwise
     */
    User findByUsername(String username);

    /**
     * Save or update a user
     */
    void save(User user);
}

