package controller;

import model.bean.MatchBean;
import model.dao.MatchDAO;
import model.domain.Role;
import model.domain.Sport;
import model.domain.User;

import java.time.LocalDate;
import java.util.List;

public class HomeController {
    private final User currentUser;
    private final ApplicationController applicationController;
    private final MatchDAO matchDAO;
    private boolean viewAsPlayer;
    private view.homeview.HomeView homeView;

    public HomeController(User user, ApplicationController applicationController, MatchDAO matchDAO) {
        this.currentUser = user;
        this.applicationController = applicationController;
        this.matchDAO = matchDAO;
        this.viewAsPlayer = !user.isOrganizer();
    }

    public void setHomeView(view.homeview.HomeView homeView) {
        this.homeView = homeView;
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public Role getUserRole() {
        return Role.fromCode(currentUser.getRole());
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
        if (viewAsPlayer) {
            return matchDAO.findAllAvailable().stream()
                    .filter(match -> !match.isFull())
                    .map(model.converter.MatchConverter::toBean)
                    .toList();
        }
        return matchDAO.findByOrganizer(currentUser.getUsername()).stream()
                .map(model.converter.MatchConverter::toBean)
                .toList();
    }

    public List<MatchBean> filterMatches(Sport sport, String city, LocalDate date) {
        return getMatches().stream()
                .filter(match -> sport == null || match.getSport() == sport)
                .filter(match -> city == null || city.trim().isEmpty() ||
                        match.getCity().equalsIgnoreCase(city.trim()))
                .filter(match -> date == null || match.getMatchDate().equals(date))
                .toList();
    }

    public void viewMatchDetail(int matchId) {
        homeView.showMatchDetails(matchId);
    }

    public void organizeMatch() {
        applicationController.navigateToOrganizeMatch(currentUser);
    }

    public void bookFieldStandalone() {
        applicationController.navigateToBookFieldStandalone(currentUser);
    }
}
