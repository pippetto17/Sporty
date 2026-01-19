package controller;

import exception.ValidationException;
import model.bean.MatchBean;
import model.domain.Match;
import model.domain.User;
import model.utils.Constants;
import view.joinmatchview.JoinMatchView;

public class JoinMatchController {
    private final User currentUser;
    private final ApplicationController applicationController;
    private final model.dao.MatchDAO matchDAO;
    private JoinMatchView view;
    private MatchBean matchBean;
    private Match match;

    public JoinMatchController(User user, ApplicationController applicationController) {
        this.currentUser = user;
        this.applicationController = applicationController;
        this.matchDAO = model.dao.DAOFactory.getMatchDAO(applicationController.getPersistenceType());
    }

    public void setView(JoinMatchView view) {
        this.view = view;
    }

    public void setMatch(MatchBean matchBean) {
        this.matchBean = matchBean;
        this.match = matchDAO.findById(matchBean.getMatchId());
        if (this.match == null) {
            throw new IllegalArgumentException(Constants.ERROR_MATCH_NOT_FOUND);
        }
    }

    public void start() {
        view.display();

        if (!canJoin()) {
            view.displayCannotJoin("You cannot join this match (full, already joined, or not available)");
            return;
        }

        view.displayMatchInfo(
                match.getSport().getDisplayName(),
                match.getMatchDate().toString(),
                match.getMatchTime().toString(),
                match.getCity(),
                match.getOrganizerUsername(),
                new int[]{match.getParticipantCount(), match.getRequiredParticipants(), getAvailableSlots()},
                calculatePlayerCost());

        String choice = view.getUserChoice();

        switch (choice) {
            case "1" -> {
                try {
                    proceedToPayment();
                } catch (ValidationException e) {
                    view.displayError(e.getMessage());
                }
            }
            case "2" -> applicationController.back();
            default -> {
                view.displayError("Invalid option");
                start();
            }
        }
    }

    private boolean canJoin() {
        return match != null
                && !match.isFull()
                && !match.hasParticipant(currentUser.getUsername())
                && !match.getOrganizerUsername().equals(currentUser.getUsername())
                && match.isConfirmed();
    }

    private double calculatePlayerCost() {
        if (match == null || match.getPricePerPerson() == null) {
            return 0.0;
        }
        return match.getPricePerPerson();
    }

    private void proceedToPayment() throws ValidationException {
        if (!canJoin()) {
            throw new ValidationException("Cannot join this match");
        }
        applicationController.navigateToPaymentForJoin(matchBean, currentUser);
    }

    private int getAvailableSlots() {
        if (match == null) {
            return 0;
        }
        return match.getRequiredParticipants() - match.getParticipantCount();
    }
}
