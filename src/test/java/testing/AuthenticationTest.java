package testing;

import controller.LoginController;
import exception.ValidationException;
import model.bean.UserBean;
import model.dao.DAOFactory;
import model.dao.memory.MemoryDAOFactory;
import model.domain.Role;
import model.domain.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test for the authentication system
 * Tests registration, login and user authentication
 */
class AuthenticationTest {

    private DAOFactory daoFactory;
    private LoginController loginController;

    @BeforeEach
    void setUp() {
        // Initialize the in-memory DAO Factory
        daoFactory = new MemoryDAOFactory();
        loginController = new LoginController(daoFactory);
    }

    @Test
    void testUserRegistrationSuccess() {
        // Create a bean for registration
        UserBean userBean = new UserBean();
        userBean.setUsername("nuovoutente");
        userBean.setPassword("password123");

        // Register the user as player
        assertDoesNotThrow(() -> {
            loginController.register(userBean, "Mario", "Rossi", Role.PLAYER.getCode());
        }, "Registration should happen without errors");

        // Verify that the user has been saved
        User savedUser = daoFactory.getUserDAO().findByUsername("nuovoutente");
        assertNotNull(savedUser, "The user should be saved in the database");
        assertEquals("nuovoutente", savedUser.getUsername(), "The username should match");
        assertEquals("Mario", savedUser.getName(), "The name should match");
        assertEquals("Rossi", savedUser.getSurname(), "The surname should match");
        assertEquals(Role.PLAYER, savedUser.getRole(), "The role should be PLAYER");
    }

    @Test
    void testRegistrationWithDifferentRoles() {
        // Register a player
        UserBean player = new UserBean();
        player.setUsername("test_player_unique");
        player.setPassword("pass123");
        assertDoesNotThrow(() -> {
            loginController.register(player, "Anna", "Bianchi", Role.PLAYER.getCode());
        });

        // Register an organizer
        UserBean organizer = new UserBean();
        organizer.setUsername("test_organizer_unique");
        organizer.setPassword("pass456");
        assertDoesNotThrow(() -> {
            loginController.register(organizer, "Luigi", "Verdi", Role.ORGANIZER.getCode());
        });

        // Register a field manager
        UserBean manager = new UserBean();
        manager.setUsername("test_manager_unique");
        manager.setPassword("pass789");
        assertDoesNotThrow(() -> {
            loginController.register(manager, "Paolo", "Neri", Role.FIELD_MANAGER.getCode());
        });

        // Verify the roles
        assertEquals(Role.PLAYER, daoFactory.getUserDAO().findByUsername("test_player_unique").getRole());
        assertEquals(Role.ORGANIZER, daoFactory.getUserDAO().findByUsername("test_organizer_unique").getRole());
        assertEquals(Role.FIELD_MANAGER, daoFactory.getUserDAO().findByUsername("test_manager_unique").getRole());
    }

    @Test
    void testCannotRegisterDuplicateUsername() {
        // Register the first user
        UserBean firstUser = new UserBean();
        firstUser.setUsername("samename");
        firstUser.setPassword("password1");
        assertDoesNotThrow(() -> {
            loginController.register(firstUser, "Primo", "Utente", Role.PLAYER.getCode());
        });

        // Attempt to register with the same username
        UserBean duplicateUser = new UserBean();
        duplicateUser.setUsername("samename");
        duplicateUser.setPassword("password2");

        ValidationException exception = assertThrows(ValidationException.class, () -> {
            loginController.register(duplicateUser, "Secondo", "Utente", Role.PLAYER.getCode());
        }, "It should not be possible to register duplicate usernames");

        assertTrue(exception.getMessage().contains("exists") || exception.getMessage().contains("esiste"),
                "The error message should indicate username already exists");
    }

