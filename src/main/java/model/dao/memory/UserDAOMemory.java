package model.dao.memory;

import model.dao.UserDAO;
import model.domain.Role;
import model.domain.User;

import java.util.HashMap;
import java.util.Map;
public class UserDAOMemory implements UserDAO {
    private final Map<String, User> usersByUsername;
    private final Map<Integer, User> usersById;
    private int idCounter = 1;
    public UserDAOMemory() {
        usersByUsername = new HashMap<>();
        usersById = new HashMap<>();
        initializeDemoData();
    }
    private void initializeDemoData() {
        save(new User(idCounter++, "demo", "demo123", "Demo", "Player", Role.PLAYER));
        save(new User(idCounter++, "organizer", "org123", "Test", "Organizer", Role.ORGANIZER));
        save(new User(idCounter++, "manager", "man123", "Test", "Manager", Role.FIELD_MANAGER));
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
            user.setId(idCounter++);
        }
        usersByUsername.put(user.getUsername(), user);
        usersById.put(user.getId(), user);
    }
}