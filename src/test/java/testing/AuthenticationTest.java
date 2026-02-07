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
 * Test per il sistema di autenticazione
 * Testa registrazione, login e autenticazione degli utenti
 */
class AuthenticationTest {

    private DAOFactory daoFactory;
    private LoginController loginController;

    @BeforeEach
    void setUp() {
        // Inizializza il DAO Factory in memoria
        daoFactory = new MemoryDAOFactory();
        loginController = new LoginController(daoFactory);
    }

    @Test
    void testUserRegistrationSuccess() {
        // Crea un bean per la registrazione
        UserBean userBean = new UserBean();
        userBean.setUsername("nuovoutente");
        userBean.setPassword("password123");

        // Registra l'utente come player
        assertDoesNotThrow(() -> {
            loginController.register(userBean, "Mario", "Rossi", Role.PLAYER.getCode());
        }, "La registrazione dovrebbe avvenire senza errori");

        // Verifica che l'utente sia stato salvato
        User savedUser = daoFactory.getUserDAO().findByUsername("nuovoutente");
        assertNotNull(savedUser, "L'utente dovrebbe essere salvato nel database");
        assertEquals("nuovoutente", savedUser.getUsername(), "Lo username dovrebbe corrispondere");
        assertEquals("Mario", savedUser.getName(), "Il nome dovrebbe corrispondere");
        assertEquals("Rossi", savedUser.getSurname(), "Il cognome dovrebbe corrispondere");
        assertEquals(Role.PLAYER, savedUser.getRole(), "Il ruolo dovrebbe essere PLAYER");
    }

    @Test
    void testRegistrationWithDifferentRoles() {
        // Registra un player
        UserBean player = new UserBean();
        player.setUsername("test_player_unique");
        player.setPassword("pass123");
        assertDoesNotThrow(() -> {
            loginController.register(player, "Anna", "Bianchi", Role.PLAYER.getCode());
        });

        // Registra un organizer
        UserBean organizer = new UserBean();
        organizer.setUsername("test_organizer_unique");
        organizer.setPassword("pass456");
        assertDoesNotThrow(() -> {
            loginController.register(organizer, "Luigi", "Verdi", Role.ORGANIZER.getCode());
        });

        // Registra un field manager
        UserBean manager = new UserBean();
        manager.setUsername("test_manager_unique");
        manager.setPassword("pass789");
        assertDoesNotThrow(() -> {
            loginController.register(manager, "Paolo", "Neri", Role.FIELD_MANAGER.getCode());
        });

        // Verifica i ruoli
        assertEquals(Role.PLAYER, daoFactory.getUserDAO().findByUsername("test_player_unique").getRole());
        assertEquals(Role.ORGANIZER, daoFactory.getUserDAO().findByUsername("test_organizer_unique").getRole());
        assertEquals(Role.FIELD_MANAGER, daoFactory.getUserDAO().findByUsername("test_manager_unique").getRole());
    }

    @Test
    void testCannotRegisterDuplicateUsername() {
        // Registra il primo utente
        UserBean firstUser = new UserBean();
        firstUser.setUsername("samename");
        firstUser.setPassword("password1");
        assertDoesNotThrow(() -> {
            loginController.register(firstUser, "Primo", "Utente", Role.PLAYER.getCode());
        });

        // Tentativo di registrare con lo stesso username
        UserBean duplicateUser = new UserBean();
        duplicateUser.setUsername("samename");
        duplicateUser.setPassword("password2");

        ValidationException exception = assertThrows(ValidationException.class, () -> {
            loginController.register(duplicateUser, "Secondo", "Utente", Role.PLAYER.getCode());
        }, "Non dovrebbe essere possibile registrare username duplicati");

        assertTrue(exception.getMessage().contains("exists") || exception.getMessage().contains("esiste"),
                "Il messaggio di errore dovrebbe indicare username già esistente");
    }

    @Test
    void testRegistrationValidatesEmptyFields() {
        UserBean userBean = new UserBean();

        // Username vuoto
        userBean.setUsername("");
        userBean.setPassword("password");
        assertThrows(ValidationException.class, () -> {
            loginController.register(userBean, "Nome", "Cognome", Role.PLAYER.getCode());
        }, "Username vuoto dovrebbe generare errore");

        // Password vuota
        userBean.setUsername("username");
        userBean.setPassword("");
        assertThrows(ValidationException.class, () -> {
            loginController.register(userBean, "Nome", "Cognome", Role.PLAYER.getCode());
        }, "Password vuota dovrebbe generare errore");

        // Nome vuoto
        userBean.setPassword("password");
        assertThrows(ValidationException.class, () -> {
            loginController.register(userBean, "", "Cognome", Role.PLAYER.getCode());
        }, "Nome vuoto dovrebbe generare errore");

        // Cognome vuoto
        assertThrows(ValidationException.class, () -> {
            loginController.register(userBean, "Nome", "", Role.PLAYER.getCode());
        }, "Cognome vuoto dovrebbe generare errore");
    }

