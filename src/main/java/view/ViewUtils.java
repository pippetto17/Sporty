package view;

import javafx.scene.Scene;
import javafx.scene.control.DialogPane;
import model.bean.MatchBean;
import model.domain.Sport;
import model.utils.Constants;

public class ViewUtils {
    private ViewUtils() {
    }

    public static void applyStylesheets(Scene scene) {
        var styleResource = ViewUtils.class.getResource("/css/style.css");
        if (styleResource != null) {
            scene.getStylesheets().add(styleResource.toExternalForm());
        }
    }

    public static void applyStylesheets(DialogPane dialogPane) {
        dialogPane.getStylesheets().clear();
        var styleResource = ViewUtils.class.getResource("/css/style.css");
        if (styleResource != null) {
            dialogPane.getStylesheets().add(styleResource.toExternalForm());
        }
    }

    public static String getSportStyleClass(Sport sport) {
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

    public static String getSportImagePath(Sport sport) {
        if (sport == null)
            return Constants.IMAGE_MEDAL_PATH;
        String name = sport.name().toUpperCase();
        if (name.contains(Constants.FOOTBALL))
            return Constants.IMAGE_FOOTBALL_PATH;
        if (name.contains(Constants.BASKET))
            return Constants.IMAGE_BASKETBALL_PATH;
        if (name.contains(Constants.TENNIS))
            return Constants.IMAGE_TENNIS_PATH;
        if (name.contains(Constants.PADEL))
            return Constants.IMAGE_PADEL_PATH;
        return Constants.IMAGE_MEDAL_PATH;
    }

    public static String getSportEmoji(Sport sport) {
        if (sport == null)
            return Constants.ICON_EXTRAS_MEDAL;
        String name = sport.name().toUpperCase();
        if (name.contains(Constants.FOOTBALL))
            return Constants.ICON_FOOTBALL;
        if (name.contains(Constants.BASKET))
            return Constants.ICON_BASKETBALL;
        if (name.contains(Constants.TENNIS))
            return Constants.ICON_TENNIS;
        if (name.contains(Constants.PADEL))
            return Constants.ICON_PADEL;
        return Constants.ICON_EXTRAS_MEDAL;
    }

    public static double getCapacityBarProgress(MatchBean match) {
        if (match == null || match.getSport() == null) {
            return 0.0;
        }
        int max = match.getSport().getRequiredPlayers();
        int current = max - match.getMissingPlayers();
        return max > 0 ? (double) current / max : 0.0;
    }

    public static int getCurrentParticipants(MatchBean match) {
        if (match == null || match.getSport() == null) {
            return 0;
        }
        return match.getSport().getRequiredPlayers() - match.getMissingPlayers();
    }
}