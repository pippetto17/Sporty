package testing;

import model.bean.MatchBean;
import model.dao.DAOFactory;
import model.domain.Sport;
import model.domain.Match;
import model.domain.MatchStatus;
import model.service.MatchService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import java.time.LocalDate;
import java.time.LocalTime;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Test suite per la funzionalità di Join Match.
 * NOTA: Questa funzionalità è ancora in fase di sviluppo.
 */
@DisplayName("Match Join Test Suite")
class MatchJoinTest {

    private MatchService matchService;

    @BeforeEach
    void setUp() throws Exception {
        matchService = new MatchService(DAOFactory.PersistenceType.MEMORY);
    }

    @Test
    @DisplayName("Join a un match con posti disponibili dovrebbe avere successo")
    void testJoinMatchWithAvailableSpots() {
        // Arrange
        MatchBean match = createFootballMatch("organizer1");
        matchService.saveMatch(match);
        int matchId = match.getMatchId();

        // Act
        boolean joined = matchService.joinMatch(matchId, "player1");

        // Assert
        assertTrue(joined);

        MatchBean updatedMatch = matchService.getMatchById(matchId);
        assertNotNull(updatedMatch.getParticipants());
        assertTrue(updatedMatch.getParticipants().contains("player1"));
    }

    @Test
    @DisplayName("Join multiplo di player diversi dovrebbe funzionare")
    void testMultiplePlayersJoinMatch() {
        // Arrange
        MatchBean match = createFootballMatch("organizer1");
        matchService.saveMatch(match);
        int matchId = match.getMatchId();

        // Act
        boolean joined1 = matchService.joinMatch(matchId, "player1");
        boolean joined2 = matchService.joinMatch(matchId, "player2");
        boolean joined3 = matchService.joinMatch(matchId, "player3");

        // Assert
        assertTrue(joined1);
        assertTrue(joined2);
        assertTrue(joined3);

        MatchBean updatedMatch = matchService.getMatchById(matchId);
        assertEquals(3, updatedMatch.getParticipants().size());
    }

    @Test
    @DisplayName("Join a match pieno dovrebbe fallire")
    void testJoinFullMatch() {
        // Arrange
        MatchBean match = createTennisMatch("tennisOrg");
        matchService.saveMatch(match);
        int matchId = match.getMatchId();

        matchService.joinMatch(matchId, "player1");

        // Act
        boolean joined = matchService.joinMatch(matchId, "player2");

        // Assert
        assertFalse(joined);
    }

    @Test
    @DisplayName("Join duplicato dello stesso player dovrebbe fallire")
    void testDuplicateJoinSamePlayer() {
        // Arrange
        MatchBean match = createFootballMatch("organizer1");
        matchService.saveMatch(match);
        int matchId = match.getMatchId();

        // Act
        boolean firstJoin = matchService.joinMatch(matchId, "player1");
        boolean secondJoin = matchService.joinMatch(matchId, "player1");

        // Assert
        assertTrue(firstJoin);
        assertFalse(secondJoin);
    }

    @Test
    @DisplayName("Join dovrebbe decrementare i posti disponibili")
    void testJoinDecreasesAvailableSpots() {
        // Arrange
        MatchBean match = createFootballMatch("organizer1");
        matchService.saveMatch(match);
        int matchId = match.getMatchId();

        MatchBean initialMatch = matchService.getMatchById(matchId);
        int initialParticipants = initialMatch.getParticipants() != null ? initialMatch.getParticipants().size() : 0;

        // Act
        matchService.joinMatch(matchId, "player1");

        // Assert
        MatchBean updatedMatch = matchService.getMatchById(matchId);
        int finalParticipants = updatedMatch.getParticipants().size();
        assertEquals(initialParticipants + 1, finalParticipants);
    }

    @Test
    @DisplayName("Join a match inesistente dovrebbe fallire")
    void testJoinNonExistentMatch() {
        // Act
        boolean joined = matchService.joinMatch(99999, "player1");

        // Assert
        assertFalse(joined);
    }

    @Test
    @DisplayName("Creazione e join multipli per raggiungere il limite")
    void testMatchBecomesFull() {
        // Arrange
        MatchBean match = createBasketballMatch("organizer2");
        matchService.saveMatch(match);
        int matchId = match.getMatchId();

        // Act - join fino al limite (basketball richiede 4 giocatori)
        matchService.joinMatch(matchId, "player1");
        matchService.joinMatch(matchId, "player2");
        matchService.joinMatch(matchId, "player3");
        matchService.joinMatch(matchId, "player4");

        // Assert - il quinto join dovrebbe fallire
        boolean fifthJoin = matchService.joinMatch(matchId, "player5");
        assertFalse(fifthJoin);
    }

    @Test
    @DisplayName("Verifica che la lista partecipanti non sia null dopo join")
    void testParticipantsListNotNull() {
        // Arrange
        MatchBean match = createFootballMatch("organizer3");
        matchService.saveMatch(match);
        int matchId = match.getMatchId();

        // Act
        matchService.joinMatch(matchId, "player1");

        // Assert
        MatchBean updatedMatch = matchService.getMatchById(matchId);
        assertNotNull(updatedMatch.getParticipants());
        assertFalse(updatedMatch.getParticipants().isEmpty());
    }

    @Test
    @DisplayName("Join a match di volley dovrebbe funzionare")
    void testJoinVolleyballMatch() {
        // Arrange
        MatchBean match = createVolleyballMatch("volleyOrg");
        matchService.saveMatch(match);
        int matchId = match.getMatchId();

        // Act
        boolean joined = matchService.joinMatch(matchId, "volleyPlayer");

        // Assert
        assertTrue(joined);
    }

