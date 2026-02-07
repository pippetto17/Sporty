package model.notification;

import javafx.application.Platform;
import javafx.scene.control.Alert;

/* Concrete Observer */

public class FieldManagerObserver implements Observer {
    @Override
    public void update(NotificationEvent event) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Nuova Notifica");
            alert.setHeaderText(event.title);
            alert.setContentText(event.message);
            alert.show();
        });
    }
}