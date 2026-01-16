package testing;

import model.bean.MatchBean;
import model.dao.DAOFactory;
import model.domain.Sport;
import model.service.MatchService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import java.time.LocalDate;
import java.time.LocalTime;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Test suite per la creazione di Match.
 */
@DisplayName("Match Creation Test Suite")
class MatchCreationTest {

    private MatchService matchService;

    @BeforeEach
    void setUp() throws Exception {
        matchService = new MatchService(DAOFactory.PersistenceType.MEMORY);
    }

    @Test
    @DisplayName("Creazione match di calcio con dati validi dovrebbe avere successo")
    void testCreateFootballMatch() {
        // Arrange
        MatchBean matchBean = new MatchBean();
        matchBean.setOrganizerUsername("organizer1");
        matchBean.setSport(Sport.FOOTBALL_11);
        matchBean.setMatchDate(LocalDate.of(2026, 6, 15));
        matchBean.setMatchTime(LocalTime.of(18, 0));
        matchBean.setCity("Milano");
        matchBean.setRequiredParticipants(10);
        matchBean.setFieldId("1");

        // Act
        assertDoesNotThrow(() -> matchService.saveMatch(matchBean));

        // Assert
        assertNotNull(matchBean.getMatchId());
        assertTrue(matchBean.getMatchId() > 0);
    }

    @Test
    @DisplayName("Creazione match di basket dovrebbe funzionare")
    void testCreateBasketballMatch() {
        MatchBean matchBean = new MatchBean();
        matchBean.setOrganizerUsername("organizer2");
        matchBean.setSport(Sport.BASKETBALL);
        matchBean.setMatchDate(LocalDate.of(2026, 7, 20));
        matchBean.setMatchTime(LocalTime.of(20, 30));
        matchBean.setCity("Roma");
        matchBean.setRequiredParticipants(4);
        matchBean.setFieldId("2");

        matchService.saveMatch(matchBean);

        assertNotNull(matchBean.getMatchId());
    }


    @Test
    @DisplayName("Recupero match per ID dovrebbe funzionare")
    void testGetMatchById() {
        // Arrange
        MatchBean matchBean = new MatchBean();
        matchBean.setOrganizerUsername("testOrg");
        matchBean.setSport(Sport.FOOTBALL_11);
        matchBean.setMatchDate(LocalDate.of(2026, 6, 15));
        matchBean.setMatchTime(LocalTime.of(18, 0));
        matchBean.setCity("Milano");
        matchBean.setRequiredParticipants(10);
        matchBean.setFieldId("1");

        matchService.saveMatch(matchBean);
        int matchId = matchBean.getMatchId();

        // Act
        MatchBean retrieved = matchService.getMatchById(matchId);

        // Assert
        assertNotNull(retrieved);
        assertEquals(matchId, retrieved.getMatchId());
        assertEquals("testOrg", retrieved.getOrganizerUsername());
    }

    @Test
    @DisplayName("Validazione dettagli match con dati validi dovrebbe passare")
    void testValidateMatchDetailsWithValidData() {
        LocalDate futureDate = LocalDate.now().plusDays(5);
        LocalTime time = LocalTime.of(18, 0);

        boolean isValid = matchService.validateMatchDetails(
            Sport.FOOTBALL_11, futureDate, time, "Milano", 10
        );

        assertTrue(isValid);
    }

    @Test
    @DisplayName("Validazione con sport null dovrebbe fallire")
    void testValidateMatchDetailsWithNullSport() {
        LocalDate futureDate = LocalDate.now().plusDays(5);
        LocalTime time = LocalTime.of(18, 0);

        boolean isValid = matchService.validateMatchDetails(
            null, futureDate, time, "Milano", 10
        );

        assertFalse(isValid);
    }

    @Test
    @DisplayName("Validazione con data passata dovrebbe fallire")
    void testValidateMatchDetailsWithPastDate() {
        LocalDate pastDate = LocalDate.of(2020, 1, 1);
        LocalTime time = LocalTime.of(18, 0);

        boolean isValid = matchService.validateMatchDetails(
            Sport.FOOTBALL_11, pastDate, time, "Milano", 10
        );

        assertFalse(isValid);
    }

