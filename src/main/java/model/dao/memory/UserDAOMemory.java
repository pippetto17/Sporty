package model.dao.memory;

import model.dao.UserDAO;
import model.domain.Role;
import model.domain.User;

import java.util.HashMap;
import java.util.Map;

public class UserDAOMemory implements UserDAO {
    private static final Map<String, User> usersByUsername = new HashMap<>();
    private static final Map<Integer, User> usersById = new HashMap<>();
    private static int idCounter = 1;

    static {
        addStaticUser(new User(idCounter++, "demo", "demo123", "Demo", "Player", Role.PLAYER));
        addStaticUser(new User(idCounter++, "organizer", "org123", "Test", "Organizer", Role.ORGANIZER));
        addStaticUser(new User(idCounter++, "manager", "man123", "Test", "Manager", Role.FIELD_MANAGER));
    }

    private static void addStaticUser(User user) {
        usersByUsername.put(user.getUsername(), user);
        usersById.put(user.getId(), user);
    }

    private static synchronized int getNextId() {
        return idCounter++;
    }

    public UserDAOMemory() {
        // Default constructor
    }

    @Override
    public User authenticate(String username, String password) {
        User user = findByUsername(username);
        if (user != null && user.getPassword() != null && user.getPassword().equals(password)) {
            return user;
        }
        return null;
    }

    @Override
    public User findById(int id) {
        return usersById.get(id);
    }

    @Override
    public User findByUsername(String username) {
        return usersByUsername.get(username);
    }

    @Override
    public void save(User user) {
        if (user.getId() == 0) {
            user.setId(getNextId());
        }
        addStaticUser(user);
    }
}