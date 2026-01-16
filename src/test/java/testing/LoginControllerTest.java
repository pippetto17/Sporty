package testing;

import controller.LoginController;
import model.bean.UserBean;
import model.dao.DAOFactory;
import model.domain.Role;
import model.domain.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Test suite per LoginController.
 * Testa le funzionalità di login e registrazione utenti.
 */
@DisplayName("LoginController Test Suite")
class LoginControllerTest {

    private LoginController loginController;

    @BeforeEach
    void setUp() {
        loginController = new LoginController(DAOFactory.PersistenceType.MEMORY);
    }

    // ============================================================
    // TEST LOGIN
    // ============================================================

    @Test
    @DisplayName("Login con credenziali valide dovrebbe avere successo")
    void testLoginWithValidCredentials() {
        // Arrange
        UserBean registerBean = new UserBean("testuser", "password123");
        loginController.register(registerBean, "John", "Doe", Role.PLAYER.getCode());

        // Act
        UserBean loginBean = new UserBean("testuser", "password123");
        User loggedUser = loginController.login(loginBean);

        // Assert
        assertNotNull(loggedUser);
        assertEquals("testuser", loggedUser.getUsername());
        assertEquals("John", loggedUser.getName());
    }

    @Test
    @DisplayName("Login con username vuoto dovrebbe fallire")
    void testLoginWithEmptyUsername() {
        UserBean userBean = new UserBean("", "password");
        assertThrows(IllegalArgumentException.class, () -> loginController.login(userBean));
    }

    @Test
    @DisplayName("Login con password vuota dovrebbe fallire")
    void testLoginWithEmptyPassword() {
        UserBean userBean = new UserBean("username", "");
        assertThrows(IllegalArgumentException.class, () -> loginController.login(userBean));
    }

    @Test
    @DisplayName("Registrazione con dati validi dovrebbe avere successo")
    void testRegisterWithValidData() {
        UserBean userBean = new UserBean("newuser", "password123");
        assertDoesNotThrow(() -> loginController.register(userBean, "Alice", "Brown", Role.PLAYER.getCode()));
    }

    @Test
    @DisplayName("Registrazione con username duplicato dovrebbe fallire")
    void testRegisterWithDuplicateUsername() {
        UserBean firstUser = new UserBean("duplicate", "pass1");
        loginController.register(firstUser, "First", "User", Role.PLAYER.getCode());

        UserBean secondUser = new UserBean("duplicate", "pass2");
        assertThrows(IllegalArgumentException.class,
            () -> loginController.register(secondUser, "Second", "User", Role.ORGANIZER.getCode()));
    }
}