    // Helper methods
    private MatchBean createFootballMatch(String organizer) {
        MatchBean match = new MatchBean();
        match.setOrganizerUsername(organizer);
        match.setSport(Sport.FOOTBALL_11);
        match.setMatchDate(LocalDate.of(2026, 6, 15));
        match.setMatchTime(LocalTime.of(18, 0));
        match.setCity("Milano");
        match.setRequiredParticipants(10);
        match.setFieldId("1");
        return match;
    }

    private MatchBean createTennisMatch(String organizer) {
        MatchBean match = new MatchBean();
        match.setOrganizerUsername(organizer);
        match.setSport(Sport.TENNIS_SINGLE);
        match.setMatchDate(LocalDate.of(2026, 7, 10));
        match.setMatchTime(LocalTime.of(15, 0));
        match.setCity("Roma");
        match.setRequiredParticipants(1);
        match.setFieldId("2");
        return match;
    }

    private MatchBean createBasketballMatch(String organizer) {
        MatchBean match = new MatchBean();
        match.setOrganizerUsername(organizer);
        match.setSport(Sport.BASKETBALL);
        match.setMatchDate(LocalDate.of(2026, 8, 5));
        match.setMatchTime(LocalTime.of(19, 30));
        match.setCity("Torino");
        match.setRequiredParticipants(4);
        match.setFieldId("3");
        return match;
    }

    private MatchBean createVolleyballMatch(String organizer) {
        MatchBean match = new MatchBean();
        match.setOrganizerUsername(organizer);
        match.setSport(Sport.FOOTBALL_8);
        match.setMatchDate(LocalDate.of(2026, 9, 20));
        match.setMatchTime(LocalTime.of(17, 0));
        match.setCity("Napoli");
        match.setRequiredParticipants(5);
        match.setFieldId("4");
        return match;
    }

    // ============================================================
    // TEST MATCH DOMAIN CLASS
    // ============================================================

    @Test
    @DisplayName("Creazione Match con costruttore completo dovrebbe funzionare")
    void testMatchCreation() {
        LocalDate date = LocalDate.of(2026, 6, 15);
        LocalTime time = LocalTime.of(18, 0);

        Match match = new Match(Sport.FOOTBALL_5, date, time, "Milano", 9, "org1");
        match.setFieldId("field1");

        assertEquals(Sport.FOOTBALL_5, match.getSport());
        assertEquals(date, match.getMatchDate());
        assertEquals(time, match.getMatchTime());
        assertEquals("Milano", match.getCity());
        assertEquals(9, match.getRequiredParticipants());
        assertEquals("org1", match.getOrganizerUsername());
        assertEquals("field1", match.getFieldId());
    }

    @Test
    @DisplayName("Match.isFull dovrebbe restituire true quando pieno")
    void testMatchIsFull() {
        LocalDate date = LocalDate.of(2026, 7, 1);
        LocalTime time = LocalTime.of(19, 0);

        Match match = new Match(Sport.TENNIS_SINGLE, date, time, "Roma", 1, "org2");

        // Aggiungi 1 partecipante (match per tennis single richiede 1 giocatore aggiuntivo)
        match.addParticipant("player1");

        assertTrue(match.isFull());
    }

    @Test
    @DisplayName("Match.isFull dovrebbe restituire false quando non pieno")
    void testMatchIsNotFull() {
        LocalDate date = LocalDate.of(2026, 7, 1);
        LocalTime time = LocalTime.of(19, 0);

        Match match = new Match(Sport.FOOTBALL_5, date, time, "Torino", 9, "org3");

        assertFalse(match.isFull());
    }

    @Test
    @DisplayName("Match.addParticipant dovrebbe aggiungere giocatore")
    void testMatchAddParticipant() {
        LocalDate date = LocalDate.of(2026, 8, 1);
        LocalTime time = LocalTime.of(20, 0);

        Match match = new Match(Sport.BASKETBALL, date, time, "Napoli", 9, "org4");

        boolean added = match.addParticipant("player1");

        assertTrue(added);
        assertTrue(match.getParticipants().contains("player1"));
    }

    @Test
    @DisplayName("Match.addParticipant duplicato dovrebbe fallire")
    void testMatchAddDuplicateParticipant() {
        LocalDate date = LocalDate.of(2026, 8, 1);
        LocalTime time = LocalTime.of(20, 0);

        Match match = new Match(Sport.BASKETBALL, date, time, "Napoli", 9, "org5");

        match.addParticipant("player1");
        boolean addedAgain = match.addParticipant("player1");

        assertFalse(addedAgain);
    }

    @Test
    @DisplayName("Match.setStatus dovrebbe aggiornare lo stato")
    void testMatchSetStatus() {
        LocalDate date = LocalDate.of(2026, 9, 1);
        LocalTime time = LocalTime.of(17, 0);

        Match match = new Match(Sport.PADEL_DOUBLE, date, time, "Firenze", 3, "org6");

        match.setStatus(MatchStatus.CONFIRMED);

        assertEquals(MatchStatus.CONFIRMED, match.getStatus());
    }

    @Test
    @DisplayName("Match.getParticipants dovrebbe restituire lista aggiornata")
    void testMatchGetParticipants() {
        LocalDate date = LocalDate.of(2026, 10, 1);
        LocalTime time = LocalTime.of(16, 0);

        Match match = new Match(Sport.FOOTBALL_5, date, time, "Bologna", 9, "org7");

        assertEquals(0, match.getParticipants().size());

        match.addParticipant("player1");
        match.addParticipant("player2");

        assertEquals(2, match.getParticipants().size());
        assertTrue(match.getParticipants().contains("player1"));
        assertTrue(match.getParticipants().contains("player2"));
    }
}
