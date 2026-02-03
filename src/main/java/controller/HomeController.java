package controller;

import exception.ValidationException;
import model.bean.MatchBean;
import model.domain.Role;
import model.domain.Sport;
import model.domain.User;
import java.time.LocalDate;
import java.util.List;

public class HomeController {
    private final User currentUser;
    private final ApplicationController applicationController;
    private final model.dao.UserDAO userDAO;
    private final model.dao.FieldDAO fieldDAO;
    private final model.dao.MatchDAO matchDAO;
    private boolean viewAsPlayer;
    private view.homeview.HomeView homeView;

    public HomeController(User user, ApplicationController applicationController, model.dao.DAOFactory daoFactory) {
        this.currentUser = user;
        this.applicationController = applicationController;
        this.matchDAO = daoFactory.getMatchDAO();
        this.fieldDAO = daoFactory.getFieldDAO();
        this.userDAO = daoFactory.getUserDAO();
        this.viewAsPlayer = user.isPlayer();
        matchDAO.deleteExpiredMatches();
    }

    public void setHomeView(view.homeview.HomeView homeView) {
        this.homeView = homeView;
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public Role getUserRole() {
        return currentUser.getRole();
    }

    public boolean isViewingAsPlayer() {
        return viewAsPlayer;
    }

    public void switchRole() {
        if (currentUser.isOrganizer()) {
            viewAsPlayer = !viewAsPlayer;
        }
    }

    public List<MatchBean> getMatches() {
        try {
            List<MatchBean> matches;
            List<model.domain.Match> matchEntities;
            if (viewAsPlayer) {
                matchEntities = matchDAO.findApprovedMatches();
                matchEntities = matchEntities.stream()
                        .filter(m -> m.getOrganizerId() != currentUser.getId())
                        .toList();
            } else {
                matchEntities = matchDAO.findByOrganizer(currentUser.getId());
            }
            matches = matchEntities.stream()
                    .map(model.converter.MatchConverter::toBean)
                    .toList();
            for (MatchBean match : matches) {
                enrichMatchBean(match);
            }
            return matches;
        } catch (exception.DataAccessException e) {
            throw new exception.DataAccessException("Error loading matches: " + e.getMessage(), e);
        }
    }

    public MatchBean getMatchById(int matchId) {
        model.domain.Match matchEntity = matchDAO.findById(matchId);
        if (matchEntity == null) {
            return null;
        }
        MatchBean matchBean = model.converter.MatchConverter.toBean(matchEntity);
        enrichMatchBean(matchBean);
        return matchBean;
    }

    private void enrichMatchBean(MatchBean match) {
        model.domain.Field field = fieldDAO.findById(match.getFieldId());
        if (field != null) {
            match.setCity(field.getCity());
            match.setSport(field.getSport());
            match.setFieldName(field.getName());
            match.setFieldAddress(field.getAddress());
            match.setPricePerHour(field.getPricePerHour());
        }
        model.domain.User organizer = userDAO.findById(match.getOrganizerId());
        if (organizer != null) {
            match.setOrganizerName(organizer.getName() + " " + organizer.getSurname());
        }
    }

    public List<MatchBean> filterMatches(Sport sport, String city, LocalDate date) {
        return getMatches().stream()
                .filter(match -> sport == null || match.getSport() == sport)
                .filter(match -> city == null || city.trim().isEmpty() || matchesCity(match, city))
                .filter(match -> date == null || match.getMatchDate().equals(date))
                .toList();
    }

    public void viewMatchDetail(int matchId) {
        homeView.showMatchDetails(matchId);
    }

    public void organizeMatch() {
        applicationController.navigateToOrganizeMatch(currentUser);
    }

    public void joinMatch(int matchId) throws ValidationException {
        throw new ValidationException("Join Match feature is currently disabled.");
    }

    private boolean matchesCity(MatchBean match, String city) {
        if (match.getCity() == null)
            return false;
        return match.getCity().equalsIgnoreCase(city.trim());
    }

    public boolean isMatchFull(MatchBean match) {
        if (match == null) {
            return true;
        }
        return match.getMissingPlayers() <= 0;
    }
}