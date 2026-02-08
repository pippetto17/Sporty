package testing;

import controller.ApplicationController;
import controller.HomeController;
import exception.ValidationException;
import model.dao.DAOFactory;
import model.dao.memory.MemoryDAOFactory;
import model.domain.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test per il caso d'uso "Partecipa a Partita"
 * Testa il flusso di unione di un player a una partita esistente
 */
class JoinMatchTest {

    private DAOFactory daoFactory;
    private User player;
    private User organizer;
    private Match approvedMatch;

    @BeforeEach
    void setUp() throws Exception {
        // Inizializza il DAO Factory in memoria
        daoFactory = new MemoryDAOFactory();

        // Crea un player di test
        player = new User(1, "player1", "password", "Marco", "Bianchi", Role.PLAYER);
        daoFactory.getUserDAO().save(player);

        // Crea un organizer di test
        organizer = new User(2, "organizer1", "password", "Luigi", "Rossi", Role.ORGANIZER);
        daoFactory.getUserDAO().save(organizer);

        // Crea un field manager e un campo
        User fieldManager = new User(3, "manager1", "password", "Paolo", "Verdi", Role.FIELD_MANAGER);
        daoFactory.getUserDAO().save(fieldManager);

        Field field = new Field();
        field.setId(1);
        field.setName("Campo di Calcio");
        field.setCity("Milano");
        field.setAddress("Via Milano 1");
        field.setSport(Sport.FOOTBALL_5);
        field.setPricePerHour(60.0);
        field.setManager(fieldManager);
        daoFactory.getFieldDAO().save(field);

        // Crea una partita approvata
        approvedMatch = new Match();
        approvedMatch.setOrganizer(organizer);
        approvedMatch.setField(field);
        approvedMatch.setDate(LocalDate.now().plusDays(5));
        approvedMatch.setTime(LocalTime.of(17, 0));
        approvedMatch.setMissingPlayers(5);
        approvedMatch.setStatus(MatchStatus.APPROVED);
        daoFactory.getMatchDAO().save(approvedMatch);
    }

    @Test
    void testPlayerCanJoinApprovedMatch() throws Exception {
        // Verifica lo stato iniziale
        assertEquals(5, approvedMatch.getMissingPlayers(), "Dovrebbero mancare 5 giocatori");
        assertFalse(approvedMatch.isUserJoined(player.getId()), "Il player non dovrebbe essere già unito");

        // Il player si unisce alla partita
        approvedMatch.addJoinedPlayer(player.getId());

        // Verifica che il player sia stato aggiunto
        assertTrue(approvedMatch.isUserJoined(player.getId()), "Il player dovrebbe essere unito alla partita");
        assertEquals(4, approvedMatch.getMissingPlayers(), "Dovrebbero mancare 4 giocatori");
        assertEquals(1, approvedMatch.getJoinedPlayersCount(), "Dovrebbe esserci 1 giocatore unito");

        // Salva le modifiche
        daoFactory.getMatchDAO().save(approvedMatch);

        // Verifica persistenza
        Match savedMatch = daoFactory.getMatchDAO().findById(approvedMatch.getId());
        assertTrue(savedMatch.isUserJoined(player.getId()), "Il player dovrebbe essere salvato nella partita");
    }

    @Test
    void testPlayerCannotJoinMatchTwice() throws Exception {
        // Il player si unisce alla partita
        approvedMatch.addJoinedPlayer(player.getId());

        // Tentativo di unirsi di nuovo
        ValidationException exception = assertThrows(ValidationException.class, () -> {
            approvedMatch.addJoinedPlayer(player.getId());
        }, "Non dovrebbe essere possibile unirsi due volte");

        assertEquals("User has already joined this match", exception.getMessage());
    }

