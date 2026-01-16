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

    @Test
    @DisplayName("Login con username null dovrebbe fallire")
    void testLoginWithNullUsername() {
        UserBean userBean = new UserBean(null, "password");
        assertThrows(IllegalArgumentException.class, () -> loginController.login(userBean));
    }

    @Test
    @DisplayName("Login con password null dovrebbe fallire")
    void testLoginWithNullPassword() {
        UserBean userBean = new UserBean("username", null);
        assertThrows(IllegalArgumentException.class, () -> loginController.login(userBean));
    }

    @Test
    @DisplayName("Registrazione con nome vuoto dovrebbe fallire")
    void testRegisterWithEmptyName() {
        UserBean userBean = new UserBean("newuser", "password");
        assertThrows(IllegalArgumentException.class,
            () -> loginController.register(userBean, "", "Surname", Role.PLAYER.getCode()));
    }

    @Test
    @DisplayName("Registrazione con cognome vuoto dovrebbe fallire")
    void testRegisterWithEmptySurname() {
        UserBean userBean = new UserBean("newuser", "password");
        assertThrows(IllegalArgumentException.class,
            () -> loginController.register(userBean, "Name", "", Role.PLAYER.getCode()));
    }

    @Test
    @DisplayName("Registrazione con nome null dovrebbe fallire")
    void testRegisterWithNullName() {
        UserBean userBean = new UserBean("newuser", "password");
        assertThrows(IllegalArgumentException.class,
            () -> loginController.register(userBean, null, "Surname", Role.PLAYER.getCode()));
    }

    @Test
    @DisplayName("Registrazione con cognome null dovrebbe fallire")
    void testRegisterWithNullSurname() {
        UserBean userBean = new UserBean("newuser", "password");
        assertThrows(IllegalArgumentException.class,
            () -> loginController.register(userBean, "Name", null, Role.PLAYER.getCode()));
    }

    @Test
    @DisplayName("Login con credenziali errate dovrebbe fallire")
    void testLoginWithWrongPassword() {
        // Arrange: registra un utente
        UserBean registerBean = new UserBean("testuser2", "correctpass");
        loginController.register(registerBean, "Test", "User", Role.PLAYER.getCode());

        // Act & Assert: prova login con password sbagliata
        UserBean loginBean = new UserBean("testuser2", "wrongpass");
        User result = loginController.login(loginBean);

        // Dovrebbe restituire null o lanciare eccezione
        assertNull(result);
    }

    @Test
    @DisplayName("Login con username inesistente dovrebbe fallire")
    void testLoginWithNonexistentUsername() {
        UserBean loginBean = new UserBean("nonexistent", "password");
        User result = loginController.login(loginBean);
        assertNull(result);
    }

    @Test
    @DisplayName("Registrazione di organizer dovrebbe funzionare")
    void testRegisterOrganizer() {
        UserBean userBean = new UserBean("organizer1", "password");
        assertDoesNotThrow(() -> loginController.register(userBean, "John", "Organizer", Role.ORGANIZER.getCode()));
    }
}