    @Test
    @DisplayName("Validazione con città vuota dovrebbe fallire")
    void testValidateMatchDetailsWithEmptyCity() {
        LocalDate futureDate = LocalDate.now().plusDays(5);
        LocalTime time = LocalTime.of(18, 0);

        boolean isValid = matchService.validateMatchDetails(
            Sport.FOOTBALL_11, futureDate, time, "", 10
        );

        assertFalse(isValid);
    }

    @Test
    @DisplayName("Validazione con partecipanti invalidi dovrebbe fallire")
    void testValidateMatchDetailsWithInvalidParticipants() {
        LocalDate futureDate = LocalDate.now().plusDays(5);
        LocalTime time = LocalTime.of(18, 0);

        boolean isValid = matchService.validateMatchDetails(
            Sport.FOOTBALL_11, futureDate, time, "Milano", 100
        );

        assertFalse(isValid);
    }

    @Test
    @DisplayName("SaveMatch con bean null dovrebbe lanciare eccezione")
    void testSaveMatchWithNullBean() {
        assertThrows(IllegalArgumentException.class, () -> matchService.saveMatch(null));
    }

    @Test
    @DisplayName("Conferma match dovrebbe cambiare stato")
    void testConfirmMatch() {
        // Arrange
        MatchBean matchBean = new MatchBean();
        matchBean.setOrganizerUsername("organizer3");
        matchBean.setSport(Sport.TENNIS_SINGLE);
        matchBean.setMatchDate(LocalDate.of(2026, 8, 15));
        matchBean.setMatchTime(LocalTime.of(10, 0));
        matchBean.setCity("Torino");
        matchBean.setRequiredParticipants(1);
        matchBean.setFieldId("3");

        matchService.saveMatch(matchBean);

        // Act
        matchService.confirmMatch(matchBean);

        // Assert
        assertNotNull(matchBean.getStatus());
    }

    @Test
    @DisplayName("Recupero match disponibili dovrebbe funzionare")
    void testGetAllAvailableMatches() {
        // Arrange
        MatchBean match1 = new MatchBean();
        match1.setOrganizerUsername("org1");
        match1.setSport(Sport.FOOTBALL_5);
        match1.setMatchDate(LocalDate.of(2026, 6, 20));
        match1.setMatchTime(LocalTime.of(19, 0));
        match1.setCity("Napoli");
        match1.setRequiredParticipants(4);
        match1.setFieldId("4");

        matchService.saveMatch(match1);
        matchService.confirmMatch(match1);

        // Act
        var matches = matchService.getAllAvailableMatches();

        // Assert
        assertNotNull(matches);
    }

    @Test
    @DisplayName("Recupero match per organizer dovrebbe funzionare")
    void testGetOrganizerMatches() {
        // Arrange
        String organizerUsername = "testOrganizer";

        MatchBean match1 = new MatchBean();
        match1.setOrganizerUsername(organizerUsername);
        match1.setSport(Sport.PADEL_DOUBLE);
        match1.setMatchDate(LocalDate.of(2026, 9, 10));
        match1.setMatchTime(LocalTime.of(17, 0));
        match1.setCity("Firenze");
        match1.setRequiredParticipants(5);
        match1.setFieldId("5");

        matchService.saveMatch(match1);

        // Act
        var matches = matchService.getOrganizerMatches(organizerUsername);

        // Assert
        assertNotNull(matches);
        assertFalse(matches.isEmpty());
    }

    @Test
    @DisplayName("Cancellazione match dovrebbe restituire lista partecipanti")
    void testCancelMatch() {
        // Arrange
        MatchBean matchBean = new MatchBean();
        matchBean.setOrganizerUsername("orgToCancel");
        matchBean.setSport(Sport.FOOTBALL_11);
        matchBean.setMatchDate(LocalDate.of(2026, 10, 15));
        matchBean.setMatchTime(LocalTime.of(18, 0));
        matchBean.setCity("Bologna");
        matchBean.setRequiredParticipants(10);
        matchBean.setFieldId("6");

        matchService.saveMatch(matchBean);
        int matchId = matchBean.getMatchId();

        // Act
        var participants = matchService.cancelMatch(matchId);

        // Assert
        assertNotNull(participants);
    }
}
