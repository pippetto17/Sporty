package testing;

import controller.ApplicationController;
import controller.FieldManagerController;
import controller.OrganizeMatchController;
import exception.AuthorizationException;
import model.bean.MatchBean;
import model.dao.DAOFactory;
import model.dao.memory.MemoryDAOFactory;
import model.domain.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test for the "Organize Match" use case
 * Tests the complete flow from organization to acceptance by
 * the field manager
 */
class OrganizeMatchTest {

    private DAOFactory daoFactory;
    private User organizer;
    private User fieldManager;
    private Field testField;

    @BeforeEach
    void setUp() {
        // Initialize the in-memory DAO Factory for tests
        daoFactory = new MemoryDAOFactory();

        // Create a test organizer
        organizer = new User(1, "mario.rossi", "password123", "Mario", "Rossi", Role.ORGANIZER);
        daoFactory.getUserDAO().save(organizer);

        // Create a test field manager
        fieldManager = new User(2, "gestore.campo", "password456", "Luigi", "Verdi", Role.FIELD_MANAGER);
        daoFactory.getUserDAO().save(fieldManager);

        // Create a test field managed by the field manager
        testField = new Field();
        testField.setId(1);
        testField.setName("Campo Sportivo Comunale");
        testField.setCity("Roma");
        testField.setAddress("Via dello Sport 123");
        testField.setSport(Sport.FOOTBALL_5);
        testField.setPricePerHour(50.0);
        testField.setManager(fieldManager);
        daoFactory.getFieldDAO().save(testField);
    }

    @Test
    void testOrganizeMatchCompleteFlow() throws Exception {
        // Step 1: The organizer creates a controller to organize a match
        ApplicationController appController = new ApplicationController(daoFactory);
        OrganizeMatchController organizeController = new OrganizeMatchController(organizer, appController);

        assertNotNull(organizeController, "The controller should be created correctly");
        assertEquals(organizer, organizeController.getOrganizer(),
                "The organizer should be set correctly");

        // Step 2: The organizer sets the match details
        Sport sport = Sport.FOOTBALL_5;
        LocalDate date = LocalDate.now().plusDays(7);
        LocalTime time = LocalTime.of(18, 0);
        String city = "Roma";
        int additionalParticipants = 3;

        // Validate the details
        assertDoesNotThrow(() -> {
            organizeController.validateMatchDetails(sport, date, time, city, additionalParticipants);
        }, "The match details should be valid");

        // Set the details in the bean
        organizeController.setMatchDetails(sport, date, time, city, additionalParticipants);
        MatchBean matchBean = organizeController.getCurrentMatchBean();

        assertNotNull(matchBean, "The match bean should be created");
        assertEquals(sport, matchBean.getSport(), "The sport should be set correctly");
        assertEquals(date, matchBean.getMatchDate(), "The date should be set correctly");
        assertEquals(time, matchBean.getMatchTime(), "The time should be set correctly");
        assertEquals(city, matchBean.getCity(), "The city should be set correctly");

        // Step 3: Select the available field
        matchBean.setFieldId(testField.getId());
        matchBean.setFieldName(testField.getName());

        // Step 4: Save the match (status = PENDING)
        matchBean.setStatus(MatchStatus.PENDING);
        organizeController.saveMatch();

        // Step 5: Verify that the match has been saved with PENDING status
        Match savedMatch = daoFactory.getMatchDAO().findById(matchBean.getMatchId());
        assertNotNull(savedMatch, "The match should be saved in the database");
        assertEquals(MatchStatus.PENDING, savedMatch.getStatus(), "The match should have PENDING status");
        assertEquals(organizer.getId(), savedMatch.getOrganizer().getId(), "The organizer should be correct");
        assertEquals(testField.getId(), savedMatch.getField().getId(), "The field should be correct");

        // Step 6: The field manager views pending requests
        FieldManagerController fieldManagerController = new FieldManagerController(fieldManager, daoFactory);
        var pendingRequests = fieldManagerController.getPendingRequests();

        assertFalse(pendingRequests.isEmpty(), "There should be at least one pending request");
        assertTrue(pendingRequests.stream()
                .anyMatch(m -> m.getMatchId() == savedMatch.getId()),
                "The created match should be in the pending requests");

        // Step 7: The field manager approves the match
        assertDoesNotThrow(() -> {
            fieldManagerController.approveMatch(savedMatch.getId());
        }, "Approval should happen without errors");

        // Step 8: Verify that the match has been approved
        Match approvedMatch = daoFactory.getMatchDAO().findById(savedMatch.getId());
        assertNotNull(approvedMatch, "The match should still exist");
        assertEquals(MatchStatus.APPROVED, approvedMatch.getStatus(), "The match should have APPROVED status");
        assertTrue(approvedMatch.isApproved(), "The isApproved() method should return true");
    }

    @Test
    void testOnlyOrganizerCanOrganizeMatch() {
        // A player cannot organize matches
        User player = new User(3, "player", "password", "Giovanni", "Bianchi", Role.PLAYER);
        daoFactory.getUserDAO().save(player);

        ApplicationController appController = new ApplicationController(daoFactory);

        assertThrows(AuthorizationException.class, () -> {
            new OrganizeMatchController(player, appController);
        }, "A player should not be able to create an OrganizeMatchController");
    }

    @Test
    void testFieldManagerCanOnlyApproveOwnFields() throws Exception {
        // Create a second field manager
        User otherFieldManager = new User(4, "altro.gestore", "password", "Paolo", "Neri", Role.FIELD_MANAGER);
        daoFactory.getUserDAO().save(otherFieldManager);

        // Create a match for the first field manager's field
        Match match = new Match();
        match.setOrganizer(organizer);
        match.setField(testField);
        match.setDate(LocalDate.now().plusDays(7));
        match.setTime(LocalTime.of(18, 0));
        match.setMissingPlayers(5);
        match.setStatus(MatchStatus.PENDING);
        daoFactory.getMatchDAO().save(match);

        // The second field manager should not be able to approve the first's matches
        FieldManagerController otherController = new FieldManagerController(otherFieldManager, daoFactory);

        assertThrows(AuthorizationException.class, () -> {
            otherController.approveMatch(match.getId());
        }, "A field manager should not be able to approve matches for fields they don't manage");
    }
}
