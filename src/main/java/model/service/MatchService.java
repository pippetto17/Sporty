package model.service;

import exception.DataAccessException;
import exception.ServiceInitializationException;
import model.bean.MatchBean;
import model.converter.MatchConverter;
import model.dao.DAOFactory;
import model.dao.MatchDAO;
import model.domain.Match;
import model.domain.MatchStatus;
import model.domain.Sport;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * Service per la gestione della logica di business relativa ai match.
 * Centralizza le operazioni di creazione, validazione e salvataggio dei match,
 * separando la logica di business dal controller e dal layer di persistenza.
 */
public class MatchService {
    private final MatchDAO matchDAO;

    public MatchService(DAOFactory.PersistenceType persistenceType) throws SQLException {
        this.matchDAO = DAOFactory.getMatchDAO(persistenceType);
    }

    /**
     * Valida i dettagli di un match prima della creazione.
     * Verifica che tutti i campi obbligatori siano presenti e validi.
     */
    public boolean validateMatchDetails(Sport sport, LocalDate date, LocalTime time,
            String city, int additionalParticipants) {
        if (sport == null || date == null || time == null ||
                city == null || city.trim().isEmpty()) {
            return false;
        }

        if (date.isBefore(LocalDate.now())) {
            return false;
        }

        return sport.isValidAdditionalParticipants(additionalParticipants);
    }

    /**
     * Salva un match nel database.
     * Aggiorna anche il bean con l'ID generato se la persistenza lo supporta.
     */
    public void saveMatch(MatchBean matchBean) {
        if (matchBean == null) {
            throw new IllegalArgumentException("MatchBean non può essere null");
        }

        try {
            Match match = MatchConverter.toEntity(matchBean);
            matchDAO.save(match);

            if (match.getMatchId() != null) {
                matchBean.setMatchId(match.getMatchId());
            }
        } catch (DataAccessException e) {
            throw new ServiceInitializationException("Errore durante il salvataggio del match: " + e.getMessage(), e);
        }
    }

    /**
     * Conferma un match impostando lo stato a CONFIRMED e salvandolo.
     * Utilizzato dopo il completamento del pagamento.
     */
    public void confirmMatch(MatchBean matchBean) {
        if (matchBean == null) {
            throw new IllegalArgumentException("MatchBean non può essere null");
        }

        matchBean.setStatus(MatchStatus.CONFIRMED);
        saveMatch(matchBean);
    }

    /**
     * Recupera un match per ID.
     */
    public MatchBean getMatchById(int matchId) {
        Match match = matchDAO.findById(matchId);
        return match != null ? MatchConverter.toBean(match) : null;
    }

    /**
     * Recupera tutti i match disponibili (CONFIRMED e non pieni).
     */
    public List<MatchBean> getAllAvailableMatches() {
        return matchDAO.findAllAvailable().stream()
                .filter(match -> !match.isFull())
                .map(MatchConverter::toBean)
                .toList();
    }

    /**
     * Recupera tutti i match organizzati da un utente specifico.
     */
    public List<MatchBean> getOrganizerMatches(String username) {
        return matchDAO.findByOrganizer(username).stream()
                .map(MatchConverter::toBean)
                .toList();
    }

    /**
     * Aggiunge un partecipante a un match.
     */
    public boolean joinMatch(int matchId, String username) {
        return matchDAO.addParticipant(matchId, username);
    }

    /**
     * Cancella un match e restituisce la lista dei partecipanti da notificare.
     */
    public List<String> cancelMatch(int matchId) {
        Match match = matchDAO.findById(matchId);
        if (match == null) {
            return List.of();
        }

        List<String> participants = match.getParticipants();
        match.setStatus(MatchStatus.CANCELLED);
        matchDAO.save(match);

        return participants;
    }
}
