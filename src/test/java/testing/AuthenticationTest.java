package testing;

import controller.LoginController;
import exception.ValidationException;
import model.bean.UserBean;
import model.dao.DAOFactory;
import model.domain.Role;
import model.domain.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Authentication & User Management Test Suite")
class AuthenticationTest {

    private LoginController loginController;

    @BeforeEach
    void setUp() {
        loginController = new LoginController(DAOFactory.PersistenceType.MEMORY);
    }

    @Test
    @DisplayName("Login Player con credenziali valide")
    void testPlayerLoginSuccess() throws ValidationException {
        UserBean registerBean = new UserBean("player1", "pass123");
        loginController.register(registerBean, "Mario", "Rossi", Role.PLAYER.getCode());

        UserBean loginBean = new UserBean("player1", "pass123");
        User loggedUser = loginController.login(loginBean);

        assertNotNull(loggedUser);
        assertEquals("player1", loggedUser.getUsername());
        assertEquals(Role.PLAYER, Role.fromCode(loggedUser.getRole()));
        assertEquals("Mario", loggedUser.getName());
    }

    @Test
    @DisplayName("Login Field Manager con credenziali valide")
    void testFieldManagerLoginSuccess() throws ValidationException {
        UserBean registerBean = new UserBean("manager1", "secure456");
        loginController.register(registerBean, "Luca", "Bianchi", Role.FIELD_MANAGER.getCode());

        UserBean loginBean = new UserBean("manager1", "secure456");
        User loggedUser = loginController.login(loginBean);

        assertNotNull(loggedUser);
        assertEquals(Role.FIELD_MANAGER, Role.fromCode(loggedUser.getRole()));
    }

    @Test
    @DisplayName("Login fallisce con password errata")
    void testLoginWithWrongPassword() throws ValidationException {
        UserBean registerBean = new UserBean("user1", "correctpass");
        loginController.register(registerBean, "Anna", "Verdi", Role.PLAYER.getCode());

        UserBean loginBean = new UserBean("user1", "wrongpass");
        User result = loginController.login(loginBean);

        assertNull(result);
    }

    @Test
    @DisplayName("Login fallisce con username inesistente")
    void testLoginWithNonExistentUser() {
        UserBean loginBean = new UserBean("nonexistent", "anypass");
        User result = loginController.login(loginBean);

        assertNull(result);
    }

    @Test
    @DisplayName("Registrazione con username duplicato lancia ValidationException")
    void testRegisterDuplicateUsername() throws ValidationException {
        UserBean firstUser = new UserBean("duplicate", "pass1");
        loginController.register(firstUser, "First", "User", Role.PLAYER.getCode());

        UserBean secondUser = new UserBean("duplicate", "pass2");

        assertThrows(ValidationException.class,
            () -> loginController.register(secondUser, "Second", "User", Role.FIELD_MANAGER.getCode())
        );
    }

    @Test
    @DisplayName("Registrazione multi-ruolo: Player e Field Manager coesistono")
    void testMultiRoleRegistration() throws ValidationException {
        UserBean player = new UserBean("player2", "pass1");
        UserBean manager = new UserBean("manager2", "pass2");

        loginController.register(player, "Paolo", "Neri", Role.PLAYER.getCode());
        loginController.register(manager, "Giulia", "Gialli", Role.FIELD_MANAGER.getCode());

        User loggedPlayer = loginController.login(new UserBean("player2", "pass1"));
        User loggedManager = loginController.login(new UserBean("manager2", "pass2"));

        assertNotNull(loggedPlayer);
        assertNotNull(loggedManager);
        assertEquals(Role.PLAYER, Role.fromCode(loggedPlayer.getRole()));
        assertEquals(Role.FIELD_MANAGER, Role.fromCode(loggedManager.getRole()));
    }
}

