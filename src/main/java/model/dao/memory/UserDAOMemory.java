package model.dao.memory;

import model.dao.UserDAO;
import model.domain.Role;
import model.domain.User;

import java.util.HashMap;
import java.util.Map;

public class UserDAOMemory implements UserDAO {
    private final Map<String, User> users;

    public UserDAOMemory() {
        users = new HashMap<>();
        initializeDemoData();
    }

    private void initializeDemoData() {
        users.put("demo", new User("demo", "demo123", "Demo", "Player", Role.PLAYER.getCode()));
        users.put("organizer", new User("organizer", "org123", "Test", "Organizer", Role.ORGANIZER.getCode()));
        users.put("fieldmanager", new User("fieldmanager", "fm123", "Test", "Field Manager", Role.FIELD_MANAGER.getCode()));
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
