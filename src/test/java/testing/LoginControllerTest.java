package testing;

import controller.LoginController;
import model.bean.UserBean;
import model.dao.DAOFactory;
import model.domain.User;
import model.domain.Role;
import model.domain.Sport;
import model.domain.Match;
import model.domain.MatchStatus;
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

    // ============================================================
    // TEST DOMAIN CLASSES
    // ============================================================

    @Test
    @DisplayName("Creazione User con tutti i parametri dovrebbe funzionare")
    void testUserCreation() {
        User user = new User("username", "password", "John", "Doe", Role.PLAYER.getCode());

        assertEquals("username", user.getUsername());
        assertEquals("password", user.getPassword());
        assertEquals("John", user.getName());
        assertEquals("Doe", user.getSurname());
        assertEquals(Role.PLAYER.getCode(), user.getRole());
    }

    @Test
    @DisplayName("Role.fromCode dovrebbe restituire il ruolo corretto")
    void testRoleFromCode() {
        Role player = Role.fromCode(1);
        Role organizer = Role.fromCode(2);

        assertEquals(Role.PLAYER, player);
        assertEquals(Role.ORGANIZER, organizer);
    }

    @Test
    @DisplayName("Role.fromCode con codice invalido dovrebbe lanciare eccezione")
    void testRoleFromCodeInvalid() {
        assertThrows(IllegalArgumentException.class, () -> Role.fromCode(999));
    }

    @Test
    @DisplayName("Role.getDisplayName dovrebbe restituire nome formattato")
    void testRoleGetDisplayName() {
        String playerName = Role.PLAYER.getDisplayName();
        String organizerName = Role.ORGANIZER.getDisplayName();

        assertEquals("Player", playerName);
        assertEquals("Organizer", organizerName);
    }

    @Test
    @DisplayName("Sport.getRequiredPlayers dovrebbe restituire numero corretto")
    void testSportGetRequiredPlayers() {
        assertEquals(10, Sport.FOOTBALL_5.getRequiredPlayers());
        assertEquals(22, Sport.FOOTBALL_11.getRequiredPlayers());
        assertEquals(10, Sport.BASKETBALL.getRequiredPlayers());
        assertEquals(2, Sport.TENNIS_SINGLE.getRequiredPlayers());
    }

    @Test
    @DisplayName("Sport.getAdditionalParticipantsNeeded dovrebbe calcolare correttamente")
    void testSportGetAdditionalParticipantsNeeded() {
        assertEquals(9, Sport.FOOTBALL_5.getAdditionalParticipantsNeeded());
        assertEquals(21, Sport.FOOTBALL_11.getAdditionalParticipantsNeeded());
        assertEquals(1, Sport.TENNIS_SINGLE.getAdditionalParticipantsNeeded());
    }

    @Test
    @DisplayName("Sport.isValidAdditionalParticipants dovrebbe validare correttamente")
    void testSportIsValidAdditionalParticipants() {
        assertTrue(Sport.FOOTBALL_5.isValidAdditionalParticipants(5));
        assertTrue(Sport.FOOTBALL_5.isValidAdditionalParticipants(9));
        assertFalse(Sport.FOOTBALL_5.isValidAdditionalParticipants(0));
        assertFalse(Sport.FOOTBALL_5.isValidAdditionalParticipants(10));
    }

    @Test
    @DisplayName("Sport.getDisplayName dovrebbe restituire nome leggibile")
    void testSportGetDisplayName() {
        assertEquals("Calcio a 5", Sport.FOOTBALL_5.getDisplayName());
        assertEquals("Basket", Sport.BASKETBALL.getDisplayName());
        assertEquals("Tennis Singolo", Sport.TENNIS_SINGLE.getDisplayName());
    }

    @Test
    @DisplayName("MatchStatus enum dovrebbe avere tutti i valori")
    void testMatchStatusValues() {
        MatchStatus[] statuses = MatchStatus.values();

        assertNotNull(statuses);
        assertTrue(statuses.length >= 3);
    }

    @Test
    @DisplayName("User con ruolo PLAYER dovrebbe avere codice corretto")
    void testUserWithPlayerRole() {
        User player = new User("player1", "pass", "Test", "Player", Role.PLAYER.getCode());
        assertEquals(Role.PLAYER.getCode(), player.getRole());
    }

    @Test
    @DisplayName("User con ruolo ORGANIZER dovrebbe avere codice corretto")
    void testUserWithOrganizerRole() {
        User organizer = new User("org1", "pass", "Test", "Organizer", Role.ORGANIZER.getCode());
        assertEquals(Role.ORGANIZER.getCode(), organizer.getRole());
    }
}

