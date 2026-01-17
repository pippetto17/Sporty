package testing;

import model.bean.MatchBean;
import model.dao.DAOFactory;
import model.domain.Sport;

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

    private model.dao.MatchDAO matchDAO;

    @BeforeEach
    void setUp() throws Exception {
        matchDAO = DAOFactory.getMatchDAO(DAOFactory.PersistenceType.MEMORY);
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

        model.domain.Match match = model.converter.MatchConverter.toEntity(matchBean);
        matchDAO.save(match);

        // Update bean with ID if generated (mock DAO usually generates ID)
        if (match.getMatchId() != null) {
            matchBean.setMatchId(match.getMatchId());
        }

        assertNotNull(match.getMatchId());
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

        model.domain.Match match = model.converter.MatchConverter.toEntity(matchBean);
        matchDAO.save(match);

        assertNotNull(match.getMatchId());
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

        model.domain.Match match = model.converter.MatchConverter.toEntity(matchBean);
        matchDAO.save(match);
        int matchId = match.getMatchId();

        model.domain.Match retrieved = matchDAO.findById(matchId);

        assertNotNull(retrieved);
        assertEquals(matchId, retrieved.getMatchId());
    }
}
