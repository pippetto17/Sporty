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

/**
 * Test suite per LoginController.
 * Testa le funzionalità base di login e registrazione.
 */
@DisplayName("Login Test Suite")
class LoginControllerTest {

    private LoginController loginController;

    @BeforeEach
    void setUp() {
        loginController = new LoginController(DAOFactory.PersistenceType.MEMORY);
    }

    @Test
    @DisplayName("Login con credenziali valide")
    void testValidLogin() throws ValidationException {
        // Registra un utente
        UserBean registerBean = new UserBean("testuser", "password123");
        loginController.register(registerBean, "John", "Doe", Role.PLAYER.getCode());

        // Tenta il login
        UserBean loginBean = new UserBean("testuser", "password123");
        User loggedUser = loginController.login(loginBean);

        // Verifica
        assertNotNull(loggedUser);
        assertEquals("testuser", loggedUser.getUsername());
    }

    @Test
    @DisplayName("Login con credenziali errate")
    void testInvalidLogin() {
        UserBean loginBean = new UserBean("nonexistent", "wrongpass");
        User result = loginController.login(loginBean);

        assertNull(result);
    }

    @Test
    @DisplayName("Registrazione nuovo utente")
    void testRegisterNewUser() {
        UserBean userBean = new UserBean("newuser", "password");

        assertDoesNotThrow(() ->
            loginController.register(userBean, "Alice", "Smith", Role.PLAYER.getCode())
        );
    }

    @Test
    @DisplayName("Registrazione con username duplicato")
    void testRegisterDuplicateUsername() throws ValidationException {
        UserBean firstUser = new UserBean("duplicate", "pass1");
        loginController.register(firstUser, "First", "User", Role.PLAYER.getCode());

        UserBean secondUser = new UserBean("duplicate", "pass2");
        String name = "Second";
        String surname = "User";
        int roleCode = Role.PLAYER.getCode();

        assertThrows(ValidationException.class,
            () -> loginController.register(secondUser, name, surname, roleCode)
        );
    }
}

