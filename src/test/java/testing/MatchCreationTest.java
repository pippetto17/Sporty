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
    @DisplayName("Creazione match di calcio")
    void testCreateFootballMatch() {
        MatchBean matchBean = new MatchBean();
        matchBean.setOrganizerUsername("organizer1");
        matchBean.setSport(Sport.FOOTBALL_11);
        matchBean.setMatchDate(LocalDate.of(2026, 6, 15));
        matchBean.setMatchTime(LocalTime.of(18, 0));
        matchBean.setCity("Milano");
        matchBean.setRequiredParticipants(10);
        matchBean.setFieldId("1");

        matchService.saveMatch(matchBean);

        assertNotNull(matchBean.getMatchId());
    }

    @Test
    @DisplayName("Creazione match di basket")
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
    @DisplayName("Recupero match per ID")
    void testGetMatchById() {
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

        MatchBean retrieved = matchService.getMatchById(matchId);

        assertNotNull(retrieved);
        assertEquals(matchId, retrieved.getMatchId());
    }

    @Test
    @DisplayName("Validazione match con dati corretti")
    void testValidateMatchWithCorrectData() {
        LocalDate futureDate = LocalDate.now().plusDays(5);
        LocalTime time = LocalTime.of(18, 0);

        boolean isValid = matchService.validateMatchDetails(
            Sport.FOOTBALL_11, futureDate, time, "Milano", 10
        );

        assertTrue(isValid);
    }

    @Test
    @DisplayName("Validazione match con data passata")
    void testValidateMatchWithPastDate() {
        LocalDate pastDate = LocalDate.of(2020, 1, 1);
        LocalTime time = LocalTime.of(18, 0);

        boolean isValid = matchService.validateMatchDetails(
            Sport.FOOTBALL_11, pastDate, time, "Milano", 10
        );

        assertFalse(isValid);
    }
}