    @Test
    void testRegistrationValidatesEmptyFields() {
        UserBean userBean = new UserBean();

        // Empty username
        userBean.setUsername("");
        userBean.setPassword("password");
        assertThrows(ValidationException.class, () -> {
            loginController.register(userBean, "Nome", "Cognome", Role.PLAYER.getCode());
        }, "Empty username should generate error");

        // Empty password
        userBean.setUsername("username");
        userBean.setPassword("");
        assertThrows(ValidationException.class, () -> {
            loginController.register(userBean, "Nome", "Cognome", Role.PLAYER.getCode());
        }, "Empty password should generate error");

        // Empty name
        userBean.setPassword("password");
        assertThrows(ValidationException.class, () -> {
            loginController.register(userBean, "", "Cognome", Role.PLAYER.getCode());
        }, "Empty name should generate error");

        // Empty surname
        assertThrows(ValidationException.class, () -> {
            loginController.register(userBean, "Nome", "", Role.PLAYER.getCode());
        }, "Empty surname should generate error");
    }

    @Test
    void testLoginSuccess() throws Exception {
        // First register a user
        User user = new User(1, "testuser", "testpass", "Test", "User", Role.PLAYER);
        daoFactory.getUserDAO().save(user);

        // Perform login
        UserBean loginBean = new UserBean();
        loginBean.setUsername("testuser");
        loginBean.setPassword("testpass");

        UserBean result = loginController.login(loginBean);

        // Verify the result
        assertNotNull(result, "Login should return a UserBean");
        assertEquals("testuser", result.getUsername(), "The username should match");
        assertEquals("Test", result.getName(), "The name should match");
        assertEquals("User", result.getSurname(), "The surname should match");
        assertEquals(Role.PLAYER.getCode(), result.getRole(), "The role should match");
    }

    @Test
    void testLoginWithWrongPassword() throws Exception {
        // Register a user
        User user = new User(1, "user1", "correctpass", "Nome", "Cognome", Role.PLAYER);
        daoFactory.getUserDAO().save(user);

        // Attempt login with wrong password
        UserBean loginBean = new UserBean();
        loginBean.setUsername("user1");
        loginBean.setPassword("wrongpass");

        UserBean result = loginController.login(loginBean);

        // Login should fail
        assertNull(result, "Login with wrong password should return null");
    }

    @Test
    void testLoginWithNonexistentUser() throws Exception {
        // Attempt login with nonexistent user
        UserBean loginBean = new UserBean();
        loginBean.setUsername("nonexistent");
        loginBean.setPassword("password");

        UserBean result = loginController.login(loginBean);

        // Login should fail
        assertNull(result, "Login with nonexistent user should return null");
    }

    @Test
    void testLoginValidatesEmptyCredentials() {
        UserBean loginBean = new UserBean();

        // Empty username
        loginBean.setUsername("");
        loginBean.setPassword("password");
        assertThrows(ValidationException.class, () -> {
            loginController.login(loginBean);
        }, "Empty username should generate error");

        // Empty password
        loginBean.setUsername("username");
        loginBean.setPassword("");
        assertThrows(ValidationException.class, () -> {
            loginController.login(loginBean);
        }, "Empty password should generate error");
    }

    @Test
    void testCompleteRegistrationAndLoginFlow() throws Exception {
        // Step 1: Registration
        UserBean registerBean = new UserBean();
        registerBean.setUsername("newplayer");
        registerBean.setPassword("securepass");

        assertDoesNotThrow(() -> {
            loginController.register(registerBean, "Giovanni", "Bianchi", Role.ORGANIZER.getCode());
        }, "Registration should complete successfully");

        // Step 2: Login with the same credentials
        UserBean loginBean = new UserBean();
        loginBean.setUsername("newplayer");
        loginBean.setPassword("securepass");

        UserBean loggedInUser = loginController.login(loginBean);

        // Verify
        assertNotNull(loggedInUser, "Login after registration should work");
        assertEquals("newplayer", loggedInUser.getUsername(), "Correct username");
        assertEquals("Giovanni", loggedInUser.getName(), "Correct name");
        assertEquals("Bianchi", loggedInUser.getSurname(), "Correct surname");
        assertEquals(Role.ORGANIZER.getCode(), loggedInUser.getRole(), "The role should be ORGANIZER");
        assertTrue(new User(0, "", "", "", "", Role.fromCode(loggedInUser.getRole())).isOrganizer(),
                "User with ORGANIZER role should have isOrganizer() = true");
    }
}
