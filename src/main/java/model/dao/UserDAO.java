package model.dao;

import model.domain.User;

public interface UserDAO {
    User authenticate(String username, String password);

    User findByUsername(String username);

    void save(User user);
}