    @Test
    void testLoginSuccess() throws Exception {
        // Prima registra un utente
        User user = new User(1, "testuser", "testpass", "Test", "User", Role.PLAYER);
        daoFactory.getUserDAO().save(user);

        // Esegue il login
        UserBean loginBean = new UserBean();
        loginBean.setUsername("testuser");
        loginBean.setPassword("testpass");

        UserBean result = loginController.login(loginBean);

        // Verifica il risultato
        assertNotNull(result, "Il login dovrebbe restituire un UserBean");
        assertEquals("testuser", result.getUsername(), "Lo username dovrebbe corrispondere");
        assertEquals("Test", result.getName(), "Il nome dovrebbe corrispondere");
        assertEquals("User", result.getSurname(), "Il cognome dovrebbe corrispondere");
        assertEquals(Role.PLAYER.getCode(), result.getRole(), "Il ruolo dovrebbe corrispondere");
    }

    @Test
    void testLoginWithWrongPassword() throws Exception {
        // Registra un utente
        User user = new User(1, "user1", "correctpass", "Nome", "Cognome", Role.PLAYER);
        daoFactory.getUserDAO().save(user);

        // Tentativo di login con password sbagliata
        UserBean loginBean = new UserBean();
        loginBean.setUsername("user1");
        loginBean.setPassword("wrongpass");

        UserBean result = loginController.login(loginBean);

        // Il login dovrebbe fallire
        assertNull(result, "Il login con password errata dovrebbe restituire null");
    }

    @Test
    void testLoginWithNonexistentUser() throws Exception {
        // Tentativo di login con utente inesistente
        UserBean loginBean = new UserBean();
        loginBean.setUsername("nonexistent");
        loginBean.setPassword("password");

        UserBean result = loginController.login(loginBean);

        // Il login dovrebbe fallire
        assertNull(result, "Il login con utente inesistente dovrebbe restituire null");
    }

    @Test
    void testLoginValidatesEmptyCredentials() {
        UserBean loginBean = new UserBean();

        // Username vuoto
        loginBean.setUsername("");
        loginBean.setPassword("password");
        assertThrows(ValidationException.class, () -> {
            loginController.login(loginBean);
        }, "Username vuoto dovrebbe generare errore");

        // Password vuota
        loginBean.setUsername("username");
        loginBean.setPassword("");
        assertThrows(ValidationException.class, () -> {
            loginController.login(loginBean);
        }, "Password vuota dovrebbe generare errore");
    }

    @Test
    void testCompleteRegistrationAndLoginFlow() throws Exception {
        // Step 1: Registrazione
        UserBean registerBean = new UserBean();
        registerBean.setUsername("newplayer");
        registerBean.setPassword("securepass");

        assertDoesNotThrow(() -> {
            loginController.register(registerBean, "Giovanni", "Bianchi", Role.ORGANIZER.getCode());
        }, "La registrazione dovrebbe completarsi con successo");

        // Step 2: Login con le stesse credenziali
        UserBean loginBean = new UserBean();
        loginBean.setUsername("newplayer");
        loginBean.setPassword("securepass");

        UserBean loggedInUser = loginController.login(loginBean);

        // Verifica
        assertNotNull(loggedInUser, "Il login dopo registrazione dovrebbe funzionare");
        assertEquals("newplayer", loggedInUser.getUsername(), "Username corretto");
        assertEquals("Giovanni", loggedInUser.getName(), "Nome corretto");
        assertEquals("Bianchi", loggedInUser.getSurname(), "Cognome corretto");
        assertEquals(Role.ORGANIZER.getCode(), loggedInUser.getRole(), "Il ruolo dovrebbe essere ORGANIZER");
        assertTrue(new User(0, "", "", "", "", Role.fromCode(loggedInUser.getRole())).isOrganizer(),
                "L'utente con ruolo ORGANIZER dovrebbe avere isOrganizer() = true");
    }
}
