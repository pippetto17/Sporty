package testing;

import model.bean.MatchBean;
import model.dao.DAOFactory;
import model.domain.MatchStatus;
import model.domain.Sport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("UC1: Organize Match Test Suite")
class OrganizeMatchTest {

    private model.dao.FieldDAO fieldDAO;
    private model.dao.MatchDAO matchDAO;

    @BeforeEach
    void setUp() throws Exception {
        fieldDAO = DAOFactory.getFieldDAO(DAOFactory.PersistenceType.MEMORY);
        matchDAO = DAOFactory.getMatchDAO(DAOFactory.PersistenceType.MEMORY);

        model.domain.Field testField = new model.domain.Field();
        testField.setFieldId("1");
        testField.setName("Campo Test");
        testField.setSport(Sport.FOOTBALL_5);
        testField.setCity("Milano");
        testField.setAddress("Via Test 1");
        testField.setManagerId("manager1");
        testField.setPricePerHour(50.0);
        testField.setIndoor(true);
        fieldDAO.save(testField);
    }

    @Test
    @DisplayName("Organizza match: creazione bean con dati validi")
    void testOrganizeMatchCreation() {
        MatchBean matchBean = new MatchBean();
        matchBean.setOrganizerUsername("organizer1");
        matchBean.setSport(Sport.FOOTBALL_5);
        matchBean.setMatchDate(LocalDate.now().plusDays(3));
        matchBean.setMatchTime(LocalTime.of(18, 0));
        matchBean.setCity("Milano");
        matchBean.setRequiredParticipants(10);

        assertEquals(Sport.FOOTBALL_5, matchBean.getSport());
        assertEquals("Milano", matchBean.getCity());
        assertEquals(10, matchBean.getRequiredParticipants());
    }

    @Test
    @DisplayName("Organizza match: verifica campo disponibile")
    void testFieldAvailability() {
        model.domain.Field field = fieldDAO.findById("1");

        assertNotNull(field);
        assertEquals("Campo Test", field.getName());
        assertEquals(Sport.FOOTBALL_5, field.getSport());
        assertEquals("Milano", field.getCity());
        assertTrue(field.getPricePerHour() > 0);
    }

    @Test
    @DisplayName("Organizza match: conferma e persistenza con status CONFIRMED")
    void testMatchConfirmation(){
        MatchBean matchBean = new MatchBean();
        matchBean.setOrganizerUsername("organizer1");
        matchBean.setSport(Sport.FOOTBALL_5);
        matchBean.setMatchDate(LocalDate.now().plusDays(3));
        matchBean.setMatchTime(LocalTime.of(18, 0));
        matchBean.setCity("Milano");
        matchBean.setRequiredParticipants(10);
        matchBean.setFieldId("1");
        matchBean.setPricePerPerson(5.0);
        matchBean.setStatus(MatchStatus.CONFIRMED);

        model.domain.Match match = model.converter.MatchConverter.toEntity(matchBean);
        matchDAO.save(match);

        assertNotNull(match.getMatchId());
        assertEquals(MatchStatus.CONFIRMED, match.getStatus());
        assertEquals("1", match.getFieldId());

        model.domain.Match retrieved = matchDAO.findById(match.getMatchId());
        assertNotNull(retrieved);
        assertEquals("organizer1", retrieved.getOrganizerUsername());
        assertEquals(Sport.FOOTBALL_5, retrieved.getSport());
    }
}

