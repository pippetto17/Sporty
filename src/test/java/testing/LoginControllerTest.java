package testing;
}
    }
            () -> loginController.register(secondUser, "Second", "User", Role.ORGANIZER.getValue()));
        assertThrows(IllegalArgumentException.class,
        UserBean secondUser = new UserBean("duplicate", "pass2");

        loginController.register(firstUser, "First", "User", Role.PLAYER.getValue());
        UserBean firstUser = new UserBean("duplicate", "pass1");
    void testRegisterWithDuplicateUsername() {
    @DisplayName("Registrazione con username duplicato dovrebbe fallire")
    @Test

    }
        assertDoesNotThrow(() -> loginController.register(userBean, "Alice", "Brown", Role.PLAYER.getValue()));
        UserBean userBean = new UserBean("newuser", "password123");
    void testRegisterWithValidData() {
    @DisplayName("Registrazione con dati validi dovrebbe avere successo")
    @Test

    }
        assertThrows(IllegalArgumentException.class, () -> loginController.login(userBean));
        UserBean userBean = new UserBean("username", "");
    void testLoginWithEmptyPassword() {
    @DisplayName("Login con password vuota dovrebbe fallire")
    @Test

    }
        assertThrows(IllegalArgumentException.class, () -> loginController.login(userBean));
        UserBean userBean = new UserBean("", "password");
    void testLoginWithEmptyUsername() {
    @DisplayName("Login con username vuoto dovrebbe fallire")
    @Test

    }
        assertEquals("John", loggedUser.getName());
        assertEquals("testuser", loggedUser.getUsername());
        assertNotNull(loggedUser);
        // Assert

        User loggedUser = loginController.login(loginBean);
        UserBean loginBean = new UserBean("testuser", "password123");
        // Act

        loginController.register(registerBean, "John", "Doe", Role.PLAYER.getValue());
        UserBean registerBean = new UserBean("testuser", "password123");
        // Arrange
    void testLoginWithValidCredentials() {
    @DisplayName("Login con credenziali valide dovrebbe avere successo")
    @Test

    // ============================================================
    // TEST LOGIN
    // ============================================================

    }
        loginController = new LoginController(DAOFactory.PersistenceType.MEMORY);
    void setUp() {
    @BeforeEach

    private LoginController loginController;

class LoginControllerTest {
@DisplayName("LoginController Test Suite")
 */
 * Testa le funzionalità di login e registrazione utenti.
 * Test suite per LoginController.
/**

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import model.domain.User;
import model.domain.Role;
import model.dao.DAOFactory;
import model.bean.UserBean;
import controller.LoginController;


