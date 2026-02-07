package controller;

import exception.DataAccessException;
import exception.ValidationException;
import model.bean.MatchBean;
import model.bean.UserBean;
import model.converter.MatchConverter;
import model.dao.DAOFactory;
import model.dao.FieldDAO;
import model.dao.MatchDAO;
import model.dao.UserDAO;
import model.domain.Field;
import model.domain.Match;
import model.domain.Sport;
import model.domain.User;
import view.homeview.HomeView;

import java.time.LocalDate;
import java.util.List;

public class HomeController {
    private final User currentUser;
    private final ApplicationController applicationController;
    private final UserDAO userDAO;
    private final FieldDAO fieldDAO;
    private final MatchDAO matchDAO;
    private boolean viewAsPlayer;
    private HomeView homeView;

    public HomeController(User user, ApplicationController applicationController, DAOFactory daoFactory) {
        this.currentUser = user;
        this.applicationController = applicationController;
        this.matchDAO = daoFactory.getMatchDAO();
        this.fieldDAO = daoFactory.getFieldDAO();
        this.userDAO = daoFactory.getUserDAO();
        this.viewAsPlayer = user.isPlayer();
        matchDAO.deleteExpiredMatches();
    }

    public void setHomeView(HomeView homeView) {
        this.homeView = homeView;
    }

    public UserBean getCurrentUser() {
        UserBean bean = new UserBean();
        bean.setId(currentUser.getId());
        bean.setUsername(currentUser.getUsername());
        bean.setName(currentUser.getName());
        bean.setSurname(currentUser.getSurname());
        bean.setRole(currentUser.getRole().getCode());
        return bean;
    }

    public String getUserRoleName() {
        return currentUser.getRole().getDisplayName();
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
            List<Match> matchEntities;
            if (viewAsPlayer) {
                matchEntities = matchDAO.findApprovedMatches();
                matchEntities = matchEntities.stream()
                        .filter(m -> (m.getOrganizer() != null ? m.getOrganizer().getId() : 0) != currentUser.getId())
                        .filter(m -> !m.isUserJoined(currentUser.getId())) // Filter out already joined matches
                        .toList();
            } else {
                matchEntities = matchDAO.findByOrganizer(currentUser.getId());
            }
            matches = matchEntities.stream()
                    .map(MatchConverter::toBean)
                    .toList();
            for (MatchBean match : matches) {
                enrichMatchBean(match);
            }
            return matches;
        } catch (DataAccessException e) {
            throw new DataAccessException("Error loading matches: " + e.getMessage(), e);
        }
    }

    public MatchBean getMatchById(int matchId) {
        Match matchEntity = matchDAO.findById(matchId);
        if (matchEntity == null) {
            return null;
        }
        MatchBean matchBean = MatchConverter.toBean(matchEntity);
        enrichMatchBean(matchBean);
        return matchBean;
    }

    private void enrichMatchBean(MatchBean match) {
        Field field = fieldDAO.findById(match.getFieldId());
        if (field != null) {

            match.setCity(field.getCity());
            match.setSport(field.getSport());
            match.setFieldName(field.getName());
            match.setFieldAddress(field.getAddress());
            match.setPricePerHour(field.getPricePerHour());

        }
        User organizer = userDAO.findById(match.getOrganizerId());
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
        MatchBean matchBean = getMatchById(matchId);
        if (matchBean == null) {
            throw new ValidationException("Match not found");
        }
        applicationController.navigateToJoinMatchPayment(matchBean, currentUser);
    }

    public List<MatchBean> getJoinedMatches() {
        try {
            List<Match> matchEntities = matchDAO.findByJoinedPlayer(currentUser.getId());
            List<MatchBean> matches = matchEntities.stream()
                    .map(MatchConverter::toBean)
                    .toList();
            for (MatchBean match : matches) {
                enrichMatchBean(match);
            }
            return matches;
        } catch (DataAccessException e) {
            throw new DataAccessException("Error loading joined matches: " + e.getMessage(), e);
        }
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