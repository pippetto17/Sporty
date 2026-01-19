package controller;

import exception.DataAccessException;
import model.bean.MatchBean;
import model.domain.Match;
import model.domain.User;
import model.utils.Constants;

import java.sql.SQLException;
public class JoinMatchController {
    private final User currentUser;
    private final ApplicationController applicationController;
    private final model.dao.MatchDAO matchDAO;
    private MatchBean matchBean;
    private Match match;
    public JoinMatchController(User user, ApplicationController applicationController) {
        this.currentUser = user;
        this.applicationController = applicationController;
        try {
            this.matchDAO = model.dao.DAOFactory.getMatchDAO(applicationController.getPersistenceType());
        } catch (SQLException e) {
            throw new DataAccessException(Constants.ERROR_DAO_INIT + e.getMessage(), e);
        }
    }
    public void setMatch(MatchBean matchBean) {
        this.matchBean = matchBean;
        this.match = matchDAO.findById(matchBean.getMatchId());
        if (this.match == null) {
            throw new IllegalArgumentException(Constants.ERROR_MATCH_NOT_FOUND);
        }
    }
    public MatchBean getMatchBean() {
        return matchBean;
    }
    public Match getMatch() {
        return match;
    }
    public User getCurrentUser() {
        return currentUser;
    }
    public boolean canJoin() {
        if (match == null) return false;
        if (match.isFull()) return false;
        if (match.hasParticipant(currentUser.getUsername())) return false;
        if (match.getOrganizerUsername().equals(currentUser.getUsername())) return false;
        return match.isConfirmed();
    }
    public double calculatePlayerCost() {
        if (match == null || match.getPricePerPerson() == null) return 0.0;
        return match.getPricePerPerson();
    }
    public void proceedToPayment() throws exception.ValidationException {
        if (!canJoin()) {
            throw new exception.ValidationException("Cannot join this match");
        }
        applicationController.navigateToPaymentForJoin(matchBean, currentUser);
    }
    public int getAvailableSlots() {
        if (match == null) return 0;
        return match.getRequiredParticipants() - match.getParticipantCount();
    }
}
