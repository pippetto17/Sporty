package view;

import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.DialogPane;

public class ViewUtils {

    private ViewUtils() {
    }

    public static void applyStylesheets(Scene scene) {
        var styleResource = ViewUtils.class.getResource("/css/style.css");
        if (styleResource != null) {
            scene.getStylesheets().add(styleResource.toExternalForm());
        }
        // controls-dark.css removed in favor of AtlantaFX
    }

    public static void applyStylesheets(DialogPane dialogPane) {
        dialogPane.getStylesheets().clear();
        var styleResource = ViewUtils.class.getResource("/css/style.css");
        if (styleResource != null) {
            dialogPane.getStylesheets().add(styleResource.toExternalForm());
        }

    }

    public static void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
