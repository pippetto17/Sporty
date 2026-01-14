package view.BookFieldView;

import controller.ApplicationController;
import view.View;

public interface BookFieldView extends View {
    void setApplicationController(ApplicationController applicationController);
    void displayAvailableFields();
    void displayFieldDetails(int fieldIndex);
    void displaySortOptions();
    void displayFilterOptions();
    void displayError(String message);
    void displaySuccess(String message);
}

