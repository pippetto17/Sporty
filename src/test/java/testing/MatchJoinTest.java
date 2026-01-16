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
 * Test suite per la funzionalità di Join Match.
 */
@DisplayName("Match Join Test Suite")
class MatchJoinTest {

    private MatchService matchService;

    @BeforeEach
    void setUp() throws Exception {
        matchService = new MatchService(DAOFactory.PersistenceType.MEMORY);
    }

    @Test
    @DisplayName("Join a un match")
    void testJoinMatch() {
        MatchBean match = createFootballMatch();
        matchService.saveMatch(match);
        int matchId = match.getMatchId();

        boolean joined = matchService.joinMatch(matchId, "player1");

        assertTrue(joined);
    }

    @Test
    @DisplayName("Join multiplo di player diversi")
    void testMultiplePlayersJoin() {
        MatchBean match = createFootballMatch();
        matchService.saveMatch(match);
        int matchId = match.getMatchId();

        boolean joined1 = matchService.joinMatch(matchId, "player1");
        boolean joined2 = matchService.joinMatch(matchId, "player2");
        boolean joined3 = matchService.joinMatch(matchId, "player3");

        assertTrue(joined1);
        assertTrue(joined2);
        assertTrue(joined3);
    }

    @Test
    @DisplayName("Join duplicato dello stesso player")
    void testDuplicateJoin() {
        MatchBean match = createFootballMatch();
        matchService.saveMatch(match);
        int matchId = match.getMatchId();

        boolean firstJoin = matchService.joinMatch(matchId, "player1");
        boolean secondJoin = matchService.joinMatch(matchId, "player1");

        assertTrue(firstJoin);
        assertFalse(secondJoin);
    }

    private MatchBean createFootballMatch() {
        MatchBean match = new MatchBean();
        match.setOrganizerUsername("organizer1");
        match.setSport(Sport.FOOTBALL_11);
        match.setMatchDate(LocalDate.of(2026, 6, 15));
        match.setMatchTime(LocalTime.of(18, 0));
        match.setCity("Milano");
        match.setRequiredParticipants(10);
        match.setFieldId("1");
        return match;
    }
}
