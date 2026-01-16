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
    @DisplayName("Validazione con sport e partecipanti validi dovrebbe passare")
    void testValidateMatchWithValidData() {
        boolean isValid = matchService.isValidMatch(Sport.FOOTBALL_11, 10);
        assertTrue(isValid);
    }

    @Test
    @DisplayName("Validazione con troppi partecipanti dovrebbe fallire")
    void testValidateMatchWithTooManyParticipants() {
        boolean isValid = matchService.isValidMatch(Sport.FOOTBALL_11, 25);
        assertFalse(isValid);
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
}

