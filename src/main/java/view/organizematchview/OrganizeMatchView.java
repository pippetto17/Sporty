package view.organizematchview;

import controller.ApplicationController;
import model.bean.MatchBean;
import view.View;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public interface OrganizeMatchView extends View {
    void setApplicationController(ApplicationController applicationController);
    void displayMatchList();
    void displayNewMatchForm();
    void displayError(String message);
    void displaySuccess(String message);
    void displayRecap(MatchBean matchBean);

    default LocalTime parseTime(String timeStr) {
        if (timeStr == null) return null;
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
            return LocalTime.parse(timeStr.trim(), formatter);
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    default LocalDate parseDate(String dateStr) {
        if (dateStr == null) return null;
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            return LocalDate.parse(dateStr.trim(), formatter);
        } catch (DateTimeParseException e) {
            return null;
        }
    }
}
