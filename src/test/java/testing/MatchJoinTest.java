package testing;

import model.dao.DAOFactory;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Test suite per la funzionalità di Join Match.
 */
@DisplayName("Match Join Test Suite")

class MatchJoinTest {

    private model.dao.MatchDAO matchDAO;

    @BeforeEach
    void setUp() throws Exception {
        matchDAO = DAOFactory.getMatchDAO(DAOFactory.PersistenceType.MEMORY);
    }

    @Test
    @DisplayName("Join a un match")
    void testJoinMatch() {
        model.domain.Match match = createFootballMatch();
        matchDAO.save(match);
        int matchId = match.getMatchId();

        model.domain.Match retrieved = matchDAO.findById(matchId);
        boolean joined = retrieved.addParticipant("player1");
        matchDAO.save(retrieved);

        assertTrue(joined);
    }

    @Test
    @DisplayName("Join multiplo di player diversi")
    void testMultiplePlayersJoin() {
        model.domain.Match match = createFootballMatch();
        matchDAO.save(match);
        int matchId = match.getMatchId();

        model.domain.Match retrieved = matchDAO.findById(matchId);
        boolean joined1 = retrieved.addParticipant("player1");
        boolean joined2 = retrieved.addParticipant("player2");
        boolean joined3 = retrieved.addParticipant("player3");
        matchDAO.save(retrieved);

        assertTrue(joined1);
        assertTrue(joined2);
        assertTrue(joined3);
    }

    @Test
    @DisplayName("Join duplicato dello stesso player")
    void testDuplicateJoin() {
        model.domain.Match match = createFootballMatch();
        matchDAO.save(match);
        int matchId = match.getMatchId();

        model.domain.Match retrieved = matchDAO.findById(matchId);
        boolean firstJoin = retrieved.addParticipant("player1");
        boolean secondJoin = retrieved.addParticipant("player1");

        assertTrue(firstJoin);
        assertFalse(secondJoin);
    }

    private model.domain.Match createFootballMatch() {
        return new model.domain.Match(
                model.domain.Sport.FOOTBALL_5,
                java.time.LocalDate.now().plusDays(1),
                java.time.LocalTime.of(18, 0),
                "Roma",
                10,
                "organizer");
    }
}
