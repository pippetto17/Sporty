package view;

import javafx.scene.Scene;

import javafx.scene.control.DialogPane;
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

    public static String getSportStyleClass(model.domain.Sport sport) {
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

    public static String getSportImagePath(model.domain.Sport sport) {
        if (sport == null)
            return model.utils.Constants.IMAGE_MEDAL_PATH;
        String name = sport.name().toUpperCase();
        if (name.contains(Constants.FOOTBALL))
            return model.utils.Constants.IMAGE_FOOTBALL_PATH;
        if (name.contains(Constants.BASKET))
            return model.utils.Constants.IMAGE_BASKETBALL_PATH;
        if (name.contains(Constants.TENNIS))
            return model.utils.Constants.IMAGE_TENNIS_PATH;
        if (name.contains(Constants.PADEL))
            return model.utils.Constants.IMAGE_PADEL_PATH;
        return model.utils.Constants.IMAGE_MEDAL_PATH;
    }

    public static String getSportEmoji(model.domain.Sport sport) {
        if (sport == null)
            return model.utils.Constants.ICON_EXTRAS_MEDAL;
        String name = sport.name().toUpperCase();
        if (name.contains(Constants.FOOTBALL))
            return model.utils.Constants.ICON_FOOTBALL;
        if (name.contains(Constants.BASKET))
            return model.utils.Constants.ICON_BASKETBALL;
        if (name.contains(Constants.TENNIS))
            return model.utils.Constants.ICON_TENNIS;
        if (name.contains(Constants.PADEL))
            return model.utils.Constants.ICON_PADEL;
        return model.utils.Constants.ICON_EXTRAS_MEDAL;
    }

    public static double getCapacityBarProgress(model.bean.MatchBean match) {
        if (match == null || match.getSport() == null) {
            return 0.0;
        }
        int max = match.getSport().getRequiredPlayers();
        int current = max - match.getMissingPlayers();
        return max > 0 ? (double) current / max : 0.0;
    }

    public static int getCurrentParticipants(model.bean.MatchBean match) {
        if (match == null || match.getSport() == null) {
            return 0;
        }
        return match.getSport().getRequiredPlayers() - match.getMissingPlayers();
    }
}