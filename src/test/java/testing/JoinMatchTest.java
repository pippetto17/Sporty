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
 * Test for the "Join Match" use case
 * Tests the flow of a player joining an existing match
 */
class JoinMatchTest {

    private DAOFactory daoFactory;
    private User player;
    private User organizer;
    private Match approvedMatch;

    @BeforeEach
    void setUp() throws Exception {
        // Initialize the in-memory DAO Factory
        daoFactory = new MemoryDAOFactory();

        // Create a test player
        player = new User(1, "player1", "password", "Marco", "Bianchi", Role.PLAYER);
        daoFactory.getUserDAO().save(player);

        // Create a test organizer
        organizer = new User(2, "organizer1", "password", "Luigi", "Rossi", Role.ORGANIZER);
        daoFactory.getUserDAO().save(organizer);

        // Create a field manager and a field
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

        // Create an approved match
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
        // Verify initial state
        assertEquals(5, approvedMatch.getMissingPlayers(), "5 players should be missing");
        assertFalse(approvedMatch.isUserJoined(player.getId()), "The player should not already be joined");

        // The player joins the match
        approvedMatch.addJoinedPlayer(player.getId());

        // Verify that the player has been added
        assertTrue(approvedMatch.isUserJoined(player.getId()), "The player should be joined to the match");
        assertEquals(4, approvedMatch.getMissingPlayers(), "4 players should be missing");
        assertEquals(1, approvedMatch.getJoinedPlayersCount(), "There should be 1 joined player");

        // Save the changes
        daoFactory.getMatchDAO().save(approvedMatch);

        // Verify persistence
        Match savedMatch = daoFactory.getMatchDAO().findById(approvedMatch.getId());
        assertTrue(savedMatch.isUserJoined(player.getId()), "The player should be saved in the match");
    }

    @Test
    void testPlayerCannotJoinMatchTwice() throws Exception {
        // The player joins the match
        approvedMatch.addJoinedPlayer(player.getId());

        // Attempt to join again
        ValidationException exception = assertThrows(ValidationException.class, () -> {
            approvedMatch.addJoinedPlayer(player.getId());
        }, "It should not be possible to join twice");

        assertEquals("User has already joined this match", exception.getMessage());
    }

    @Test
    void testCannotJoinFullMatch() throws Exception {
        // Create a match with 0 free spots
        Match fullMatch = new Match();
        fullMatch.setOrganizer(organizer);
        fullMatch.setField(approvedMatch.getField());
        fullMatch.setDate(LocalDate.now().plusDays(3));
        fullMatch.setTime(LocalTime.of(19, 0));
        fullMatch.setMissingPlayers(0);
        fullMatch.setStatus(MatchStatus.APPROVED);
        daoFactory.getMatchDAO().save(fullMatch);

        // Attempt to join an already full match
        ValidationException exception = assertThrows(ValidationException.class, () -> {
            fullMatch.addJoinedPlayer(player.getId());
        }, "It should not be possible to join a full match");

        assertEquals("Match is full, cannot join", exception.getMessage());
    }

    @Test
    void testMultiplePlayersCanJoinMatch() throws Exception {
        // Create two more players
        User player2 = new User(4, "player2", "password", "Anna", "Neri", Role.PLAYER);
        User player3 = new User(5, "player3", "password", "Luca", "Gialli", Role.PLAYER);
        daoFactory.getUserDAO().save(player2);
        daoFactory.getUserDAO().save(player3);

        // Three players join the match
        approvedMatch.addJoinedPlayer(player.getId());
        approvedMatch.addJoinedPlayer(player2.getId());
        approvedMatch.addJoinedPlayer(player3.getId());

        // Verify
        assertEquals(3, approvedMatch.getJoinedPlayersCount(), "There should be 3 joined players");
        assertEquals(2, approvedMatch.getMissingPlayers(), "2 players should be missing");
        assertTrue(approvedMatch.isUserJoined(player.getId()), "Player 1 should be joined");
        assertTrue(approvedMatch.isUserJoined(player2.getId()), "Player 2 should be joined");
        assertTrue(approvedMatch.isUserJoined(player3.getId()), "Player 3 should be joined");
    }

    @Test
    void testHomeControllerShowsOnlyApprovedMatches() throws Exception {
        // Create a PENDING match that should not be visible
        Match pendingMatch = new Match();
        pendingMatch.setOrganizer(organizer);
        pendingMatch.setField(approvedMatch.getField());
        pendingMatch.setDate(LocalDate.now().plusDays(10));
        pendingMatch.setTime(LocalTime.of(16, 0));
        pendingMatch.setMissingPlayers(3);
        pendingMatch.setStatus(MatchStatus.PENDING);
        daoFactory.getMatchDAO().save(pendingMatch);

        // The player views available matches
        ApplicationController appController = new ApplicationController(daoFactory);
        HomeController homeController = new HomeController(player, appController, daoFactory);

        var availableMatches = homeController.getMatches();

        // Verify that only the approved match is visible
        assertFalse(availableMatches.isEmpty(), "There should be available matches");
        assertTrue(availableMatches.stream()
                .anyMatch(m -> m.getMatchId() == approvedMatch.getId()),
                "The approved match should be visible");
        assertFalse(availableMatches.stream()
                .anyMatch(m -> m.getMatchId() == pendingMatch.getId()),
                "The pending match should NOT be visible");
    }

    @Test
    void testPlayerCanSeeJoinedMatches() throws Exception {
        // The player joins the match
        approvedMatch.addJoinedPlayer(player.getId());
        daoFactory.getMatchDAO().save(approvedMatch);

        // The player views their matches
        ApplicationController appController = new ApplicationController(daoFactory);
        HomeController homeController = new HomeController(player, appController, daoFactory);

        var joinedMatches = homeController.getJoinedMatches();

        // Verify
        assertFalse(joinedMatches.isEmpty(), "There should be at least one joined match");
        assertTrue(joinedMatches.stream()
                .anyMatch(m -> m.getMatchId() == approvedMatch.getId()),
                "The joined match should be in the list");
    }
}
