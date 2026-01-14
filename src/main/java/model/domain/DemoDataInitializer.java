package model.domain;

import java.util.ArrayList;
import java.util.List;

/**
 * Centralized demo data initialization for Memory/Demo mode
 */
public class DemoDataInitializer {

    private DemoDataInitializer() {
        // Prevent instantiation
    }

    /**
     * Get list of demo users for testing
     * @return List of pre-configured demo users
     */
    public static List<User> getDemoUsers() {
        List<User> users = new ArrayList<>();

        // Player user
        User playerUser = new User("demo", "demo123", "Demo", "Player", Role.PLAYER.getCode());
        users.add(playerUser);

        // Organizer user
        User organizerUser = new User("organizer", "org123", "Test", "Organizer", Role.ORGANIZER.getCode());
        users.add(organizerUser);

        return users;
    }
}

