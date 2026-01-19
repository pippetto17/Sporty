package controller;

import exception.ValidationException;
import model.bean.MatchBean;
import model.dao.MatchDAO;
import model.domain.Role;
import model.domain.Sport;
import model.domain.User;
import model.utils.Constants;

import java.time.LocalDate;
import java.util.List;

public class HomeController {
    private final User currentUser;
    private final ApplicationController applicationController;
    private final MatchDAO matchDAO;
    private boolean viewAsPlayer;
    private view.homeview.HomeView homeView;

    private final model.dao.FieldDAO fieldDAO;

    public HomeController(User user, ApplicationController applicationController, MatchDAO matchDAO) {
        this.currentUser = user;
        this.applicationController = applicationController;
        this.matchDAO = matchDAO;
        this.fieldDAO = model.dao.DAOFactory.getFieldDAO(applicationController.getPersistenceType());
        this.viewAsPlayer = user.isPlayer();
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
        List<MatchBean> matches;
        if (viewAsPlayer) {
            matches = matchDAO.findAllAvailable().stream()
                    .map(model.converter.MatchConverter::toBean)
                    .toList();
        } else {
            matches = matchDAO.findByOrganizer(currentUser.getUsername()).stream()
                    .map(model.converter.MatchConverter::toBean)
                    .toList();
        }

        // Enrich with Field Price
        for (MatchBean match : matches) {
            if (match.getFieldId() != null) {
                model.domain.Field field = fieldDAO.findById(match.getFieldId());
                if (field != null) {
                    model.bean.FieldBean fieldBean = model.converter.FieldConverter.toBean(field);
                    // This uses the smart calculator we added earlier
                    match.setPricePerPerson(fieldBean.getPricePerPerson());
                }
            }
        }
        return matches;
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

    public void bookFieldStandalone() {
        applicationController.navigateToBookFieldStandalone(currentUser);
    }

    public void joinMatch(int matchId) throws ValidationException {
        var matchBean = getMatches().stream()
                .filter(m -> m.getMatchId() == matchId)
                .findFirst()
                .orElseThrow(() -> new ValidationException("Match not found with ID: " + matchId));

        applicationController.navigateToJoinMatch(matchBean, currentUser);
    }

    private boolean matchesCity(MatchBean match, String city) {
        return match.getCity().equalsIgnoreCase(city.trim());
    }

    // --- UI State Management Methods ---

    public String getSportStyleClass(Sport sport) {
        if (sport == null)
            return "sport-default";
        String name = sport.name().toUpperCase();

        if (name.contains(Constants.FOOTBALL))
            return "sport-soccer";
        if (name.contains(Constants.BASKET))
            return "sport-basket";
        if (name.contains(Constants.TENNIS) || name.contains(Constants.PADEL))
            return "sport-tennis";

        return "sport-default";
    }

    public String getSportImagePath(Sport sport) {
        if (sport == null)
            return model.utils.Constants.IMAGE_MEDAL_PATH;
        String name = sport.name().toUpperCase();

        if (name.contains("FOOTBALL"))
            return model.utils.Constants.IMAGE_FOOTBALL_PATH;
        if (name.contains("BASKET"))
            return model.utils.Constants.IMAGE_BASKETBALL_PATH;
        if (name.contains("TENNIS"))
            return model.utils.Constants.IMAGE_TENNIS_PATH;
        if (name.contains("PADEL"))
            return model.utils.Constants.IMAGE_PADEL_PATH;

        return model.utils.Constants.IMAGE_MEDAL_PATH;
    }

    public String getSportEmoji(Sport sport) {
        if (sport == null)
            return model.utils.Constants.ICON_EXTRAS_MEDAL;
        String name = sport.name().toUpperCase();
        if (name.contains("FOOTBALL"))
            return model.utils.Constants.ICON_FOOTBALL;
        if (name.contains("BASKET"))
            return model.utils.Constants.ICON_BASKETBALL;
        if (name.contains("TENNIS"))
            return model.utils.Constants.ICON_TENNIS;
        if (name.contains("PADEL"))
            return model.utils.Constants.ICON_PADEL;
        return model.utils.Constants.ICON_EXTRAS_MEDAL;
    }

    public boolean shouldShowPriceBadge() {
        return viewAsPlayer;
    }

    public boolean shouldShowJoinButton(MatchBean match) {
        return viewAsPlayer && !isMatchFull(match);
    }

    public boolean shouldShowOrganizerActions() {
        return !viewAsPlayer;
    }

    public String getMatchesTitle() {
        return viewAsPlayer ? model.utils.Constants.LABEL_EXPLORE_MATCHES
                : model.utils.Constants.LABEL_YOUR_MATCHES;
    }

    public int getAvailableSlots(MatchBean match) {
        if (match == null) {
            return 0;
        }
        int current = getCurrentParticipants(match);
        return match.getRequiredParticipants() - current;
    }

    public int getCurrentParticipants(MatchBean match) {
        return match.getParticipants() != null ? match.getParticipants().size() : 0;
    }

    public double getCapacityBarProgress(MatchBean match) {
        if (match == null) {
            return 0.0;
        }
        int current = getCurrentParticipants(match);
        int max = match.getRequiredParticipants();
        return max > 0 ? (double) current / max : 0.0;
    }

    public boolean isMatchFull(MatchBean match) {
        if (match == null) {
            return true;
        }
        return getCurrentParticipants(match) >= match.getRequiredParticipants();
    }
}
