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
 * Test per il caso d'uso "Organizza Partita"
 * Testa il flusso completo dall'organizzazione fino all'accettazione da parte
 * del field manager
 */
class OrganizeMatchTest {

    private DAOFactory daoFactory;
    private User organizer;
    private User fieldManager;
    private Field testField;

    @BeforeEach
    void setUp() {
        // Inizializza il DAO Factory in memoria per i test
        daoFactory = new MemoryDAOFactory();

        // Crea un organizer di test
        organizer = new User(1, "mario.rossi", "password123", "Mario", "Rossi", Role.ORGANIZER);
        daoFactory.getUserDAO().save(organizer);

        // Crea un field manager di test
        fieldManager = new User(2, "gestore.campo", "password456", "Luigi", "Verdi", Role.FIELD_MANAGER);
        daoFactory.getUserDAO().save(fieldManager);

        // Crea un campo di test gestito dal field manager
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
        // Step 1: L'organizzatore crea un controller per organizzare una partita
        ApplicationController appController = new ApplicationController(daoFactory);
        OrganizeMatchController organizeController = new OrganizeMatchController(organizer, appController);

        assertNotNull(organizeController, "Il controller dovrebbe essere creato correttamente");
        assertEquals(organizer, organizeController.getOrganizer(),
                "L'organizer dovrebbe essere impostato correttamente");

        // Step 2: L'organizzatore imposta i dettagli della partita
        Sport sport = Sport.FOOTBALL_5;
        LocalDate date = LocalDate.now().plusDays(7);
        LocalTime time = LocalTime.of(18, 0);
        String city = "Roma";
        int additionalParticipants = 3;

        // Valida i dettagli
        assertDoesNotThrow(() -> {
            organizeController.validateMatchDetails(sport, date, time, city, additionalParticipants);
        }, "I dettagli della partita dovrebbero essere validi");

        // Imposta i dettagli nel bean
        organizeController.setMatchDetails(sport, date, time, city, additionalParticipants);
        MatchBean matchBean = organizeController.getCurrentMatchBean();

        assertNotNull(matchBean, "Il bean della partita dovrebbe essere creato");
        assertEquals(sport, matchBean.getSport(), "Lo sport dovrebbe essere impostato correttamente");
        assertEquals(date, matchBean.getMatchDate(), "La data dovrebbe essere impostata correttamente");
        assertEquals(time, matchBean.getMatchTime(), "L'orario dovrebbe essere impostato correttamente");
        assertEquals(city, matchBean.getCity(), "La città dovrebbe essere impostata correttamente");

        // Step 3: Seleziona il campo disponibile
        matchBean.setFieldId(testField.getId());
        matchBean.setFieldName(testField.getName());

        // Step 4: Salva la partita (status = PENDING)
        matchBean.setStatus(MatchStatus.PENDING);
        organizeController.saveMatch();

        // Step 5: Verifica che la partita sia stata salvata con status PENDING
        Match savedMatch = daoFactory.getMatchDAO().findById(matchBean.getMatchId());
        assertNotNull(savedMatch, "La partita dovrebbe essere salvata nel database");
        assertEquals(MatchStatus.PENDING, savedMatch.getStatus(), "La partita dovrebbe avere status PENDING");
        assertEquals(organizer.getId(), savedMatch.getOrganizer().getId(), "L'organizzatore dovrebbe essere corretto");
        assertEquals(testField.getId(), savedMatch.getField().getId(), "Il campo dovrebbe essere corretto");

        // Step 6: Il field manager visualizza le richieste pendenti
        FieldManagerController fieldManagerController = new FieldManagerController(fieldManager, daoFactory);
        var pendingRequests = fieldManagerController.getPendingRequests();

        assertFalse(pendingRequests.isEmpty(), "Dovrebbe esserci almeno una richiesta pendente");
        assertTrue(pendingRequests.stream()
                .anyMatch(m -> m.getMatchId() == savedMatch.getId()),
                "La partita creata dovrebbe essere nelle richieste pendenti");

        // Step 7: Il field manager approva la partita
        assertDoesNotThrow(() -> {
            fieldManagerController.approveMatch(savedMatch.getId());
        }, "L'approvazione dovrebbe avvenire senza errori");

        // Step 8: Verifica che la partita sia stata approvata
        Match approvedMatch = daoFactory.getMatchDAO().findById(savedMatch.getId());
        assertNotNull(approvedMatch, "La partita dovrebbe ancora esistere");
        assertEquals(MatchStatus.APPROVED, approvedMatch.getStatus(), "La partita dovrebbe avere status APPROVED");
        assertTrue(approvedMatch.isApproved(), "Il metodo isApproved() dovrebbe restituire true");
    }

    @Test
    void testOnlyOrganizerCanOrganizeMatch() {
        // Un player non può organizzare partite
        User player = new User(3, "player", "password", "Giovanni", "Bianchi", Role.PLAYER);
        daoFactory.getUserDAO().save(player);

        ApplicationController appController = new ApplicationController(daoFactory);

        assertThrows(AuthorizationException.class, () -> {
            new OrganizeMatchController(player, appController);
        }, "Un player non dovrebbe poter creare un OrganizeMatchController");
    }

    @Test
    void testFieldManagerCanOnlyApproveOwnFields() throws Exception {
        // Crea un secondo field manager
        User otherFieldManager = new User(4, "altro.gestore", "password", "Paolo", "Neri", Role.FIELD_MANAGER);
        daoFactory.getUserDAO().save(otherFieldManager);

        // Crea una partita per il campo del primo field manager
        Match match = new Match();
        match.setOrganizer(organizer);
        match.setField(testField);
        match.setDate(LocalDate.now().plusDays(7));
        match.setTime(LocalTime.of(18, 0));
        match.setMissingPlayers(5);
        match.setStatus(MatchStatus.PENDING);
        daoFactory.getMatchDAO().save(match);

        // Il secondo field manager non dovrebbe poter approvare partite del primo
        FieldManagerController otherController = new FieldManagerController(otherFieldManager, daoFactory);

        assertThrows(AuthorizationException.class, () -> {
            otherController.approveMatch(match.getId());
        }, "Un field manager non dovrebbe poter approvare partite di campi non suoi");
    }
}