    @Test
    void testCannotJoinFullMatch() throws Exception {
        // Crea una partita con 0 posti liberi
        Match fullMatch = new Match();
        fullMatch.setOrganizer(organizer);
        fullMatch.setField(approvedMatch.getField());
        fullMatch.setDate(LocalDate.now().plusDays(3));
        fullMatch.setTime(LocalTime.of(19, 0));
        fullMatch.setMissingPlayers(0);
        fullMatch.setStatus(MatchStatus.APPROVED);
        daoFactory.getMatchDAO().save(fullMatch);

        // Tentativo di unirsi a una partita già piena
        ValidationException exception = assertThrows(ValidationException.class, () -> {
            fullMatch.addJoinedPlayer(player.getId());
        }, "Non dovrebbe essere possibile unirsi a una partita piena");

        assertEquals("Match is full, cannot join", exception.getMessage());
    }

    @Test
    void testMultiplePlayersCanJoinMatch() throws Exception {
        // Crea altri due giocatori
        User player2 = new User(4, "player2", "password", "Anna", "Neri", Role.PLAYER);
        User player3 = new User(5, "player3", "password", "Luca", "Gialli", Role.PLAYER);
        daoFactory.getUserDAO().save(player2);
        daoFactory.getUserDAO().save(player3);

        // Tre giocatori si uniscono alla partita
        approvedMatch.addJoinedPlayer(player.getId());
        approvedMatch.addJoinedPlayer(player2.getId());
        approvedMatch.addJoinedPlayer(player3.getId());

        // Verifica
        assertEquals(3, approvedMatch.getJoinedPlayersCount(), "Dovrebbero esserci 3 giocatori uniti");
        assertEquals(2, approvedMatch.getMissingPlayers(), "Dovrebbero mancare 2 giocatori");
        assertTrue(approvedMatch.isUserJoined(player.getId()), "Player 1 dovrebbe essere unito");
        assertTrue(approvedMatch.isUserJoined(player2.getId()), "Player 2 dovrebbe essere unito");
        assertTrue(approvedMatch.isUserJoined(player3.getId()), "Player 3 dovrebbe essere unito");
    }

    @Test
    void testHomeControllerShowsOnlyApprovedMatches() throws Exception {
        // Crea una partita PENDING che non dovrebbe essere visibile
        Match pendingMatch = new Match();
        pendingMatch.setOrganizer(organizer);
        pendingMatch.setField(approvedMatch.getField());
        pendingMatch.setDate(LocalDate.now().plusDays(10));
        pendingMatch.setTime(LocalTime.of(16, 0));
        pendingMatch.setMissingPlayers(3);
        pendingMatch.setStatus(MatchStatus.PENDING);
        daoFactory.getMatchDAO().save(pendingMatch);

        // Il player visualizza le partite disponibili
        ApplicationController appController = new ApplicationController(daoFactory);
        HomeController homeController = new HomeController(player, appController, daoFactory);

        var availableMatches = homeController.getMatches();

        // Verifica che solo la partita approvata sia visibile
        assertFalse(availableMatches.isEmpty(), "Dovrebbero esserci partite disponibili");
        assertTrue(availableMatches.stream()
                .anyMatch(m -> m.getMatchId() == approvedMatch.getId()),
                "La partita approvata dovrebbe essere visibile");
        assertFalse(availableMatches.stream()
                .anyMatch(m -> m.getMatchId() == pendingMatch.getId()),
                "La partita pendente NON dovrebbe essere visibile");
    }

    @Test
    void testPlayerCanSeeJoinedMatches() throws Exception {
        // Il player si unisce alla partita
        approvedMatch.addJoinedPlayer(player.getId());
        daoFactory.getMatchDAO().save(approvedMatch);

        // Il player visualizza le sue partite
        ApplicationController appController = new ApplicationController(daoFactory);
        HomeController homeController = new HomeController(player, appController, daoFactory);

        var joinedMatches = homeController.getJoinedMatches();

        // Verifica
        assertFalse(joinedMatches.isEmpty(), "Dovrebbe esserci almeno una partita a cui si è unito");
        assertTrue(joinedMatches.stream()
                .anyMatch(m -> m.getMatchId() == approvedMatch.getId()),
                "La partita a cui si è unito dovrebbe essere nella lista");
    }
}
