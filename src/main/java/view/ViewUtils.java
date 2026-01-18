package view;

import javafx.scene.Scene;
import javafx.scene.control.DialogPane;

public class ViewUtils {

    private ViewUtils() {}

    public static void applyStylesheets(Scene scene) {
        var styleResource = ViewUtils.class.getResource("/css/style.css");
        if (styleResource != null) {
            scene.getStylesheets().add(styleResource.toExternalForm());
        }
        var controlsResource = ViewUtils.class.getResource("/css/controls-dark.css");
        if (controlsResource != null) {
            scene.getStylesheets().add(controlsResource.toExternalForm());
        }
    }

    public static void applyStylesheets(DialogPane dialogPane) {
        dialogPane.getStylesheets().clear();
        var styleResource = ViewUtils.class.getResource("/css/style.css");
        if (styleResource != null) {
            dialogPane.getStylesheets().add(styleResource.toExternalForm());
        }
        var controlsResource = ViewUtils.class.getResource("/css/controls-dark.css");
        if (controlsResource != null) {
            dialogPane.getStylesheets().add(controlsResource.toExternalForm());
        }
    }
}

