package model.dao.memory;

import model.dao.UserDAO;
import model.domain.DemoDataInitializer;
import model.domain.User;

import java.util.HashMap;
import java.util.Map;

public class UserDAOMemory implements UserDAO {
    private final Map<String, User> users;

    public UserDAOMemory() {
        users = new HashMap<>();
        // Initialize with demo data
        for (User user : DemoDataInitializer.getDemoUsers()) {
            users.put(user.getUsername(), user);
        }
    }

    @Override
    public User authenticate(String username, String password) {
        User user = findByUsername(username);
        if (user != null && user.matchesPassword(password)) {
            return user;
        }
        return null;
    }

    @Override
    public User findByUsername(String username) {
        return users.get(username);
    }

    @Override
    public void save(User user) {
        users.put(user.getUsername(), user);
    }
}
