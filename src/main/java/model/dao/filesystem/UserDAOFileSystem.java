package model.dao.filesystem;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import model.dao.UserDAO;
import model.domain.User;

import java.io.*;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class UserDAOFileSystem implements UserDAO {
    private static final String JSON_FILE = "data/users.json";
    private final Gson gson;
    private List<User> users;

    public UserDAOFileSystem() {
        gson = new GsonBuilder().setPrettyPrinting().create();
        loadUsers();
    }

    private void loadUsers() {
        File file = new File(JSON_FILE);
        if (!file.exists() || file.length() == 0) {
            users = new ArrayList<>();
            return;
        }

        try (Reader reader = new FileReader(file)) {
            Type userListType = new TypeToken<ArrayList<User>>(){}.getType();
            users = gson.fromJson(reader, userListType);
            if (users == null) {
                users = new ArrayList<>();
            }
        } catch (IOException e) {
            throw new RuntimeException("Error loading users from file system", e);
        }
    }

    private void saveUsers() {
        File file = new File(JSON_FILE);
        file.getParentFile().mkdirs();

        try (Writer writer = new FileWriter(file)) {
            gson.toJson(users, writer);
        } catch (IOException e) {
            throw new RuntimeException("Error saving users to file system", e);
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
        return users.stream()
                .filter(user -> user.getUsername().equals(username))
                .findFirst()
                .orElse(null);
    }

    @Override
    public void save(User user) {
        User existingUser = findByUsername(user.getUsername());
        if (existingUser != null) {
            users.remove(existingUser);
        }
        users.add(user);
        saveUsers();
    }
}

